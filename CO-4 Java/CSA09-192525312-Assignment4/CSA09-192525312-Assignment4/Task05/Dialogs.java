import javax.swing.*;
import java.awt.Component;
/** Headless stand-in for JOptionPane.showMessageDialog (JDialog cannot be created without a display). */
public class Dialogs {
    public static String last;
    public static void show(Component parent, Object message) { last = String.valueOf(message); }
}
