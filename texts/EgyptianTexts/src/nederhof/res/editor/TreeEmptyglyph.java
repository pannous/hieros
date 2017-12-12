/***************************************************************************/
/*																		 */
/*  TreeEmptyglyph.java													*/
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
import nederhof.util.*;

public class TreeEmptyglyph extends ResEmptyglyph 
		implements TreeBasicgroup, TreeNodeNote {

	// Constructor from scratch.
	public TreeEmptyglyph() {
		this(new ResEmptyglyph(sizeDefault, sizeDefault));
	}
	// Same for point glyph.
	public TreeEmptyglyph(float width, float height) {
		this(new ResEmptyglyph(width, height));
	}
	// If switch is given.
	public TreeEmptyglyph(TreeSwitch switchs) {
		this(new ResEmptyglyph(sizeDefault, sizeDefault, switchs));
	}
	public TreeEmptyglyph(float width, float height, TreeSwitch switchs) {
		this(new ResEmptyglyph(width, height, switchs));
	}

	// Constructor.
	public TreeEmptyglyph(ResEmptyglyph empty) {
		super(empty.width,
				empty.height,
				empty.shade,
				ResShadeHelper.clone(empty.shades),
				empty.firm,
				TreeNote.makeNote(empty.note),
				new TreeSwitch(empty.switchs));
		panel = new NodePanel("empty", this);
	}

	public TreeNote tNote() {
		return (TreeNote) note;
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
		if (tNote() != null)
			tNote().connectNodes(this);
		tSwitchs().connectNodes(this);
	}

	// All child nodes.
	public Vector allChildren() {
		Vector children = new Vector(2);
		if (tNote() != null)
			children.add(tNote());
		children.add(tSwitchs());
		return children;
	}

	// Children nodes.
	public Vector children() {
		Vector children = new Vector(1);
		if (tNote() != null)
			children.add(tNote());
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
		return "empty";
	}

	// String representation of this in context;
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
		if (tNote() != null)
			tNote().makeAllChanged();
		tSwitchs().makeAllChanged();
		changed = true;
	}

	// Print again if needed. Return whether there were changes.
	public boolean printChanged() {
		boolean anyChanged = changed;
		if (tNote() != null)
			anyChanged |= tNote().printChanged();
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
		params.addRow("width", 
				new LegendReal(sizePoint, sizeDefault, LegendReal.REAL, width) {
				protected void processChanged(float val) {
					prepareParamChange();
					width = val;
					paramChanged();
				} });
		params.addRow("height", 
				new LegendReal(sizePoint, sizeDefault, LegendReal.REAL, height) {
				protected void processChanged(float val) {
					prepareParamChange();
					height = val;
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
		params.addRow("firm", 
				new LegendBinary(firm) {
				protected void processChanged(boolean val) {
					prepareParamChange();
					firm = val;
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
				new StructureButton("<u>n</u>amed", 'n') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.replaceBy(TreeEmptyglyph.this,
						new TreeNamedglyph(tSwitchs()));
					finishStructChange();
				} });
		buttons.addButtonLeft(
				new StructureButton("*", '*') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.appendHor(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonLeft(
				new StructureButton("+", '+') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.prependHor(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonLeft(
				new StructureButton(":", ':') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.appendVert(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonLeft(
				new StructureButton(";", ';') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.prependVert(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonLeft(
				new StructureButton("-", '-') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.appendNamedBehind(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonRight(
				new StructureButton("<u>b</u>ox", 'b') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.placeInBox(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonRight(
				new StructureButton("<u>s</u>tack", 's') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.placeInStack(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonRight(
				new StructureButton("<u>i</u>nsert", 'i') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.placeInInsert(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.addButtonRight(
				new StructureButton("<u>m</u>odify", 'm') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.placeInModify(TreeEmptyglyph.this);
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
					if (tNote() == null) {
						prepareStructChange();
						addNote();
						finishStructChange();
					}
					tNote().claimFocus();
				} });
		buttons.addButtonRight(
				new StructureButton("delete", '\u007F') {
				protected void pushed() {
					prepareStructChange();
					EditHelper.remove(TreeEmptyglyph.this);
					finishStructChange();
				} });
		buttons.format();
		return buttons;
	}

	private void addNote() {
		note = new TreeNote();
		root().refresh();
	}

	public void removeNote(TreeNote note) {
		this.note = null;
		changed = true;
		root().refresh();
		claimFocus();
	}

	public void appendNote(TreeNote note) {
		// Should not happen. 
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
