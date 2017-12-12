/***************************************************************************/
/*                                                                         */
/*  TreeModify.java                                                        */
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

public class TreeModify extends ResModify 
        implements TreeBasicgroup, TreeNode {

    // Constructor from group.
    public TreeModify(TreeTopgroup group) {
        super(new TreeSwitch(), group, new TreeSwitch());
        panel = new NodePanel("modify", this);
    }

    // Constructor.
    public TreeModify(ResModify modify) {
        super(modify.width,
                modify.height,
                modify.above,
                modify.below,
                modify.before,
                modify.after,
                modify.omit,
                modify.shade,
                ResShadeHelper.clone(modify.shades),
                new TreeSwitch(modify.switchs1),
                TreeTopgroupHelper.makeGroup(modify.group),
                new TreeSwitch(modify.switchs2));
        panel = new NodePanel("modify", this);
    }

    public TreeTopgroup tGroup() {
        return (TreeTopgroup) group;
    }
    public TreeSwitch tSwitchs1() {
        return (TreeSwitch) switchs1;
    }
    public TreeSwitch tSwitchs2() {
        return (TreeSwitch) switchs2;
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Parent node.
    private TreeNode parent = null;

    // Connect nodes of the tree.
    public void connectNodes(TreeNode parent) {
        this.parent = parent;
        tSwitchs1().connectNodes(this);
        tGroup().connectNodes(this);
        tSwitchs2().connectNodes(this);
    }

    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(3);
        children.add(tSwitchs1());
        children.add(tGroup());
        children.add(tSwitchs2());
        return children;
    }

    // Children nodes.
    public Vector children() {
        Vector children = new Vector(1);
        if (tSwitchs1().toShow())
            children.add(tSwitchs1());
        TreeTopgroup group = tGroup();
        TreeNode sibling = group.sibling();
        children.add(group);
        if (sibling != null)
            children.add(sibling);
        if (ResValues.isRL(globals.direction))
            children = VectorAux.mirror(children);
        return children;
    }

    // Child to be printed to side, if any.
    public TreeNode sibling() {
        if (tSwitchs2().toShow())
            return tSwitchs2();
        else
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
        return "modify";
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
        tSwitchs1().makeAllChanged();
        tGroup().makeAllChanged();
        tSwitchs2().makeAllChanged();
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
        anyChanged |= tGroup().printChanged();
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
        params.addRow("width", 
                new LegendReal(1.0f, Float.NaN, LegendReal.NON_ZERO_REAL, width) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    width = val;
                    paramChanged();
                } });
        params.addRow("height", 
                new LegendReal(1.0f, Float.NaN, LegendReal.NON_ZERO_REAL, height) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    height = val;
                    paramChanged();
                } });
        params.addRow("above", 
                new LegendReal(boundingDefault, boundingDefault, LegendReal.REAL, above) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    above = val;
                    paramChanged();
                } });
        params.addRow("below", 
                new LegendReal(boundingDefault, boundingDefault, LegendReal.REAL, below) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    below = val;
                    paramChanged();
                } });
        params.addRow("before", 
                new LegendReal(boundingDefault, boundingDefault, LegendReal.REAL, before) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    before = val;
                    paramChanged();
                } });
        params.addRow("after", 
                new LegendReal(boundingDefault, boundingDefault, LegendReal.REAL, after) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    after = val;
                    paramChanged();
                } });
        params.addRow("omit", 
                new LegendBinary(omit) {
                protected void processChanged(boolean val) {
                    prepareParamChange();
                    omit = val;
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
                    EditHelper.appendHor(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("+", '+') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependHor(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendVert(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(";", ';') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependVert(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>b</u>ox", 'b') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInBox(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInStack(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInInsert(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>m</u>odify", 'm') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInModify(TreeModify.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("!fron<u>t</u>", 't') {
                protected void pushed() {
                    tSwitchs1().claimFocus();
                } });
        buttons.addButtonRight(
                new StructureButton("!bac<u>k</u>", 'k') {
                protected void pushed() {
                    tSwitchs2().claimFocus();
                } });
        buttons.addButtonRight(
                new StructureButton("delete", '\u007F') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.replaceBy(TreeModify.this, tGroup());
                    finishStructChange();
                } });
        buttons.format();
        return buttons;
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
