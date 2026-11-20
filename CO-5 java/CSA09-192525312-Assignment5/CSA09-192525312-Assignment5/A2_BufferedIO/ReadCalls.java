import java.io.*;
import java.nio.file.*;
import java.util.List;

/** Task A2: how many calls reach the file layer for a 50 MB log, and how much memory readAllLines needs. */
public class ReadCalls {
    static long calls;
    static class Counting extends FileInputStream {            // counts calls that go down to the file
        Counting(File f) throws FileNotFoundException { super(f); }
        @Override public int read() throws IOException { calls++; return super.read(); }
        @Override public int read(byte[] b) throws IOException { calls++; return super.read(b); }
        @Override public int read(byte[] b, int o, int l) throws IOException { calls++; return super.read(b, o, l); }
    }
    static long countLines(InputStream in) throws IOException {
        long lines = 0; int b; while ((b = in.read()) != -1) if (b == '\n') lines++; return lines;
    }
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("attendance", ".log");
        long t0 = System.nanoTime(); makeLog(f, 50);
        long size = Files.size(f);
        System.out.printf("attendance log: %,d bytes (%.1f MB)%n%n", size, size / 1048576.0);
        System.out.printf("%-42s %14s %12s%n", "strategy", "read calls", "lines");
        calls = 0; long l1;
        try (InputStream in = new Counting(f.toFile())) { l1 = countLines(in); }
        System.out.printf("%-42s %,14d %,12d%n", "FileInputStream.read() one byte at a time", calls, l1);
        calls = 0; long l2;
        try (InputStream in = new BufferedInputStream(new Counting(f.toFile()))) { l2 = countLines(in); }     // 8192-byte buffer
        System.out.printf("%-42s %,14d %,12d%n", "BufferedInputStream (8 KB buffer)", calls, l2);
        calls = 0; long l3 = 0;
        try (InputStream in = new Counting(f.toFile())) {
            byte[] buf = new byte[65536]; int n; while ((n = in.read(buf)) != -1) for (int i = 0; i < n; i++) if (buf[i] == '\n') l3++;
        }
        System.out.printf("%-42s %,14d %,12d%n", "byte[65536] array buffer", calls, l3);
        System.out.println();
        System.out.printf("predicted unbuffered calls = file size + 1 (EOF) = %,d%n", size + 1);
        System.out.printf("predicted 8 KB buffered    = ceil(%,d / 8192) + 1 = %,d%n", size, (size + 8191) / 8192 + 1);
        System.out.printf("reduction in calls         = x%,.0f%n%n", (double) (size + 1) / ((size + 8191) / 8192 + 1));
        Runtime rt = Runtime.getRuntime(); System.gc(); long m0 = rt.totalMemory() - rt.freeMemory();
        List<String> all = Files.readAllLines(f);
        System.gc(); long m1 = rt.totalMemory() - rt.freeMemory();
        System.out.printf("Files.readAllLines(): %,d lines held in memory, about %d MB of heap for a %d MB file%n", all.size(), (m1 - m0) >> 20, size >> 20);
        System.out.println("BufferedInputStream: heap used by the buffer = 8 KB (independent of file size)");
        Files.delete(f);
    }
    static void makeLog(Path f, int mb) throws IOException {
        long target = mb * 1024L * 1024L, written = 0; int i = 0;
        try (BufferedWriter w = Files.newBufferedWriter(f)) {
            while (written < target) { String s = "2026-11-19,19" + (2300000 + (i++ % 90000)) + ",PRESENT,Hall-" + (i % 7) + "\n"; w.write(s); written += s.length(); }
        }
    }
}
