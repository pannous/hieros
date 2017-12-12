/***************************************************************************/
/*                                                                         */
/*  TreeSwitch.java                                                        */
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

public class TreeSwitch extends ResSwitch
        implements TreeNode {

    // Constructor from scratch.
    public TreeSwitch() {
        this(new ResSwitch());
    }

    // Constructor.
    public TreeSwitch(ResSwitch s) {
        super(s.color,
                s.shade,
                s.sep,
                s.fit,
                s.mirror);
        panel = new NodePanel(this);
    }

    // Make switches for tree.
    public static Vector makeSwitches(Vector switches) {
        Vector treeSwitches = new Vector(switches.size());
        for (int i = 0; i < switches.size(); i++) {
            ResSwitch s = (ResSwitch) switches.get(i);
            treeSwitches.add(new TreeSwitch(s));
        }
        return treeSwitches;
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
        paramChanged();
        root().refresh();
        panel.scrollToView();
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
        return "switch";
    }

    // String representation of this in context.
    public String resString() {
        return "[size=0.3]" + this + "\"!\"[size=0.3]";
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

    // Panel to be shown if focus or not default values.
    public boolean toShow() {
        return !isDefault() || hasFocus();
    }

    ////////////////////////////////////////////////////////////
    // Editing parameters.

    // Make buttons for parameters.
    public LegendParams makeParams() {
        LegendParams params = new LegendParams();
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
        params.addRow("mirror",
                new LegendBool(mirror) {
                protected void processChanged(Boolean val) {
                    prepareParamChange();
                    mirror = val;
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
    public void paramChanged() {
        changed = true;
    }

    ////////////////////////////////////////////////////////////////
    // Structural editing.

    // Make buttons for structure.
    public LegendStructure makeStructureButtons() {
        LegendStructure buttons = new LegendStructure();
        if (parent instanceof TreeHorgroup)
            buttons.addButtonLeft(
                    new StructureButton("*", '*') {
                    protected void pushed() {
                        prepareStructChange();
                        EditHelper.appendNamedBehindOp(TreeSwitch.this);
                        finishStructChange();
                    } });
        if (parent instanceof TreeVertgroup)
            buttons.addButtonLeft(
                    new StructureButton(":", ':') {
                    protected void pushed() {
                        prepareStructChange();
                        EditHelper.appendNamedBehindOp(TreeSwitch.this);
                        finishStructChange();
                    } });
        buttons.addButtonLeft(
                    new StructureButton("-", '-') {
                protected void pushed() {
                    prepareStructChange();
                    EditHelper.appendNamedBehindSwitch(TreeSwitch.this);
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
