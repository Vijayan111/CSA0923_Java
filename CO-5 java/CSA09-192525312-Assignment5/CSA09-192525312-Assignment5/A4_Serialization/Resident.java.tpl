import java.io.Serializable;
import java.sql.Connection;

public class Resident implements Serializable {
    /*UID*/
    String name;
    int regNo;
    transient String password;        // secret: must never reach the file
    transient Connection conn;        // live resource: not Serializable, meaningless after reload
    /*PHONE*/
    Resident(String name, int regNo, String password, Connection conn) { this.name = name; this.regNo = regNo; this.password = password; this.conn = conn; }
    @Override public String toString() { return name + "(" + regNo + ", pwd=" + password + ", conn=" + (conn == null ? "null" : "live") + /*PHONESTR*/ + ")"; }
}
