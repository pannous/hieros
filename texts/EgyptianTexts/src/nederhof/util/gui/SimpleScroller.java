package nederhof.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Simple scroll pane. Not focusable.
public class SimpleScroller extends JScrollPane {

    // vert = scrollbar vertically? 
    // hor = scrollbar vertically? 
    public SimpleScroller(JComponent pane, boolean vert, boolean hor) {
	super(pane,
		vert ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS :
			JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		hor ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS :
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	setFocusable(false);
	getVerticalScrollBar().setUnitIncrement(10);
	getHorizontalScrollBar().setUnitIncrement(10);
    }

}
