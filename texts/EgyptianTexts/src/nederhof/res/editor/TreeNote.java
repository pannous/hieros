/***************************************************************************/
/*                                                                         */
/*  TreeNote.java                                                          */
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

public class TreeNote extends ResNote
        implements TreeNode {

    // Constructor from scratch.
    public TreeNote() {
        this(new ResNote());
    }

    // Constructor.
    public TreeNote(ResNote note) {
        super(note.string, note.color);
        panel = new NodePanel("^", this);
    }

    // Make note, unless it is null.
    public static TreeNote makeNote(ResNote note) {
        if (note == null)
            return null;
        else
            return new TreeNote(note);
    }

    // Make formatted notes.
    public static Vector makeNotes(Vector notes) {
        Vector formatNotes = new Vector(notes.size());
        for (int i = 0; i < notes.size(); i++) {
            ResNote note = (ResNote) notes.get(i);
            formatNotes.add(new TreeNote(note));
        }
        return formatNotes;
    }

    ////////////////////////////////////////////////////////////
    // Structure.

    // Parent node.
    private TreeNodeNote parent = null;

    // Connect nodes of the tree.
    public void connectNodes(TreeNodeNote parent) {
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
        return "note";
    }

    // String representation of this in context.
    public String resString() {
        return "[size=0.4]empty[height=0.4,width=0.7]" + this;
    }

    // Should RES be printed in legend?
    public boolean legendPreview() {
        return false;
    }

    // Have values changed since last time.
    private boolean changed = true;

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
        params.addRow("string", 
                new LegendString(stringify(string)) {
                protected void processChanged(String val) {
                    prepareParamChange();
                    string = escape(val);
                    paramChanged();
                } });
        params.addRow("color", 
                new LegendColor(color) {
                protected void processChanged(Color16 val) {
                    prepareParamChange();
                    color = val;
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
                new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehind(parent);
                    finishStructChange();
                } });
        if (parent != null && !(parent instanceof TreeEmptyglyph))
            buttons.addButtonRight(
                    new StructureButton("^", '^') {
                    protected void pushed() {
                        prepareStructChange();
                        parent.appendNote(TreeNote.this);
                        finishStructChange();
                    } });
        buttons.addButtonRight(
                new StructureButton("delete", '\u007F') {
                protected void pushed() {
                    prepareStructChange();
                    parent.removeNote(TreeNote.this);
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
