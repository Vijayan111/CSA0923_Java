import java.io.*;
import java.sql.*;
public class BadResidentDemo {
    static class BadResident implements Serializable { String name = "Arun"; Connection conn; }      // conn NOT transient
    public static void main(String[] a) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:");
             ObjectOutputStream o = new ObjectOutputStream(new ByteArrayOutputStream())) {
            BadResident b = new BadResident(); b.conn = con;
            o.writeObject(b);
        } catch (NotSerializableException e) { System.out.println("without transient: java.io.NotSerializableException: " + e.getMessage()); }
    }
}
