import java.awt.*;
import java.awt.event.*;
import java.sql.*;
// The junior developer's program as given (imports and the three constants added so that javac can analyse it).
public class FeeWindowBad extends Frame implements ActionListener {
    static final String URL = "jdbc:mysql://localhost/college", USER = "root", PWD = "";
    TextField regNo = new TextField(); TextField amount = new TextField();
    Button pay = new Button("Pay"), clear = new Button("Clear"), receipt = new Button("Receipt");
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "Pay") {
            double a = Double.parseDouble(amount.getText());
            Connection c = DriverManager.getConnection(URL, USER, PWD);
            c.createStatement().executeUpdate("INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")");
            Thread.sleep(3000); // wait for bank confirmation
            System.out.println("Paid");
        } else { regNo.setText(""); amount.setText(""); }
    }
}
