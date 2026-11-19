import javax.swing.*;
import java.awt.*;

/** The same screen built with GridBagLayout (one container) and with absolute positioning (setLayout(null)). */
public class LayoutAlt {
    public static JPanel gridBag() {
        JPanel p = new JPanel(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints();
        JLabel title = new JLabel("Campus Library - Catalogue Search", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f)); title.setOpaque(true);
        title.setBackground(new Color(0x1F3864)); title.setForeground(Color.WHITE);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 5; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.ipady = 10; p.add(title, c);
        c.ipady = 0; c.gridwidth = 1; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.insets = new Insets(6, 6, 6, 6); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; p.add(new JLabel("Find:"), c); c.gridx = 1; p.add(new JTextField(18), c);
        c.gridx = 2; p.add(new JComboBox<>(new String[]{"All", "Title", "Author", "Subject"}), c); c.gridx = 3; p.add(new JButton("Search"), c);
        c.gridx = 4; c.weightx = 1; p.add(new JLabel(), c);                                   // filler takes spare width
        c.gridx = 0; c.gridy = 2; c.gridwidth = 5; c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        p.add(new JScrollPane(new JList<>(new String[]{"(results)"})), c);
        JPanel buttons = new JPanel(new FlowLayout()); buttons.add(new JButton("Prev")); buttons.add(new JButton("Next")); buttons.add(new JButton("Reserve"));
        c.gridy = 3; c.weighty = 0; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER; p.add(buttons, c);
        c.gridy = 4; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(2, 8, 4, 8); p.add(new JLabel("Enter a search term."), c);
        return p;
    }
    public static JPanel absolute() {
        JPanel p = new JPanel(null);
        JLabel title = new JLabel("Campus Library - Catalogue Search", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f)); title.setOpaque(true);
        title.setBackground(new Color(0x1F3864)); title.setForeground(Color.WHITE); title.setBounds(0, 0, 480, 38);
        JLabel l = new JLabel("Find:"); l.setBounds(8, 46, 40, 26); JTextField t = new JTextField(); t.setBounds(52, 46, 190, 26);
        JComboBox<String> cb = new JComboBox<>(new String[]{"All", "Title", "Author", "Subject"}); cb.setBounds(250, 46, 90, 26);
        JButton s = new JButton("Search"); s.setBounds(348, 46, 90, 26);
        JScrollPane sp = new JScrollPane(new JList<>(new String[]{"(results)"})); sp.setBounds(0, 80, 480, 180);
        JButton pr = new JButton("Prev"), nx = new JButton("Next"), rs = new JButton("Reserve"); pr.setBounds(120, 268, 80, 28); nx.setBounds(210, 268, 80, 28); rs.setBounds(300, 268, 90, 28);
        JLabel st = new JLabel("Enter a search term."); st.setBounds(8, 302, 300, 22);
        for (JComponent c : new JComponent[]{title, l, t, cb, s, sp, pr, nx, rs, st}) p.add(c);
        p.setPreferredSize(new Dimension(480, 330));
        return p;
    }
}
