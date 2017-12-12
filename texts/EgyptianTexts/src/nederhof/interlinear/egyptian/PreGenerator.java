/***************************************************************************/
/*                                                                         */
/*  PreGenerator.java                                                      */
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

// Generates editing panel for pre part.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import nederhof.util.*;
import nederhof.interlinear.frame.*;

public class PreGenerator implements EditorComponentGenerator {

    // Make new panel, with fields for text and link.
    public Component makeComponent(ChangeListener listener) {
	return new PrePanel("", listener);
    }

    // As above, with given values.
    public Component makeComponent(Object object, ChangeListener listener) {
	String pre = (String) object;
	return new PrePanel(pre, listener);
    }

    // Extract value.
    public Object extract(Component comp) {
	PrePanel prePanel = (PrePanel) comp;
	String pre = prePanel.pre.getText();
	return pre;
    }

    // Panel with text and link.
    private class PrePanel extends JPanel implements DocumentListener {
	// Information carrying subpanel.
	public JTextField pre = new JTextField(4);
	// Labels to be unabled.
	private JLabel preLabel = new JLabel("Pre:");

	// External agent that listens to changes.
	private ChangeListener listener;

	// Construct.
	public PrePanel(String preString, 
		ChangeListener listener) {
	    this.listener = listener;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(Color.WHITE);
	    setBorder(new LineBorder(Color.BLUE, 2));
	    pre.setText(preString);
	    pre.getDocument().addDocumentListener(this);
	    add(preLabel);
	    add(pre);
	    setMaximumSize(getPreferredSize());
	    setAlignmentY(0.75f);
	}

	// Overrides supertype, to propagate within panel.
	public void setEnabled(boolean allow) {
	    super.setEnabled(allow);
	    pre.setEnabled(allow);
	    preLabel.setEnabled(allow);
	    if (allow)
		setBorder(new LineBorder(Color.BLUE, 2));
	    else
		setBorder(new LineBorder(Color.GRAY, 2));
	}

	// Notify user of changes.
	public void changedUpdate(DocumentEvent e) {
	    listener.stateChanged(new ChangeEvent(this));
	}
	public void insertUpdate(DocumentEvent e) {
	    listener.stateChanged(new ChangeEvent(this));
	}
	public void removeUpdate(DocumentEvent e) {
	    listener.stateChanged(new ChangeEvent(this));
	}

    }

}
