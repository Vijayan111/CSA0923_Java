public class Student {            // class field -> table column
    int regNo;                    // reg_no  INTEGER PRIMARY KEY
    String name;                  // name    VARCHAR(50) NOT NULL
    String dept;                  // dept    VARCHAR(20)
    double cgpa;                  // cgpa    REAL
    Student(int regNo, String name, String dept, double cgpa) { this.regNo = regNo; this.name = name; this.dept = dept; this.cgpa = cgpa; }
    @Override public String toString() { return regNo + " " + name + " " + dept + " " + cgpa; }
}
