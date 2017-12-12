/***************************************************************************/
/*                                                                         */
/*  TreeOp.java                                                            */
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

public class TreeOp extends ResOp 
        implements TreeNode {

    // Constructor from scratch.
    public TreeOp() {
        this(new ResOp());
    }

    // Constructor.
    public TreeOp(ResOp op) {
        super(op.sep,
                op.fit,
                op.fix,
                op.shade,
                ResShadeHelper.clone(op.shades),
                op.size);
        panel = new NodePanel(this);
    }

    // Make ops for tree.
    public static Vector makeOps(Vector ops) {
        Vector treeOps = new Vector(ops.size());
        for (int i = 0; i < ops.size(); i++) {
            ResOp op = (ResOp) ops.get(i);
            treeOps.add(new TreeOp(op));
        }
        return treeOps;
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Parent node.
    private TreeNode parent = null;

    // Connect nodes of the tree.
    public void connectNodes(TreeNode parent) {
        this.parent = parent;
    }

    // All child nodes.
    public Vector allChildren() {
        return new Vector(0);
    }

    // Children nodes.
    public Vector children() {
        return new Vector(0);
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
        return "operator";
    }

    // String representation of this in context.
    public String resString() {
        if (parent instanceof TreeHorgroup)
            return "[size=0.3]\"*\"[size=0.3]";
        else if (parent instanceof TreeVertgroup)
            return "[size=0.3]\":\"[size=0.3]";
        else
            return "[size=0.3]\"-\"[size=0.3]";
    }

    // Should RES be printed in legend?
    public boolean legendPreview() {
        return false;
    }

    // Have values changed since last time.
    public boolean changed = true;

    // Make all nodes in subtree changed.
    public void makeAllChanged() {
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
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
        params.addRow("sep", 
                new LegendReal(1.0f, Float.NaN, LegendReal.REAL, sep) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    sep = val;
                    paramChanged();
                } });
        params.addRow("fit", 
                new LegendBool(fit) {
                protected void processChanged(Boolean val) {
                    prepareParamChange();
                    fit = val;
                    paramChanged();
                } });
        params.addRow("fix", 
                new LegendBinary(fix) {
                protected void processChanged(boolean val) {
                    prepareParamChange();
                    fix = val;
                    paramChanged();
                } });
        params.addRow("shade", 
                new LegendBool(shade) {
                protected void processChanged(Boolean val) {
                    prepareParamChange();
                    shade = val;
                    paramChanged();
                } });
        params.addRow("shades", 
                new LegendShades(shades) {
                protected void processChanged(Vector val) {
                    prepareParamChange();
                    shades = val;
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
                    if (parent instanceof TreeHorgroup)
                        EditHelper.appendNamedBehindOp(TreeOp.this);
                    else
                        joinGroupsIntoHor();
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    if (parent instanceof TreeVertgroup)
                        EditHelper.appendNamedBehindOp(TreeOp.this);
                    else
                        joinGroupsIntoVert();
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehindMinus(TreeOp.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("!", '!') {
                protected void pushed() {
                    TreeSwitch s = EditHelper.opRightSiblingSwitch(TreeOp.this);
                    s.claimFocus();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    joinGroupsIntoStack();
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    joinGroupsIntoInsert();
                    finishStructChange();
                } });
        buttons.format();
        return buttons;
    }

    // Take group before and after, and join into stack.
    private void joinGroupsIntoStack() {
        int opNum = EditHelper.opIndex(this);
        ResTopgroup group1 = EditHelper.groupFromParent(this, opNum);
        ResSwitch switchs1 = EditHelper.switchFromParent(this, opNum);
        ResTopgroup group2 = EditHelper.groupFromParent(this, opNum+1);
        ResStack stack = new ResStack(group1, switchs1, group2);
        stack.propagateBack(new ResSwitch());
        TreeStack treeStack = new TreeStack(stack);
        boolean done = EditHelper.replaceIn(opNum, treeStack, parent);
        root().refresh();
        if (done) 
            treeStack.claimFocus();
    }

    private void joinGroupsIntoInsert() {
        int opNum = EditHelper.opIndex(this);
        ResTopgroup group1 = EditHelper.groupFromParent(this, opNum);
        ResSwitch switchs1 = EditHelper.switchFromParent(this, opNum);
        ResTopgroup group2 = EditHelper.groupFromParent(this, opNum+1);
        ResInsert insert = new ResInsert(group1, switchs1, group2);
        insert.propagateBack(new ResSwitch());
        TreeInsert treeInsert = new TreeInsert(insert);
        boolean done = EditHelper.replaceIn(opNum, treeInsert, parent);
        root().refresh();
        if (done) 
            treeInsert.claimFocus();
    }

    private void joinGroupsIntoHor() {
        int opNum = EditHelper.opIndex(this);
        ResTopgroup group1 = EditHelper.groupFromParent(this, opNum);
        ResSwitch switchs = EditHelper.switchFromParent(this, opNum);
        ResTopgroup group2 = EditHelper.groupFromParent(this, opNum+1);
        ResHorgroup hor = ResComposer.joinHor(group1, this, switchs, group2);
        TreeHorgroup treeHor = new TreeHorgroup(hor);
        boolean done = EditHelper.replaceIn(opNum, treeHor, parent);
        root().refresh();
        if (done) 
            treeHor.claimFocus();
    }

    private void joinGroupsIntoVert() {
        int opNum = EditHelper.opIndex(this);
        ResTopgroup group1 = EditHelper.groupFromParent(this, opNum);
        ResSwitch switchs = EditHelper.switchFromParent(this, opNum);
        ResTopgroup group2 = EditHelper.groupFromParent(this, opNum+1);
        ResVertgroup hor = ResComposer.joinVert(group1, this, switchs, group2);
        TreeVertgroup treeVert = new TreeVertgroup(hor);
        boolean done = EditHelper.replaceIn(opNum, treeVert, parent);
        root().refresh();
        if (done) 
            treeVert.claimFocus();
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
