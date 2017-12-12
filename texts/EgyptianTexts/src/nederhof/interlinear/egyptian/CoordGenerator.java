/***************************************************************************/
/*                                                                         */
/*  CoordGenerator.java                                                    */
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

// Generates editing panel for coordinate.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import nederhof.util.*;
import nederhof.interlinear.frame.*;

public class CoordGenerator implements EditorComponentGenerator {

    // Make new panel, with fields for text and link.
    public Component makeComponent(ChangeListener listener) {
	return new CoordPanel("", listener);
    }

    // As above, with given values.
    public Component makeComponent(Object object, ChangeListener listener) {
	String coord = (String) object;
	return new CoordPanel(coord, listener);
    }

    // Extract value.
    public Object extract(Component comp) {
	CoordPanel coordPanel = (CoordPanel) comp;
	String coord = coordPanel.coord.getText();
	return coord;
    }

    // Panel with text and link.
    private class CoordPanel extends JPanel implements DocumentListener {
	// Information carrying subpanel.
	public JTextField coord = new JTextField(4);
	// Labels to be unabled.
	private JLabel coordLabel = new JLabel("Coord:");

	// External agent that listens to changes.
	private ChangeListener listener;

	// Construct.
	public CoordPanel(String coordString, 
		ChangeListener listener) {
	    this.listener = listener;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(Color.WHITE);
	    setBorder(new LineBorder(Color.BLUE, 2));
	    coord.setText(coordString);
	    coord.getDocument().addDocumentListener(this);
	    add(coordLabel);
	    add(coord);
	    setMaximumSize(getPreferredSize());
	    setAlignmentY(0.75f);
	}

	// Overrides supertype, to propagate within panel.
	public void setEnabled(boolean allow) {
	    super.setEnabled(allow);
	    coord.setEnabled(allow);
	    coordLabel.setEnabled(allow);
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
