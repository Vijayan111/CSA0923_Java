import java.awt.event.*;
public class DrawPadOverride {
    static MouseAdapter a = new MouseAdapter() {
        @Override public void mouseclicked(MouseEvent e) { }   // misspelled on purpose
    };
}
