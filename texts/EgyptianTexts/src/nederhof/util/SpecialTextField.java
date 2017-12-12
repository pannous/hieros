/***************************************************************************/
/*                                                                         */
/*  SpecialTextField.java                                                  */
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

// Text field allowing special characters.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;

public class SpecialTextField extends JTextField 
	implements ActionListener {

    // Whether full window to be shown or excerpt in pop-up menu.
    private final boolean full = true;

    // Window showing mapping to special characters.
    private HTMLWindow mappingWindow = null;

    // Pops up menu of special characters.
    protected JPopupMenu menu = new JPopupMenu();

    public SpecialTextField(int size) {
	super(size);
	registerKeyboardAction(this, "merge", 
		GuiAux.shortcut(KeyEvent.VK_Z),
		JComponent.WHEN_FOCUSED);
	registerKeyboardAction(this, "show", 
		GuiAux.shortcut(KeyEvent.VK_X),
		JComponent.WHEN_FOCUSED);
	if (!full) {
	    for (int i = 0; i < CharAux.exampleSpecialMapping.length; i += 2) {
		JMenuItem item = new SpecialItem(CharAux.exampleSpecialMapping[i], 
			CharAux.exampleSpecialMapping[i+1]);
		menu.add(item);
	    }
	    JMenuItem etc = new JMenuItem("<html><i>etc.</i></html>"); // ineffective
	    menu.add(etc);
	}
    }

    // Only used to get handle on resources in directory.
    private SpecialTextField() {
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("merge")) 
	    combineCharacters();
	else if (e.getActionCommand().equals("show")) {
	    if (full)
		showMenu();
	    else {
		Point point = getCaret().getMagicCaretPosition();
		menu.show((Component) e.getSource(), point.x, point.y);
	    }
	} else 
	    for (int i = 0; i < CharAux.specialMapping.length; i += 2) 
		if (e.getActionCommand().equals(CharAux.specialMapping[i]))
		    replaceSelection(CharAux.specialMapping[i+1]);
    }

    // Combine two previous characters.
    private void combineCharacters() {
	int pos = getCaretPosition();
	if (0 <= pos-2) {
	    setCaretPosition(pos-2);
	    moveCaretPosition(pos);
	    String selected = getSelectedText();
	    for (int i = 0; i < CharAux.specialMapping.length; i += 2)
		if (selected.equals(CharAux.specialMapping[i]))
		    replaceSelection(CharAux.specialMapping[i+1]);
	}
    }

    // Item for one character combination.
    private class SpecialItem extends JMenuItem {
	public SpecialItem(String from, String to) {
	    setText(from + " \u00BB " + to);
	    setActionCommand(from);
	    addActionListener(SpecialTextField.this);
	}
    }

    // Show window with table of mapping.
    private void showMenu() {
	if (mappingWindow == null) {
	    URL url = FileAux.fromBase("data/help/util/specialchars.html");
	    mappingWindow = new HTMLWindow("Special characters", url);
	}
	mappingWindow.setVisible(true);
    }

    // If removed, remove extra window.
    public void removeNotify() {
	super.removeNotify();
	if (mappingWindow != null) {
	    mappingWindow.dispose();
	    mappingWindow = null;
	}
    }

}
