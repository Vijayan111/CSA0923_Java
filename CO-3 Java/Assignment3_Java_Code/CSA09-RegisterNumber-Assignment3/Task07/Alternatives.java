import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Alternatives {
    static long rssMb() { try { for (String l : Files.readAllLines(Path.of("/proc/self/status"))) if (l.startsWith("VmRSS")) return Long.parseLong(l.replaceAll("\\D+", "")) / 1024; } catch (Exception e) { } return -1; }
    public static void main(String[] args) throws Exception {
        List<Path> files = Processor.list(Path.of("scores"), 10_000);
        System.out.println("common pool parallelism = " + ForkJoinPool.commonPool().getParallelism());

        System.out.println("\n1) parallelStream, failure NOT handled inside the lambda:");
        AtomicInteger done = new AtomicInteger();
        long t = System.nanoTime();
        try { files.parallelStream().forEach(p -> { try { Processor.compute(p); done.incrementAndGet(); } catch (Exception e) { throw new RuntimeException(e); } }); }
        catch (RuntimeException e) { System.out.printf("   stream aborted after %d of 10000 files: %s%n", done.get(), e.getCause().getClass().getSimpleName()); }

        System.out.println("\n2) parallelStream, failure caught per file:");
        Processor.Summary s = new Processor.Summary();
        t = System.nanoTime();
        files.parallelStream().forEach(p -> { try { s.record(Processor.task(p, s).call()); } catch (Exception e) { s.record(new Result(p.toString(), "ERROR", 0, e.toString())); } });
        Processor.print(s, (System.nanoTime() - t) / 1_000_000);

        System.out.println("\n3) one platform thread per file (10,000 threads):");
        long rss0 = rssMb(); Processor.Summary s3 = new Processor.Summary(); t = System.nanoTime();
        AtomicInteger peak = new AtomicInteger(), live = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        try {
            for (Path p : files) { Thread th = new Thread(() -> { peak.accumulateAndGet(live.incrementAndGet(), Math::max); try { Processor.task(p, s3).call(); } catch (Exception e) { } live.decrementAndGet(); }); th.start(); ts.add(th); }
            for (Thread th : ts) th.join();
            Processor.print(s3, (System.nanoTime() - t) / 1_000_000);
            System.out.println("   peak live threads = " + peak.get() + ", RSS before " + rss0 + " MB, after " + rssMb() + " MB (default stack = 1 MB reserved per thread)");
        } catch (Throwable e) { System.out.println("   FAILED after starting " + ts.size() + " threads: " + e); }

        System.out.println("\n4) bonus: virtual threads (Java 21):");
        Processor.Summary s4 = new Processor.Summary(); t = System.nanoTime();
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) { for (Path p : files) ex.submit(Processor.task(p, s4)); }
        Processor.print(s4, (System.nanoTime() - t) / 1_000_000);
    }
}
