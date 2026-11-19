import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class ConverterC extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");
    private static double rate(String c) { return c.equals("USD") ? 0.012 : 0.011; }

    public ConverterC() {
        super("Currency Converter - (c) anonymous inner class");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        ActionListener h = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try { double v = Double.parseDouble(amount.getText().trim()); String c = (String) target.getSelectedItem();
                      JOptionPane.showMessageDialog(ConverterC.this, String.format("%.2f %s", v * rate(c), c)); }
                catch (NumberFormatException ex) { JOptionPane.showMessageDialog(ConverterC.this, "Invalid amount"); }
            }
        };
        convertButton.addActionListener(h);
        amount.addActionListener(h);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }

    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterC().setVisible(true)); }
}
