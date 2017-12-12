/***************************************************************************/
/*                                                                         */
/*  StyledPhraseEditor.java                                                */
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

// Editing tier in phrase as text.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.util.*;

public abstract class StyledPhraseEditor extends PhraseTierEditor 
	implements DocumentListener {

    // Styled text.
    protected StyledTextPane styledPane;

    // Popup in pane.
    private PhraseEditPopup popup;

    // Title around border.
    private TitledBorder title;

    // Initial value.
    private Vector parts;

    // Construct.
    public StyledPhraseEditor(PhraseEditPopup popup, int tierNum, String name, Vector parts) {
	super(tierNum);
        this.popup = popup;
	this.parts = parts;

        title = BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY, 2), name);
        setBorder(
                BorderFactory.createCompoundBorder(title,
                    BorderFactory.createEmptyBorder(0,5,5,5)));
    }

    // Creation of components delayed until parent is known.
    public void setParent(EditChainElement parent) {
	super.setParent(parent);
        styledPane = new StyledTextPane(Settings.inputTextFontName,
                Settings.inputTextFontSize) {
            public void stateChanged(ChangeEvent e) {
                changed = true;
		makeChanged();
            }
        };
        styledPane.getDocument().addDocumentListener(StyledPhraseEditor.this);
	if (popup != null) {
	    popup.setUsers(styledPane, parent);
	    styledPane.addMouseListener(popup);
	}
	add(styledPane);
	putValue(parts);

	panelElements.add(this);
	panelElements.add(styledPane);
    }

    // Refresh layout, via text.
    public void refreshLayout() {
	styledPane.revalidate();
    }

    ////////////////////////////////////////
    // Values.

    // Turn into elements that styled editor can use.
    // To be defined by subclass.
    protected abstract Vector toEditParts(Vector parts);
    // Convert back,
    protected abstract Vector fromEditParts(Vector parts);

    // Put string in text. The parts form a single paragraph.
    public void putValue(Vector parts) {
        Vector pars = new Vector();
        pars.add(toEditParts(parts));
        styledPane.setParagraphs(pars);
        changed = false;
    }

    // Retrieve value. These are paragraphs. The parts have to be merged.
    public Vector getValue() {
	Vector pars = styledPane.extractParagraphs();
	Vector parts = new Vector();
	for (int i = 0; i < pars.size(); i++) {
	    Vector par = (Vector) pars.get(i);
	    parts.addAll(par);
	}
	return fromEditParts(parts);
    }

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
        changed = true;
	makeChanged();
    }
    public void insertUpdate(DocumentEvent e) {
        changed = true;
	makeChanged();
    }
    public void removeUpdate(DocumentEvent e) {
        changed = true;
	makeChanged();
    }

    /////////////////////////////////////////////
    // Appearance.

    // Make border title gray if not editing.
    public void setEnabled(boolean allow) {
	super.setEnabled(allow);
        title.setTitleColor(allow ? Color.BLACK : Color.GRAY);
        styledPane.setEnabled(allow);
    }

    // Backgroup color.
    private Color backColor() {
        return Color.WHITE;
    }

}
