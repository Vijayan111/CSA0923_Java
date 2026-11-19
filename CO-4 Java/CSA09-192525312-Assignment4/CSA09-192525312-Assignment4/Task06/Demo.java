import javax.swing.*;
import java.awt.*;

/** Off-screen driver for the screenshots: drives the real MainPanel through normal, invalid and duplicate registrations. */
public class Demo {
    static MainPanel p;
    static void waitIdle() { for (int i = 0; i < 100; i++) { Render.wait(30); Render.sync(); if (!p.isBusy()) { Render.wait(60); Render.sync(); if (!p.isBusy()) return; } } }
    static void shot(String title, String file) throws Exception { Render.window(p, p.buildMenuBar(), title, 900, 540, file); }
    static void fill(String id, String name, String mail, String ev) throws Exception {
        SwingUtilities.invokeAndWait(() -> { p.studentIdField().setText(id); p.nameField().setText(name); p.emailField().setText(mail); p.eventFormBox().setSelectedItem(ev); });
    }
    static void register() throws Exception { SwingUtilities.invokeAndWait(() -> p.registerButton().doClick()); waitIdle(); }
    public static void main(String[] a) throws Exception {
        String out = a[0]; String T = "Campus Event Registration  [DEMO: in-memory data]";
        p = new MainPanel(new RegistrationService(InMemoryRegistrationDao.seeded(46)));
        p.setSize(900, 540); waitIdle();
        System.out.println("start        : " + p.tableModel().getRowCount() + " rows on page 1");
        shot(T, out + "/t6_start.png");
        fill("192525312", "Vijayan P", "vijayan@simats.edu", "Hackathon"); register();
        System.out.println("normal       : table rows = " + p.tableModel().getRowCount());
        shot(T, out + "/t6_normal.png");
        fill("192525313", "Karthik R", "karthik.simats.edu", "Hackathon"); register();
        shot(T, out + "/t6_invalid.png");
        fill("192525312", "Vijayan P", "vijayan@simats.edu", "Hackathon"); register();
        shot(T, out + "/t6_duplicate.png");
        SwingUtilities.invokeAndWait(() -> p.filterBox().setSelectedItem("Hackathon")); waitIdle();
        shot(T, out + "/t6_filter.png");
        System.exit(0);
    }
}
