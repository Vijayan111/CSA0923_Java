import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AppointmentForm extends JPanel {
    static final String PROMPT = "-- select --";
    private final DoctorDirectory directory; private final AppointmentService service;
    private final JTextField patientId = new JTextField(10);
    private final JComboBox<String> dept = new JComboBox<>();
    private final JComboBox<Object> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>();
    private final JButton book = new JButton("Book appointment");
    private final JLabel status = new JLabel(" ");
    private boolean dirty;                                   // a booking has been started but not completed

    public AppointmentForm(DoctorDirectory directory, AppointmentService service) {
        this.directory = directory; this.service = service;
        setLayout(new BorderLayout(8, 10)); setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel grid = new JPanel(new GridLayout(4, 2, 6, 8));
        grid.add(new JLabel("Patient ID")); grid.add(patientId);
        grid.add(new JLabel("Department")); grid.add(dept);
        grid.add(new JLabel("Doctor")); grid.add(doctor);
        grid.add(new JLabel("Time slot")); grid.add(slot);
        JPanel south = new JPanel(new BorderLayout(4, 6));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER)); row.add(book);
        south.add(row, BorderLayout.NORTH); south.add(status, BorderLayout.SOUTH);
        add(grid, BorderLayout.CENTER); add(south, BorderLayout.SOUTH);
        dept.addItem(PROMPT); for (String d : directory.departments()) dept.addItem(d);
        slot.addItem(PROMPT); for (String s : AppointmentService.SLOTS) slot.addItem(s);
        resetDoctors();

        dept.addActionListener(e -> { refreshDoctors(); dirty = true; });               // ActionEvent on JComboBox
        patientId.getDocument().addDocumentListener(new DocumentListener() {             // DocumentEvent on the text field
            public void insertUpdate(DocumentEvent e) { dirty = true; }
            public void removeUpdate(DocumentEvent e) { dirty = true; }
            public void changedUpdate(DocumentEvent e) { }
        });
        slot.addActionListener(e -> dirty = true);
        book.addActionListener(e -> onBook());                                           // ActionEvent on JButton
    }
    private void resetDoctors() { doctor.removeAllItems(); doctor.addItem(PROMPT); doctor.setEnabled(false); }
    /** (a) refresh the doctor list for the chosen department: one Map lookup, no database call. */
    void refreshDoctors() {
        resetDoctors();
        String d = (String) dept.getSelectedItem();
        if (d == null || PROMPT.equals(d)) return;
        for (Doctor doc : directory.doctorsIn(d)) doctor.addItem(doc);
        doctor.setEnabled(true);
    }
    /** (b) validate and book. */
    private void onBook() {
        Object d = doctor.getSelectedItem(); String s = (String) slot.getSelectedItem(); String dp = (String) dept.getSelectedItem();
        AppointmentService.Result r = service.book(patientId.getText(),
            PROMPT.equals(dp) ? null : dp, d instanceof Doctor doc ? doc : null, PROMPT.equals(s) ? null : s);
        status.setForeground(r.ok() ? new Color(0x1B5E20) : new Color(0xB00020)); status.setText(r.message());
        if (r.ok()) dirty = false;
    }
    public boolean bookingInProgress() { return dirty; }
    // used by the demo
    JTextField patient() { return patientId; } JComboBox<String> deptBox() { return dept; }
    JComboBox<Object> doctorBox() { return doctor; } JComboBox<String> slotBox() { return slot; } JButton bookButton() { return book; }
}
