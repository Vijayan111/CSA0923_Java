import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Headless renderer: paints a Swing component tree to a PNG and draws a window title bar around it. */
public class Render {
    static { try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception e) { } }
    public static void wait(int ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { } }
    public static void sync() { try { SwingUtilities.invokeAndWait(() -> { }); } catch (Exception e) { } }
    public static void layoutAll(Component c) {
        if (c instanceof Container k) { k.doLayout(); for (Component x : k.getComponents()) layoutAll(x); }
    }
    public static void layoutTimes(Component c) { layoutAll(c); layoutAll(c); layoutAll(c); }
    public static BufferedImage body(Component c, int w, int h) {
        c.setSize(w, h); layoutTimes(c);
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(c.getBackground() == null ? Color.WHITE : c.getBackground()); g.fillRect(0, 0, w, h);
        c.printAll(g); g.dispose(); return im;
    }
    /** Window look: title bar + optional menu bar + content. */
    public static void window(Component content, JMenuBar menu, String title, int w, int h, String file) throws Exception {
        BufferedImage body = body(content, w, h);
        int bar = 28, mh = 0; BufferedImage mi = null;
        if (menu != null) { mh = 26; mi = body(menu, w, mh); }
        BufferedImage out = new BufferedImage(w + 2, h + bar + mh + 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x808080)); g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.setPaint(new GradientPaint(0, 0, new Color(0xE9EEF7), 0, bar, new Color(0xC9D4E8))); g.fillRect(1, 0, w, bar);
        g.setColor(new Color(0x222222)); g.setFont(new Font("SansSerif", Font.BOLD, 12)); g.drawString(title, 10, 18);
        g.setStroke(new BasicStroke(1.5f)); int x = w - 78;
        g.drawLine(x, 14, x + 9, 14); g.drawRect(x + 26, 9, 9, 9); g.drawLine(x + 52, 9, x + 61, 18); g.drawLine(x + 61, 9, x + 52, 18);
        g.drawImage(body, 1, bar + mh, null);
        if (mi != null) g.drawImage(mi, 1, bar, null);
        g.dispose();
        ImageIO.write(out, "png", new File(file));
    }
    public static void window(Component content, String title, int w, int h, String file) throws Exception { window(content, null, title, w, h, file); }
    /** Dialog look for a JOptionPane rendered as a component. */
    public static void dialog(Component content, String title, String file) throws Exception {
        Dimension d = content.getPreferredSize(); window(content, null, title, d.width + 8, d.height + 8, file);
    }
    public static JButton findButton(Container c, String text) {
        for (Component x : c.getComponents()) {
            if (x instanceof JButton b && text.equals(b.getText())) return b;
            if (x instanceof Container k) { JButton r = findButton(k, text); if (r != null) return r; }
        }
        return null;
    }
    public static <T extends Component> T find(Container c, Class<T> type, int index) {
        int[] n = {0}; return find(c, type, index, n);
    }
    private static <T extends Component> T find(Container c, Class<T> type, int index, int[] n) {
        for (Component x : c.getComponents()) {
            if (type.isInstance(x)) { if (n[0]++ == index) return type.cast(x); }
            if (x instanceof Container k) { T r = find(k, type, index, n); if (r != null) return r; }
        }
        return null;
    }
}
