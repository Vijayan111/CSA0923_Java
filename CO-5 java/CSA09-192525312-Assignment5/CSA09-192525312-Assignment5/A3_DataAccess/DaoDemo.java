import java.sql.*;

public class DaoDemo {
    public static void main(String[] a) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE student(reg_no INTEGER PRIMARY KEY, name VARCHAR(50) NOT NULL, dept VARCHAR(20), cgpa REAL)");
            }
            Dao<Student, Integer> dao = new StudentDao(con);          // callers see only the interface
            dao.insert(new Student(192525312, "Vijayan P", "CSE", 8.4));
            dao.insert(new Student(192525313, "Divya", "CSE", 9.1));
            dao.insert(new Student(192525314, "Arun", "ECE", 7.8));
            System.out.println("findAll()        : "); dao.findAll().forEach(s -> System.out.println("   " + s));
            System.out.println("findById(313)    : " + dao.findById(192525313).orElse(null));
            System.out.println("findById(999)    : " + dao.findById(999).orElse(null));
            System.out.println("update cgpa 7.8->8.0 rows: " + dao.update(new Student(192525314, "Arun", "ECE", 8.0)));
            System.out.println("delete(192525313) rows   : " + dao.delete(192525313));
            System.out.println("findAll() after  : "); dao.findAll().forEach(s -> System.out.println("   " + s));
            System.out.println("\nObject-table mapping (from ResultSetMetaData):");
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM student")) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) System.out.printf("   column %d: %-8s %-10s%n", i, md.getColumnName(i), md.getColumnTypeName(i));
            }
            try (ResultSet pk = con.getMetaData().getPrimaryKeys(null, null, "student")) { while (pk.next()) System.out.println("   primary key: " + pk.getString("COLUMN_NAME")); }
        }
    }
}
