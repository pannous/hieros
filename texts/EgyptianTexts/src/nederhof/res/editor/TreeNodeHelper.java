/***************************************************************************/
/*                                                                         */
/*  TreeNodeHelper.java                                                    */
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

import java.util.*;

public class TreeNodeHelper {

    // Generic traversal of tree, with inorder node numbering.
    private static abstract class InorderTraverser {
	// Traverse inorder from root.
	public void traverse(TreeNode node) {
	    traverse(node, 0);
	}
	// Traverse inorder, return how many nodes seen so far.
	public int traverse(TreeNode node, int i) {
	    apply(node, i++);
	    Vector children = node.allChildren();
	    for (int j = 0; j < children.size(); j++) {
		TreeNode child = (TreeNode) children.get(j);
		i = traverse(child, i);
	    }
	    return i;
	}

	// Subclass to specify action.
	public abstract void apply(TreeNode node, int i);
    }

    // Set focus to be ith-element. 
    private static class SetFocusTraverser extends InorderTraverser {
	private int el;
	public SetFocusTraverser(int el) {
	    this.el = el;
	}
	public void apply(TreeNode node, int i) {
	    if (i == el)
		node.claimFocus();
	}
    }
    public static void setFocus(TreeNode root, int el) {
	SetFocusTraverser traverser = new SetFocusTraverser(el);
	traverser.traverse(root);
    }

    // Get number of focussed element. Zero if not found.
    private static class GetFocusTraverser extends InorderTraverser {
	private TreeNode focus;
	public int el = 0;
	public GetFocusTraverser(TreeNode focus) {
	    this.focus = focus;
	}
	public void apply(TreeNode node, int i) {
	    if (node == focus)
		el = i;
	}
    }
    public static int getFocus(TreeNode root, TreeNode focus) {
	GetFocusTraverser traverser = new GetFocusTraverser(focus);
	traverser.traverse(root);
	return traverser.el;
    }

    // Get node just below root.
    public static TreeNode subroot(TreeNode node) {
	while (node != null && !(node.parent() instanceof TreeFragment))
	    node = node.parent();
	return node;
    }

    // Parent in panel, which is not the same as connection parent for
    // switchs, as switches may be placed one level higher in panel. 
    // The parent is null for TreeFragment.
    public static TreeNode panelParent(TreeNode node) {
	TreeNode parent = node.parent();
	if (node instanceof TreeSwitch) {
	    if (parent == null)
		return null;
	    else {
		Vector siblings = parent.children();
		if (siblings.contains(node))
		    return parent;
		else
		    return parent.parent();
	    }
	} else
	    return parent;
    }

    // Right sibling, or null if none. 
    public static TreeNode panelRightSibling(TreeNode node) {
	TreeNode parent = panelParent(node);
	if (parent == null)
	    return null;
	else {
	    Vector siblings = parent.children();
	    int index = siblings.indexOf(node);
	    if (index >= 0 && index+1 < siblings.size())
		return (TreeNode) siblings.get(index+1);
	    else
		return null;
	}
    }

    // Left sibling, or null if none. 
    public static TreeNode panelLeftSibling(TreeNode node) {
	TreeNode parent = panelParent(node);
	if (parent == null)
	    return null;
	else {
	    Vector siblings = parent.children();
	    int index = siblings.indexOf(node);
	    if (index-1 >= 0)
		return (TreeNode) siblings.get(index-1);
	    else
		return null;
	}
    }

    // First child, or null if none.
    public static TreeNode panelFirstChild(TreeNode node) {
	Vector children = node.children();
	if (children.size() > 0)
	    return (TreeNode) children.get(0);
	else
	    return null;
    }

}
