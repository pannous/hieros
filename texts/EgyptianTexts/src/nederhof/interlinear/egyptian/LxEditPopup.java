/***************************************************************************/
/*                                                                         */
/*  LxEditPopup.java                                                       */
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

public class LxEditPopup extends PhraseEditPopup {

    public LxEditPopup() {
        addAction("<u>L</u>exical", "lx", KeyEvent.VK_L);
        menu.addSeparator();
        addAction("<u>E</u>tc", "etc", KeyEvent.VK_E);
        addAction("c<u>O</u>ord", "coord", KeyEvent.VK_O);
        addAction("pre", "pre", KeyEvent.VK_COMMA);
        addAction("post", "post", KeyEvent.VK_PERIOD);
    }

    // Disallow special characters.
    protected void addSpecialItems() {
    }

    // Add generator of components for hieroglyphic.
    public void setUsers(StyledTextPane text, EditChainElement parent) {
	super.setUsers(text, parent);
	text.addComponent("lx", new LxGenerator(text, parent));
	text.addComponent("etc", new EtcGenerator());
	text.addComponent("coord", new CoordGenerator());
	text.addComponent("pre", new PreGenerator());
	text.addComponent("post", new PostGenerator());
	text.enableTextInput(false);
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("lx")) {
	    text.insertFreshComponent("lx");
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
