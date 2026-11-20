import java.sql.*;

/** Alternative concept: escaping / regex validation instead of PreparedStatement. */
public class EscapeBench {
    static String escape(String s) { return s.replace("'", "''"); }
    public static void main(String[] a) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE users(id TEXT PRIMARY KEY, pwd TEXT)");
                st.execute("INSERT INTO users VALUES('admin','S3cret#26')");
                st.execute("CREATE TABLE bus_pass(pass_id TEXT PRIMARY KEY, route_no INTEGER)");
                st.execute("INSERT INTO bus_pass VALUES('P1',12),('P2',7),('P3',12)");
            }
            System.out.println("-- escaping quotes stops the string attack --");
            String pwd = escape("x' OR '1'='1");
            String sql = "SELECT * FROM users WHERE id='admin' AND pwd='" + pwd + "'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { System.out.println("   " + sql + "\n   login accepted: " + rs.next()); }
            System.out.println("-- ...but not a numeric context (no quotes to escape) --");
            String route = escape("99 OR 1=1");
            sql = "SELECT COUNT(*) FROM bus_pass WHERE route_no=" + route;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { rs.next(); System.out.println("   " + sql + "\n   rows returned for route 99 (should be 0): " + rs.getInt(1)); }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bus_pass WHERE route_no=?")) {
                try { ps.setString(1, "99 OR 1=1"); try (ResultSet rs = ps.executeQuery()) { rs.next(); System.out.println("   PreparedStatement, same input as a value: " + rs.getInt(1) + " rows"); } } catch (SQLException e) { System.out.println("   PreparedStatement: " + e.getMessage()); }
            }
            System.out.println();
            int q = 20000; String[] ids = new String[q]; for (int i = 0; i < q; i++) ids[i] = "P" + (i % 3 + 1);
            double best1 = 1e9, best2 = 1e9, best3 = 1e9;
            for (int run = 0; run < 7; run++) {
                long t = System.nanoTime();
                try (PreparedStatement ps = con.prepareStatement("SELECT route_no FROM bus_pass WHERE pass_id=?")) {
                    for (String id : ids) { ps.setString(1, id); try (ResultSet rs = ps.executeQuery()) { rs.next(); } }
                }
                best1 = Math.min(best1, (System.nanoTime() - t) / 1e3 / q);
                t = System.nanoTime();
                for (String id : ids) { try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT route_no FROM bus_pass WHERE pass_id='" + escape(id) + "'")) { rs.next(); } }
                best2 = Math.min(best2, (System.nanoTime() - t) / 1e3 / q);
                t = System.nanoTime();
                for (String id : ids) { if (!id.matches("P\\d{1,6}")) continue; try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT route_no FROM bus_pass WHERE pass_id='" + id + "'")) { rs.next(); } }
                best3 = Math.min(best3, (System.nanoTime() - t) / 1e3 / q);
            }
            System.out.printf("time per query over %d queries (best of 7 runs):%n", q);
            System.out.printf("   PreparedStatement (reused)      : %6.2f us%n   Statement + escape quotes        : %6.2f us%n   Statement + regex validation     : %6.2f us%n", best1, best2, best3);
            long t = System.nanoTime(); int c = 0; for (int i = 0; i < 1_000_000; i++) if (("P" + (i % 999999)).matches("P\\d{1,6}")) c++;
            System.out.printf("   regex match alone                : %6.3f us per call (String.matches, compiled each time)%n", (System.nanoTime() - t) / 1e3 / 1_000_000);
        }
    }
}
