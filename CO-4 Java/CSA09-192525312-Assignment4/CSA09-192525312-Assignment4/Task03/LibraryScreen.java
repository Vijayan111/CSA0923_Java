import javax.swing.*;
import java.awt.*;

/** Nested BorderLayout / FlowLayout design. Only the CENTER (results) grows when the window is resized. */
public class LibraryScreen extends JPanel {
    static final String[][] CATALOGUE = {
        {"Programming in Java", "Herbert Schildt", "Computer Science"}, {"Effective Java", "Joshua Bloch", "Computer Science"},
        {"Database System Concepts", "Silberschatz", "Computer Science"}, {"Artificial Intelligence", "Stuart Russell", "AI"},
        {"Pattern Recognition", "C. M. Bishop", "AI"}, {"Wings of Fire", "A. P. J. Abdul Kalam", "Biography"},
        {"The Discovery of India", "J. Nehru", "History"}, {"Engineering Mathematics", "B. S. Grewal", "Mathematics"}};
    private final JTextField query = new JTextField(18);
    private final JComboBox<String> category = new JComboBox<>(new String[]{"All", "Title", "Author", "Subject"});
    private final JButton search = new JButton("Search");
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> results = new JList<>(model);
    private final JButton prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
    private final JLabel status = new JLabel("Enter a search term.");

    public LibraryScreen() {
        setLayout(new BorderLayout(0, 4));                                          // root: 3 regions used
        JLabel title = new JLabel("Campus Library - Catalogue Search", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f)); title.setOpaque(true);
        title.setBackground(new Color(0x1F3864)); title.setForeground(Color.WHITE); title.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));       // natural size, never stretched
        searchRow.add(new JLabel("Find:")); searchRow.add(query); searchRow.add(category); searchRow.add(search);
        JPanel north = new JPanel(new BorderLayout()); north.add(title, BorderLayout.NORTH); north.add(searchRow, BorderLayout.CENTER);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));    // natural size, centred
        buttonRow.add(prev); buttonRow.add(next); buttonRow.add(reserve);
        JPanel south = new JPanel(new BorderLayout()); south.add(buttonRow, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(2, 8, 4, 8)); south.add(status, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH); add(new JScrollPane(results), BorderLayout.CENTER); add(south, BorderLayout.SOUTH);

        search.addActionListener(e -> doSearch());
        query.addActionListener(e -> doSearch());
        prev.addActionListener(e -> move(-1)); next.addActionListener(e -> move(1));
        reserve.addActionListener(e -> {
            String s = results.getSelectedValue();
            status.setText(s == null ? "Select a book first." : "Reserved: " + s.split("  -  ")[0]);
        });
    }
    void doSearch() {
        String q = query.getText().trim().toLowerCase(); int cat = category.getSelectedIndex(); model.clear();
        for (String[] b : CATALOGUE) {
            String hay = switch (cat) { case 1 -> b[0]; case 2 -> b[1]; case 3 -> b[2]; default -> String.join(" ", b); };
            if (q.isEmpty() || hay.toLowerCase().contains(q)) model.addElement(b[0] + "  -  " + b[1] + "  (" + b[2] + ")");
        }
        if (!model.isEmpty()) results.setSelectedIndex(0);
        status.setText(model.size() + " result(s) for \"" + query.getText().trim() + "\" in " + category.getSelectedItem());
    }
    void move(int d) { int i = results.getSelectedIndex() + d; if (i >= 0 && i < model.size()) results.setSelectedIndex(i); }
    JTextField queryField() { return query; } JButton searchButton() { return search; } JList<String> resultList() { return results; }
    JButton reserveButton() { return reserve; } JButton nextButton() { return next; }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> {
        JFrame f = new JFrame("Library Search"); f.setContentPane(new LibraryScreen()); f.setSize(640, 420);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setVisible(true); }); }
}
