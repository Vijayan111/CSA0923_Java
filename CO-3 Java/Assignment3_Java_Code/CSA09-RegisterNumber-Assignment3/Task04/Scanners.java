import java.util.*;
import java.util.concurrent.*;

public class Scanners {
    static final List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());

    static class StationThread extends Thread {              // way 1: extend Thread
        final int batch; volatile int scanned;
        StationThread(String n, int batch) { super(n); this.batch = batch; }
        @Override public void run() {
            for (int i = 0; i < batch; i++) { scanned++; try { Thread.sleep(1); } catch (InterruptedException e) { return; } }
            finishOrder.add(getName());
        }
    }
    static class StationTask implements Runnable {            // way 2: implement Runnable
        final int batch; volatile int scanned;
        StationTask(int batch) { this.batch = batch; }
        @Override public void run() {
            for (int i = 0; i < batch; i++) { scanned++; try { Thread.sleep(1); } catch (InterruptedException e) { return; } }
            finishOrder.add(Thread.currentThread().getName());
        }
    }
    static String order() { return String.join(" ", finishOrder); }

    public static void main(String[] args) throws Exception {
        System.out.println("== 1. Life cycle and join() ==");
        StationThread a = new StationThread("A", 40);
        StationTask tb = new StationTask(40), tc = new StationTask(40);
        Thread b = new Thread(tb, "B"), c = new Thread(tc, "C");
        StationThread d = new StationThread("D", 40);
        Thread[] all = {a, b, c, d};
        System.out.println("before start : " + a.getState() + " alive=" + a.isAlive());
        for (Thread t : all) t.start();
        System.out.println("after start  : " + a.getState() + " alive=" + a.isAlive());
        Thread.sleep(10);
        System.out.println("while sleeping: " + a.getState());
        for (Thread t : all) t.join();
        System.out.println("after join   : " + a.getState() + " alive=" + a.isAlive());
        int total = a.scanned + tb.scanned + tc.scanned + d.scanned;
        System.out.println("Total scripts scanned (printed after join) = " + total + "   finish order: " + order());

        System.out.println("\n== 2. run() instead of start() ==");
        StationThread e = new StationThread("E", 1);
        System.out.println("main prints own name : " + Thread.currentThread().getName());
        Thread probe = new Thread(() -> System.out.println("body executed by      : " + Thread.currentThread().getName()), "T-probe");
        probe.run();   probe.start(); probe.join();
        System.out.println("(first line = run() on the main thread; second = start() on T-probe)");

        System.out.println("\n== 3. Finish order over 10 runs (equal batches of 40) ==");
        for (int run = 1; run <= 10; run++) {
            finishOrder.clear();
            Thread[] s = { new StationThread("A", 40), new Thread(new StationTask(40), "B"), new Thread(new StationTask(40), "C"), new StationThread("D", 40) };
            for (Thread t : s) t.start(); for (Thread t : s) t.join();
            System.out.printf("run %2d : %s%n", run, order());
        }

        System.out.println("\n== 4. Priorities (A=10, B=1, C=5, D=5) over 10 runs ==");
        Map<String, Integer> firsts = new TreeMap<>();
        for (int run = 1; run <= 10; run++) {
            finishOrder.clear();
            Thread[] s = { new StationThread("A", 40), new Thread(new StationTask(40), "B"), new Thread(new StationTask(40), "C"), new StationThread("D", 40) };
            s[0].setPriority(10); s[1].setPriority(1); s[2].setPriority(5); s[3].setPriority(5);
            for (Thread t : s) t.start(); for (Thread t : s) t.join();
            firsts.merge(finishOrder.get(0), 1, Integer::sum);
            System.out.printf("run %2d : %s%n", run, order());
        }
        System.out.println("who finished first, count: " + firsts);

        System.out.println("\n== 5. Same work with a fixed thread pool (p = 2, 4 stations) ==");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        finishOrder.clear(); long t0 = System.nanoTime();
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < 4; i++) fs.add(pool.submit(new StationTask(40)));
        for (Future<?> f : fs) f.get();
        System.out.printf("pool(2): 4 stations finished in %d ms; threads used: %s%n", (System.nanoTime() - t0) / 1_000_000, new TreeSet<>(finishOrder));
        pool.shutdown();
        Thread[] four = new Thread[4]; long t1 = System.nanoTime();
        for (int i = 0; i < 4; i++) { four[i] = new Thread(new StationTask(40)); four[i].start(); }
        for (Thread t : four) t.join();
        System.out.printf("4 own threads: finished in %d ms%n", (System.nanoTime() - t1) / 1_000_000);
    }
}
