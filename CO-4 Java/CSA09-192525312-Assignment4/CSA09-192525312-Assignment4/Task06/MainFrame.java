import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame(RegistrationService service, String title) {
        super(title);
        MainPanel panel = new MainPanel(service);
        setContentPane(panel); setJMenuBar(panel.buildMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(900, 560); setLocationRelativeTo(null);
    }
    /** java -Ddb.url=jdbc:mysql://localhost:3306/campus_events -Ddb.user=root -Ddb.pwd=secret MainFrame  (MySQL driver on the class path) */
    public static void main(String[] args) {
        String url = System.getProperty("db.url");
        RegistrationDao dao = url != null ? new JdbcRegistrationDao(url, System.getProperty("db.user", "root"), System.getProperty("db.pwd", ""))
                                          : InMemoryRegistrationDao.seeded(46);
        String title = url != null ? "Campus Event Registration" : "Campus Event Registration  [DEMO: in-memory data]";
        SwingUtilities.invokeLater(() -> new MainFrame(new RegistrationService(dao), title).setVisible(true));
    }
}
