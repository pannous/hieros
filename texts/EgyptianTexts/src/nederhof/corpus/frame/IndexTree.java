/***************************************************************************/
/*                                                                         */
/*  IndexTree.java                                                         */
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

// Tree for index.

package nederhof.corpus.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import nederhof.corpus.*;
import nederhof.corpus.TreeNode;

public abstract class IndexTree extends JTree 
		implements TreeSelectionListener {

    // Produce panel with tree.
    // The root is not printed.
    public IndexTree(TreeSet nodes) {
	super(getTree(nodes, "", ""));
	setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
	setCellRenderer(new IndexRenderer());
	putClientProperty("JTree.lineStyle", "None");
	TreeSelectionModel model = getSelectionModel();
	model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	setRootVisible(false);
	addTreeSelectionListener(this);
	addMouseListener(new ClickListener());
    }

    private static DefaultMutableTreeNode getTree(TreeSet tree, 
	    String label, String name) {
	NodeInfo nodeInfo = new NodeInfo(label, name, null);
	DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeInfo);
        Iterator it = tree.iterator();
        while (it.hasNext()) {
            TreeNode treeNode = (TreeNode) it.next();
            if (treeNode instanceof LeafNode) {
                LeafNode leaf = (LeafNode) treeNode;
		String leafLabel = 
		    (leaf.label.equals(leaf.name) ? "" : leaf.label + "; ") +
		    ("<font color=blue>" + leaf.name + "</font>") +
		    (leaf.post.matches("\\s*") ? "" : " [" + leaf.post + "]");
		NodeInfo leafInfo = 
		    new NodeInfo(leafLabel, leaf.name, leaf.text);
		DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(leafInfo);
		node.add(leafNode);
            } else {
                InternalNode internal = (InternalNode) treeNode;
                DefaultMutableTreeNode internalNode =
		    getTree(internal.children, internal.label, "");
		node.add(internalNode);
            }
        }
        return node;
    }

    // User part of nodes in tree.
    private static class NodeInfo {
	// The line in visible tree.
        public String label;
	// For reporting busy.
        public String name;
	// For processing click.
        public Text text;

        public NodeInfo(String label, String name, Text text) {
            this.label = label;
            this.name = name;
            this.text = text;
        }

        public String toString() {
            return "<html>" + label + "</html>";
        }
    }

    // How to depict tree.
    private static class IndexRenderer extends DefaultTreeCellRenderer {
        public IndexRenderer() {
        }
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
            return this;
        }
        public void setLeafIcon(Icon newIcon) {
            super.setLeafIcon(null);
        }
        public void setClosedIcon(Icon newIcon) {
            super.setClosedIcon(null);
        }
        public void setOpenIcon(Icon newIcon) {
            super.setOpenIcon(null);
        }
    }

    // If clicked, either expand if internal, or show text.
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
	    getLastSelectedPathComponent();
        TreePath path = getSelectionPath();
        if (node == null || path == null)
            return;
        if (!node.isLeaf()) {
            collapse();
            expandPath(path);
        } else {
	    // Do nothing. Is taken over by ClickListener.
	}
    }

    // Listen for right mouse clicks. Lead to editing.
    private class ClickListener extends MouseAdapter {
	public void mousePressed(MouseEvent e) {
	    int selRow = getRowForLocation(e.getX(), e.getY());
	    TreePath selPath = getPathForLocation(e.getX(), e.getY());
	    if (selRow != -1) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    selPath.getLastPathComponent();
		if (node != null && node.isLeaf()) {
		    NodeInfo info = (NodeInfo) node.getUserObject();
		    // For Mac, right click = control+click 
		    if (e.getButton() == MouseEvent.BUTTON1 && 
			    !e.isControlDown())
			viewSelect(info.text);
		    else if (e.getButton() == MouseEvent.BUTTON3 || 
			    e.isControlDown())
			editSelect(info.text);
		}
	    }
	    // Seems to be needed on Mac.
	    IndexTree.this.repaint();
	}
    }

    // Collapse expanded nodes again.
    private void collapse() {
        for (int row = 0; row < getRowCount(); row++) {
            if (isExpanded(row))
                collapseRow(row);
        }
    }

    /////////////////////////////////////////////////////////
    // Interface to caller. 

    // Select text for viewing.
    protected abstract void viewSelect(Text text);

    // Select text for editing.
    protected abstract void editSelect(Text text);

}
