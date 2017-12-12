/***************************************************************************/
/*                                                                         */
/*  PropertiesEditor.java                                                  */
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

// A panel bundeling a number of property editors.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.*;

public class PropertiesEditor extends JPanel {

    // The resource being edited.
    private TextResource resource;

    // The property editors.
    protected Vector editors;

    // The parent.
    private EditChainElement parent;

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    // Construct from vector of property editors.
    public PropertiesEditor(TextResource resource, Vector editors, EditChainElement parent) {
	this.resource = resource;
	this.editors = (Vector) editors.clone();
	this.parent = parent;
	panelElements.add(this);
	for (int i = 0; i < editors.size(); i++) 
	    editor(i).setParent(parent);
	layoutEditors();
    }

    // Get i-th editor.
    private PropertyEditor editor(int i) {
	return (PropertyEditor) editors.get(i);
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

    // Save edited resource. Return whether globally important
    // values have changed.
    public boolean save() {
	boolean globalChange = getChanged();
	try {
	    resource.save();
	} catch (IOException exc) {
	    JOptionPane.showMessageDialog(this,
		    "Could not save resource:\n" + exc.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	return globalChange;
    }

    // Ask user whether loss of data is intended.
    private boolean userConfirmsLoss() {
	Object[] options = {"proceed", "cancel"};
	int answer = JOptionPane.showOptionDialog(this,
		"Do you want to proceed and discard the edits?",
		"warning: impending loss of data",
		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		null, options, options[1]);
	return answer == 0;
    }

    ////////////////////////////////////////////
    // Value manipulation.

    // Initialize values to current.
    public void initValues() {
	for (int i = 0; i < editors.size(); i++) 
	    editor(i).initValue();
    }

    // Has any property been changed?
    public boolean isChanged() {
	boolean changed = false;
	for (int i = 0; i < editors.size(); i++) 
	    changed = changed || editor(i).isChanged();
	return changed;
    }

    // Save value.
    public void saveValues() {
	for (int i = 0; i < editors.size(); i++) 
	    editor(i).saveValue();
    }

    // Save values that were changed.
    // Return whether any editor represents change to be
    // propagated to text.
    public boolean getChanged() {
	boolean globalChange = false;
	for (int i = 0; i < editors.size(); i++) {
	    if (editor(i).isChanged()) {
		editor(i).saveValue();
		if (editor(i).isGlobal())
		    globalChange = true;
	    }
	}
	return globalChange;
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
