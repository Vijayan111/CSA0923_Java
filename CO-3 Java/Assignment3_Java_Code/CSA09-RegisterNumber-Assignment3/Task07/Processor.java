import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

class CorruptFileException extends Exception {
    CorruptFileException(String m) { super(m); }
}
record Result(String file, String status, int total, String message) { }

public class Processor {
    static final int LATENCY_MS = 1;                 // simulated disk/network latency per file

    // ---------- data generation (hand-checkable rules) --------------------------------------
    // file i is MISSING if i%100==13, CORRUPT if i%50==7 (even i: letters, odd i: mark > 100), else OK
    static int[] marks(int i) { return new int[]{(i * 7) % 101, (i * 13) % 101, (i * 17) % 101}; }
    static void generate(Path dir, int n) throws IOException {
        Files.createDirectories(dir); long expectTotal = 0; int ok = 0, bad = 0, miss = 0;
        for (int i = 0; i < n; i++) {
            Path p = dir.resolve(String.format("score%05d.txt", i));
            if (i % 100 == 13) { Files.deleteIfExists(p); miss++; continue; }
            int[] m = marks(i);
            if (i % 50 == 7) { Files.writeString(p, i % 2 == 0 ? "REG" + i + ",abc," + m[1] + "," + m[2] : "REG" + i + ",150," + m[1] + "," + m[2]); bad++; continue; }
            Files.writeString(p, "REG" + i + "," + m[0] + "," + m[1] + "," + m[2]); ok++; expectTotal += m[0] + m[1] + m[2];
        }
        System.out.printf("generated %d files: expected OK=%d CORRUPT=%d MISSING=%d, expected total marks=%d%n", n, ok, bad, miss, expectTotal);
    }

    // ---------- the task: read -> validate -> compute --------------------------------------
    static int compute(Path p) throws IOException, CorruptFileException, InterruptedException {
        Thread.sleep(LATENCY_MS);
        String line = Files.readAllLines(p).get(0);                    // NoSuchFileException if missing
        String[] f = line.split(",");
        if (f.length != 4) throw new CorruptFileException(p.getFileName() + ": expected 4 fields");
        int total = 0;
        for (int k = 1; k < 4; k++) {
            int v;
            try { v = Integer.parseInt(f[k].trim()); }
            catch (NumberFormatException e) { throw new CorruptFileException(p.getFileName() + ": mark '" + f[k] + "' is not a number"); }
            if (v < 0 || v > 100) throw new CorruptFileException(p.getFileName() + ": mark " + v + " outside 0..100");
            total += v;
        }
        return total;
    }

    // ---------- design: ExecutorService + Callable + Future + ConcurrentHashMap + LongAdder -------
    static class Summary {
        final ConcurrentHashMap<String, Result> results = new ConcurrentHashMap<>();
        final LongAdder ok = new LongAdder(), corrupt = new LongAdder(), missing = new LongAdder(), error = new LongAdder(), marks = new LongAdder();
        void record(Result r) {
            if (results.putIfAbsent(r.file(), r) != null) return;       // each file counted once
            switch (r.status()) { case "OK" -> { ok.increment(); marks.add(r.total()); } case "CORRUPT" -> corrupt.increment(); case "MISSING" -> missing.increment(); default -> error.increment(); }
        }
        long accounted() { return ok.sum() + corrupt.sum() + missing.sum() + error.sum(); }
    }
    static Callable<Result> task(Path p, Summary s) {
        return () -> {
            Result r;
            try { r = new Result(p.toString(), "OK", compute(p), ""); }
            catch (NoSuchFileException e) { r = new Result(p.toString(), "MISSING", 0, "file not found"); }
            catch (CorruptFileException e) { r = new Result(p.toString(), "CORRUPT", 0, e.getMessage()); }
            catch (IOException e) { r = new Result(p.toString(), "ERROR", 0, e.toString()); }
            s.record(r);
            return r;
        };
    }
    static Summary runPool(List<Path> files, int threads) throws Exception {
        Summary s = new Summary();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Result>> fs = new ArrayList<>();
        for (Path p : files) fs.add(pool.submit(task(p, s)));
        for (int i = 0; i < fs.size(); i++) {
            try { fs.get(i).get(30, TimeUnit.SECONDS); }
            catch (ExecutionException e) { s.record(new Result(files.get(i).toString(), "ERROR", 0, "unexpected: " + e.getCause())); }   // safety net
            catch (TimeoutException e) { fs.get(i).cancel(true); s.record(new Result(files.get(i).toString(), "ERROR", 0, "timed out")); }
        }
        pool.shutdown(); pool.awaitTermination(1, TimeUnit.MINUTES);
        return s;
    }
    static List<Path> list(Path dir, int n) { List<Path> l = new ArrayList<>(); for (int i = 0; i < n; i++) l.add(dir.resolve(String.format("score%05d.txt", i))); return l; }
    static void print(Summary s, long ms) {
        System.out.printf("  OK=%d CORRUPT=%d MISSING=%d ERROR=%d | accounted=%d of %d | total marks=%d | %d ms%n", s.ok.sum(), s.corrupt.sum(), s.missing.sum(), s.error.sum(), s.accounted(), s.results.size(), s.marks.sum(), ms);
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "small";
        if (mode.equals("small")) {                                   // hand-checked set of 10 files
            Path d = Path.of("small"); Files.createDirectories(d);
            for (int i = 0; i < 10; i++) {
                Path p = d.resolve(String.format("score%05d.txt", i)); Files.deleteIfExists(p);
                if (i <= 5) Files.writeString(p, "REG" + i + "," + (10 + i) + "," + (20 + i) + "," + (30 + i));
                else if (i == 6) Files.writeString(p, "REG6,x,20,30");
                else if (i == 7) Files.writeString(p, "REG7,10,200,30");
            }
            System.out.println("Hand-check: files 0-5 valid (totals 60,63,66,69,72,75 = 405), 6 and 7 corrupt, 8 and 9 missing");
            long t = System.nanoTime(); Summary s = runPool(list(d, 10), 4); print(s, (System.nanoTime() - t) / 1_000_000);
            new TreeMap<>(s.results).values().forEach(r -> System.out.printf("  %-14s %-8s %s%n", Path.of(r.file()).getFileName(), r.status(), r.status().equals("OK") ? "total " + r.total() : r.message()));
        } else if (mode.equals("gen")) generate(Path.of("scores"), 10_000);
        else if (mode.equals("pools")) {
            List<Path> files = list(Path.of("scores"), 10_000);
            runPool(files.subList(0, 500), 8);                         // warm-up
            System.out.println("10,000 files, simulated latency " + LATENCY_MS + " ms per file, cores available = " + Runtime.getRuntime().availableProcessors());
            System.out.printf("%-8s %-9s %-8s %-9s %-7s %-10s%n", "threads", "time(ms)", "speedup", "OK", "CORRUPT", "MISSING");
            long base = 0;
            for (int p : new int[]{1, 2, 4, 8, 16, 64, 256}) {
                long t = System.nanoTime(); Summary s = runPool(files, p); long ms = (System.nanoTime() - t) / 1_000_000;
                if (p == 1) base = ms;
                System.out.printf("%-8d %-9d %-8.2f %-9d %-7d %-10d%n", p, ms, (double) base / ms, s.ok.sum(), s.corrupt.sum(), s.missing.sum());
            }
        }
    }
}
