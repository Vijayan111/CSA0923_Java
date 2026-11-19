import java.awt.*;
import java.awt.event.*;
// Original defective program (Task 4) - only imports added so that it compiles.
public class DrawPadBuggy extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadBuggy() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) {          // DEFECT: lower-case c
                count++;
                Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6);
            }
        });
        setSize(400, 300); setVisible(true);
    }
}
