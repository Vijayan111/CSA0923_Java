import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LineCounter {
    static long unbuffered(Path p) throws IOException {          // one read() call per byte
        long lines = 0;
        try (FileInputStream in = new FileInputStream(p.toFile())) {
            int b; while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }
    static long buffered(Path p) throws IOException {            // 8 KB array buffer
        long lines = 0; byte[] buf = new byte[8192];
        try (FileInputStream in = new FileInputStream(p.toFile())) {
            int n; while ((n = in.read(buf)) != -1) for (int i = 0; i < n; i++) if (buf[i] == '\n') lines++;
        }
        return lines;
    }
    static long readerBuffered(Path p) throws IOException {      // BufferedReader.readLine()
        long lines = 0;
        try (BufferedReader r = Files.newBufferedReader(p)) { while (r.readLine() != null) lines++; }
        return lines;
    }
    static long readAll(Path p) throws IOException { return Files.readAllLines(p).size(); }

    interface Job { long run(Path p) throws IOException; }
    static double ms(Job j, Path p, int runs, long[] result) throws IOException {
        double[] t = new double[runs];
        for (int i = 0; i < runs; i++) { long s = System.nanoTime(); result[0] = j.run(p); t[i] = (System.nanoTime() - s) / 1e6; }
        Arrays.sort(t); return t[runs / 2];
    }
    public static void main(String[] a) throws Exception {
        int[] sizes = {1, 10, 50};
        System.out.printf("%-8s %-8s %12s %14s %14s %14s%n", "size", "lines", "unbuffered", "byte[8192]", "BufferedReader", "readAllLines");
        for (int mb : sizes) {
            Path f = Files.createTempFile("log" + mb, ".txt"); PassGen.log(f, mb);
            long[] r = new long[1];
            int runs = mb == 50 ? 1 : 3;
            double u = ms(LineCounter::unbuffered, f, runs, r); long lines = r[0];
            double b = ms(LineCounter::buffered, f, 5, r);
            double br = ms(LineCounter::readerBuffered, f, 5, r);
            double ra = ms(LineCounter::readAll, f, 5, r);
            System.out.printf("%-8s %-8d %9.1f ms %11.1f ms %11.1f ms %11.1f ms   (x%.0f)%n", mb + " MB", lines, u, b, br, ra, u / b);
            Files.delete(f);
        }
    }
}
