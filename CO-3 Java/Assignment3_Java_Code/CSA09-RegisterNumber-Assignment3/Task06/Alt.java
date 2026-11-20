import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Alt {
    // ---- bounded buffer with wait/notifyAll ------------------------------------------------
    static class WaitBuf {
        final int[] b = new int[5]; int c, in, out;
        synchronized void put(int t) throws InterruptedException { while (c == 5) wait(); b[in] = t; in = (in + 1) % 5; c++; notifyAll(); }
        synchronized int take() throws InterruptedException { while (c == 0) wait(); int t = b[out]; out = (out + 1) % 5; c--; notifyAll(); return t; }
    }
    interface Put { void put(int t) throws Exception; } interface Take { int take() throws Exception; }
    static long[] runBuffer(Put p, Take t, int n) throws Exception {
        long[] sum = new long[2]; long t0 = System.nanoTime();
        Thread cook = new Thread(() -> { try { for (int i = 1; i <= n; i++) p.put(i); } catch (Exception e) { } });
        Thread[] st = new Thread[2];
        for (int s = 0; s < 2; s++) { final int id = s; st[s] = new Thread(() -> { try { for (int k = 0; k < n / 2; k++) sum[id] += t.take(); } catch (Exception e) { } }); }
        cook.start(); for (Thread x : st) x.start(); cook.join(); for (Thread x : st) x.join();
        return new long[]{(System.nanoTime() - t0) / 1_000_000, sum[0] + sum[1]};
    }
    // ---- transfers ---------------------------------------------------------------------------
    static class Acc { final int id; long bal = 1_000_000; final ReentrantLock lock = new ReentrantLock(); Acc(int id) { this.id = id; } }
    static void ordered(Acc f, Acc t, int amt) {
        Acc a = f.id < t.id ? f : t, b = a == f ? t : f;
        synchronized (a) { synchronized (b) { f.bal -= amt; t.bal += amt; } }
    }
    static boolean tryLockTransfer(Acc f, Acc t, int amt) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            if (f.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    if (t.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                        try { f.bal -= amt; t.bal += amt; return true; } finally { t.lock.unlock(); }
                    }
                } finally { f.lock.unlock(); }
            }
            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 4));       // back off, then retry
        }
        return false;
    }
    static double transfers(boolean tryLock, int each) throws Exception {
        Acc a = new Acc(1), b = new Acc(2); long t0 = System.nanoTime();
        Thread x = new Thread(() -> { try { for (int i = 0; i < each; i++) { if (tryLock) tryLockTransfer(a, b, 1); else ordered(a, b, 1); } } catch (Exception e) { } });
        Thread y = new Thread(() -> { try { for (int i = 0; i < each; i++) { if (tryLock) tryLockTransfer(b, a, 1); else ordered(b, a, 1); } } catch (Exception e) { } });
        x.start(); y.start(); x.join(); y.join();
        if (a.bal + b.bal != 2_000_000) throw new IllegalStateException("money lost");
        return (System.nanoTime() - t0) / 1e3 / (2.0 * each);
    }
    public static void main(String[] args) throws Exception {
        int n = 200_000;
        for (int w = 0; w < 2; w++) { WaitBuf wb = new WaitBuf(); runBuffer(wb::put, wb::take, n); ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(5); runBuffer(q::put, q::take, n); }
        WaitBuf wb = new WaitBuf(); long[] r1 = runBuffer(wb::put, wb::take, n);
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(5); long[] r2 = runBuffer(q::put, q::take, n);
        System.out.println("Producer-consumer, 200,000 tokens, capacity 5, 1 cook + 2 students");
        System.out.printf("  wait()/notifyAll()   : %5d ms  (%.2f us per token)  sum=%d%n", r1[0], r1[0] * 1000.0 / n, r1[1]);
        System.out.printf("  ArrayBlockingQueue(5): %5d ms  (%.2f us per token)  sum=%d%n", r2[0], r2[0] * 1000.0 / n, r2[1]);
        long expect = (long) n * (n + 1) / 2; System.out.println("  expected sum = " + expect);
        transfers(false, 20000); transfers(true, 20000);
        System.out.println("\nOpposing wallet transfers, 2 threads x 100,000 (total money checked)");
        System.out.printf("  lock ordering (synchronized): %6.2f us per transfer%n", transfers(false, 100_000));
        System.out.printf("  tryLock(10 ms) + back-off    : %6.2f us per transfer%n", transfers(true, 100_000));
    }
}
