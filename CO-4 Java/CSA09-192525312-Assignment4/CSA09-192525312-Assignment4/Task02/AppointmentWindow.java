import javax.swing.*;
import java.awt.event.*;

public class AppointmentWindow extends JFrame {
    private final AppointmentForm form;
    public AppointmentWindow() {
        super("Outpatient Appointment Desk");
        form = new AppointmentForm(DoctorDirectory.sample(), new AppointmentService());
        setContentPane(form);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {                       // WindowEvent on the frame
            @Override public void windowClosing(WindowEvent e) {
                if (form.bookingInProgress()) {
                    int ans = JOptionPane.showConfirmDialog(AppointmentWindow.this,
                        "A booking is in progress. Close without saving?", "Confirm close", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) return;
                }
                dispose();
            }
        });
        pack(); setLocationRelativeTo(null);
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new AppointmentWindow().setVisible(true)); }
}
