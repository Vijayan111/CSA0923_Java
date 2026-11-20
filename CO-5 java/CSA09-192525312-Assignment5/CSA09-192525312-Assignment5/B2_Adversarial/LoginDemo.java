import java.sql.*;

public class LoginDemo {
    static boolean unsafe(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";      // Task A5.2 original
        System.out.println("   SQL sent : " + sql);
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { return rs.next(); }
    }
    static boolean safe(Connection con, String id, String pwd) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id=? AND pwd=?")) {
            ps.setString(1, id); ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    public static void main(String[] a) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {        // local test DB, created only for this assignment
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE users(id TEXT PRIMARY KEY, pwd TEXT)");
                st.execute("INSERT INTO users VALUES('admin','S3cret#26'),('arun','bus1234')");
            }
            String[][] tests = {
                {"valid login",            "admin", "S3cret#26"},
                {"wrong password",         "admin", "guess"},
                {"ATTACK 1: OR '1'='1",    "admin", "x' OR '1'='1"},
                {"ATTACK 2: comment out",  "admin'--", "anything"},
            };
            for (String[] t : tests) {
                System.out.println(t[0] + "   (id=" + t[1] + "  pwd=" + t[2] + ")");
                boolean u = unsafe(con, t[1], t[2]), s = safe(con, t[1], t[2]);
                System.out.printf("   unsafe version: %-9s PreparedStatement: %s%n%n", u ? "LOGIN OK" : "rejected", s ? "LOGIN OK" : "rejected");
            }
        }
    }
}
