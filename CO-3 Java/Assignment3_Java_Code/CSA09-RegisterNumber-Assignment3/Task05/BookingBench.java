import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class BookingBench {
    static final int SEATS = 1000, BOOKINGS = 10_000, THREADS = 8;
    static long workNanos;
    static void work() { if (workNanos > 0) LockSupport.parkNanos(workNanos); }   // simulated payment latency

    interface Booker { boolean book(int seat); int sold(); }
    static class SyncMethod implements Booker {
        boolean[] b = new boolean[SEATS]; int n;
        public synchronized boolean book(int s) { work(); if (b[s]) return false; b[s] = true; n++; return true; }  // whole method locked
        public synchronized int sold() { return n; }
    }
    static class SyncBlock implements Booker {
        boolean[] b = new boolean[SEATS]; int n; final Object lock = new Object();
        public boolean book(int s) { work(); synchronized (lock) { if (b[s]) return false; b[s] = true; n++; return true; } }
        public int sold() { synchronized (lock) { return n; } }
    }
    static class Cas implements Booker {
        AtomicBoolean[] b = new AtomicBoolean[SEATS]; AtomicInteger n = new AtomicInteger();
        Cas() { for (int i = 0; i < SEATS; i++) b[i] = new AtomicBoolean(); }
        public boolean book(int s) { work(); if (b[s].compareAndSet(false, true)) { n.incrementAndGet(); return true; } return false; }
        public int sold() { return n.get(); }
    }
    static class Reentrant implements Booker {
        boolean[] b = new boolean[SEATS]; int n; final ReentrantLock l = new ReentrantLock();
        public boolean book(int s) { work(); l.lock(); try { if (b[s]) return false; b[s] = true; n++; return true; } finally { l.unlock(); } }
        public int sold() { l.lock(); try { return n; } finally { l.unlock(); } }
    }
    static class Chm implements Booker {
        ConcurrentHashMap<Integer, Boolean> m = new ConcurrentHashMap<>();
        public boolean book(int s) { work(); return m.putIfAbsent(s, Boolean.TRUE) == null; }
        public int sold() { return m.size(); }
    }
    static double run(Booker bk) throws Exception {
        Thread[] t = new Thread[THREADS]; long t0 = System.nanoTime();
        for (int k = 0; k < THREADS; k++) { final int id = k; t[k] = new Thread(() -> { for (int i = id; i < BOOKINGS; i += THREADS) bk.book(i % SEATS); }); t[k].start(); }
        for (Thread x : t) x.join();
        if (bk.sold() != SEATS) throw new IllegalStateException("double booking! sold=" + bk.sold());
        return (System.nanoTime() - t0) / 1e6;
    }
    static double median(java.util.function.Supplier<Booker> f) throws Exception {
        double[] r = new double[5]; for (int i = 0; i < 5; i++) r[i] = run(f.get()); Arrays.sort(r); return r[2];
    }
    public static void main(String[] a) throws Exception {
        Map<String, java.util.function.Supplier<Booker>> s = new LinkedHashMap<>();
        s.put("synchronized method", SyncMethod::new); s.put("synchronized block", SyncBlock::new);
        s.put("AtomicBoolean[] CAS", Cas::new); s.put("ReentrantLock", Reentrant::new); s.put("ConcurrentHashMap", Chm::new);
        for (long w : new long[]{0, 100_000}) {
            workNanos = w;
            for (int i = 0; i < 2; i++) for (var e : s.values()) run(e.get());      // warm-up
            System.out.println("10,000 bookings, 8 threads, 1000 seats, simulated work per booking = " + (w / 1000) + " us   (median of 5, ms)");
            for (var e : s.entrySet()) System.out.printf("  %-22s %9.1f ms%n", e.getKey(), median(e.getValue()));
            System.out.println();
        }
    }
}
