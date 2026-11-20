import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Alternative concept: Java serialization vs CSV vs JSON for n residents (needs Resident with phone). */
public class FormatBench {
    public static void main(String[] a) throws Exception {
        System.out.printf("%-9s %-16s %10s %10s %10s%n", "n", "format", "size (KB)", "save (ms)", "load (ms)");
        for (int n : new int[]{10000, 100000}) {
            List<Resident> list = new ArrayList<>();
            for (int i = 0; i < n; i++) { Resident r = new Resident("Student" + i, 192300000 + i, "pw", null); r.phone = "98" + (40000000 + i); list.add(r); }
            Path ser = Files.createTempFile("r", ".ser"), csv = Files.createTempFile("r", ".csv"), json = Files.createTempFile("r", ".json");
            for (int warm = 0; warm < 2; warm++) {
                long t = System.nanoTime();
                try (ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ser.toFile())))) { o.writeObject(list); }
                double s1 = (System.nanoTime() - t) / 1e6; t = System.nanoTime();
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(ser.toFile())))) { in.readObject(); }
                double l1 = (System.nanoTime() - t) / 1e6;
                t = System.nanoTime();
                try (BufferedWriter w = Files.newBufferedWriter(csv)) { for (Resident r : list) { w.write(r.name + "," + r.regNo + "," + r.phone); w.newLine(); } }
                double s2 = (System.nanoTime() - t) / 1e6; t = System.nanoTime();
                List<Resident> back = new ArrayList<>();
                try (BufferedReader rd = Files.newBufferedReader(csv)) { String l; while ((l = rd.readLine()) != null) { String[] f = l.split(","); Resident r = new Resident(f[0], Integer.parseInt(f[1]), null, null); r.phone = f.length > 2 ? f[2] : null; back.add(r); } }
                double l2 = (System.nanoTime() - t) / 1e6;
                t = System.nanoTime();
                try (BufferedWriter w = Files.newBufferedWriter(json)) {
                    w.write("["); boolean first = true;
                    for (Resident r : list) { if (!first) w.write(","); first = false; w.write("{\"name\":\"" + r.name + "\",\"regNo\":" + r.regNo + ",\"phone\":\"" + r.phone + "\"}"); }
                    w.write("]");
                }
                double s3 = (System.nanoTime() - t) / 1e6; t = System.nanoTime();
                String txt = Files.readString(json); int cnt = 0, idx = 0; while ((idx = txt.indexOf("{\"name\"", idx)) >= 0) { idx += 7; cnt++; }   // tokenising scan (no JSON library offline)
                double l3 = (System.nanoTime() - t) / 1e6;
                if (warm == 1) {
                    System.out.printf("%-9d %-16s %10d %10.1f %10.1f%n", n, "serialization", Files.size(ser) / 1024, s1, l1);
                    System.out.printf("%-9d %-16s %10d %10.1f %10.1f%n", n, "CSV", Files.size(csv) / 1024, s2, l2);
                    System.out.printf("%-9d %-16s %10d %10.1f %10s%n", n, "JSON (hand-made)", Files.size(json) / 1024, s3, "n/a*");
                }
            }
            if (n == 100000) System.out.println("* no JSON library offline: a real parser (e.g. Jackson/Gson) would be slower than the token scan I timed, so it is not reported");
            Files.delete(ser); Files.delete(csv); Files.delete(json);
        }
    }
}
