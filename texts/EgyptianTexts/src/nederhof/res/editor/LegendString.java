/***************************************************************************/
/*                                                                         */
/*  LegendString.java                                                      */
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

// Part of legend dealing with strings.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.util.*;

abstract class LegendString extends LegendParam implements DocumentListener  {

    // Old value.
    private String old;
    // Last returned value.
    private String lastReturned;

    // Containing name.
    private JTextField text;

    // To capture typed commands.
    private boolean captureKeys;

    // Make entry.
    public LegendString(String old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	text = new JTextField(10);
	text.getDocument().addDocumentListener(this);

	add(text);

	setValue(old);

	// Let initially capture keys.
	enterFocus();
    }

    // Set to value
    private void setValue(String name) {
	text.setText(name);
	if (name.equals("?")) {
	    text.getCaret().setSelectionVisible(true);
	    text.setCaretPosition(0);
	    text.moveCaretPosition(1);
	}
	text.getCaret().setVisible(true);
    }

    // Get value, provided it is valid. Otherwise "?".
    private String getValue() {
	if (!text.getText().equals(""))
	    return text.getText();
	else
	    return "?";
    }

    // Restore to blank.
    public void clear() {
	setValue("?");
	processMaybeChanged(getValue());
    }

    public void resetValue() {
	setValue(old);
	processMaybeChanged(getValue());
    }

    // Process command.
    public boolean processCommand(char code) {
	if (captureKeys) {
	    if (code == '\n') {
		leaveFocus();
		return true;
	    } else if (code == '\b') {
		String t = text.getText();
		if (!t.equals("")) {
		    if (text.getSelectedText() == null) {
			text.setSelectionStart(t.length()-1);
			text.setSelectionEnd(t.length());
		    }
		    text.replaceSelection("");
		    text.getCaret().setVisible(true);
		}
		return true;
	    } else if (CharacterAux.isPrintableChar(code)) {
		text.replaceSelection("" + code);
		text.getCaret().setVisible(true);
		return true;
	    }
	} else if (code == '\n') {
	    enterFocus();
	    return true;
	} 
	return false;
    }

    // Process changed string.
    public void changedUpdate(DocumentEvent e) {
	processMaybeChanged(getValue());
    }
    public void insertUpdate(DocumentEvent e) {
	processMaybeChanged(getValue());
    }
    public void removeUpdate(DocumentEvent e) {
	processMaybeChanged(getValue());
    }

    private void processMaybeChanged(String other) {
        if (!other.equals(lastReturned)) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(String val);

    // Make focus.
    public void enterFocus() {
	captureKeys = true;
        text.setBorder(new LineBorder(Color.BLUE, 2));
	if (text.getText().equals("?")) {
	    text.getCaret().setSelectionVisible(true);
	    text.setCaretPosition(0);
	    text.moveCaretPosition(1);
	}
	text.getCaret().setVisible(true);
    }

    // Has focus.
    public void leaveFocus() {
	captureKeys = false;
        text.setBorder(new LineBorder(Color.BLACK, 1));
	text.getCaret().setVisible(false);
    }

    // Hack to avoid resource leak.
    // The blinking caret when component has no focus causes trouble.
    public void removeNotify() {
        super.removeNotify();
        text.getCaret().setVisible(false);
    }

}
