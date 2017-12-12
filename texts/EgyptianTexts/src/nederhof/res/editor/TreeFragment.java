/***************************************************************************/
/*																		 */
/*  TreeFragment.java													  */
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

// A fragment of RES.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.*;
import java.util.*;

import nederhof.res.*;
import nederhof.util.*;

public abstract class TreeFragment extends ResFragment 
		implements TreeNode {

	// Context for rendering.
	private HieroRenderContext context;

	// Element in tree that has focus. If none then this.
	private TreeNode focus;

	// Whether parameters of focus have changed.
	private boolean focusParamChanged;

	// Legend for editing element under focus.
	private TreeLegend legend;

	// Constructor for parsed fragment.
	public TreeFragment(ResFragment frag, HieroRenderContext context,
			KeyListener listener, WindowAdapter closeListener) {
		this(frag.normalizedSwitches(), context);
		focus = this;
		focusParamChanged = false;
		legend = new TreeLegend(context, listener, closeListener);
		legend.newContent(focus);
	}
	// Auxiliary constructor. (Trick to call normalisation before
	// calling constructor of super.)
	private TreeFragment(ResFragment frag, HieroRenderContext context) {
		super(frag.direction,
				frag.size,
				new TreeSwitch(frag.switchs),
				TreeHieroglyphic.makeHiero(frag.hiero));
		this.context = context;
		panel = new RootPanel(this);
		propagate();
		connectNodes();
		makeAllChanged();
		printChanged();
	}

	public TreeSwitch tSwitchs() {
		return (TreeSwitch) switchs;
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

	public HieroRenderContext getContext() {
		return context;
	}

	// Dispose of auxiliary windows.
	public void dispose() {
		legend.dispose();
	}

	// Make auxiliary windows (in)visible.
	public void setVisible(boolean b) {
		if (b)
			legend.setState(Frame.NORMAL);
		legend.setVisible(b);
	}

	////////////////////////////////////////////////////////////
	// Structure.

	// Connect nodes of the tree.
	private void connectNodes() {
		tSwitchs().connectNodes(this);
		if (tHiero() != null) 
			tHiero().connectNodes(this);
	}

	// All child nodes.
	public Vector allChildren() {
		Vector children = new Vector(5);
		children.add(tSwitchs());
		if (tHiero() != null)
			children.addAll(tHiero().allChildren());
		return children;
	}

	// Children nodes.
	public Vector children() {
		Vector children = new Vector(5);
		if (tSwitchs().toShow())
			children.add(tSwitchs());
		if (tHiero() != null)
			children.addAll(tHiero().children());
		if (ResValues.isRL(globals.direction))
			children = VectorAux.mirror(children);
		return children;
	}

	// No parent in tree above root.
	public TreeNode parent() {
		return null;
	}

	// Root of tree is this.
	public TreeFragment root() {
		return this;
	}

	//////////////////////////////////////////////////
	// Focus.

	// Current focus.
	public TreeNode focus() {
		return focus;
	}

	// Make focus to be element.
	// If switch, make sure it is shown.
	public void setFocus(TreeNode node) {
		if (node != focus) {
			if (focusParamChanged)
				refresh();
			if (focus.panel() != null)
				focus.panel().setBorderNoFocus();
			focus = node;
			focusParamChanged = false;
			legend.newContent(focus);
			if (focus.panel() != null)
				focus.panel().setBorderFocus();
			transferFocus();
		} 
	}

	// Has focus?
	public boolean hasFocus() {
		return focus() == this;
	}

	// Claim focus.
	public void claimFocus() {
		setFocus(this);
	}

	// Set focus to be ith-element. 
	public void setFocus(int i) {
		TreeNodeHelper.setFocus(this, i);
	}

	// Get position of focus.
	public int getFocus() {
		return TreeNodeHelper.getFocus(this, focus);
	}

	// Scroll to focus.
	public void scrollToFocus() {
		if (focus.panel() != null)
			focus.panel().scrollToView();
	}

	// Move focus.
	public void moveFocusUp() {
		TreeNode toFocus = TreeNodeHelper.panelParent(focus);
		if (toFocus != null && !(toFocus instanceof TreeFragment))
			setFocus(toFocus);
	}
	public void moveFocusRight() {
		TreeNode toFocus = TreeNodeHelper.panelRightSibling(focus);
		if (toFocus != null)
			setFocus(toFocus);
		else if (ResValues.isRL(direction)) {
			Vector children = children();
			if (children.size() > 0 && children.get(children.size()-1) == focus)
				setFocus(this);
		} else if (focus == this)
			moveFocusStart();
	}
	public void moveFocusLeft() {
		TreeNode toFocus = TreeNodeHelper.panelLeftSibling(focus);
		if (toFocus != null)
			setFocus(toFocus);
		else if (!ResValues.isRL(direction)) {
			Vector children = children();
			if (children.size() > 0 && children.get(0) == focus)
				setFocus(this);
		} else if (focus == this)
			moveFocusEnd();
	}
	public void moveFocusDown() {
		TreeNode toFocus = TreeNodeHelper.panelFirstChild(focus);
		if (toFocus != null)
			setFocus(toFocus);
	}
	public void moveFocusStart() {
		Vector children = children();
		if (children.size() > 0) {
			TreeNode first = (TreeNode) children.get(0);
			setFocus(first);
		}
	}
	public void moveFocusEnd() {
		Vector children = children();
		if (children.size() > 0) {
			TreeNode last = (TreeNode) children.get(children.size() - 1);
			setFocus(last);
		}
	}

	// Set focus to top of group.
	public void setFocusGroup(int i) {
		if (i < 0)
			setFocus(this);
		else if (i % 2 == 0) {
			int n = i / 2;
			if (n < nGroups())
				setFocus(tGroup(n));
		} else {
			int n = (i-1) / 2;
			if (n < nOps())
				setFocus(tOp(n));
		}
	}

	// Get number of group or operator connected to focus.
	// Negative if none.
	public int getFocusGroup() {
		TreeNode subroot = TreeNodeHelper.subroot(focus);
		for (int i = 0; i < nGroups(); i++)
			if (subroot == group(i))
				return i * 2;
		for (int i = 0; i < nOps(); i++)
			if (subroot == op(i))
				return i * 2 + 1;
		for (int i = 0; i < nSwitches(); i++)
			if (subroot == switchs(i))
				return i * 2 + 1;
		return -1;
	}

	////////////////////////////////////////////////////////////
	// Window.

	// Panel in window.
	private RootPanel panel;

	// Not used.
	public NodePanel panel() {
		return null;
	}
	// Used instead of above.
	public RootPanel rootPanel() {
		return panel;
	}

	// Label in panel. (Not used at this class.)
	public String label() {
		return "fragment";
	}

	// String representation of this in context.
	public String resString() {
		return "" + this;
	}

	// Print in preview.
	public boolean legendPreview() {
		return false;
	}

	// Refresh legend only.
	public void refreshLegend() {
		legend.refresh();
	}

	// Move legend.
	public void moveLegend(Point legendLocation) {
		legend.setLocation(legendLocation);
		setVisible(true);
	}

	// Get legend location.
	public Point getLegendLocation() {
		return legend.getLocation();
	}

	// Move legend by indicated number of pixels.
	// But avoid overlap with main window.
	public void moveLegend(int xIncr, int yIncr, int xMin, int yMin) {
		int xNew = legend.getLocation().x + xIncr;
		int yNew = legend.getLocation().y + yIncr;
		if (xNew < xMin && yNew < yMin) {
			if (xMin-xNew < yMin-yNew)
				xNew = xMin;
			else
				yNew = yMin;
		}
		legend.setLocation(xNew, yNew);
	}

	// Show legend. 
	public void showLegend() {
		legend.toFront();
		legend.setAlwaysOnTop(true);
		legend.setAlwaysOnTop(false);
	}

	// Have values changed since last time.
	public boolean changed = true;

	// Make all nodes in subtree changed.
	public void makeAllChanged() {
		tSwitchs().makeAllChanged();
		if (tHiero() != null)
			tHiero().makeAllChanged();
		changed = true;
	}

	// Print again if needed. Return whether there were changes.
	public boolean printChanged() {
		boolean anyChanged = changed;
		anyChanged |= tSwitchs().printChanged();
		if (tHiero() != null)
			anyChanged |= tHiero().printChanged();
		if (anyChanged)
			panel.refresh();
		changed = false;
		return anyChanged;
	}

	// Refresh after edits.
	public void refresh() {
		propagate();
		connectNodes();
		printChanged();
		scrollToFocus();
		transferChange();
	}

	// Make buttons for parameters.
	public LegendParams makeParams() {
		LegendParams params = new LegendParams();
		params.format();
		return params;
	}

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
		buttons.addButtonRight(
				new StructureButton("!", '!') {
				protected void pushed() {
					prepareStructChange();
					tSwitchs().claimFocus();
					finishStructChange();
				} });
		buttons.format();
		return buttons;
	}

	// Insert group at beginning.
	private void prepend(TreeTopgroup newGroup) {
		insertAt(newGroup, 0);
		refresh();
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

	////////////////////////////////////////////////////////////
	// Editing.

	// Just before changing any parameters to a node.
	// Only save if not saved before.
	public void prepareParamChange() {
		if (!focusParamChanged) {
			focusParamChanged = true;
			prepareChange();
		}
	}

	// Change direction and size.
	public void changeDirectionSize(int direction, float size) {
		prepareChange();
		this.direction = direction;
		this.size = size;
		makeAllChanged();
		refresh();
		legend.refresh();
	}

	// Just before structural change,
	// save current state.
	public void prepareStructChange() {
		prepareChange();
	}
	// Just after structural change, save position.
	public void finishStructChange() {
		finishChange();
	}

	// Command coming from application.
	public void processCommand(char c) {
		legend.processCommand(c);
	}

	// In advance of edit, save state.
	public abstract void prepareChange();

	// After edit, save focus.
	public abstract void finishChange();

	// Export to user that there is change.
	public abstract void transferChange();

	// Export to user that there is change in group focus.
	public abstract void transferFocus();

	// To get glyph chooser if needed in Legend.
	public abstract GlyphChooser getGlyphChooserWindow();

	// Receive glyph for legend.
	public void receiveGlyph(String name) {
		legend.receiveGlyph(name);
	}

	///////////////////////////////////////////////
	// Ad hoc editing routines.

	// If focussed element is named glyph, and there
	// is next named glyph, then swap the two names.
	public void swap() {
		Vector<ResNamedglyph> glyphs = glyphs();
		for (int i = 0; i < glyphs.size(); i++) 
			if (glyphs.get(i) == focus && i < glyphs.size() - 1) {
				ResNamedglyph glyph1 = glyphs.get(i);
				ResNamedglyph glyph2 = glyphs.get(i+1);

				String name1 = glyph1.name;
				Boolean mirror1 = glyph1.mirror;
				int rotate1 = glyph1.rotate;
				float scale1 = glyph1.scale;
				float xscale1 =  glyph1.xscale;
				float yscale1 = glyph1.yscale;
				Color16 color1 = glyph1.color;
				Boolean shade1 =  glyph1.shade;
				Vector shades1 = glyph1.shades;
				Vector notes1 =  glyph1.notes;

				glyph1.name = glyph2.name;
				glyph1.rotate = glyph2.rotate;
				glyph1.scale = glyph2.scale;
				glyph1.xscale = glyph2.xscale;
				glyph1.yscale = glyph2.yscale;
				glyph1.color = glyph2.color;
				glyph1.shade = glyph2.shade;
				glyph1.shades = glyph2.shades;
				glyph1.notes = glyph2.notes;

				glyph2.name = name1;
				glyph2.mirror = mirror1;
				glyph2.rotate = rotate1;
				glyph2.scale = scale1;
				glyph2.xscale = xscale1;
				glyph2.yscale = yscale1;
				glyph2.color = color1;
				glyph2.shade = shade1;
				glyph2.shades = shades1;
				glyph2.notes = notes1;
			}
	}


}
