import javax.swing.*;
import java.awt.*;

public class AppointmentDemo {
    static void shot(AppointmentForm f, String file) throws Exception {
        Dimension d = f.getPreferredSize(); Render.window(f, "Outpatient Appointment Desk", Math.max(d.width, 380), d.height, file);
    }
    public static void main(String[] a) throws Exception {
        String out = a[0];
        AppointmentForm f = new AppointmentForm(DoctorDirectory.sample(), new AppointmentService());
        f.setSize(Math.max(f.getPreferredSize().width, 380), f.getPreferredSize().height);
        shot(f, out + "/t2_initial.png");
        SwingUtilities.invokeAndWait(() -> { f.patient().setText("P10234"); f.deptBox().setSelectedItem("Cardiology"); });
        System.out.println("Department = Cardiology -> doctor list: " + items(f.doctorBox()));
        SwingUtilities.invokeAndWait(() -> { f.doctorBox().setSelectedIndex(1); f.slotBox().setSelectedItem("10:00"); });
        shot(f, out + "/t2_selected.png");
        SwingUtilities.invokeAndWait(() -> f.bookButton().doClick());
        shot(f, out + "/t2_booked.png");
        SwingUtilities.invokeAndWait(() -> { f.patient().setText("P20567"); f.bookButton().doClick(); });      // same doctor + slot again
        shot(f, out + "/t2_conflict.png");
        SwingUtilities.invokeAndWait(() -> { f.deptBox().setSelectedItem("Paediatrics"); });
        System.out.println("Department = Paediatrics -> doctor list: " + items(f.doctorBox()));
        System.out.println("bookingInProgress() = " + f.bookingInProgress() + "  (window would ask for confirmation before closing)");
        System.exit(0);
    }
    static String items(JComboBox<Object> b) { StringBuilder s = new StringBuilder(); for (int i = 0; i < b.getItemCount(); i++) s.append(b.getItemAt(i)).append(i < b.getItemCount() - 1 ? " | " : ""); return s.toString(); }
}
