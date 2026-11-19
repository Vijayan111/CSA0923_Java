import javax.swing.*;
import java.awt.*;

/** Off-screen driver. usage: Harness <className> <label|dialog> <outPrefix or -> */
public class Harness {
    static JPanel panel; static JTextField amount; static JButton button; static JLabel result;
    static String act(String how, String text) throws Exception {
        Dialogs.last = null;
        SwingUtilities.invokeAndWait(() -> { amount.setText(text); if (how.equals("button")) button.doClick(); else amount.postActionEvent(); });
        return Dialogs.last != null ? "dialog: \"" + Dialogs.last + "\"" : "label : \"" + result.getText() + "\"";
    }
    public static void main(String[] a) throws Exception {
        String cls = a[0], mode = a[1], prefix = a[2];
        panel = (JPanel) Class.forName(cls).getDeclaredConstructor().newInstance();
        Dimension d = panel.getPreferredSize(); panel.setSize(d); Render.layoutTimes(panel);
        amount = Render.find(panel, JTextField.class, 0); button = Render.findButton(panel, "Convert"); result = Render.find(panel, JLabel.class, 1);
        System.out.println(cls + " (" + mode + ")");
        System.out.println("  5000 + button      -> " + act("button", "5000"));
        if (!prefix.equals("-")) { Render.window(panel, "Currency Converter", d.width + 30, d.height + 4, prefix + "_valid.png"); }
        if (mode.equals("dialog")) Render.dialog(new JOptionPane(Dialogs.last, JOptionPane.INFORMATION_MESSAGE), "Message", prefix + "_dialog.png");
        System.out.println("  abc  + button      -> " + act("button", "abc"));
        if (!prefix.equals("-")) { Render.window(panel, "Currency Converter", d.width + 30, d.height + 4, prefix + "_invalid.png"); }
        String before = result.getText();
        System.out.println("  2500 + Enter key   -> " + act("enter", "2500"));
        System.exit(0);
    }
}
