/***************************************************************************/
/*                                                                         */
/*  NoteEditPopup.java                                                     */
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

// Popup for editing translation.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.fonts.*;
import nederhof.interlinear.frame.*;
import nederhof.res.editor.*;
import nederhof.util.*;

public class NoteEditPopup extends PhraseEditPopup {

    public NoteEditPopup() {
	addAction("<u>P</u>lain", "plain", KeyEvent.VK_P);
	addAction("<u>I</u>talic", "italic", KeyEvent.VK_I);
	addAction("<u>E</u>xternal link", "link", KeyEvent.VK_E);
	menu.addSeparator();
	addAction("<u>T</u>ranslit.", "translower", KeyEvent.VK_T);
	addAction("<u>B</u>ig translit.", "transupper", KeyEvent.VK_B);
	addAction("hierogl<u>Y</u>phic", "hiero", KeyEvent.VK_Y);
    }

    // Add generator of components for hieroglyphic.
    public void setUsers(StyledTextPane text, EditChainElement parent) {
	super.setUsers(text, parent);
	int style = Settings.translitFontStyle;
	float size = (float) Settings.translitFontSize;
	Font lower = TransHelper.translitLower(style, size);
	Font upper = TransHelper.translitUpper(style, size);
	text.addFont("TransLower", style, lower);
	text.addStyle("translower", "TransLower", style);
	text.addFont("TransUpper", style, upper);
	text.addStyle("transupper", "TransUpper", style);
	text.addComponent("hiero", new HiGenerator(text, parent));
	text.addComponent("link", new LinkGenerator());
	text.setDefaultStyle("plain");
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("plain")) {
	    text.makePlain();
	} else if (e.getActionCommand().equals("italic")) {
	    text.makeItalic();
	} else if (e.getActionCommand().equals("translower")) {
	    text.makeStyle("translower");
	} else if (e.getActionCommand().equals("transupper")) {
	    text.makeStyle("transupper");
	} else if (e.getActionCommand().equals("hiero")) {
	    text.insertFreshComponent("hiero");
	} else if (e.getActionCommand().equals("link")) {
	    text.insertFreshComponent("link");
	} 
    }

}
