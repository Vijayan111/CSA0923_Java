import java.awt.*;
import java.awt.event.*;
// Defect 1 fixed (mouseClicked + label) but dots still drawn with getGraphics().
public class DrawPadHalfFixed extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadHalfFixed() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                count++; status.setText("Clicks: " + count);
                Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6);  // not repaint-safe
            }
        });
        setSize(400, 300); setVisible(true);
    }
}
