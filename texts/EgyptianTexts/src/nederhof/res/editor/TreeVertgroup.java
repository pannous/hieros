/***************************************************************************/
/*                                                                         */
/*  TreeVertgroup.java                                                     */
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
import nederhof.util.*;

public class TreeVertgroup extends ResVertgroup
        implements TreeTopgroup, TreeHorsubgroupPart {

    // Constructor with only two groups.
    public TreeVertgroup(TreeVertsubgroupPart group1,
            TreeVertsubgroupPart group2) {
        this(group1, new TreeOp(), new TreeSwitch(), group2);
    }
    // Constructor with only two groups and op and switch.
    public TreeVertgroup(TreeVertsubgroupPart group1,
            TreeOp op, TreeSwitch switchs,
            TreeVertsubgroupPart group2) {
        super(new TreeVertsubgroup(group1),
                op, switchs,
                new TreeVertsubgroup(group2));
        panel = new NodePanel(this);
        op.changed = true;
    }
    // Auxiliary to constructor.
    private void addTreeGroup(TreeOp op, TreeSwitch switchs, 
            TreeVertsubgroupPart group) {
        addGroup(op, switchs, new TreeVertsubgroup(group));
        op.changed = true;
    }

    // Constructor.
    public TreeVertgroup(ResVertgroup group) {
        super(TreeVertsubgroup.makeGroups(group.groups),
                TreeOp.makeOps(group.ops),
                TreeSwitch.makeSwitches(group.switches));
        panel = new NodePanel(this);
    }

    public TreeVertsubgroupPart tGroup(int i) {
        return (TreeVertsubgroupPart) group(i).group;
    }
    public TreeOp tOp(int i) {
        return (TreeOp) op(i);
    }
    public TreeSwitch tSwitchs(int i) {
        return (TreeSwitch) switchs(i);
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Parent node.
    public TreeNode parent = null;

    // Connect nodes of the tree.
    public void connectNodes(TreeNode parent) {
        this.parent = parent;
        for (int i = 0; i < nGroups(); i++)
            tGroup(i).connectNodes(this);
        for (int i = 0; i < nOps(); i++)
            tOp(i).connectNodes(this);
        for (int i = 0; i < nSwitches(); i++)
            tSwitchs(i).connectNodes(this);
    }

    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(5);
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
        Vector children = new Vector(3);
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
        if (ResValues.isRL(globals.direction))
            children = VectorAux.mirror(children);
        return children;
    }

    // No sibling.
    public TreeNode sibling() {
        return null;
    }

    // Parent in tree.
    public TreeNode parent() {
        return parent;
    }

    // Root of tree.
    public TreeFragment root() {
        return parent.root();
    }

    ////////////////////////////////////////////////////////////
    // Focus.

    // Has focus?
    public boolean hasFocus() {
        return root().focus() == this;
    }

    // Claim focus.
    public void claimFocus() {
        root().setFocus(this);
    }

    ////////////////////////////////////////////////////////////
    // Window.

    // Panel in window.
    public NodePanel panel;

    // Return panel.
    public NodePanel panel() {
        return panel;
    }

    // Label in panel.
    public String label() {
        return "vertical";
    }

    // String representation of this in context.
    public String resString() {
        return ResFragment.argsToString(globals.direction,
                globals.size) + this;
    }

    // Should RES be printed in legend?
    public boolean legendPreview() {
        return true;
    }

    // Have values changed since last time.
    public boolean changed = true;

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
        if (anyChanged)
            panel.refresh();
        changed = false;
        return anyChanged;
    }

    ////////////////////////////////////////////////////////////
    // Editing parameters.

    // Make buttons for parameters.
    public LegendParams makeParams() {
        LegendParams params = new LegendParams();
        params.addRow("size", 
                new LegendRealInf(tOp(0).size) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    tOp(0).size = val;
                    paramChanged();
                } });
        params.format();
        return params;
    }

    // Prepare parameter change.
    private void prepareParamChange() {
        root().prepareParamChange();
    }

    // Process that parameter has changed.
    private void paramChanged() {
        changed = true;
        root().refreshLegend();
    }

    ////////////////////////////////////////////////////////////////
    // Structural editing.

    // Make buttons for structure.
    public LegendStructure makeStructureButtons() {
        LegendStructure buttons = new LegendStructure();
        buttons.addButtonLeft(
                new StructureButton("*", '*') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendHor(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("+", '+') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependHor(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendVert(tGroup(nGroups()-1));
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(";", ';') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependVert(tGroup(0));
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>b</u>ox", 'b') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInBox(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInStack(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInInsert(TreeVertgroup.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>m</u>odify", 'm') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInModify(TreeVertgroup.this);
                    finishStructChange();
                } });
        if (parent instanceof TreeFragment ||
                parent instanceof TreeBox ||
                parent instanceof TreeHorgroup ||
                parent instanceof TreeVertgroup)
            buttons.addButtonRight(
                    new StructureButton("delete", '\u007F') {
                    protected void pushed() {
                        prepareStructChange();
                        removeNode();
                        finishStructChange();
                    } });
        buttons.format();
        return buttons;
    }

    // Insert group among groups at index i.
    public void insertAt(TreeVertsubgroupPart newGroup, int i) {
        addGroupAt(new TreeVertsubgroup(newGroup),
                new TreeOp(), new TreeSwitch(), i);
    }

    // Remove node and merge subgroups into node above.
    private void removeNode() {
        Vector<ResTopgroup> topGroups = new Vector<ResTopgroup>(nGroups());
        for (int i = 0; i < nGroups(); i++)
            topGroups.add(tGroup(i));
        EditHelper.replaceBy(this, topGroups, ops, switches);
    }

    // Prepare structural change.
    private void prepareStructChange() {
        root().prepareStructChange();
    }
    // Finish structural change.
    private void finishStructChange() {
        root().finishStructChange();
    }

}
