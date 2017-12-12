/***************************************************************************/
/*                                                                         */
/*  HiEditPopup.java                                                       */
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
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.fonts.*;
import nederhof.interlinear.frame.*;
import nederhof.res.editor.*;
import nederhof.util.*;

public class HiEditPopup extends PhraseEditPopup {

    public HiEditPopup() {
	addAction("hierogl<u>Y</u>phic", "hiero", KeyEvent.VK_Y);
	menu.addSeparator();
        addAction("<u>E</u>tc", "etc", KeyEvent.VK_E);
        addAction("c<u>O</u>ord", "coord", KeyEvent.VK_O);
    }

    // Disallow special characters.
    protected void addSpecialItems() {
    }

    // Add generator of components for hieroglyphic.
    public void setUsers(StyledTextPane text, EditChainElement parent) {
	super.setUsers(text, parent);
	text.addComponent("hiero", new HiGenerator(text, parent));
	text.addComponent("etc", new EtcGenerator());
	text.addComponent("coord", new CoordGenerator());
	text.addComponent("pos", new PosGenerator());
	text.enableTextInput(false);
	text.revalidate();
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("hiero")) {
	    text.insertFreshComponent("hiero");
	} else if (e.getActionCommand().equals("etc")) {
	    text.insertFreshComponent("etc");
	} else if (e.getActionCommand().equals("coord")) {
	    text.insertFreshComponent("coord");
	} 
    }

}
