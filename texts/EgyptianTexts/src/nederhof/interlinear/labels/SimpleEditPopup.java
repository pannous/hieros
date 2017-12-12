/***************************************************************************/
/*                                                                         */
/*  SimpleEditPopup.java                                                   */
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

// Simple popup for edit operations in StyledTextPane.

package nederhof.interlinear.labels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public class SimpleEditPopup extends EditPopup {

    // Add a few extra items to menu.
    public SimpleEditPopup() {
	addAction("<u>P</u>lain", "plain", KeyEvent.VK_P);
	addAction("<u>I</u>talic", "italic", KeyEvent.VK_I);
	addAction("<u>L</u>ist item", "item", KeyEvent.VK_L);
	addAction("<u>E</u>xternal link", "link", KeyEvent.VK_E);
    }

    // Add generator of hyper links.
    public void setUsers(StyledTextPane text, EditChainElement parent) {
	super.setUsers(text, parent);
	text.addComponent("link", new LinkGenerator());
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("plain")) {
	    text.makePlain();
	} else if (e.getActionCommand().equals("italic")) {
	    text.makeItalic();
	} else if (e.getActionCommand().equals("item")) {
	    text.makeItem();
	} else if (e.getActionCommand().equals("link")) {
	    text.insertFreshComponent("link");
	}
    }

}
