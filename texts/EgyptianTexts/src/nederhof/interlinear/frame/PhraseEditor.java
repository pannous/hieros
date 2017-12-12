/***************************************************************************/
/*                                                                         */
/*  PhraseEditor.java                                                      */
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

// A panel bundeling a number of editors for tiers.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.*;

public class PhraseEditor extends JPanel {

    // The parent of editing.
    protected EditChainElement parent;

    // The resource being edited.
    private TextResource resource;

    // The number of the phrase to be replaced.
    // If none, then negative.
    private int oldPhraseNum;

    // The number of phrase, before which this is to be put.
    private int beforePhraseNum;

    // The property editors.
    protected Vector editors;

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    // Construct from vector of editors.
    public PhraseEditor(TextResource resource, 
	    int oldPhraseNum, int beforePhraseNum,
	    Vector editors, EditChainElement parent) {
	this.resource = resource;
	this.oldPhraseNum = oldPhraseNum;
	this.beforePhraseNum = beforePhraseNum;
	this.editors = editors;
	this.parent = parent;
	panelElements.add(this);
	for (int i = 0; i < editors.size(); i++)
	    editor(i).setParent(parent);
	layoutEditors();
	setEnabled(true);
    }

    // Get i-th editor.
    private PhraseTierEditor editor(int i) {
        return (PhraseTierEditor) editors.get(i);
    }

    // One editor under the other.
    protected void layoutEditors() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(sep());
        for (int i = 0; i < editors.size(); i++) {
            add(editor(i));
            add(sep());
        }
    }

    // Go down to text level to redo layout.
    public void refreshLayout() {
	for (int i = 0; i < editors.size(); i++)
	    editor(i).refreshLayout();
    }

    ////////////////////////////////////////////
    // Value manipulation.

    // Has anything been changed?
    public boolean isChanged() {
        boolean changed = false;
        for (int i = 0; i < editors.size(); i++)
            changed = changed || editor(i).isChanged();
        return changed;
    }

    // Save edited phrase. Return whether changed.
    public boolean save() {
	try {
	    if (!isChanged())
		return false;
	    TextPhrase phrase = resource.emptyPhrase();
	    if (oldPhraseNum >= 0)
		resource.removePhrase(oldPhraseNum);
	    for (int i = 0; i < editors.size(); i++) {
		int tierNum = editor(i).getTierNum();
		Vector parts = editor(i).getValue();
		Vector tier = phrase.getTier(tierNum);
		tier.addAll(parts);
	    }
	    resource.insertPhrase(phrase, beforePhraseNum);
	    resource.makeModified();
	    resource.save();
	} catch (IOException exc) {
	    JOptionPane.showMessageDialog(this,
		    "Could not save resource:\n" + exc.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	return true;
    }

    ////////////////////////////////////////////
    // Appearance.

    // Allow editing. (I.e. not blocked due to other edit.)
    public void setEnabled(boolean allow) {
        for (int i = 0; i < panelElements.size(); i++) {
            JComponent comp = (JComponent) panelElements.get(i);
            comp.setBackground(backColor(allow));
        }
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(allow);
        }
        for (int i = 0; i < editors.size(); i++)
            editor(i).setEnabled(allow);
        repaint();
    }

    // Color may depend on allowed editing.
    protected Color backColor(boolean editable) {
        return Color.WHITE;
    }

    //////////////////////////////
    // Auxiliaries.

    // Horizontal glue.
    private Component horGlue() {
        return Box.createHorizontalGlue();
    }
    // Some separation between panels.
    private Component sep() {
        return Box.createRigidArea(new Dimension(10, 10));
    }

}
