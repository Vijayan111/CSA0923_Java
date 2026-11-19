import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Off-screen harness for Task 4 (the sandbox has no window toolkit, so java.awt.Frame cannot be opened).
 * The three pads below repeat the listener / paint logic of DrawPadBuggy, DrawPadHalfFixed and DrawPad
 * on a JPanel; "screen" stands for what the window system shows, and a minimise/restore makes the
 * system clear the surface and call paint() again.
 */
public class Harness {
    static abstract class Pad extends JPanel {
        final JLabel status = new JLabel("Clicks: 0");
        BufferedImage screen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Pad() { setLayout(new BorderLayout()); add(status, BorderLayout.SOUTH); setBackground(Color.WHITE); setSize(400, 300); clearScreen(); }
        void clearScreen() { Graphics2D g = screen.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, 400, 300); g.dispose(); }
        @Override public Graphics getGraphics() { return screen.getGraphics(); }   // what getGraphics() paints on = the screen
        void clickAt(int x, int y) {
            for (MouseListener l : getMouseListeners())
                l.mouseClicked(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, 0, 0, x, y, 1, false));
        }
        /** The window system's repaint after minimise/restore: surface cleared, then paint() runs. */
        void systemRepaint() { clearScreen(); Graphics2D g = screen.createGraphics(); paint(g); g.dispose(); }
        BufferedImage shotImage() { Graphics2D g = screen.createGraphics(); Render.layoutAll(this); status.setBounds(0, 278, 400, 22);
            g.translate(0, 0); status.paint(g.create(0, 278, 400, 22)); g.dispose(); return screen; }
    }
    static class Buggy extends Pad {
        int count = 0;
        Buggy() { addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) { count++; Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6); } }); }
        @Override public void paint(Graphics g) { }               // nothing stored, nothing to redraw
    }
    static class HalfFixed extends Pad {
        int count = 0;
        HalfFixed() { addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { count++; status.setText("Clicks: " + count);
                Graphics g = getGraphics(); g.setColor(Color.BLACK); g.fillOval(e.getX(), e.getY(), 6, 6); } }); }
        @Override public void paint(Graphics g) { }
    }
    static class Fixed extends Pad {
        final List<Point> points = new ArrayList<>();
        Fixed() { addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { points.add(e.getPoint()); status.setText("Clicks: " + points.size()); repaintNow(); } }); }
        void repaintNow() { Graphics2D g = screen.createGraphics(); paint(g); g.dispose(); }
        @Override public void paint(Graphics g) { g.setColor(Color.BLACK); for (Point p : points) g.fillOval(p.x - 3, p.y - 3, 6, 6); }
    }
    static final int[][] PTS = {{80, 60}, {200, 120}, {320, 90}, {150, 200}};
    static void save(Pad p, String title, String file) throws Exception {
        BufferedImage im = p.shotImage(); JPanel holder = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) { g.drawImage(im, 0, 0, null); } };
        Render.window(holder, title, 400, 300, file);
    }
    public static void main(String[] a) throws Exception {
        String out = a[0];
        Buggy b = new Buggy(); for (int[] p : PTS) b.clickAt(p[0], p[1]);
        System.out.println("DrawPadBuggy   after 4 clicks : count = " + b.count + ", label = \"" + b.status.getText() + "\"  (mouseClicked never ran)");
        save(b, "DrawPadBuggy (original)", out + "/t4_buggy.png");

        HalfFixed h = new HalfFixed(); for (int[] p : PTS) h.clickAt(p[0], p[1]);
        System.out.println("HalfFixed      after 4 clicks : count = " + h.count + ", label = \"" + h.status.getText() + "\"");
        save(h, "DrawPadHalfFixed - before minimise", out + "/t4_half_before.png");
        h.systemRepaint();
        System.out.println("HalfFixed      after minimise + restore: count = " + h.count + ", dots visible = 0");
        save(h, "DrawPadHalfFixed - after restore", out + "/t4_half_after.png");

        Fixed f = new Fixed(); for (int[] p : PTS) f.clickAt(p[0], p[1]);
        System.out.println("DrawPad (TODO) after 4 clicks : points stored = " + f.points.size() + ", label = \"" + f.status.getText() + "\"");
        save(f, "DrawPad - before minimise", out + "/t4_fixed_before.png");
        f.systemRepaint();
        System.out.println("DrawPad (TODO) after minimise + restore: points redrawn = " + f.points.size());
        save(f, "DrawPad - after restore", out + "/t4_fixed_after.png");
    }
}
