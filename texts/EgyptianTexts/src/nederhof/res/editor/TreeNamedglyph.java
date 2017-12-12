/***************************************************************************/
/*                                                                         */
/*  TreeNamedglyph.java                                                    */
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

public class TreeNamedglyph extends ResNamedglyph 
        implements TreeBasicgroup, TreeNodeNote {

    // Constructor from scratch.
    public TreeNamedglyph() {
        this(new ResNamedglyph("\"?\""));
    }
    // With switch.
    public TreeNamedglyph(TreeSwitch switchs) {
        this(new ResNamedglyph("\"?\"", switchs));
    }

    // Constructor.
    public TreeNamedglyph(ResNamedglyph glyph) {
        super(glyph.name,
                glyph.mirror,
                glyph.rotate,
                glyph.scale,
                glyph.xscale,
                glyph.yscale,
                glyph.color,
                glyph.shade,
                ResShadeHelper.clone(glyph.shades),
                TreeNote.makeNotes(glyph.notes),
                new TreeSwitch(glyph.switchs));
        panel = new NodePanel(this);
    }

   public TreeNote tNote(int i) {
        return (TreeNote) note(i);
    }
    public TreeSwitch tSwitchs() {
        return (TreeSwitch) switchs;
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Parent node.
    private TreeNode parent = null;

    // Connect nodes of the tree.
    public void connectNodes(TreeNode parent) {
        this.parent = parent;
        for (int i = 0; i < nNotes(); i++)
            tNote(i).connectNodes(this);
        tSwitchs().connectNodes(this);
    }
    // All child nodes.
    public Vector allChildren() {
        Vector children = new Vector(2);
        for (int i = 0; i < nNotes(); i++)
            children.add(tNote(i));
        children.add(tSwitchs());
        return children;
    }

    // Children nodes.
    public Vector children() {
        Vector children = new Vector(1);
        for (int i = 0; i < nNotes(); i++)
            children.add(tNote(i));
        if (ResValues.isRL(globals.direction))
            children = VectorAux.mirror(children);
        return children;
    }

    // Child to be printed to side, if any.
    public TreeNode sibling() {
        if (tSwitchs().toShow())
            return tSwitchs();
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
        return "named glyph";
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
    private boolean changed = true;

    // Make all nodes in subtree changed.
    public void makeAllChanged() {
        for (int i = 0; i < nNotes(); i++)
            tNote(i).makeAllChanged();
        tSwitchs().makeAllChanged();
        changed = true;
    }

    // Print again if needed. Return whether there were changes.
    public boolean printChanged() {
        boolean anyChanged = changed;
        for (int i = 0; i < nNotes(); i++)
            anyChanged |= tNote(i).printChanged();
        anyChanged |= tSwitchs().printChanged();
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
        params.addRow("name", 
                new LegendName(name) { 
                protected GlyphChooser getGlyphChooserWindow() {
                    return root().getGlyphChooserWindow();
                }
                protected void processChanged(String val) {
                    prepareParamChange();
                    name = root().getContext().customNameToGardiner(val);
                    paramChanged();
                } });
        params.addRow("mirror", 
                new LegendBool(mirror) {
                protected void processChanged(Boolean val) {
                    prepareParamChange();
                    mirror = val;
                    paramChanged();
                } });
        params.addRow("rotate", 
                new LegendInt(rotateDefault, -360, 360, 15, rotate, 
                    new int[] {-90,+90,+180}) {
                protected void processChanged(int val) {
                    prepareParamChange();
                    rotate = val % 360;
                    while (rotate < 0)
                        rotate += 360;
                    paramChanged();
                } });
        params.addRow("scale", 
                new LegendReal(1.0f, scaleDefault, LegendReal.NON_ZERO_REAL, scale) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    scale = val;
                    paramChanged();
                } });
        params.addRow("xscale", 
                new LegendReal(1.0f, scaleDefault, LegendReal.NON_ZERO_REAL, xscale) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    xscale = val;
                    paramChanged();
                } });
        params.addRow("yscale", 
                new LegendReal(1.0f, scaleDefault, LegendReal.NON_ZERO_REAL, yscale) {
                protected void processChanged(float val) {
                    prepareParamChange();
                    yscale = val;
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
                new StructureButton("<u>e</u>mpty", 'e') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.replaceBy(TreeNamedglyph.this, 
                        new TreeEmptyglyph(tSwitchs()));
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(".", '.') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.replaceBy(TreeNamedglyph.this, 
                        new TreeEmptyglyph(0f, 0f, tSwitchs()));
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("*", '*') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendHor(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton("+", '+') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependHor(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(":", ':') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendVert(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                new StructureButton(";", ';') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.prependVert(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonLeft(
                    new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                    new StructureButton("<u>b</u>ox", 'b') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInBox(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>s</u>tack", 's') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInStack(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>i</u>nsert", 'i') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInInsert(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("<u>m</u>odify", 'm') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.placeInModify(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.addButtonRight(
                new StructureButton("!", '!') {
                protected void pushed() {
                    tSwitchs().claimFocus();
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
                    EditHelper.remove(TreeNamedglyph.this);
                    finishStructChange();
                } });
        buttons.format();
        return buttons;
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
