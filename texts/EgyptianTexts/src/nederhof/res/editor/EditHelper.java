/***************************************************************************/
/*																		 */
/*  EditHelper.java														*/
/*																		 */
/*  Copyright (c) 2009 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

package nederhof.res.editor;

import java.util.*;

import nederhof.res.*;

// Various routines to help edit hieroglyphic.

public class EditHelper {

	// Get switch to the right of an operator.
	public static TreeSwitch opRightSiblingSwitch(TreeOp op) {
		return switchFromParent(op, opIndex(op));
	}

	// Get switch of index relative to parent of node.
	public static TreeSwitch switchFromParent(TreeNode node, int i) {
		TreeNode parent = node.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			return frag.tSwitchs(i);
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			return box.tSwitchs(i);
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			return hor.tSwitchs(i);
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			return vert.tSwitchs(i);
		} 
		System.err.println("not recognized group");
		return null;
	}

	// Get group of index relative to parent of node.
	public static TreeTopgroup groupFromParent(TreeNode node, int i) {
		TreeNode parent = node.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			return frag.tGroup(i);
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			return box.tGroup(i);
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			return hor.tGroup(i);
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			return vert.tGroup(i);
		} 
		System.err.println("not recognized group");
		return null;
	}

	// Get index of operator relative to parent.
	public static int opIndex(TreeOp op) {
		TreeNode parent = op.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			for (int i = 0; i < frag.nOps(); i++)
				if (frag.tOp(i) == op) 
					return i;
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			for (int i = 0; i < box.nOps(); i++)
				if (box.tOp(i) == op) 
					return i;
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			for (int i = 0; i < hor.nOps(); i++)
				if (hor.tOp(i) == op) 
					return i;
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			for (int i = 0; i < vert.nOps(); i++)
				if (vert.tOp(i) == op) 
					return i;
		} 
		System.err.println("operator not child of recognized group");
		return -1;
	}

	// Get index of operator relative to parent.
	// Return -1 if none, 0 stands for initial switch.
	public static int switchIndex(TreeSwitch s) {
		TreeNode parent = s.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			for (int i = 0; i < frag.nSwitches(); i++)
				if (frag.tSwitchs(i) == s) 
					return i+1;
			if (frag.tSwitchs() == s)
				return 0;
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			for (int i = 0; i < box.nSwitches(); i++)
				if (box.tSwitchs(i) == s) 
					return i+1;
			if (box.tSwitchs1() == s)
				return 0;
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			for (int i = 0; i < hor.nSwitches(); i++)
				if (hor.tSwitchs(i) == s) 
					return i+1;
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			for (int i = 0; i < vert.nSwitches(); i++)
				if (vert.tSwitchs(i) == s) 
					return i+1;
		} 
		return -1;
	}

	// Get index of group relative to parent.
	public static int groupIndex(TreeNode group) {
		TreeNode parent = group.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			for (int i = 0; i < frag.nGroups(); i++)
				if (frag.tGroup(i) == group) 
					return i;
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			for (int i = 0; i < box.nGroups(); i++)
				if (box.tGroup(i) == group) 
					return i;
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			for (int i = 0; i < hor.nGroups(); i++)
				if (hor.tGroup(i) == group) 
					return i;
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			for (int i = 0; i < vert.nGroups(); i++)
				if (vert.tGroup(i) == group) 
					return i;
		} 
		System.err.println("group not child of recognized group");
		return -1;
	}

	// Get ancestor just below Fragment or Box, where there is hieroglyphic.
	public static TreeNode hieroLevelAncestor(TreeNode node) {
		while (node.parent() != null &&
				!(node.parent() instanceof TreeFragment) &&
				!(node.parent() instanceof TreeBox))
			node = node.parent();
		return node;
	}

	// Put topgroup inside box.
	public static void placeInBox(TreeTopgroup group) {
		TreeNode parent = group.parent();
		TreeBox box = new TreeBox();
		box.insertAt(group, 0);
		boolean done = replaceIn(group, box, parent);
		parent.root().refresh();
		if (done)
			box.claimFocus();
	}

	// Put topgroup inside stack.
	public static void placeInStack(TreeTopgroup group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeStack stack = new TreeStack(group, named);
		boolean done = replaceIn(group, stack, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Put topgroup inside insert.
	public static void placeInInsert(TreeTopgroup group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeInsert insert = new TreeInsert(group, named);
		boolean done = replaceIn(group, insert, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Put topgroup inside modify.
	public static void placeInModify(TreeTopgroup group) {
		TreeNode parent = group.parent();
		TreeModify modify = new TreeModify(group);
		boolean done = replaceIn(group, modify, parent);
		parent.root().refresh();
		if (done)
			modify.claimFocus();
	}

	// Replace one group by another.
	public static void replaceBy(TreeTopgroup fromGroup, TreeTopgroup toGroup) {
		TreeNode parent = fromGroup.parent();
		boolean done = replaceIn(fromGroup, toGroup, parent);
		parent.root().refresh();
		if (done)
			toGroup.claimFocus();
	}

	// Replace group by vectors of groups, ops and switches.
	public static void replaceBy(TreeTopgroup fromGroup,
			Vector<ResTopgroup> groups, Vector<ResOp> ops, Vector<ResSwitch> switches) {
		TreeNode parent = fromGroup.parent();
		TreeNode focus = replaceIn(fromGroup,
				groups, ops, switches, parent);
		parent.root().refresh();
		if (focus != null) 
			focus.claimFocus();
	}

	// Replace groups in larger structure.
	// Return whether successful.
	public static boolean replaceIn(TreeTopgroup fromGroup, TreeTopgroup toGroup,
			TreeNode parent) {
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			for (int i = 0; i < frag.nGroups(); i++)
				if (frag.tGroup(i) == fromGroup) {
					frag.hiero.groups.set(i, toGroup);
					frag.changed = true;
					return true;
				}
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			for (int i = 0; i < box.nGroups(); i++) {
				if (box.tGroup(i) == fromGroup) {
					box.hiero.groups.set(i, toGroup);
					box.changed = true;
					return true;
				}
			}
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			for (int i = 0; i < hor.nGroups(); i++)
				if (hor.tGroup(i) == fromGroup) {
					if (toGroup instanceof TreeHorsubgroupPart) {
						TreeHorsubgroupPart toPart = (TreeHorsubgroupPart) toGroup;
						hor.groups.set(i, new TreeHorsubgroup(toPart));
						hor.changed = true;
						return true;
					} else {
						TreeHorgroup toHor = (TreeHorgroup) toGroup;
						float size = hor.op(0).size;
						// copy subgroups before
						for (int j = i-1; j >= 0; j--) {
							toHor.switches.add(0, hor.switches.get(j));
							toHor.ops.add(0, hor.ops.get(j));
							toHor.groups.add(0, hor.groups.get(j));
						}
						// copy subgroups after
						for (int j = i+1; j < hor.nGroups(); j++) {
							toHor.ops.add(hor.ops.get(j-1));
							toHor.switches.add(hor.switches.get(j-1));
							toHor.groups.add(hor.groups.get(j));
						}
						toHor.op(0).size = size;
						toHor.changed = true;
						return replaceIn(hor, toHor, hor.parent());
					}
				} 
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			for (int i = 0; i < vert.nGroups(); i++)
				if (vert.tGroup(i) == fromGroup) {
					if (toGroup instanceof TreeVertsubgroupPart) {
						TreeVertsubgroupPart toPart = (TreeVertsubgroupPart) toGroup;
						vert.groups.set(i, new TreeVertsubgroup(toPart));
						vert.changed = true;
						return true;
					} else {
						TreeVertgroup toVert = (TreeVertgroup) toGroup;
						float size = vert.op(0).size;
						// copy subgroups before
						for (int j = i-1; j >= 0; j--) {
							toVert.switches.add(0, vert.switches.get(j));
							toVert.ops.add(0, vert.ops.get(j));
							toVert.groups.add(0, vert.groups.get(j));
						}
						// copy subgroups after
						for (int j = i+1; j < vert.nGroups(); j++) {
							toVert.ops.add(vert.ops.get(j-1));
							toVert.switches.add(vert.switches.get(j-1));
							toVert.groups.add(vert.groups.get(j));
						}
						toVert.op(0).size = size;
						toVert.changed = true;
						return replaceIn(vert, toVert, vert.parent());
					}
				}
		} else if (parent instanceof TreeInsert) {
			TreeInsert insert = (TreeInsert) parent;
			if (insert.tGroup1() == fromGroup)
				insert.group1 = toGroup;
			else 
				insert.group2 = toGroup;
			insert.changed = true;
			return true;
		} else if (parent instanceof TreeModify) {
			TreeModify modify = (TreeModify) parent;
			modify.group = toGroup;
			modify.changed = true;
			return true;
		} else if (parent instanceof TreeStack) {
			TreeStack stack = (TreeStack) parent;
			if (stack.tGroup1() == fromGroup)
				stack.group1 = toGroup;
			else 
				stack.group2 = toGroup;
			stack.changed = true;
			return true;
		}
		System.err.println("group to be replaced not found");
		return false;
	}

	// Replace groups in larger structure.
	// Return whether second-last group, or null if unsuccessful.
	private static TreeNode replaceIn(TreeTopgroup fromGroup, 
			Vector<ResTopgroup> toGroups, Vector<ResOp> ops, Vector<ResSwitch> switches,
			TreeNode parent) {
		TreeNode secondLast = (TreeNode) toGroups.get(toGroups.size() - 1);
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			for (int i = 0; i < frag.nGroups(); i++)
				if (frag.tGroup(i) == fromGroup) {
					frag.hiero.groups.set(i, toGroups.get(0));
					for (int j = 1; j < toGroups.size(); j++) {
						TreeOp op = (TreeOp) ops.get(j-1);
						op.changed = true;
						frag.hiero.ops.add(i + j-1, op);
						frag.hiero.switches.add(i + j-1, switches.get(j-1));
						frag.hiero.groups.add(i + j, toGroups.get(j));
					}
					frag.changed = true;
					return secondLast;
				}
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			for (int i = 0; i < box.nGroups(); i++) {
				if (box.tGroup(i) == fromGroup) {
					box.hiero.groups.set(i, toGroups.get(0));
					for (int j = 1; j < toGroups.size(); j++) {
						TreeOp op = (TreeOp) ops.get(j-1);
						op.changed = true;
						box.hiero.ops.add(i + j-1, ops.get(j-1));
						box.hiero.switches.add(i + j-1, switches.get(j-1));
						box.hiero.groups.add(i + j, toGroups.get(j));
					}
					box.changed = true;
					return secondLast;
				}
			}
		} else if (parent instanceof TreeHorgroup) {
			ResHorgroup resGroup = ComposeHelper.toHorgroup(toGroups, 
					ops, switches, Float.NaN);
			TreeHorgroup treeGroup = new TreeHorgroup(resGroup);
			boolean done = replaceIn(fromGroup, treeGroup, parent);
			return done ? treeGroup : null;
		} else if (parent instanceof TreeVertgroup) {
			ResVertgroup resGroup = ComposeHelper.toVertgroup(toGroups,
					ops, switches, Float.NaN);
			TreeVertgroup treeGroup = new TreeVertgroup(resGroup);
			boolean done =  replaceIn(fromGroup, treeGroup, parent);
			return done ? treeGroup : null;
		} else if (parent instanceof TreeInsert ||
				parent instanceof TreeModify ||
				parent instanceof TreeStack) {
			ResHorgroup resGroup = ComposeHelper.toHorgroup(toGroups,
					ops, switches, Float.NaN);
			TreeHorgroup treeGroup = new TreeHorgroup(resGroup);
			boolean done = replaceIn(fromGroup, treeGroup, parent);
			return done ? treeGroup : null;
			/*
			boolean done = replaceIn(fromGroup, new TreeHorgroup(toGroups, ops, switches),
					parent);
			return done ? secondLast : null;
			*/
		}
		System.err.println("group to be replaced not found");
		return null;
	}

	// Replace two groups in large structure, from index i, by single group.
	// Return whether successful.
	public static boolean replaceIn(int i, TreeTopgroup toGroup, TreeNode parent) {
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			frag.hiero.groups.set(i, toGroup);
			frag.hiero.ops.removeElementAt(i);
			frag.hiero.switches.removeElementAt(i);
			frag.hiero.groups.removeElementAt(i+1);
			frag.changed = true;
			return true;
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			box.hiero.groups.set(i, toGroup);
			box.hiero.ops.removeElementAt(i);
			box.hiero.switches.removeElementAt(i);
			box.hiero.groups.removeElementAt(i+1);
			box.changed = true;
			return true;
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			if (hor.nGroups() > 2) {
					   if (toGroup instanceof TreeHorsubgroupPart) {
					TreeHorsubgroupPart toPart = (TreeHorsubgroupPart) toGroup;
					hor.groups.set(i, new TreeHorsubgroup(toPart));
					hor.ops.removeElementAt(i);
					hor.switches.removeElementAt(i);
					hor.groups.removeElementAt(i+1);
					hor.changed = true;
					return true;
				} else 
					return false; // should not happen
			} else 
				return replaceIn(hor, toGroup, hor.parent());
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			if (vert.nGroups() > 2) {
				if (toGroup instanceof TreeVertsubgroupPart) {
					TreeVertsubgroupPart toPart = (TreeVertsubgroupPart) toGroup;
					vert.groups.set(i, new TreeVertsubgroup(toPart));
					vert.ops.removeElementAt(i);
					vert.switches.removeElementAt(i);
					vert.groups.removeElementAt(i+1);
					vert.changed = true;
					return true;
				} else 
					return false; // should not happen
			} else
				return replaceIn(vert, toGroup, vert.parent());
		}
		System.err.println("parent not recognized");
		return false;
	}

	// Insert group in larger structure, at index.
	// Return whether successful.
	public static boolean insertAt(TreeNode parent, int i, TreeBasicgroup basic) {
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			frag.insertAt(basic, i);
			return true;
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			box.insertAt(basic, i);
			return true;
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			hor.insertAt(basic, i);
			return true;
		} else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			vert.insertAt(basic, i);
			return true;
		}
		System.err.println("parent not recognized");
		return false;
	}

	// Append named behind current group as deep as possible.
	// The node should not be TreeSwitch or TreeOp.
	public static void appendNamedBehind(TreeNode node) {
		TreeNode ancestor = hieroLevelAncestor(node);
		TreeNode parent = ancestor.parent();
		if (parent != null) {
			TreeNamedglyph named = new TreeNamedglyph();
			int i = groupIndex(ancestor);
			boolean done = insertAt(parent, i+1, named);
			ancestor.root().refresh();
			if (done)
				named.claimFocus();
		}
	}

	// Append named behind current op as deep as possible.
	public static void appendNamedBehindMinus(TreeOp node) {
		TreeNode parent = node.parent();
		if (parent instanceof TreeHorgroup ||
				parent instanceof TreeVertgroup)
			appendNamedBehind(parent);
		else if (parent instanceof TreeFragment ||
				parent instanceof TreeBox) {
			TreeNamedglyph named = new TreeNamedglyph();
			int i = opIndex(node);
			boolean done = insertAt(parent, i+1, named);
			parent.root().refresh();
			if (done)
				named.claimFocus();
		}
	}

	// Append named behind current switch as deep as possible.
	public static void appendNamedBehindSwitch(TreeSwitch node) {
		TreeNode parent = node.parent();
		if ((parent instanceof TreeFragment ||
				parent instanceof TreeBox) &&
				switchIndex(node) >= 0) {
			TreeNamedglyph named = new TreeNamedglyph();
			int i = switchIndex(node);
			boolean done = insertAt(parent, i, named);
			parent.root().refresh();
			if (done)
				named.claimFocus();
		} else
			appendNamedBehind(parent);
	}

	// Append named behind operator preceding switch.
	public static void appendNamedBehindOp(TreeSwitch node) {
		TreeNode parent = node.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		int i = switchIndex(node);
		boolean done = insertAt(parent, i, named);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Append named behind op of horgroup or vertgroup.
	public static void appendNamedBehindOp(TreeOp node) {
		TreeNode parent = node.parent();
		if (parent instanceof TreeHorgroup ||
				parent instanceof TreeVertgroup) {
			TreeNamedglyph named = new TreeNamedglyph();
			int i = opIndex(node);
			boolean done = insertAt(parent, i+1, named);
			parent.root().refresh();
			if (done)
				named.claimFocus();
		}
	}

	// Make horizontal group out of group, placing named glyph behind.
	public static void appendHor(TreeHorsubgroupPart group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeHorgroup hor = new TreeHorgroup(group, named);
		boolean done = EditHelper.replaceIn(group, hor, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Make horizontal group out of group, placing named glyph before.
	public static void prependHor(TreeHorsubgroupPart group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeHorgroup hor = new TreeHorgroup(named, group);
		boolean done = EditHelper.replaceIn(group, hor, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Make vertical group out of group, placing named glyph behind.
	public static void appendVert(TreeVertsubgroupPart group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeVertgroup vert = new TreeVertgroup(group, named);
		boolean done = EditHelper.replaceIn(group, vert, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Make vertical group out of group, placing named glyph before.
	public static void prependVert(TreeVertsubgroupPart group) {
		TreeNode parent = group.parent();
		TreeNamedglyph named = new TreeNamedglyph();
		TreeVertgroup vert = new TreeVertgroup(named, group);
		boolean done = EditHelper.replaceIn(group, vert, parent);
		parent.root().refresh();
		if (done)
			named.claimFocus();
	}

	// Remove group from parent.
	public static void remove(TreeTopgroup group) {
		TreeNode parent = group.parent();
		if (parent instanceof TreeFragment) {
			TreeFragment frag = (TreeFragment) parent;
			if (frag.nGroups() == 1) {
				frag.hiero = null;
				frag.changed = true;
				frag.root().refresh();
				frag.claimFocus();
				return;
			} else
				for (int i = 0; i < frag.nGroups(); i++)
					if (frag.tGroup(i) == group) {
						frag.hiero.groups.removeElementAt(i);
						if (i < frag.nOps()) {
							frag.hiero.ops.removeElementAt(i);
							frag.hiero.switches.removeElementAt(i);
						} else {
							frag.hiero.ops.removeElementAt(i-1);
							frag.hiero.switches.removeElementAt(i-1);
						}
						frag.changed = true;
						frag.root().refresh();
						if (i < frag.nGroups())
							frag.tGroup(i).claimFocus();
						else if (i-1 >= 0)
							frag.tGroup(frag.nGroups()-1).claimFocus();
						return;
					}
		} else if (parent instanceof TreeBox) {
			TreeBox box = (TreeBox) parent;
			if (box.nGroups() == 1) {
				box.hiero = null;
				box.changed = true;
				box.root().refresh();
				box.claimFocus();
				return;
			} else
				for (int i = 0; i < box.nGroups(); i++) 
					if (box.tGroup(i) == group) {
						box.hiero.groups.removeElementAt(i);
						if (i < box.nOps()) {
							box.hiero.ops.removeElementAt(i);
							box.hiero.switches.removeElementAt(i);
						} else {
							box.hiero.ops.removeElementAt(i-1);
							box.hiero.switches.removeElementAt(i-1);
						}
						box.changed = true;
						box.root().refresh();
						if (i < box.nGroups())
							box.tGroup(i).claimFocus();
						else 
							box.tGroup(box.nGroups()-1).claimFocus();
						return;
					}
		} else if (parent instanceof TreeHorgroup) {
			TreeHorgroup hor = (TreeHorgroup) parent;
			if (hor.nGroups() == 2) {
				if (hor.tGroup(0) == group)
					replaceBy(hor, hor.tGroup(1));
				else
					replaceBy(hor, hor.tGroup(0));
				return;
			} else 
				for (int i = 0; i < hor.nGroups(); i++)
					if (hor.tGroup(i) == group) {
						hor.groups.removeElementAt(i);
						if (i < hor.nOps()) {
							hor.ops.removeElementAt(i);
							hor.switches.removeElementAt(i);
						} else {
							hor.ops.removeElementAt(i-1);
							hor.switches.removeElementAt(i-1);
						}
						hor.changed = true;
						hor.root().refresh();
						if (i < hor.nGroups())
							hor.tGroup(i).claimFocus();
						else 
							hor.tGroup(hor.nGroups()-1).claimFocus();
						return;
					}
	   } else if (parent instanceof TreeVertgroup) {
			TreeVertgroup vert = (TreeVertgroup) parent;
			if (vert.nGroups() == 2) {
				if (vert.tGroup(0) == group)
					replaceBy(vert, vert.tGroup(1));
				else
					replaceBy(vert, vert.tGroup(0));
				return;
			} else
				for (int i = 0; i < vert.nGroups(); i++)
					if (vert.tGroup(i) == group) {
						vert.groups.removeElementAt(i);
						if (i < vert.nOps()) {
							vert.ops.removeElementAt(i);
							vert.switches.removeElementAt(i);
						} else {
							vert.ops.removeElementAt(i-1);
							vert.switches.removeElementAt(i-1);
						}
						vert.changed = true;
						vert.root().refresh();
						if (i < vert.nGroups())
							vert.tGroup(i).claimFocus();
						else 
							vert.tGroup(vert.nGroups()-1).claimFocus();
						return;
					}
		} else if (parent instanceof TreeInsert) {
			TreeInsert insert = (TreeInsert) parent;
			if (insert.tGroup1() == group) 
				replaceBy(insert, insert.tGroup2());
			else
				replaceBy(insert, insert.tGroup1());
			return;
		} else if (parent instanceof TreeModify) {
			TreeModify modify = (TreeModify) parent;
			remove(modify);
			return;
		} else if (parent instanceof TreeStack) {
			TreeStack stack = (TreeStack) parent;
			if (stack.tGroup1() == group) 
				replaceBy(stack, stack.tGroup2());
			else
				replaceBy(stack, stack.tGroup1());
			return;
		} 
		System.err.println("group to be removed not found");
	}

}
