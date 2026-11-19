import javax.swing.*;
import java.awt.*;

/** Off-screen demo driver: drives the real FeeForm (buttons are clicked programmatically) and saves PNGs. */
public class FeeDemo {
    static void shot(FeeForm f, String file) throws Exception {
        Dimension d = f.getPreferredSize(); Render.window(f, "Student Fee Payment", d.width + 40, d.height, file);
    }
    public static void main(String[] a) throws Exception {
        String out = a[0];
        FeeForm f = new FeeForm(new FeeService(new InMemoryFeeRepository(800)));
        f.setSize(f.getPreferredSize());
        JButton pay = Render.findButton(f, "Pay"), receipt = Render.findButton(f, "Receipt"), clear = Render.findButton(f, "Clear");
        SwingUtilities.invokeAndWait(() -> pay.doClick());                            // both fields empty
        shot(f, out + "/t1_empty.png");
        SwingUtilities.invokeAndWait(() -> { f.regNoField().setText("192525312"); f.amountField().setText("abc"); pay.doClick(); });
        shot(f, out + "/t1_badamount.png");
        SwingUtilities.invokeAndWait(() -> { f.amountField().setText("45000"); pay.doClick(); });
        Render.wait(200); Render.sync(); shot(f, out + "/t1_waiting.png");           // bank wait: button disabled, message shown
        Render.wait(1200); Render.sync();
        SwingUtilities.invokeAndWait(() -> receipt.doClick());
        shot(f, out + "/t1_paid.png");
        System.exit(0);
    }
}
