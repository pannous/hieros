/***************************************************************************/
/*                                                                         */
/*  TreeHieroglyphic.java                                                  */
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

import nederhof.res.*;

public class TreeHieroglyphic extends ResHieroglyphic {

    // Constructor.
    public TreeHieroglyphic(ResHieroglyphic hiero) {
        super(TreeTopgroupHelper.makeGroups(hiero.groups),
                TreeOp.makeOps(hiero.ops),
                TreeSwitch.makeSwitches(hiero.switches));
    }
    // Constructor for single group.
    public TreeHieroglyphic(TreeTopgroup group) {
        super(group);
    }

    // Make hieroglyphic, unless it is null.
    public static TreeHieroglyphic makeHiero(ResHieroglyphic hiero) {
        if (hiero == null)
            return null;
        else
            return new TreeHieroglyphic(hiero);
    }

    public TreeTopgroup tGroup(int i) {
        return (TreeTopgroup) group(i);
    }
    public TreeOp tOp(int i) {
        return (TreeOp) op(i);
    }
    public TreeSwitch tSwitchs(int i) {
        return (TreeSwitch) switchs(i);
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Connect nodes of the tree.
    public void connectNodes(TreeNode parent) {
        for (int i = 0; i < nGroups(); i++)
            tGroup(i).connectNodes(parent);
        for (int i = 0; i < nOps(); i++)
            tOp(i).connectNodes(parent);
        for (int i = 0; i < nSwitches(); i++)
            tSwitchs(i).connectNodes(parent);
    }

    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(20);
        for (int i = 0; i < nGroups(); i++)
            children.add(tGroup(i));
        for (int i = 0; i < nOps(); i++)
            children.add(tOp(i));
        for (int i = 0; i < nSwitches(); i++)
            children.add(tSwitchs(i));
        return children;
    }

    // Children nodes.
    public Vector children() {
        Vector children = new Vector(20);
        for (int i = 0; i < nOps(); i++) {
            TreeTopgroup group = tGroup(i);
            TreeNode sibling = group.sibling();
            children.add(group);
            if (sibling != null)
                children.add(sibling);
            children.add(tOp(i));
            if (tSwitchs(i).toShow())
                children.add(tSwitchs(i));
        }
        TreeTopgroup lastGroup = tGroup(nGroups()-1);
        TreeNode lastSibling = lastGroup.sibling();
        children.add(lastGroup);
        if (lastSibling != null)
            children.add(lastSibling);
        return children;
    }

    ////////////////////////////////////////////////////////////
    // Window.

    // Have values changed since last time.
    private boolean changed = true;

    // Make all nodes in subtree changed.
    public void makeAllChanged() {
        for (int i = 0; i < nGroups(); i++)
            tGroup(i).makeAllChanged();
        for (int i = 0; i < nOps(); i++)
            tOp(i).makeAllChanged();
        for (int i = 0; i < nSwitches(); i++)
            tSwitchs(i).makeAllChanged();
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
        for (int i = 0; i < nGroups(); i++)
            anyChanged |= tGroup(i).printChanged();
        for (int i = 0; i < nOps(); i++)
            anyChanged |= tOp(i).printChanged();
        for (int i = 0; i < nSwitches(); i++)
            anyChanged |= tSwitchs(i).printChanged();
        changed = false;
        return anyChanged;
    }

}
