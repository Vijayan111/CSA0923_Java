import javax.swing.*;

public class FeeWindow extends JFrame {
    public FeeWindow(FeeService service) {
        super("Student Fee Payment");
        setContentPane(new FeeForm(service));
        setDefaultCloseOperation(EXIT_ON_CLOSE);      // connections are opened per payment and closed there
        pack(); setLocationRelativeTo(null);
    }
    public static void main(String[] args) {
        FeeRepository repo = System.getenv("FEE_DB_URL") != null
            ? new JdbcFeeRepository(System.getenv("FEE_DB_URL"), System.getenv("FEE_DB_USER"), System.getenv("FEE_DB_PWD"))
            : new InMemoryFeeRepository(1500);
        SwingUtilities.invokeLater(() -> new FeeWindow(new FeeService(repo)).setVisible(true));
    }
}
