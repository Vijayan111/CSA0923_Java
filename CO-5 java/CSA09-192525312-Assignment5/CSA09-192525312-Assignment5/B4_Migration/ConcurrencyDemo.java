import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/** B4.3: four counters issue 500 passes each at the same time. */
public class ConcurrencyDemo {
    static final int THREADS = 4, EACH = 500;
    static int countLines(Path f) throws Exception { try (var s = Files.lines(f)) { return (int) s.count(); } }
    public static void main(String[] a) throws Exception {
        // ---- text file: next ID = lines + 1, then append ----
        Path f = Files.createTempFile("counter", ".csv");
        PassStore store = new PassStore(f);
        ExecutorService ex = Executors.newFixedThreadPool(THREADS); CountDownLatch go = new CountDownLatch(1); List<Future<?>> fs = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) { final int tt = t; fs.add(ex.submit(() -> { go.await(); for (int i = 0; i < EACH; i++) { int id = countLines(f) + 1; store.add(String.format("P%06d", id), "1923" + tt, "Counter" + tt, 1 + tt, 4500); } return null; })); }
        go.countDown(); for (Future<?> x : fs) x.get(); ex.shutdown();
        List<String> all = Files.readAllLines(f); Set<String> ids = new HashSet<>(); int dup = 0, bad = 0;
        for (String l : all) { String[] p = l.split(",", -1); if (p.length != 5) { bad++; continue; } if (!ids.add(p[0])) dup++; }
        System.out.println("TEXT FILE : " + THREADS + " counters x " + EACH + " passes = " + (THREADS * EACH) + " issued");
        System.out.println("            lines in file = " + all.size() + ", distinct pass IDs = " + ids.size() + ", DUPLICATE IDs = " + dup + ", damaged lines = " + bad);
        Files.delete(f);
        // ---- database: engine assigns the key, each counter has its own connection + transaction ----
        Path dbf = Files.createTempFile("counter", ".sqlite");
        try (Connection c0 = DriverManager.getConnection("jdbc:sqlite:" + dbf); Statement st = c0.createStatement()) {
            st.execute("CREATE TABLE issued(pass_no INTEGER PRIMARY KEY AUTOINCREMENT, counter TEXT, route_no INT, amount DOUBLE)");
        }
        ex = Executors.newFixedThreadPool(THREADS); CountDownLatch go2 = new CountDownLatch(1); fs.clear();
        for (int t = 0; t < THREADS; t++) { final int tt = t; fs.add(ex.submit(() -> {
            go2.await();
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbf)) {
                try (Statement st = c.createStatement()) { st.execute("PRAGMA busy_timeout=60000"); }
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO issued(counter,route_no,amount) VALUES(?,?,?)")) {
                    for (int i = 0; i < EACH; i++) { ps.setString(1, "Counter" + tt); ps.setInt(2, 1 + tt); ps.setDouble(3, 4500); ps.executeUpdate(); }
                }
                c.commit();
            }
            return null; })); }
        go2.countDown(); for (Future<?> x : fs) x.get(); ex.shutdown();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbf); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*), COUNT(DISTINCT pass_no), MIN(pass_no), MAX(pass_no) FROM issued")) {
            rs.next(); System.out.println("DATABASE  : rows = " + rs.getInt(1) + ", distinct pass numbers = " + rs.getInt(2) + ", range " + rs.getInt(3) + ".." + rs.getInt(4) + ", DUPLICATES = " + (rs.getInt(1) - rs.getInt(2)));
        }
        Files.delete(dbf);
    }
}
