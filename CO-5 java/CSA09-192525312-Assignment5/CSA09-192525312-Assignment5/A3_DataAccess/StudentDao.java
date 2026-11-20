import java.sql.*;
import java.util.*;

public class StudentDao implements Dao<Student, Integer> {
    private final Connection con;
    StudentDao(Connection con) { this.con = con; }

    private Student map(ResultSet rs) throws SQLException {          // the ONLY row-to-object code
        return new Student(rs.getInt("reg_no"), rs.getString("name"), rs.getString("dept"), rs.getDouble("cgpa"));
    }
    public void insert(Student s) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO student(reg_no,name,dept,cgpa) VALUES(?,?,?,?)")) {
            ps.setInt(1, s.regNo); ps.setString(2, s.name); ps.setString(3, s.dept); ps.setDouble(4, s.cgpa); ps.executeUpdate();
        }
    }
    public Optional<Student> findById(Integer id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM student WHERE reg_no=?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }
    public List<Student> findAll() throws SQLException {
        List<Student> out = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM student ORDER BY reg_no")) { while (rs.next()) out.add(map(rs)); }
        return out;
    }
    public int update(Student s) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE student SET name=?, dept=?, cgpa=? WHERE reg_no=?")) {
            ps.setString(1, s.name); ps.setString(2, s.dept); ps.setDouble(3, s.cgpa); ps.setInt(4, s.regNo); return ps.executeUpdate();
        }
    }
    public int delete(Integer id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM student WHERE reg_no=?")) { ps.setInt(1, id); return ps.executeUpdate(); }
    }
}
