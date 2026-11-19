import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.List;

/** Alternative concept: redraw n stored points in paint() vs copy one off-screen BufferedImage. */
public class RepaintBench {
    static long alloc() { return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().getId()); }
    public static void main(String[] a) {
        int W = 400, H = 300; Random rnd = new Random(1);
        System.out.println("   n clicks | redraw n points (ms/repaint) | copy 1 image (ms/repaint) | point list (KB) | image (KB)");
        for (int n : new int[]{100, 10_000, 100_000, 1_000_000}) {
            long b0 = alloc(); List<Point> pts = new ArrayList<>(); for (int i = 0; i < n; i++) pts.add(new Point(rnd.nextInt(W), rnd.nextInt(H)));
            long listBytes = alloc() - b0;
            BufferedImage screen = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB), buffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = buffer.createGraphics(); bg.setColor(Color.BLACK); for (Point p : pts) bg.fillOval(p.x - 3, p.y - 3, 6, 6); bg.dispose();   // one-off cost
            int reps = Math.max(3, 2_000_000 / n); double bestA = 1e18, bestB = 1e18;
            for (int run = 0; run < 5; run++) {
                Graphics2D g = screen.createGraphics(); g.setColor(Color.BLACK);
                long t = System.nanoTime(); for (int r = 0; r < reps; r++) { for (Point p : pts) g.fillOval(p.x - 3, p.y - 3, 6, 6); } bestA = Math.min(bestA, (System.nanoTime() - t) / 1e6 / reps);
                t = System.nanoTime(); for (int r = 0; r < Math.max(reps, 200); r++) g.drawImage(buffer, 0, 0, null); bestB = Math.min(bestB, (System.nanoTime() - t) / 1e6 / Math.max(reps, 200));
                g.dispose();
            }
            System.out.printf("%11d | %28.3f | %25.3f | %15.0f | %10.0f%n", n, bestA, bestB, listBytes / 1024.0, W * H * 4 / 1024.0);
        }
    }
}
