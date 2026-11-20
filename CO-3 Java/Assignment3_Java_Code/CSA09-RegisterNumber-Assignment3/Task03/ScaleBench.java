import java.io.*;
import java.nio.file.*;
public class ScaleBench {
    static long twr(String[] f) throws IOException { long c = 0; for (String s : f) try (BufferedReader r = new BufferedReader(new FileReader(s))) { c += r.readLine().length(); } return c; }
    static long fin(String[] f) throws IOException { long c = 0; for (String s : f) { BufferedReader r = null; try { r = new BufferedReader(new FileReader(s)); c += r.readLine().length(); } finally { if (r != null) r.close(); } } return c; }
    public static void main(String[] a) throws Exception {
        Files.createDirectories(Path.of("bench"));
        System.out.println("f files    try-with-resources (ms)   finally+close (ms)   handles open at once");
        for (int f : new int[]{500, 1000, 2000, 4000}) {
            String[] n = new String[f];
            for (int i = 0; i < f; i++) { n[i] = "bench/store" + i + ".csv"; Files.writeString(Path.of(n[i]), "pen,1,1.0\n"); }
            twr(n); fin(n);
            long t1 = Long.MAX_VALUE, t2 = Long.MAX_VALUE;
            for (int k = 0; k < 5; k++) { long t = System.nanoTime(); twr(n); t1 = Math.min(t1, System.nanoTime() - t); t = System.nanoTime(); fin(n); t2 = Math.min(t2, System.nanoTime() - t); }
            System.out.printf("%-10d %-25.2f %-20.2f 1 (sequential)%n", f, t1 / 1e6, t2 / 1e6);
        }
    }
}
