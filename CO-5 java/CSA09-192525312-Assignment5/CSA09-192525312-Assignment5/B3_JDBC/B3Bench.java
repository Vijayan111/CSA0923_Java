import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class B3Bench {
    static final String SCHEMA = System.getProperty("schema", "schema_sqlite.sql");
    static final String URL = "jdbc:sqlite:";       // MySQL: jdbc:mysql://localhost:3306/transport_test?rewriteBatchedStatements=true
    static List<String[]> gen(int n) {
        List<String[]> l = new ArrayList<>(n); Random r = new Random(5);
        for (int i = 1; i <= n; i++) l.add(new String[]{String.format("P%06d", i), "" + (192300000 + i), PassGen.NAMES[r.nextInt(10)], "" + (1 + r.nextInt(30)), "" + (3000.0 + r.nextInt(3000))});
        return l;
    }
    static Connection open(Path db) throws Exception {
        Connection c = DriverManager.getConnection(URL + db);
        try (Statement st = c.createStatement()) { st.execute("DROP TABLE IF EXISTS bus_pass"); st.execute(Files.readString(Path.of(SCHEMA))); }
        return c;
    }
    static int count(Connection c) throws SQLException { try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) { rs.next(); return rs.getInt(1); } }
    static void clear(Connection c) throws SQLException { try (Statement st = c.createStatement()) { st.execute("DELETE FROM bus_pass"); } }

    static void viaStatement(Connection c, List<String[]> l) throws SQLException {
        try (Statement st = c.createStatement()) {
            for (String[] p : l) st.executeUpdate("INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES('" + p[0] + "','" + p[1] + "','" + p[2] + "'," + p[3] + "," + p[4] + ")");
        }
    }
    static void viaPrepared(Connection c, List<String[]> l) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES(?,?,?,?,?)")) {
            for (String[] p : l) { ps.setString(1, p[0]); ps.setString(2, p[1]); ps.setString(3, p[2]); ps.setInt(4, Integer.parseInt(p[3])); ps.setDouble(5, Double.parseDouble(p[4])); ps.executeUpdate(); }
        }
    }
    public static void main(String[] a) throws Exception {
        Path db = Files.createTempFile("busdb", ".sqlite");
        try (Connection c = open(db)) {
            System.out.println("== B3.1 correctness on 10 records, each approach ==");
            List<String[]> ten = gen(10);
            viaStatement(c, ten);            System.out.println("Statement           rows=" + count(c)); clear(c);
            viaPrepared(c, ten);             System.out.println("PreparedStatement   rows=" + count(c)); clear(c);
            System.out.println("PreparedStatement+batch rows=" + PassLoader.loadAll(c, ten) + " (returned) / " + count(c) + " (SELECT COUNT)");
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM bus_pass ORDER BY pass_id LIMIT 3")) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) System.out.printf("   col %d %-9s %-8s (JDBC type %s)%n", i, md.getColumnName(i), md.getColumnTypeName(i), md.getColumnClassName(i));
                while (rs.next()) System.out.printf("   %s %s %-8s %2d %.1f%n", rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5));
            }
            clear(c);
            System.out.println("\n== B3.2 time (ms): auto-commit ON for (a) and (b); (c) = PassLoader.loadAll, one transaction ==");
            System.out.printf("%-9s %14s %20s %22s%n", "rows", "(a) Statement", "(b) PreparedStatement", "(c) PrepStmt + batch");
            for (int n : new int[]{1000, 10000, 100000}) {
                List<String[]> l = gen(n); double[] t = new double[3];
                long s = System.nanoTime(); viaStatement(c, l); t[0] = (System.nanoTime() - s) / 1e6; int c1 = count(c); clear(c);
                s = System.nanoTime(); viaPrepared(c, l); t[1] = (System.nanoTime() - s) / 1e6; int c2 = count(c); clear(c);
                s = System.nanoTime(); int loaded = PassLoader.loadAll(c, l); t[2] = (System.nanoTime() - s) / 1e6; int c3 = count(c); clear(c);
                System.out.printf("%-9d %11.0f ms %17.0f ms %19.0f ms   rows ok: %b%n", n, t[0], t[1], t[2], c1 == n && c2 == n && c3 == n && loaded == n);
            }
            System.out.println("\n== B3.3 batch insert of 100,000: auto-commit ON vs one transaction ==");
            List<String[]> big = gen(100000);
            long s = System.nanoTime(); PassLoader.loadAllAutoCommit(c, big); double ac = (System.nanoTime() - s) / 1e6; clear(c);
            s = System.nanoTime(); PassLoader.loadAll(c, big); double tx = (System.nanoTime() - s) / 1e6; clear(c);
            System.out.printf("auto-commit ON  : %8.0f ms%n one transaction  : %8.0f ms   (x%.1f)%n", ac, tx, ac / tx);
            System.out.println("\n-- forced failure: record 5,001 of 10,000 has amount 'ABC' --");
            List<String[]> bad = gen(10000); bad.get(5000)[4] = "ABC";
            try { PassLoader.loadAllAutoCommit(c, bad); } catch (RuntimeException e) { System.out.println("auto-commit ON : " + e.getClass().getSimpleName() + " -> rows left in table: " + count(c)); }
            clear(c);
            try { PassLoader.loadAll(c, bad); } catch (RuntimeException e) { System.out.println("one transaction: " + e.getClass().getSimpleName() + " -> rows left in table: " + count(c)); }
        }
        Files.deleteIfExists(db);
    }
}
