import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** UI layer. Every call that touches the service (and therefore the database) runs in a SwingWorker, never on the EDT. */
public class MainPanel extends JPanel {
    static final int PAGE_SIZE = 10; static final String ALL = "All events";
    private static final String[] COLUMNS = {"ID", "Student ID", "Name", "E-mail", "Event", "Registered at"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final RegistrationService service;
    private final JTextField studentId = new JTextField(10), name = new JTextField(14), email = new JTextField(16);
    private final JComboBox<String> eventForm = new JComboBox<>(), eventFilter = new JComboBox<>();
    private final JButton register = new JButton("Register"), prev = new JButton("< Prev"), next = new JButton("Next >");
    private final JLabel feedback = new JLabel(" "), pageLabel = new JLabel("Page -"), status = new JLabel("Starting ...");
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);
    private int page = 0, totalPages = 1; private boolean updating, busy;

    public MainPanel(RegistrationService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 6)); setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        JPanel grid = new JPanel(new GridLayout(2, 4, 8, 2));
        for (String s : new String[]{"Student ID", "Name", "E-mail", "Event"}) grid.add(new JLabel(s));
        grid.add(studentId); grid.add(name); grid.add(email); grid.add(eventForm);
        JPanel form = new JPanel(new BorderLayout(10, 2)); form.setBorder(BorderFactory.createTitledBorder("New registration"));
        form.add(grid, BorderLayout.CENTER); JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 14)); btn.add(register); form.add(btn, BorderLayout.EAST);
        form.add(feedback, BorderLayout.SOUTH);
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2)); filterRow.add(new JLabel("Filter by event:")); filterRow.add(eventFilter);
        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2)); pager.add(prev); pager.add(pageLabel); pager.add(next);
        JPanel list = new JPanel(new BorderLayout(0, 4)); list.setBorder(BorderFactory.createTitledBorder("Registrations"));
        list.add(filterRow, BorderLayout.NORTH); list.add(new JScrollPane(table), BorderLayout.CENTER); list.add(pager, BorderLayout.SOUTH);
        int[] w = {40, 100, 120, 200, 110, 120}; for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.setFillsViewportHeight(true); table.setRowHeight(20);
        add(form, BorderLayout.NORTH); add(list, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);

        register.addActionListener(e -> onRegister());
        prev.addActionListener(e -> { if (page > 0) { page--; loadPage(); } });
        next.addActionListener(e -> { if (page < totalPages - 1) { page++; loadPage(); } });
        eventFilter.addActionListener(e -> { if (!updating) { page = 0; loadPage(); } });
        loadEvents();
    }

    public JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File"), view = new JMenu("View"), help = new JMenu("Help");
        JMenuItem export = new JMenuItem("Export CSV ..."), exit = new JMenuItem("Exit"), refresh = new JMenuItem("Refresh"), clear = new JMenuItem("Clear filter"), about = new JMenuItem("About");
        export.addActionListener(e -> onExport()); exit.addActionListener(e -> System.exit(0));
        refresh.addActionListener(e -> loadPage()); clear.addActionListener(e -> eventFilter.setSelectedIndex(0));
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Campus Event Registration\nSIMATS Engineering - CSA09"));
        file.add(export); file.addSeparator(); file.add(exit); view.add(refresh); view.add(clear); help.add(about);
        bar.add(file); bar.add(view); bar.add(help); return bar;
    }

    private String filter() { String s = (String) eventFilter.getSelectedItem(); return s == null || ALL.equals(s) ? null : s; }
    private void setBusy(boolean b) { busy = b; register.setEnabled(!b); eventFilter.setEnabled(!b); prev.setEnabled(!b && page > 0); next.setEnabled(!b && page < totalPages - 1); }
    private void feedback(String msg, boolean error) { feedback.setForeground(error ? new Color(0xB00020) : new Color(0x1B5E20)); feedback.setText(msg); }

    private void loadEvents() {
        setBusy(true); status.setText("Loading events ...");
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    updating = true; eventForm.removeAllItems(); eventFilter.removeAllItems(); eventFilter.addItem(ALL);
                    for (String s : get()) { eventForm.addItem(s); eventFilter.addItem(s); }
                    updating = false; loadPage();
                } catch (Exception ex) { updating = false; status.setText("Cannot load events: " + ex.getMessage()); setBusy(false); }
            }
        }.execute();
    }
    void loadPage() {
        setBusy(true); status.setText("Loading page " + (page + 1) + " ...");
        final String f = filter(); final int p = page;
        new SwingWorker<RegistrationService.PageData, Void>() {
            @Override protected RegistrationService.PageData doInBackground() throws Exception { return service.page(f, p, PAGE_SIZE); }
            @Override protected void done() {
                try {
                    RegistrationService.PageData d = get(); model.setRowCount(0);
                    for (Registration r : d.rows()) model.addRow(new Object[]{r.id(), r.studentId(), r.name(), r.email(), r.event(), r.registeredAt().format(FMT)});
                    totalPages = Math.max(1, (d.total() + PAGE_SIZE - 1) / PAGE_SIZE);
                    pageLabel.setText("Page " + (page + 1) + " of " + totalPages + "   (" + d.total() + " registrations)");
                    status.setText("Showing " + d.rows().size() + " row(s) - " + (f == null ? ALL : f));
                } catch (Exception ex) { status.setText("Load failed: " + ex.getMessage()); }
                finally { setBusy(false); }
            }
        }.execute();
    }
    private void onRegister() {
        String err = service.validate(studentId.getText(), name.getText(), email.getText(), (String) eventForm.getSelectedItem());   // cheap, on the EDT
        if (err != null) { feedback(err, true); return; }
        setBusy(true); status.setText("Saving ..."); feedback(" ", false);
        final String s = studentId.getText(), n = name.getText(), m = email.getText(), ev = (String) eventForm.getSelectedItem();
        new SwingWorker<RegistrationService.Result, Void>() {
            @Override protected RegistrationService.Result doInBackground() { return service.register(s, n, m, ev); }
            @Override protected void done() {
                try {
                    RegistrationService.Result r = get(); feedback(r.message(), !r.ok());
                    if (r.ok()) { studentId.setText(""); name.setText(""); email.setText(""); }
                } catch (Exception ex) { feedback("Unexpected error: " + ex.getMessage(), true); }
                setBusy(false); loadPage();
            }
        }.execute();
    }
    private void onExport() {
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new java.io.File("registrations.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final Path target = fc.getSelectedFile().toPath(); final String f = filter(); status.setText("Exporting ...");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception { return service.exportCsv(f, target); }
            @Override protected void done() { try { status.setText("Exported " + get() + " row(s) to " + target); } catch (Exception ex) { status.setText("Export failed: " + ex.getMessage()); } }
        }.execute();
    }
    // accessors for tests / demo
    boolean isBusy() { return busy; }
    JTextField studentIdField() { return studentId; } JTextField nameField() { return name; } JTextField emailField() { return email; }
    JComboBox<String> eventFormBox() { return eventForm; } JComboBox<String> filterBox() { return eventFilter; } JButton registerButton() { return register; }
    JButton nextButton() { return next; } DefaultTableModel tableModel() { return model; }
}
