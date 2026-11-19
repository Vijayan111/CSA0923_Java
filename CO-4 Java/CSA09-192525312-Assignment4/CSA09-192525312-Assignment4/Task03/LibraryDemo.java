import javax.swing.*;
import java.awt.*;

public class LibraryDemo {
    static int containers(Container c) { int n = 0; for (Component x : c.getComponents()) if (x instanceof JPanel) n += 1 + containers((Container) x); else if (x instanceof Container k && !(x instanceof JScrollPane) && !(x instanceof AbstractButton) && !(x instanceof JComboBox) && !(x instanceof JTextField) && !(x instanceof JLabel)) n += containers(k); return n; }
    static String r(Component c) { Rectangle b = c.getBounds(); return b.width + "x" + b.height; }
    public static void main(String[] a) throws Exception {
        String out = a[0];
        int[][] sizes = {{480, 330}, {720, 440}, {980, 580}};
        LibraryScreen s = new LibraryScreen();
        s.queryField().setText("java"); SwingUtilities.invokeAndWait(() -> s.searchButton().doClick());
        System.out.println("Nested BorderLayout/FlowLayout design: region sizes (width x height) at three window sizes");
        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s%n", "window", "Search button", "results area", "Reserve button", "title bar");
        for (int i = 0; i < 3; i++) {
            Render.window(s, "Library Search - size " + (i + 1), sizes[i][0], sizes[i][1], out + "/t3_size" + (i + 1) + ".png");
            Component north = s.getComponent(0);
            Component title = ((Container) north).getComponent(0);
            System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s%n", sizes[i][0] + "x" + sizes[i][1], r(s.searchButton()), r(((JScrollPane) s.getComponent(1))), r(s.reserveButton()), r(title));
        }
        // alternatives
        JPanel gb = LayoutAlt.gridBag(), ab = LayoutAlt.absolute();
        Render.window(gb, "GridBagLayout version - 980x580", 980, 580, out + "/t3_gridbag_large.png");
        Render.window(ab, "Absolute (null) layout - 980x580", 980, 580, out + "/t3_null_large.png");
        // timing: resize between two sizes and lay out the tree, 3 designs
        JPanel[] designs = {s, gb, ab}; String[] names = {"Nested Border/Flow", "GridBagLayout", "Absolute (null)"};
        System.out.println("\nLayout cost: 20,000 resize+layout passes alternating 480x330 and 980x580 (best of 5 runs)");
        System.out.printf("%-20s | %-13s | %-18s%n", "design", "panels (JPanel)", "layout time (us/pass)");
        for (int d = 0; d < 3; d++) {
            double best = 1e18;
            for (int rep = 0; rep < 5; rep++) {
                long t = System.nanoTime();
                for (int i = 0; i < 20000; i++) { designs[d].setSize(i % 2 == 0 ? 480 : 980, i % 2 == 0 ? 330 : 580); Render.layoutAll(designs[d]); }
                best = Math.min(best, (System.nanoTime() - t) / 20000.0 / 1000.0);
            }
            System.out.printf("%-20s | %-13d | %.2f%n", names[d], 1 + containers(designs[d]), best);
        }
        System.exit(0);
    }
}
