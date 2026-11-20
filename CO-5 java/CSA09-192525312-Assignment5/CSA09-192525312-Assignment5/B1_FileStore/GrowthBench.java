import java.nio.file.*;
import java.util.*;

public class GrowthBench {
    public static void main(String[] a) throws Exception {
        int[] ns = {1000, 5000, 10000, 50000, 100000};
        System.out.printf("%-9s %-10s %16s %16s %16s%n", "records", "file KB", "find last (ms)*", "find random (ms)", "listByRoute (ms)");
        double prev = 0; int prevN = 0;
        for (int n : ns) {
            Path f = Files.createTempFile("g" + n, ".csv"); PassGen.passes(f, n, 7);
            PassStore s = new PassStore(f);
            for (int w = 0; w < 30; w++) s.find(String.format("P%06d", n));     // warm-up
            double[] t = new double[30];
            for (int i = 0; i < 30; i++) { long st = System.nanoTime(); s.find(String.format("P%06d", n)); t[i] = (System.nanoTime() - st) / 1e6; }
            Arrays.sort(t); double last = t[0];   // best of 30 runs: least disturbed by GC/JIT on a 1-CPU machine
            Random r = new Random(1); long st = System.nanoTime();
            for (int i = 0; i < 60; i++) s.find(String.format("P%06d", 1 + r.nextInt(n)));
            double rnd = (System.nanoTime() - st) / 1e6 / 60;
            st = System.nanoTime(); double lr = 1e9; for (int i = 0; i < 10; i++) { st = System.nanoTime(); s.listByRoute(12); lr = Math.min(lr, (System.nanoTime() - st) / 1e6); }
            System.out.printf("%-9d %-10d %16.2f %16.2f %16.2f%s%n", n, Files.size(f) / 1024, last, rnd, lr,
                    prev > 0 ? "   (x" + String.format("%.1f", last / prev) + " vs previous; records x" + String.format("%.0f", n / (double) prevN) + ")" : "");
            prev = last; prevN = n; Files.delete(f);
        }
        // Alternative: load once into a HashMap, then q searches
        int n = 100000, q = 1000;
        Path f = Files.createTempFile("map", ".csv"); PassGen.passes(f, n, 7);
        PassStore s = new PassStore(f); Random r = new Random(3);
        String[] ids = new String[q]; for (int i = 0; i < q; i++) ids[i] = String.format("P%06d", 1 + r.nextInt(n));
        long st = System.nanoTime(); for (String id : ids) s.find(id);
        double scan = (System.nanoTime() - st) / 1e6;
        Runtime rt = Runtime.getRuntime(); System.gc(); long m0 = rt.totalMemory() - rt.freeMemory();
        st = System.nanoTime();
        Map<String, String[]> map = new HashMap<>();
        for (String line : Files.readAllLines(f)) { String[] x = line.split(",", -1); if (x.length == 5) map.put(x[0], x); }
        double load = (System.nanoTime() - st) / 1e6;
        st = System.nanoTime(); for (String id : ids) map.get(id); double look = (System.nanoTime() - st) / 1e6;
        System.gc(); long m1 = rt.totalMemory() - rt.freeMemory(); if (map.size() < 0) System.out.println();
        System.out.printf("%nq = %d searches on n = %d passes%n", q, n);
        System.out.printf("  scan every time      : %9.1f ms   memory ~ O(1) (one line at a time)%n", scan);
        System.out.printf("  HashMap, load once   : %9.1f ms load + %.2f ms for %d lookups = %.1f ms   memory ~ %d MB%n", load, look, q, load + look, Math.max(0, (m1 - m0) >> 20));
        System.out.printf("  speed-up for q = %d   : x%.0f%n", q, scan / (load + look));
        Files.delete(f);
    }
}
