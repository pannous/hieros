/***************************************************************************/
/*                                                                         */
/*  DirectionSizePanel.java                                                */
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

// Element of panel for editor, allowing selection of direction and size
// of a fragment.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.ResValues;

abstract class DirectionSizePanel extends JPanel
	implements ChangeListener, ActionListener, ItemListener {

    // Direction.
    private int direction;

    // Components for direction.
    private JPanel directionPanel = new JPanel();
    private JButton noDirButton = new JButton("no direction");
    private JButton hlrButton = new JButton("hlr");
    private JButton hrlButton = new JButton("hrl");
    private JButton vlrButton = new JButton("vlr");
    private JButton vrlButton = new JButton("vrl");

    // Components for size.
    private JPanel sizePanel = new JPanel();
    private JCheckBox sizeCheck = new JCheckBox();
    private SizeModel sizeModel = new SizeModel();
    private JSpinner sizeSpinner = new JSpinner(sizeModel);

    // Make panel for direction and size.
    public DirectionSizePanel(int direction, float size) {
	setLayout(new BorderLayout());

	noDirButton.addActionListener(this);
	add(noDirButton, BorderLayout.NORTH);

	directionPanel.setLayout(new GridLayout(2,2));
	hlrButton.addActionListener(this);
	hrlButton.addActionListener(this);
	vlrButton.addActionListener(this);
	vrlButton.addActionListener(this);
	directionPanel.add(hlrButton);
	directionPanel.add(hrlButton);
	directionPanel.add(vlrButton);
	directionPanel.add(vrlButton);
	add(directionPanel, BorderLayout.CENTER);
	setDirection(direction);

	sizeCheck.setSelected(false);
	sizeCheck.addItemListener(this);
	sizePanel.add(sizeCheck);
	sizeSpinner.setEditor(new JSpinner.NumberEditor(sizeSpinner));
	sizeSpinner.setEnabled(false);
	sizePanel.add(sizeSpinner);
	add(sizePanel, BorderLayout.SOUTH);
	setUnitSize(size);
	sizeSpinner.addChangeListener(this);

	noDirButton.setFocusable(false);
	hlrButton.setFocusable(false);
	hrlButton.setFocusable(false);
	vlrButton.setFocusable(false);
	vrlButton.setFocusable(false);
	sizeCheck.setFocusable(false);
	sizeSpinner.setFocusable(false);
    }
    
    // Make no bigger than necessary.
    public Dimension getMaximumSize() {
	return getMinimumSize();
    }
    public Dimension getPreferredSize() {
	return getMinimumSize();
    }

    // Record that button for direction has been pushed,
    // or button for hiding/showing of size.
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if (command.equals("no direction")) {
	    setDirection(ResValues.DIR_NONE);
	    returnValues();
	} else if (command.equals("hlr")) {
	    setDirection(ResValues.DIR_HLR);
	    returnValues();
	} else if (command.equals("hrl")) {
	    setDirection(ResValues.DIR_HRL);
	    returnValues();
	} else if (command.equals("vlr")) {
	    setDirection(ResValues.DIR_VLR);
	    returnValues();
	} else if (command.equals("vrl")) {
	    setDirection(ResValues.DIR_VRL);
	    returnValues();
	}
    }

    // Highlight button matching direction.
    // Also called by editor.
    public void setDirection(int direction) {
	this.direction = direction;
	unpush(noDirButton);
	unpush(hlrButton);
	unpush(hrlButton);
	unpush(vlrButton);
	unpush(vrlButton);
	switch (direction) {
	    case ResValues.DIR_NONE:
		push(noDirButton);
		break;
	    case ResValues.DIR_HLR:
		push(hlrButton);
		break;
	    case ResValues.DIR_HRL:
		push(hrlButton);
		break;
	    case ResValues.DIR_VLR:
		push(vlrButton);
		break;
	    case ResValues.DIR_VRL:
		push(vrlButton);
		break;
	}
    }

    // Make button pushed.
    private void push(JButton button) {
	button.setBorder(new BevelBorder(BevelBorder.LOWERED));
	button.setBackground(new Color(200, 200, 150));
    }
    // Border for unpushed button.
    private void unpush(JButton button) {
	button.setBorder(new BevelBorder(BevelBorder.RAISED));
	button.setBackground(Color.LIGHT_GRAY);
    }

    // Check or uncheck.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    sizeSpinner.setEnabled(true);
	    returnValues();
	} else if (e.getStateChange() == ItemEvent.DESELECTED) {
	    sizeModel.setValue(new Float(1.0f));
	    sizeSpinner.setEnabled(false);
	    returnValues();
	}
    }

    // Record that size has been changed by spinner.
    public void stateChanged(ChangeEvent e) {
	if (sizeCheck.isSelected())
	    returnValues();
    }

    // Set size.
    public void setUnitSize(float size) {
	if (!Float.isNaN(size)) {
	    sizeModel.setValue(new Float(size));
	    sizeCheck.setSelected(true);
	    sizeSpinner.setEnabled(true);
	} else {
	    sizeCheck.setSelected(false);
	    sizeModel.setValue(new Float(1.0f));
	    sizeSpinner.setEnabled(false);
	}
    }

    // Get size.
    private float getUnitSize() {
	if (sizeCheck.isSelected()) {
	    Number num = (Number) sizeModel.getNumber();
	    return num.floatValue();
	} else
	    return Float.NaN;
    }

    // Called upon change.
    private void returnValues() {
	returnValues(direction, getUnitSize());
    }

    /////////////////////////////////////////////////////////
    // Interface to allow information flow back to editor. 
    // Methods to be overridden there.

    // Called upon change.
    abstract protected void returnValues(int direction, float size);

    ///////////////////////////////////////////////////////////
    // Auxiliary.

    // Model for sizes.
    private static class SizeModel extends SpinnerNumberModel {
	SizeModel() {
	    super(1.0, 0.01, 9.99, 0.1);
	}
    }

}
