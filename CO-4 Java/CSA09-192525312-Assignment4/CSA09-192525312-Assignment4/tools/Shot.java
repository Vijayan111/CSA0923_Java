import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
public class Shot {
    public static void wait(int ms) { try { Thread.sleep(ms); } catch (InterruptedException e) {} }
    public static void capture(Window w, String file) throws Exception {
        wait(500);
        Rectangle r = new Rectangle(w.getLocationOnScreen(), w.getSize());
        BufferedImage img = new Robot().createScreenCapture(r);
        ImageIO.write(img, "png", new File(file));
    }
}
