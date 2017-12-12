/***************************************************************************/
/*                                                                         */
/*  TreeStack.java                                                         */
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

public class TreeStack extends ResStack 
        implements TreeBasicgroup, TreeNode {

    // Constructor with only two groups.
    public TreeStack(TreeTopgroup g1, TreeTopgroup g2) {
        super(new TreeSwitch(), g1, new TreeSwitch(), g2, new TreeSwitch());
        panel = new NodePanel("stack", this);
    }

    // Constructor.
    public TreeStack(ResStack stack) {
        super(stack.x,
            stack.y,
            stack.onunder,
            new TreeSwitch(stack.switchs0),
            TreeTopgroupHelper.makeGroup(stack.group1),
            stack.switchs1,
            TreeTopgroupHelper.makeGroup(stack.group2),
            new TreeSwitch(stack.switchs2));
        panel = new NodePanel("stack", this);
    }

    public TreeTopgroup tGroup1() {
        return (TreeTopgroup) group1;
    }
    public TreeTopgroup tGroup2() {
        return (TreeTopgroup) group2;
    }
    public TreeSwitch tSwitchs0() {
        return (TreeSwitch) switchs0;
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
        tSwitchs0().connectNodes(this);
        tGroup1().connectNodes(this);
        tGroup2().connectNodes(this);
        tSwitchs2().connectNodes(this);
    }

    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(4);
        children.add(tSwitchs0());
        children.add(tGroup1());
        children.add(tGroup2());
        children.add(tSwitchs2());
        return children;
    }

    // Children nodes.
    public Vector children() {
        Vector children = new Vector(2);
        if (tSwitchs0().toShow())
            children.add(tSwitchs0());
        TreeTopgroup group1 = tGroup1();
        TreeNode sibling1 = group1.sibling();
        children.add(group1);
        if (sibling1 != null)
            children.add(sibling1);
        TreeTopgroup group2 = tGroup2();
        TreeNode sibling2 = group2.sibling();
        children.add(group2);
        if (sibling2 != null)
            children.add(sibling2);
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
        return "stack";
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
        tSwitchs0().makeAllChanged();
        tGroup1().makeAllChanged();
        tGroup2().makeAllChanged();
        tSwitchs2().makeAllChanged();
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
        anyChanged |= tSwitchs0().printChanged();
        anyChanged |= tGroup1().printChanged();
        anyChanged |= tGroup2().printChanged();
        anyChanged |= tSwitchs2().printChanged();
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
        params.addRow("x", 
                new LegendReal(posDefault, posDefault, LegendReal.LOW_REAL, x) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    x = val;
                    paramChanged();
                } });
        params.addRow("y", 
                new LegendReal(posDefault, posDefault, LegendReal.LOW_REAL, y) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    y = val;
                    paramChanged();
                } });
        params.addRow("cover", 
                new LegendThreeValueString("none", null, "on", "on", "under", "under", 
                    onunder) {
                protected void processChanged(String val) {
                    prepareParamChange();
                    onunder = val;
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
                    EditHelper.appendHor(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("+", '+') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependHor(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendVert(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(";", ';') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependVert(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>b</u>ox", 'b') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInBox(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInStack(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInInsert(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>m</u>odify", 'm') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInModify(TreeStack.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("!fron<u>t</u>", 't') {
                protected void pushed() {
                    tSwitchs0().claimFocus();
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
                    removeNode();
                    finishStructChange();
                } });
        buttons.format();
        return buttons;
    }

    // Remove node and merge subgroups into node above.
    private void removeNode() {
        Vector<ResTopgroup> topGroups = new Vector<ResTopgroup>(2);
        Vector<ResOp> ops = new Vector<ResOp>(1);
        Vector<ResSwitch> switches = new Vector<ResSwitch>(1);
        topGroups.add(tGroup1());
        topGroups.add(tGroup2());
        ops.add(new TreeOp());
        switches.add(new TreeSwitch());
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
