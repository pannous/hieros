/***************************************************************************/
/*                                                                         */
/*  PhraseEditPopup.java                                                   */
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

// In text field, popup menu for edit actions.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import nederhof.util.*;

public class PhraseEditPopup extends MouseAdapter
                implements ActionListener, UndoableEditListener {

    // Text to which this refers.
    protected StyledTextPane text;

    // Parent editor.
    protected EditChainElement parent;

    // The menu.
    protected JPopupMenu menu = new JPopupMenu();

    // Items for undo.
    private JMenuItem undoItem;
    private JMenuItem redoItem;

    // Construct and make menu.
    public PhraseEditPopup() {
        undoItem = addAction("<u>U</u>ndo", "undo", KeyEvent.VK_U);
        redoItem = addAction("<u>R</u>edo", "redo", KeyEvent.VK_R);
        menu.addSeparator();
	addSpecialItems();
    }

    // Add items for making special characters.
    // Subclasses may override this to disallow special characters.
    protected void addSpecialItems() {
        JMenuItem mergeItem = addAction("make special", "merge", KeyEvent.VK_Z);
        JMenuItem showItem = addAction("show specials", "show", KeyEvent.VK_X);
        menu.addSeparator();
    }

    // Set user of this popup.
    public void setUsers(StyledTextPane text, EditChainElement parent) {
	this.text = text;
	this.parent = parent;
	text.getDocument().removeUndoableEditListener(text);
	text.getDocument().addUndoableEditListener(this);
	makeShortcuts();
	enableUndoRedo();
    }

    // Create entry in popup.
    protected EnabledMenuItem addAction(String label, String action, int key) {
        EnabledMenuItem item = new EnabledMenuItem(this, label, action, key);
        menu.add(item);
        return item;
    }

    // Create keyboard shortcuts.
    protected void makeShortcuts() {
        MenuElement[] elems = menu.getSubElements();
        for (int i = 0; i < elems.length; i++) {
            if (elems[i] instanceof JMenuItem) {
                JMenuItem elem = (JMenuItem) elems[i];
                String action = elem.getActionCommand();
                int key = elem.getMnemonic();
                text.registerKeyboardAction(this, action,
			GuiAux.shortcut(key),
                        JComponent.WHEN_FOCUSED);
            }
        }
    }

    // Process action.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("undo")) {
            text.undo();
            enableUndoRedo();
        } else if (e.getActionCommand().equals("redo")) {
            text.redo();
            enableUndoRedo();
        } else if (e.getActionCommand().equals("merge")) {
            text.combineCharacters();
        } else if (e.getActionCommand().equals("show")) {
            text.showSpecialMenu();
        }
    }

    // Show menu upon right click. Unix versus Windows.
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
             menu.show((Component) e.getSource(), e.getX(), e.getY());
    }
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
             menu.show((Component) e.getSource(), e.getX(), e.getY());
    }

    /////////////////////////////////////
    // Undo/redo.

    // Process undoable change to document.
    public void undoableEditHappened(UndoableEditEvent e) {
        text.undoableEditHappened(e);
        enableUndoRedo();
    }

    // Enable items.
    private void enableUndoRedo() {
        undoItem.setEnabled(text.canUndo());
        redoItem.setEnabled(text.canRedo());
    }

}
