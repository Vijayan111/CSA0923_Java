import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/** Generates test pass files: P000001,reg,name,route,amount */
public class PassGen {
    static final String[] NAMES = {"Arun","Divya","Karthik","Meena","Ravi","Sneha","Vikram","Anitha","Suresh","Priya"};
    public static void passes(Path f, int n, long seed) throws IOException {
        Random rnd = new Random(seed);
        try (BufferedWriter w = Files.newBufferedWriter(f)) {
            for (int i = 1; i <= n; i++) {
                w.write(String.format("P%06d,%d,%s,%d,%.1f", i, 192300000 + i, NAMES[rnd.nextInt(10)],
                        1 + rnd.nextInt(30), 3000 + rnd.nextInt(3000) + 0.0));
                w.newLine();
            }
        }
    }
    /** attendance-log style file of about the requested size in MB */
    public static void log(Path f, int mb) throws IOException {
        long target = mb * 1024L * 1024L, written = 0; int i = 0;
        try (BufferedWriter w = Files.newBufferedWriter(f)) {
            while (written < target) {
                String s = "2026-11-19,19" + (2300000 + (i++ % 90000)) + ",PRESENT,Hall-" + (i % 7) + "\n";
                w.write(s); written += s.length();
            }
        }
    }
}
