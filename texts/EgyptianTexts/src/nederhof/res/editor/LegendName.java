/***************************************************************************/
/*                                                                         */
/*  LegendName.java                                                        */
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

// Part of legend dealing with glyph names.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

abstract class LegendName extends LegendParam 
	implements ActionListener, DocumentListener {

    // Old value.
    public String old;
    // Last value returned.
    public String lastReturned;

    // Containing name.
    private JTextField text;

    // To capture typed commands.
    private boolean captureKeys;

    // Button for choosing name.
    private JButton choice; 

    // Make entry.
    public LegendName(String old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	text = new JTextField(10);
	text.getDocument().addDocumentListener(this);

	add(text);

	choice = new JButton("<html>men<u>u</u></html>");
	choice.setMinimumSize(getPreferredSize());
	choice.setFocusable(false);
	choice.addActionListener(this);
	add(choice);

	setValue(old);

	// Let initially capture keys.
	enterFocus(); 
    }

    // Set to value
    private void setValue(String name) {
	text.setText(name);
	if (name.equals("\"?\"")) {
	    text.getCaret().setSelectionVisible(true);
	    text.setCaretPosition(0);
	    text.moveCaretPosition(3);
	} 
	text.getCaret().setVisible(true);
    }

    // Get value, provided it is valid. Otherwise "?".
    private String getValue() {
	if (isValidName())
	    return text.getText();
	else
	    return "\"?\"";
    }

    // Clear to be ignored. (No default name.)
    public void clear() {
	// ignore
    }

    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Process command.
    public boolean processCommand(char code) {
	if (code == 'u') {
	    enterFocus();
	    getGlyphChooserWindow().setVisible(true);
	    return true;
	} else if (captureKeys) {
	    if (code == '\n')  {
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
	    } else if (isNamePart(code)) {
		text.replaceSelection("" + code);
		text.getCaret().setVisible(true);
		return true;
	    } else
		return false;
	} else if (code == '\n') {
	    enterFocus();
	    return true;
	} else
	    return false;

    }

    // Set to value from glyph chooser.
    public boolean receiveGlyph(String name) {
	text.setText(name);
	return true;
    }

    public static final Pattern gardinerPat = 
	Pattern.compile("^([A-I]|[K-Z]|Aa|NL|NU)([0-9]+)([a-z]?)$");

    public static final Pattern mnemonicPat =
	Pattern.compile("^([A-Za-z]+|10|100)$");

    public static final Pattern shortStringPat =
	Pattern.compile("^\\\"([^\\t\\n\\r\\f\\\"\\\\]|\\\\\\\"|\\\\\\\\)\\\"$");

    public static final Pattern shortStringPatPrefix =
	Pattern.compile(
		"^\\\"$|" + // only "
		"^\\\"[^\\t\\n\\r\\f\\\"\\\\](\\\")?$|" + // e.g. "a or "a"
		"^\\\"\\\\$|" + // only "\ 
		"^\\\"\\\\\\\"(\\\")?$|" + // "\" or "\""
		"^\\\"\\\\\\\\(\\\")?$"); // "\\ or "\\"

    public boolean isValidName() {
	Matcher gardinerMatch = gardinerPat.matcher(text.getText());
	Matcher mnemonicMatch = mnemonicPat.matcher(text.getText());
	Matcher shortStringMatch = shortStringPat.matcher(text.getText());
	return gardinerMatch.find() || 
	    mnemonicMatch.find() ||
	    shortStringMatch.find();
    }

    // Process pushed button.
    public void actionPerformed(ActionEvent e) {
        getGlyphChooserWindow().setVisible(true);
    }

    // Get glyph chooser. 
    protected abstract GlyphChooser getGlyphChooserWindow();

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

    // Is possible part of name.
    public boolean isNamePart(char code) {
	String prefix = text.getSelectedText() == null ? text.getText() : "";
	if (prefix.length() > 0 && prefix.charAt(0) == '\"')
	    return shortStringPatPrefix.matcher(prefix + code).find();
	else
	    return 
		(prefix.length() == 0 && code == '\"') ||
		'0' <= code && code <= '9' ||
		'a' <= code && code <= 'z' ||
		'A' <= code && code <= 'Z';
    }

    // Make focus.
    public void enterFocus() {
	captureKeys = true;
	text.setBorder(new LineBorder(Color.BLUE, 2));
	if (text.getText().equals("\"?\"")) {
	    text.getCaret().setSelectionVisible(true);
	    text.setCaretPosition(0);
	    text.moveCaretPosition(3);
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
