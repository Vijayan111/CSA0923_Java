import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class ConverterB extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");
    private static double rate(String c) { return c.equals("USD") ? 0.012 : 0.011; }

    public ConverterB() {
        super("Currency Converter - (b) named inner class");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        convertButton.addActionListener(new ConvertHandler());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    private class ConvertHandler implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            try { double v = Double.parseDouble(amount.getText().trim()); String c = (String) target.getSelectedItem();
                  result.setText(String.format("%.2f %s", v * rate(c), c)); }
            catch (NumberFormatException ex) { result.setText("Invalid amount"); }
        }
    }

    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterB().setVisible(true)); }
}
