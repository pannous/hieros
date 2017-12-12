package nederhof.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Panel that can be used inside scrollpane.
public class ScrollablePanel extends JPanel implements Scrollable {
    // For use with vertical scrollbar.
    private boolean vert;
    // For use with horizontal scrollbar.
    private boolean hor;

    // Constructor.
    public ScrollablePanel(boolean vert, boolean hor) {
	this.vert = vert;
	this.hor = hor;
    }

    public Dimension getPreferredScrollableViewportSize() {
	return super.getPreferredSize(); 
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	return 10;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	return 10;
    }

    public boolean getScrollableTracksViewportWidth() {
	return vert;
    }

    public boolean getScrollableTracksViewportHeight() {
	return hor; 
    }
}
