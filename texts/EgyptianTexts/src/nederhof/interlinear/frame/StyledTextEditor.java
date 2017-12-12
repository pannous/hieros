/***************************************************************************/
/*                                                                         */
/*  StyledTextEditor.java                                                  */
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

// Editing styled text.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.interlinear.*;
import nederhof.util.*;

public class StyledTextEditor extends NamedPropertyEditor
	implements DocumentListener {

    // Styled text.
    private StyledTextPane styledPane;

    // Popup in pane.
    private EditPopup popup;

    // Title around border.
    private TitledBorder title;

    // Construct.
    public StyledTextEditor(TextResource resource, String name,
	    EditPopup popup) {
	super(resource, name);
	this.popup = popup;

	title = BorderFactory.createTitledBorder(
		new LineBorder(Color.GRAY, 2), titleName(name));
	setBorder(
		BorderFactory.createCompoundBorder(title,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
    }
    // Without popup.
    public StyledTextEditor(TextResource resource, String name) {
	this(resource, name, null);
    }

    // Creation of components delayed until parent is known.
    public void setParent(EditChainElement parent) {
	super.setParent(parent);
	styledPane = new StyledTextPane(Settings.inputTextFontName,
		Settings.inputTextFontSize) {
	    public void stateChanged(ChangeEvent e) {
		changed = true;
	    }
	};
	styledPane.getDocument().addDocumentListener(StyledTextEditor.this);
	if (popup != null) {
	    popup.setUsers(styledPane, parent);
	    styledPane.addMouseListener(popup);
	}

	add(styledPane);

        panelElements.add(this);
        panelElements.add(styledPane);
    }

    // Name in title, given name of property.
    protected String titleName(String property) {
        return property;
    }

    ////////////////////////////////////////
    // Values.

    // Put string in text.
    public void putValue(Object val) {
	Vector pars = val == null ? new Vector() : (Vector) val;
	styledPane.setParagraphs(pars);
	changed = false;
    }

    // Retrieve value.
    public Object retrieveValue() {
	return styledPane.extractParagraphs();
    }

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
        changed = true;
    }
    public void insertUpdate(DocumentEvent e) {
        changed = true;
    }
    public void removeUpdate(DocumentEvent e) {
        changed = true;
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
