/***************************************************************************/
/*                                                                         */
/*  NodePanel.java                                                         */
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

// Panel depicting node in tree.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;

class NodePanel extends JPanel {

    // Lines between panels can be written over this background color.
    private static final Color TRANSPARANT = Color.WHITE;
    // Lines between panels.
    private static final Color LINE_COLOR = Color.GRAY;

    // Padding around group.
    private static int HOR_GROUP_PADDING = 5;
    private static int VERT_GROUP_PADDING = 10;
    // Padding inside header.
    private static int INNER_PADDING = 2;

    // The entire header part.
    private JPanel header = new JPanel();
    // Comprising the small margin around the inner header,
    // which is colored if focused.
    private JPanel outerHeader = new JPanel() {
	// Not bigger than necessary.
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    };
    // The header of the panel. This is the main identifying part.
    private JPanel innerHeader = new JPanel();
    // The hieroglyphic view of a subexpression, if any.
    private HieroglyphicPanel hieroView = new HieroglyphicPanel() {
	    public HieroRenderContext context() {
		return node.root().getContext();
	    }
	    public String hiero() {
		return node.resString();
	    }
	};
    // The panel containing subgroups.
    private JPanel body = new JPanel();

    // Node corresponding to panel.
    private TreeNode node;

    // Constructor.
    public NodePanel(TreeNode node) {
	this.node = node;
        setBackground(TRANSPARANT);
        setOpaque(false);

        header.setBackground(TRANSPARANT);
        header.setOpaque(false);
        outerHeader.setBackground(TRANSPARANT);
        outerHeader.setOpaque(false);
        outerHeader.addMouseListener(new ClickListener());
        innerHeader.setBackground(TRANSPARANT);
        innerHeader.setOpaque(false);
        body.setBackground(TRANSPARANT);
        body.setOpaque(false);

        add(header);
        header.add(outerHeader);
        outerHeader.add(innerHeader);
	innerHeader.add(hieroView);
        add(body);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setAlignmentX(CENTER_ALIGNMENT);
        innerHeader.setLayout(new BoxLayout(innerHeader, BoxLayout.Y_AXIS));
        body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
        body.setAlignmentX(CENTER_ALIGNMENT);

        setBorder(new EmptyBorder(VERT_GROUP_PADDING, HOR_GROUP_PADDING,
                    0, HOR_GROUP_PADDING));
        setBorderNoFocus();
        innerHeader.setBorder(new EmptyBorder(INNER_PADDING, INNER_PADDING,
                    INNER_PADDING, INNER_PADDING));
    }

    // Constructor with name.
    public NodePanel(String name, TreeNode node) {
	this(node);
        JLabel label = new JLabel(name);
        label.setAlignmentX(CENTER_ALIGNMENT);
        innerHeader.add(label, 0);
    }

    // Constructor meant for subclass (RootPanel).
    protected NodePanel() {
	// most of the above is not used
    }

    // Entire group is no bigger than necessary.
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // Listens only to mouse clicks. Click leads to change of focus.
    private class ClickListener extends MouseInputAdapter {
        public void mouseClicked(MouseEvent event) {
	    node.root().setFocus(node);
        }
    }

    // Thickness of border around header, with and without focus.
    private static final int BORDER_THICKNESS = 3;
    private static final int FOCUS_BORDER_THICKNESS = 5;
    // Make border show that focus is here.
    public void setBorderFocus() {
        outerHeader.setBorder(new LineBorder(Color.BLUE, FOCUS_BORDER_THICKNESS, true));
        innerHeader.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollToView();
    }
    // Make border show that focus is not here.
    public void setBorderNoFocus() {
        outerHeader.setBorder(new LineBorder(Color.GRAY, BORDER_THICKNESS));
        innerHeader.setBorder(new EmptyBorder(INNER_PADDING, INNER_PADDING,
                    INNER_PADDING, INNER_PADDING));
    }

    // Scroll into view, although it may already have focus.
    public void scrollToView() {
        node.root().rootPanel().scrollTree(thisRectangle());
    }

    // Get header rectangle, relative to top panel.
    public Rectangle thisRectangle() {
        Rectangle headRect = header.getBounds();
	Rectangle thisRect = getBounds();
	Point parentBodyPoint = parentPanel() == null ? new Point(0, 0) :
		parentPanel().bodyPoint();
	return new Rectangle(
		headRect.x + thisRect.x + parentBodyPoint.x,
		headRect.y + thisRect.y + parentBodyPoint.y,
		headRect.width,
		headRect.height);
    }

    // Get corner point of body of panel, relative to top panel.
    // For RootPanel, The body and the panel itself are the same.
    public Point bodyPoint() {
	Rectangle bodyRect = body.getBounds();
	Rectangle thisRect = getBounds();
	Point parentBodyPoint = parentPanel() == null ? new Point(0, 0) :
		parentPanel().bodyPoint();
	return new Point(
		bodyRect.x + thisRect.x + parentBodyPoint.x,
		bodyRect.y + thisRect.y + parentBodyPoint.y);
    }

    // Where are children found relative to this panel?
    // Overridden for RootPanel.
    public Point childPoint() {
	Rectangle bodyRect = parentPanel().body.getBounds();
	return new Point(bodyRect.x, bodyRect.y);
    }

    // Visual parent and thereof panel. If none, then null; this is
    // case for children of root.
    public NodePanel parentPanel() {
	Component parent = this.getParent();
	while (parent != null && !(parent instanceof NodePanel))
	    parent = parent.getParent();
	return (NodePanel) parent;
    }

    // Position of outer header relative to total panel.
    private int outerHeaderX() {
        return header.getX() + outerHeader.getX();
    }
    private int outerHeaderY() {
        return header.getY() + outerHeader.getY();
    }

    // Lines between nodes are attached where?
    public Point topPoint() {
        return new Point(outerHeaderX() + outerHeader.getWidth() / 2,
                outerHeaderY() - 1);
    }
    public Point bottomPoint() {
        return new Point(outerHeaderX() + outerHeader.getWidth() / 2,
                outerHeaderY() + outerHeader.getHeight() - 1);
    }

    // Connect to children downward.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(2));
        g2.setColor(LINE_COLOR);
        Point from = bottomPoint();
	Component[] children = body.getComponents();
        for (int i = 0; i < children.length; i++) 
	    if (children[i] instanceof NodePanel) {
		NodePanel toPanel = (NodePanel) children[i];
		Point to = toPanel.topPoint();
		g2.drawLine(from.x, from.y,
			body.getX() + toPanel.getX() + to.x,
			body.getY() + VERT_GROUP_PADDING);
		g2.drawLine(body.getX() + toPanel.getX() + to.x,
			body.getY() + VERT_GROUP_PADDING,
			body.getX() + toPanel.getX() + to.x,
			body.getY() + toPanel.getY() + to.y);
	    }
    }

    // Get RES anew from node.
    public void refresh() {
	body.removeAll();
	Vector treeChildren = node.children();
	for (int i = 0; i < treeChildren.size(); i++) {
	    TreeNode child = (TreeNode) treeChildren.get(i);
	    body.add(child.panel());
	}
	hieroView.refresh();
	hieroView.invalidate();
	validate();
	repaint();
    }

}
