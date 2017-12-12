/***************************************************************************/
/*                                                                         */
/*  AlEditPopup.java                                                       */
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

// Popup for editing transliteration.

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

public class AlEditPopup extends PhraseEditPopup {

    public AlEditPopup() {
	addAction("<u>T</u>ranslit.", "translower", KeyEvent.VK_T);
	addAction("<u>B</u>ig translit.", "transupper", KeyEvent.VK_B);
	addAction("<u>P</u>lain", "plain", KeyEvent.VK_P);
	addAction("<u>N</u>ote", "note", KeyEvent.VK_N);
	menu.addSeparator();
	addAction("<u>E</u>tc", "etc", KeyEvent.VK_E);
	addAction("c<u>O</u>ord", "coord", KeyEvent.VK_O);
	addAction("pre", "pre", KeyEvent.VK_COMMA);
	addAction("post", "post", KeyEvent.VK_PERIOD);
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
        text.addComponent("note", new NoteGenerator(text, parent));
        text.addComponent("etc", new EtcGenerator());
        text.addComponent("coord", new CoordGenerator());
        text.addComponent("pre", new PreGenerator());
        text.addComponent("post", new PostGenerator());
        text.addComponent("pos", new PosGenerator());
	text.setDefaultStyle("translower");
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("plain")) {
	    text.makePlain();
	} else if (e.getActionCommand().equals("translower")) {
	    text.makeStyle("translower");
	} else if (e.getActionCommand().equals("transupper")) {
	    text.makeStyle("transupper");
        } else if (e.getActionCommand().equals("note")) {
            text.insertFreshComponent("note");
        } else if (e.getActionCommand().equals("etc")) {
            text.insertFreshComponent("etc");
        } else if (e.getActionCommand().equals("coord")) {
            text.insertFreshComponent("coord");
        } else if (e.getActionCommand().equals("pre")) {
            text.insertFreshComponent("pre");
        } else if (e.getActionCommand().equals("post")) {
            text.insertFreshComponent("post");
	} 
    }

}
