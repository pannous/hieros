package nederhof.util.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class DashedBorder implements Border {
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	Graphics2D gg = (Graphics2D) g;
	gg.setColor(Color.GRAY);
	gg.setStroke(new BasicStroke(3,
		    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0));
	// gg.drawRect(x, y, w - 1, h - 1);
	gg.drawRect(x+1, y+1, w - 3, h - 3);
    }

    public Insets getBorderInsets(Component c) {
	return new Insets(3, 3, 3, 3) ;
    }

    public boolean isBorderOpaque() {
	return true ;
    }
}

