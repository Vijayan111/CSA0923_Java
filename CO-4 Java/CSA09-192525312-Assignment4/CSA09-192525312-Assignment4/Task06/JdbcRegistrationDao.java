import java.sql.*;
import java.util.*;

/** MySQL implementation: every statement is parameterised and every connection is closed by try-with-resources. */
public class JdbcRegistrationDao implements RegistrationDao {
    private static final String SELECT =
        "SELECT r.id, r.student_id, r.name, r.email, e.name AS event, r.registered_at FROM registrations r " +
        "JOIN events e ON e.id = r.event_id WHERE (? IS NULL OR e.name = ?) ORDER BY r.id";
    private final String url, user, pwd;
    public JdbcRegistrationDao(String url, String user, String pwd) { this.url = url; this.user = user; this.pwd = pwd; }
    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, pwd); }

    @Override public List<String> events() throws SQLException {
        try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT name FROM events ORDER BY name")) {
            List<String> r = new ArrayList<>(); while (rs.next()) r.add(rs.getString(1)); return r;
        }
    }
    @Override public boolean exists(String studentId, String event) throws SQLException {
        String sql = "SELECT 1 FROM registrations r JOIN events e ON e.id = r.event_id WHERE r.student_id = ? AND e.name = ? LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId); ps.setString(2, event);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    @Override public void insert(String studentId, String name, String email, String event) throws Exception {
        String sql = "INSERT INTO registrations(student_id, name, email, event_id) SELECT ?, ?, ?, id FROM events WHERE name = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId); ps.setString(2, name); ps.setString(3, email); ps.setString(4, event);
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Unknown event: " + event);
        } catch (SQLIntegrityConstraintViolationException dup) {            // UNIQUE (student_id, event_id): safe even if two desks race
            throw new DuplicateRegistrationException(studentId, event);
        }
    }
    @Override public int count(String event) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN events e ON e.id = r.event_id WHERE (? IS NULL OR e.name = ?)";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, event); ps.setString(2, event);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
    @Override public List<Registration> findPage(String event, int offset, int limit) throws SQLException {
        return query(SELECT + " LIMIT ? OFFSET ?", event, limit, offset);
    }
    @Override public List<Registration> findAll(String event) throws SQLException { return query(SELECT, event); }

    private List<Registration> query(String sql, String event, int... extra) throws SQLException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, event); ps.setString(2, event);
            for (int i = 0; i < extra.length; i++) ps.setInt(3 + i, extra[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> out = new ArrayList<>();
                while (rs.next()) out.add(new Registration(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getTimestamp(6).toLocalDateTime()));
                return out;
            }
        }
    }
}
