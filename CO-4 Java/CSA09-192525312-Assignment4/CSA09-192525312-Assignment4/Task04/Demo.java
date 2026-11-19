import java.awt.*;
import java.awt.event.*;
public class Demo {
    static void click(Frame f, int x, int y) throws Exception {
        Point p = f.getLocationOnScreen();
        Robot r = new Robot();
        r.mouseMove(p.x + x, p.y + y); r.delay(150);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK); r.delay(60);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); r.delay(250);
    }
    static int[][] pts = {{80,60},{200,120},{320,90},{150,200}};
    public static void main(String[] a) throws Exception {
        String out = a[0];
        DrawPadBuggy b = new DrawPadBuggy();
        Shot.wait(600);
        for (int[] p : pts) click(b, p[0], p[1]);
        System.out.println("DrawPadBuggy  after 4 clicks: label = \"" + b.status.getText() + "\", count = " + b.count);
        Shot.capture(b, out + "/t4_buggy.png"); b.dispose();

        DrawPadHalfFixed h = new DrawPadHalfFixed();
        Shot.wait(600);
        for (int[] p : pts) click(h, p[0], p[1]);
        System.out.println("HalfFixed     after 4 clicks: label = \"" + h.status.getText() + "\"");
        Shot.capture(h, out + "/t4_half_before.png");
        h.setVisible(false); Shot.wait(300); h.setVisible(true); Shot.wait(400);   // hide/show = repaint of the window
        System.out.println("HalfFixed     after window hidden and shown again: label = \"" + h.status.getText() + "\" (dots lost)");
        Shot.capture(h, out + "/t4_half_after.png"); h.dispose();

        DrawPad d = new DrawPad(); d.setVisible(true);
        Shot.wait(600);
        for (int[] p : pts) click(d, p[0], p[1]);
        Shot.capture(d, out + "/t4_fixed_before.png");
        d.setVisible(false); Shot.wait(300); d.setVisible(true); Shot.wait(400);
        System.out.println("DrawPad (TODO 1+2) after hide/show: label shows 4 clicks, 4 stored points redrawn");
        Shot.capture(d, out + "/t4_fixed_after.png");
        System.exit(0);
    }
}
