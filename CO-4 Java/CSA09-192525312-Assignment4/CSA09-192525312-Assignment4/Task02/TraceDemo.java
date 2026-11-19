import java.awt.*;
import java.awt.event.*;

/** Prints the order of window-event calls (Task 2.4). Run it on a machine with a display: java TraceDemo */
public class TraceDemo extends Frame implements WindowListener {
    static void log(String s) { System.out.println("[" + Thread.currentThread().getName() + "] " + s); }
    public TraceDemo() { super("Trace"); log("constructor"); addWindowListener(this); setSize(300, 120); }
    public void windowOpened(WindowEvent e)      { log("windowOpened"); }
    public void windowActivated(WindowEvent e)   { log("windowActivated"); }
    public void windowDeactivated(WindowEvent e) { log("windowDeactivated"); }
    public void windowIconified(WindowEvent e)   { log("windowIconified"); }
    public void windowDeiconified(WindowEvent e) { log("windowDeiconified"); }
    public void windowClosing(WindowEvent e)     { log("windowClosing"); dispose(); }
    public void windowClosed(WindowEvent e)      { log("windowClosed"); }
    @Override public void paint(Graphics g)      { log("paint"); }
    public static void main(String[] a) { log("main"); EventQueue.invokeLater(() -> { log("creating the window on the EDT"); new TraceDemo().setVisible(true); }); }
}
