/***************************************************************************/
/*                                                                         */
/*  TreeBox.java                                                           */
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

public class TreeBox extends ResBox 
        implements TreeBasicgroup, TreeNode, TreeNodeNote {

    // Constructor from scratch.
    public TreeBox() {
        this(new ResBox());
    }

    // Constructor.
    public TreeBox(ResBox box) {
        super(box.type,
                box.direction,
                box.mirror,
                box.scale,
                box.color,
                box.shade,
                ResShadeHelper.clone(box.shades),
                box.size,
                box.opensep,
                box.closesep,
                box.undersep,
                box.oversep,
                new TreeSwitch(box.switchs1),
                TreeHieroglyphic.makeHiero(box.hiero),
                TreeNote.makeNotes(box.notes),
                new TreeSwitch(box.switchs2));
        panel = new NodePanel("box", this);
    }

    public TreeHieroglyphic tHiero() {
        return (TreeHieroglyphic) hiero;
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
    public TreeNote tNote(int i) {
        return (TreeNote) note(i);
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
        if (tHiero() != null) 
            tHiero().connectNodes(this);
        for (int i = 0; i < nNotes(); i++) 
            tNote(i).connectNodes(this);
        tSwitchs2().connectNodes(this);
    }

    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(7);
        children.add(tSwitchs1());
        if (tHiero() != null)
            children.addAll(tHiero().allChildren());
        for (int i = 0; i < nNotes(); i++)
            children.add(tNote(i));
        children.add(tSwitchs2());
        return children;
    }

    // Children nodes.
    public Vector children() {
        Vector children = new Vector(5);
        if (tSwitchs1().toShow()) 
            children.add(tSwitchs1());
        if (tHiero() != null)
            children.addAll(tHiero().children());
        for (int i = 0; i < nNotes(); i++)
            children.add(tNote(i));
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
    private NodePanel panel;

    // Return panel.
    public NodePanel panel() {
        return panel;
    }

    // Label in panel.
    public String label() {
        return "box";
    }

    // String representation of this in context.
    public String resString() {
        return ResFragment.argsToString(globals.direction, 
                globals.size) + this;
    }

    // Print in preview.
    public boolean legendPreview() {
        return true;
    }

    // Have values changed since last time.
    public boolean changed = true;

    // Make all nodes in subtree changed.
    public void makeAllChanged() {
        tSwitchs1().makeAllChanged();
        if (tHiero() != null)
            tHiero().makeAllChanged();
        for (int i = 0; i < nNotes(); i++)
            tNote(i).makeAllChanged();
        tSwitchs2().makeAllChanged();
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
        anyChanged |= tSwitchs1().printChanged();
        if (tHiero() != null)
            anyChanged |= tHiero().printChanged();
        for (int i = 0; i < nNotes(); i++)
            anyChanged |= tNote(i).printChanged();
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
        params.addRow("type", 
                new LegendSelection(boxTypes(), typeDefault, type) {
                protected void processChanged(String val) {
                    prepareParamChange();
                    type = val;
                    paramChanged();
                } });
        params.addRow("direction", 
                new LegendThreeValue("none", ResValues.DIR_NONE, "h", ResValues.DIR_H, "v", ResValues.DIR_V,
                    direction) {
                protected void processChanged(int val) {
                    prepareParamChange();
                    direction = val;
                    paramChanged();
                } });
        params.addRow("mirror", 
                new LegendBool(mirror) {
                protected void processChanged(Boolean val) {
                    prepareParamChange();
                    mirror = val;
                    paramChanged();
                } });
        params.addRow("scale", 
                new LegendReal(1.0f, scaleDefault, LegendReal.NON_ZERO_REAL, scale) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    scale = val;
                    paramChanged();
                } });
        params.addRow("color", 
                new LegendColor(color) {
                protected void processChanged(Color16 val) {
                    prepareParamChange();
                    color = val;
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
        params.addRow("size", 
                new LegendReal(1.0f, sizeDefault, LegendReal.NON_ZERO_REAL, size) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    size = val;
                    paramChanged();
                } });
        params.addRow("opensep", 
                new LegendReal(1.0f, Float.NaN, LegendReal.REAL, opensep) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    opensep = val;
                    paramChanged();
                } });
        params.addRow("closesep", 
                new LegendReal(1.0f, Float.NaN, LegendReal.REAL, closesep) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    closesep = val;
                    paramChanged();
                } });
        params.addRow("undersep", 
                new LegendReal(1.0f, Float.NaN, LegendReal.REAL, undersep) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    undersep = val;
                    paramChanged();
                } });
        params.addRow("oversep", 
                new LegendReal(1.0f, Float.NaN, LegendReal.REAL, oversep) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    oversep = val;
                    paramChanged();
                } });
        params.format();
        return params;
    }

    // Types of boxes. Give in alphabetical order.
    private Vector boxTypes() {
        return new Vector(new TreeSet(root().getContext().getBoxTypes()));
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
                new StructureButton("<u>e</u>mpty", 'e') {
                protected void pushed() {
                    prepareStructChange();
                    prepend(new TreeEmptyglyph());
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(".", '.') {
                protected void pushed() {
                    prepareStructChange();
                    prepend(new TreeEmptyglyph(0f, 0f));
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("<u>n</u>amed", 'n') {
                protected void pushed() {
                    prepareStructChange();
                    prepend(new TreeNamedglyph());
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("*", '*') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendHor(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("+", '+') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependHor(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendVert(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(";", ';') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependVert(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>b</u>ox", 'b') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInBox(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInStack(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInInsert(TreeBox.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>m</u>odify", 'm') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInModify(TreeBox.this);
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
                new StructureButton("^", '^') {
                protected void pushed() {
                    prepareStructChange();
                    addNote();
                    finishStructChange();
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

    // Insert group at beginning.
    private void prepend(TreeTopgroup newGroup) {
        insertAt(newGroup, 0);
        root().refresh();
        tGroup(0).claimFocus();
    }

    // Insert group among groups at index i.
    public void insertAt(TreeTopgroup newGroup, int i) {
        if (hiero == null) 
            hiero = new TreeHieroglyphic(newGroup);
        else 
            hiero.addGroupAt(newGroup, new TreeOp(), new TreeSwitch(), i);
        changed = true;
    }

    // Remove box.
    private void removeNode() {
        if (nGroups() == 0)
            EditHelper.remove(this);
        else if (nGroups() == 1)
            EditHelper.replaceBy(this, tGroup(0));
        else
            EditHelper.replaceBy(this, hiero.groups, hiero.ops, hiero.switches);
    }

    // Add note.
    private void addNote() {
        TreeNote newNote = new TreeNote();
        notes.add(0, newNote);
        root().refresh();
        newNote.claimFocus();
    }

    // Remove note and have focus on next note
    // or the previous if the removed note was the last,
    // or the parent if no notes remain.
    public void removeNote(TreeNote note) {
        int i = notes.indexOf(note);
        if (i >= 0) {
            notes.removeElementAt(i);
            changed = true;
            root().refresh();
            if (nNotes() > 0) {
                if (i < nNotes())
                    tNote(i).claimFocus();
                else
                    tNote(i-1).claimFocus();
            } else
                claimFocus();
        }
    }

    // Append note behind this note.
    public void appendNote(TreeNote note) {
        TreeNote newNote = new TreeNote();
        int i = notes.indexOf(note);
        notes.add(i+1, newNote);
        changed = true;
        root().refresh();
        newNote.claimFocus();
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
