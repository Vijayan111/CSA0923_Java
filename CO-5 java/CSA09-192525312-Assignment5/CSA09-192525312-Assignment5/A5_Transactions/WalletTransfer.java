import java.sql.*;

public class WalletTransfer {
    static void balances(Connection con, String label) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT name, balance FROM wallet ORDER BY name")) {
            StringBuilder sb = new StringBuilder(String.format("  %-38s", label)); double total = 0;
            while (rs.next()) { sb.append(String.format("%s=%.0f  ", rs.getString(1), rs.getDouble(2))); total += rs.getDouble(2); }
            System.out.println(sb + "total=" + (int) total);
        }
    }
    static void reset(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) { st.execute("DELETE FROM wallet"); st.execute("INSERT INTO wallet VALUES('canteen',200),('hostel',1000)"); }
    }
    static void update(Connection con, String who, double delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE wallet SET balance = balance + ? WHERE name = ?")) { ps.setDouble(1, delta); ps.setString(2, who); ps.executeUpdate(); }
    }
    /** A5.1: both updates succeed or neither does */
    static void transfer(Connection con, double amt, boolean crash) throws SQLException {
        boolean old = con.getAutoCommit(); con.setAutoCommit(false);
        try {
            update(con, "hostel", -amt);
            if (crash) throw new IllegalStateException("simulated failure between the two updates");
            update(con, "canteen", +amt);
            con.commit();
        } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        finally { con.setAutoCommit(old); }
    }
    /** alternative: two independent auto-commit statements */
    static void transferAutoCommit(Connection con, double amt, boolean crash) throws SQLException {
        update(con, "hostel", -amt);
        if (crash) throw new IllegalStateException("simulated failure between the two updates");
        update(con, "canteen", +amt);
    }
    public static void main(String[] a) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            try (Statement st = con.createStatement()) { st.execute("CREATE TABLE wallet(name TEXT PRIMARY KEY, balance REAL CHECK(balance >= 0))"); }
            reset(con); balances(con, "start:");
            System.out.println("\nCase 1: two auto-commit statements, failure between them");
            try { transferAutoCommit(con, 500, true); } catch (RuntimeException e) { System.out.println("  exception: " + e.getMessage()); }
            balances(con, "after failure:  (500 LOST)");
            reset(con);
            System.out.println("\nCase 2: one transaction (setAutoCommit(false) / commit / rollback), same failure");
            try { transfer(con, 500, true); } catch (RuntimeException e) { System.out.println("  exception: " + e.getMessage()); }
            balances(con, "after rollback: (nothing lost)");
            System.out.println("\nCase 3: one transaction, no failure");
            transfer(con, 500, false); balances(con, "after commit:");
            System.out.println("\nCase 4: transfer 900 more (hostel only has 500) -> CHECK constraint stops it, rollback");
            try { transfer(con, 900, false); } catch (SQLException e) { System.out.println("  exception: " + e.getMessage().split("\n")[0]); }
            balances(con, "after rollback:");
        }
    }
}
