/***************************************************************************/
/*                                                                         */
/*  RootPanel.java                                                         */
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

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;

class RootPanel extends JPanel {

    // Root node.
    private TreeFragment root;

    // Constructor.
    public RootPanel(TreeFragment root) {
	this.root = root;
	setBackground(Color.WHITE);
    }

    // Scroll tree to rectangle. Try to put central in screen, but not too
    // extreme, allowing half of remaining screen width as buffer,
    // hence 1/4 on each side of the rectangle.
    public void scrollTree(Rectangle rect) {
        Rectangle wider = new Rectangle(rect);
	int visibleWidth = getVisibleRect().width;
        int widening = (visibleWidth - rect.width) / 4;
        wider.x = Math.max(0, wider.x - widening);
        wider.width += widening * 2;
        wider.y = Math.max(0, wider.y - widening);
        wider.height += widening * 2;
        scrollRectToVisible(wider);
    }

    // Make fresh.
    public void refresh() {
        removeAll();
	Vector treeChildren = root.children();
	for (int i = 0; i < treeChildren.size(); i++) {
	    TreeNode child = (TreeNode) treeChildren.get(i);
	    add(child.panel());
	}
        validate();
        repaint();
	if (getParent() != null)
	    getParent().validate();
    }

}
