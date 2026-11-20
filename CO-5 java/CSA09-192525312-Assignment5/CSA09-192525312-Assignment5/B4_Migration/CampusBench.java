import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** B4 alternative: 12 campuses x own serialized file  vs  one shared database (800,000 passes in total). */
public class CampusBench {
    record Pass(String id, String reg, String name, int route, double amount) implements Serializable {}
    public static void main(String[] a) throws Exception {
        int campuses = 12, per = 66667;                       // about 800,000 passes = 100 x 8,000
        Path dir = Files.createTempDirectory("campus"); long bytes = 0; double saveMs = 0;
        for (int c = 0; c < campuses; c++) {
            List<Pass> l = new ArrayList<>(per); Random r = new Random(c);
            for (int i = 0; i < per; i++) l.add(new Pass(String.format("C%02dP%06d", c, i), "" + (192300000 + i), "Student" + i, 1 + r.nextInt(30), 4500.0));
            long t = System.nanoTime();
            try (ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dir.resolve("campus" + c + ".ser").toFile())))) { o.writeObject(l); }
            saveMs += (System.nanoTime() - t) / 1e6; bytes += Files.size(dir.resolve("campus" + c + ".ser"));
        }
        String target = String.format("C%02dP%06d", campuses - 1, per - 1);        // worst case: last record of last campus
        double best = 1e18; long found = 0;
        for (int run = 0; run < 3; run++) {
            long t = System.nanoTime(); boolean hit = false;
            for (int c = 0; c < campuses && !hit; c++) {
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dir.resolve("campus" + c + ".ser").toFile())))) {
                    @SuppressWarnings("unchecked") List<Pass> l = (List<Pass>) in.readObject();
                    for (Pass p : l) if (p.id().equals(target)) { hit = true; found++; break; }
                }
            }
            best = Math.min(best, (System.nanoTime() - t) / 1e6);
        }
        System.out.printf("12 serialized files : %,d passes, %.1f MB total, save %.0f ms, search every file for one ID: %.0f ms (best of 3, found=%d)%n", campuses * per, bytes / 1048576.0, saveMs, best, found);
        Path dbf = Files.createTempFile("campus", ".sqlite"); double loadMs = 0;
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbf)) {
            try (Statement st = con.createStatement()) { st.execute(Files.readString(Path.of(System.getProperty("schema", "schema_sqlite.sql")))); }
            for (int c = 0; c < campuses; c++) {
                List<String[]> l = new ArrayList<>(per);
                for (int i = 0; i < per; i++) l.add(new String[]{String.format("C%02dP%06d", c, i), "" + (192300000 + i), "Student" + i, "" + (1 + i % 30), "4500.0"});
                long t = System.nanoTime(); PassLoader.loadAll(con, l); loadMs += (System.nanoTime() - t) / 1e6;
            }
            double q = 1e9; for (int run = 0; run < 50; run++) {
                long t = System.nanoTime();
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM bus_pass WHERE pass_id=?")) { ps.setString(1, target); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalStateException(); } }
                q = Math.min(q, (System.nanoTime() - t) / 1e6);
            }
            int rows; try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) { rs.next(); rows = rs.getInt(1); }
            System.out.printf("one shared database : %,d rows, %.1f MB file, load %.0f ms, find one ID by PRIMARY KEY: %.3f ms%n", rows, Files.size(dbf) / 1048576.0, loadMs, q);
            System.out.printf("search speed ratio  : x%,.0f in favour of the indexed database%n", best / q);
        }
        Files.delete(dbf);
        try (var s = Files.list(dir)) { for (Path p : (Iterable<Path>) s::iterator) Files.delete(p); } Files.delete(dir);
    }
}
