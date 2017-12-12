/***************************************************************************/
/*                                                                         */
/*  HieroButton.java                                                       */
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

// Button with hieroglyphic, allowing editor.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.alignment.generic.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;

public abstract class HieroButton extends JButton implements ActionListener {

    // All operations on RES and REScode assume the following context.
    private static final HieroRenderContext hieroContext =
        new HieroRenderContext(Settings.textHieroFontSize);

    // Parsing context.
    private static final ParsingContext parsingContext =
        new ParsingContext(hieroContext, true);

    // Margin above and below hieroglyphic.
    private int marginHor = 3;
    // Margin before and after hieroglyphic.
    private int marginVert = 2;
    // Formatted RES.
    private FormatFragment frag;
    // Dimensions of hieroglyphic.
    private int width;
    private int height;

    // The positions and notes attached to the hieroglyphic, if any.
    // If none, then this is null.
    private Vector posses = null;
    private Vector notes = null;

    // External agent that listens to changes.
    // Is null if RES is not editable.
    private ChangeListener listener = null;

    // Basic constructor.
    public HieroButton(String res, ChangeListener listener) {
	this.listener = listener;
	setBackground(Color.WHITE);
	setActionCommand("edit");
	addActionListener(this);
	setAlignmentY(0.75f);

	setRes(res);
    }

    // Constructor allowing editing of footnotes.
    public HieroButton(String res, Vector posses, Vector notes, ChangeListener listener) {
	this.listener = listener;
	this.posses = posses;
	this.notes = notes;
	setBackground(Color.WHITE);
	setAlignmentY(0.75f);
	addMouseListener(new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		if (!isEnabled())
		    return;
		if (e.getButton() == MouseEvent.BUTTON1 &&
			!e.isControlDown())
		    createResEditor();
		else if (e.getButton() == MouseEvent.BUTTON3 ||
			e.isControlDown())
		    createFootnoteEditor();
	    }
	});
	setRes(res);
    }

    // Constructor without listener.
    public HieroButton(String res) {
	setBackground(Color.WHITE);
	setBorder(new EmptyBorder(0,0,0,0));
	setAlignmentY(0.75f);
	setRes(res);
    }

    // Paint hieroglyphic if there is any.
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;
	frag.write(g2, marginVert, marginHor);
    }

    // Size.
    public Dimension getPreferredSize() {
	if (frag.nGroups() > 0)
	    return new Dimension(width, height);
	else
	    return super.getPreferredSize();
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }
    public Dimension getMaximumSize() {
	return getPreferredSize();
    }

    // Upon click, edit contents.
    public void actionPerformed(ActionEvent e) {
	createResEditor();
    }

    // Embedded editor of hieroglyphic.
    private void createResEditor() {
	startWait();
	stopEditing();
	new FragmentEditor(getRes(), true, false,
		Settings.embeddedPreviewHieroFontSize,
		Settings.embeddedTreeHieroFontSize) {
	    protected void receive(String out) {
		setRes(out);
		if (listener != null)
		    listener.stateChanged(new ChangeEvent(this));
		resumeEditing();
		requestFocus();
	    }
	    protected void cancel() {
		resumeEditing();
		requestFocus();
	    }
	    protected void error(int pos) {
		resumeEditing();
		requestFocus();
	    }
	};
	endWait();
    }

    // Embedded editor of footnotes of hieroglyphic.
    private void createFootnoteEditor() {
	stopEditing();
	new HieroNoteEditor(frag, hieroContext, notes) {
	    protected void receive(Vector newNotes) {
		notes = newNotes;
		if (listener != null)
		    listener.stateChanged(new ChangeEvent(this));
		resumeEditing();
		requestFocus();
	    }
	    protected void cancel() {
		resumeEditing();
		requestFocus();
	    }
	};
    }

    // Get formatted hieroglyph.
    private void setRes(String res) {
	ResFragment parsed = ResFragment.parse(res, parsingContext);
	updatePositions(frag, parsed);
	frag = new FormatFragment(parsed, hieroContext);
	if (frag.nGroups() > 0) {
	    setText("");
	    width = frag.width() + 2 * marginVert;
	    height = frag.height() + 2 * marginHor;
	} else {
	    if (listener != null)
		setText("EDIT");
	    else
		setText("EMPTY HIEROGLYPHIC");
	    width = 10; // should be ignored
	    height = 10; // should be ignored
	}
	revalidate();
    }

    // Translate positions before and after edit.
    // Perhaps there is no "before", so is null.
    private void updatePositions(ResFragment before, ResFragment after) {
	if (before != null && (posses != null || notes != null)) {
	    Vector glyphsBefore = before.glyphs();
	    Vector glyphsAfter = after.glyphs();
	    MinimumEditUpdater updater = new MinimumEditUpdater(glyphsBefore, glyphsAfter) {
		protected short distance(Object o1, Object o2) {
		    ResNamedglyph named1 = (ResNamedglyph) o1;
		    ResNamedglyph named2 = (ResNamedglyph) o2;
		    String name1 = hieroContext.nameToGardiner(named1.name);
		    String name2 = hieroContext.nameToGardiner(named2.name);
		    if (name1.equals(name2))
			return 0;
		    else
			return 1;
		}
	    };
	    if (posses != null)
		for (int i = 0; i < posses.size(); i++) {
		    PosPart pos = (PosPart) posses.get(i);
		    int newSym = updater.map(pos.symbol);
		    if (newSym >= 0)
			pos.symbol = newSym;
		}
	    if (notes != null)
		for (int i = 0; i < notes.size(); i++) {
		    NotePart note = (NotePart) notes.get(i);
		    int newSym = updater.map(note.symbol());
		    if (newSym >= 0)
			notes.set(i, new NotePart(note.text(), newSym));
		}
	}
    }

    // Get RES as string.
    public String getRes() {
	return frag.toString();
    }

    // Get positions.
    public Vector getPosses() {
	return posses;
    }

    // Get notes.
    public Vector getNotes() {
	return notes;
    }

    //////////////////////////////////////////////////
    // Communicate with user.

    // Request waiting.
    protected abstract void startWait();
    // End waiting.
    protected abstract void endWait();
    // User should request focus.
    protected abstract void takeFocus();

    // User should stop editing.
    protected abstract void stopEditing();
    // User should resume editing.
    protected abstract void resumeEditing();

}
