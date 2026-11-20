import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ReportGen {
    // ---- helpers ----------------------------------------------------------
    static void makeData(String dir, boolean corrupt) throws IOException {
        Files.createDirectories(Path.of(dir));
        for (int i = 1; i <= 5; i++) {
            String line2 = (corrupt && i == 4) ? "pencil,x,3.5" : "pencil,20,3.5";
            Files.writeString(Path.of(dir, "store" + i + ".csv"), "pen,10,12.0\n" + line2 + "\nbook,5,150.0\n");
        }
    }
    static int openStoreFiles() {                       // real open file descriptors pointing at store*.csv
        int c = 0;
        for (File f : new File("/proc/self/fd").listFiles()) {
            try { String t = Files.readSymbolicLink(f.toPath()).toString(); if (t.contains("store") && t.endsWith(".csv")) c++; }
            catch (Exception ignored) { }
        }
        return c;
    }
    static double process(BufferedReader r, String name) throws IOException {
        double sum = 0; String line; int n = 0;
        while ((line = r.readLine()) != null) {
            n++; String[] p = line.split(",");
            try { sum += Integer.parseInt(p[1]) * Double.parseDouble(p[2]); }
            catch (NumberFormatException e) {
                System.out.println("  [LOG] FAILED file=" + name + " line=" + n + " value='" + p[1] + "'");
                throw new IllegalStateException("Corrupt data in " + name + " line " + n, e);
            }
        }
        return sum;
    }
    static String[] names(String dir) { String[] f = new String[5]; for (int i = 0; i < 5; i++) f[i] = dir + "/store" + (i + 1) + ".csv"; return f; }

    // ---- Version 1: manual close() inside the try (the leaky pattern) --------
    static void v1Naive(String dir, String out) throws IOException {
        String[] files = names(dir);
        BufferedReader[] r = new BufferedReader[files.length];
        BufferedWriter w = new BufferedWriter(new FileWriter(out));
        double total = 0;
        for (int i = 0; i < files.length; i++) r[i] = new BufferedReader(new FileReader(files[i]));
        for (int i = 0; i < files.length; i++) {
            double s = process(r[i], files[i]);
            w.write(files[i] + "," + s + "\n"); total += s;
            r[i].close();                                   // skipped when an exception occurs
        }
        w.write("TOTAL," + total + "\n");
        w.close();
    }
    // ---- Version 2: try-catch-finally with manual close() -------------------
    static void v2Finally(String dir, String out) throws IOException {
        String[] files = names(dir);
        BufferedReader[] r = new BufferedReader[files.length];
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(out));
            double total = 0;
            for (int i = 0; i < files.length; i++) r[i] = new BufferedReader(new FileReader(files[i]));
            for (int i = 0; i < files.length; i++) {
                double s = process(r[i], files[i]);
                w.write(files[i] + "," + s + "\n"); total += s;
            }
            w.write("TOTAL," + total + "\n");
        } finally {
            for (BufferedReader x : r) if (x != null) try { x.close(); } catch (IOException ignored) { }
            if (w != null) try { w.close(); } catch (IOException ignored) { }
        }
    }
    // ---- Version 3: try-with-resources --------------------------------------
    static void v3Twr(String dir, String out) throws IOException {
        double total = 0;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
            for (String f : names(dir)) {
                try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                    double s = process(r, f);
                    w.write(f + "," + s + "\n"); total += s;
                }
            }
            w.write("TOTAL," + total + "\n");
        }
    }

    interface Gen { void run(String d, String o) throws IOException; }
    static void failingRun(String label, Gen g) {
        String out = "summary_" + label.charAt(1) + ".txt";
        try { g.run("badData", out); } catch (Exception e) { System.out.println("  exception: " + e.getMessage()); }
        System.gc();
        long len = new File(out).length();
        System.out.printf("  %-22s open store files after failure: %d   summary file size: %d bytes%n%n", label, openStoreFiles(), len);
    }
    public static void main(String[] args) throws Exception {
        makeData("goodData", false); makeData("badData", true);
        System.out.println("== Same input, no failure: do all versions give the same report? ==");
        v1Naive("goodData", "good1.txt"); v2Finally("goodData", "good2.txt"); v3Twr("goodData", "good3.txt");
        boolean same = Files.readString(Path.of("good1.txt")).equals(Files.readString(Path.of("good2.txt")))
                    && Files.readString(Path.of("good2.txt")).equals(Files.readString(Path.of("good3.txt")));
        System.out.println("  reports identical: " + same + "   (open store files now: " + openStoreFiles() + ")");
        System.out.print(Files.readString(Path.of("good3.txt")));
        System.out.println("\n== store4.csv is corrupt: run each version ==");
        System.out.println("V1 manual close() in try:");   failingRun("V1 naive manual", ReportGen::v1Naive);
        System.out.println("V2 try-catch-finally:");       failingRun("V2 finally close", ReportGen::v2Finally);
        System.out.println("V3 try-with-resources:");      failingRun("V3 try-with-resources", ReportGen::v3Twr);
    }
}
