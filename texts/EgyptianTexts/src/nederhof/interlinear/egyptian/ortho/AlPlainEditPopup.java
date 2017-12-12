// Popup for editing transliteration.

package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.fonts.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.editor.*;
import nederhof.util.*;

public class AlPlainEditPopup extends PhraseEditPopup {

    public AlPlainEditPopup() {
	addAction("<u>T</u>ranslit.", "translower", KeyEvent.VK_T);
	addAction("<u>B</u>ig translit.", "transupper", KeyEvent.VK_B);
    }

    // No special characters.
    protected void addSpecialItems() {
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
	text.setDefaultStyle("translower");
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	if (e.getActionCommand().equals("translower")) {
	    text.makeStyle("translower");
	} else if (e.getActionCommand().equals("transupper")) {
	    text.makeStyle("transupper");
	} 
    }

}
