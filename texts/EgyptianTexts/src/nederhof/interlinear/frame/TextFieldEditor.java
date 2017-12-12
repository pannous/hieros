/***************************************************************************/
/*                                                                         */
/*  TextFieldEditor.java                                                   */
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

// Element used to edit a property of a resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.interlinear.*;
import nederhof.util.*;

public class TextFieldEditor extends NamedPropertyEditor 
		implements DocumentListener {

    // The element holding the value.
    private JTextField field;

    // Field embedded in name and comment.
    public TextFieldEditor(TextResource resource, String name, 
	    int nameWidth, String comment, int length) {
	super(resource, name);

	field = new SimpleTextField(length);

	JLabel nameLabel = new BoldLabel(name + " ");
	nameLabel.setPreferredSize(new Dimension(nameWidth,
		    nameLabel.getPreferredSize().height));
	nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

	JLabel commentLabel = new PlainLabel(comment);

	add(panelSep());
	add(nameLabel);
	add(field);
	add(panelSep());
	add(commentLabel);
	add(panelGlue());

	textElements.add(nameLabel);
	textElements.add(field);
	textElements.add(commentLabel);
    }
    // With default length.
    public TextFieldEditor(TextResource resource,
	    String name, int nameWidth, String comment) {
	this(resource, name, nameWidth, comment, 15);
    }

    // Text field.
    private class SimpleTextField extends SpecialTextField {
	public SimpleTextField(int length) {
	    super(length);
	    setMaximumSize(getPreferredSize());
	    setFont(inputTextFont());
	    getDocument().addDocumentListener(TextFieldEditor.this);
	}
    }

    ////////////////////////////////////////
    // Values.

    // Put string in text.
    public void putValue(Object val) {
	String text = "";
	if (val != null) 
	    text = (String) val;
	field.setText(text);
	changed = false;
    }

    // Current string.
    public Object retrieveValue() {
	return field.getText();
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

}
