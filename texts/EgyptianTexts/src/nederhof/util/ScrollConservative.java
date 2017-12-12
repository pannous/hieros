/***************************************************************************/
/*                                                                         */
/*  ScrollConservative.java                                                */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Component in Scroll pane that does not gobble up available 
// horizontal size.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ScrollConservative extends JScrollPane {

    // Make panel around argument that restricts its width to viewport.
    public ScrollConservative(Component comp) {
	JPanel outer = new RestictedPanel();
	outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
	outer.setBackground(Color.WHITE);
	outer.add(comp);
	setViewportView(outer);
	getVerticalScrollBar().setUnitIncrement(10);
    }

    // Restricts width to that of viewport.
    private class RestictedPanel extends JPanel {
	public RestictedPanel() {
	}
	public Dimension getPreferredSize() {
	    return new Dimension(getViewport().getExtentSize().width, 
		    super.getPreferredSize().height);
	}
    }

}
