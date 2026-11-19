import os, textwrap
def indent(s, n): return "\n".join((" "*n + l) if l.strip() else l for l in s.split("\n"))
def logic(v, outer):
    ok  = 'result.setText(String.format("%.2f %s", v * rate(c), c));' if v == 1 else 'JOptionPane.showMessageDialog(%s, String.format("%%.2f %%s", v * rate(c), c));' % outer
    bad = 'result.setText("Invalid amount");' if v == 1 else 'JOptionPane.showMessageDialog(%s, "Invalid amount");' % outer
    return ("try { double v = Double.parseDouble(amount.getText().trim()); String c = (String) target.getSelectedItem();\n"
            "      " + ok + " }\n" "catch (NumberFormatException ex) { " + bad + " }")
HEAD = '''import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class %(cls)s extends JFrame%(impl)s {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");
    private static double rate(String c) { return c.equals("USD") ? 0.012 : 0.011; }

    public %(cls)s() {
        super("Currency Converter - %(style)s");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
%(reg)s
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
%(body)s
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new %(cls)s().setVisible(true)); }
}
'''
def make(style, v):
    cls = "Converter" + style
    if style == "A":
        impl = " implements ActionListener"; label = "(a) frame implements ActionListener"
        reg = "        convertButton.addActionListener(this);" + ("" if v == 1 else "\n        amount.addActionListener(this);")
        body = "    @Override public void actionPerformed(ActionEvent e) {\n" + indent(logic(v, "this"), 8) + "\n    }\n"
    elif style == "B":
        impl = ""; label = "(b) named inner class"
        reg = ("        convertButton.addActionListener(new ConvertHandler());" if v == 1 else
               "        ConvertHandler h = new ConvertHandler();\n        convertButton.addActionListener(h);\n        amount.addActionListener(h);")
        body = ("    private class ConvertHandler implements ActionListener {\n        @Override public void actionPerformed(ActionEvent e) {\n"
                + indent(logic(v, "ConverterB.this"), 12) + "\n        }\n    }\n")
    elif style == "C":
        impl = ""; label = "(c) anonymous inner class"
        inner = ("new ActionListener() {\n    @Override public void actionPerformed(ActionEvent e) {\n" + indent(logic(v, "ConverterC.this"), 8) + "\n    }\n}")
        if v == 1: reg = indent("convertButton.addActionListener(" + inner + ");", 8)
        else: reg = indent("ActionListener h = " + inner + ";\nconvertButton.addActionListener(h);\namount.addActionListener(h);", 8)
        body = ""
    else:
        impl = ""; label = "(d) lambda expression"
        reg = ("        convertButton.addActionListener(e -> convert());" if v == 1 else
               "        ActionListener h = e -> convert();\n        convertButton.addActionListener(h);\n        amount.addActionListener(h);")
        body = "    private void convert() {\n" + indent(logic(v, "this"), 8) + "\n    }\n"
    src = HEAD % dict(cls=cls, impl=impl, style=label, reg=reg, body=body)
    open("v%d/%s.java" % (v, cls), "w").write(src)
for s in "ABCD":
    for v in (1, 2): make(s, v)
