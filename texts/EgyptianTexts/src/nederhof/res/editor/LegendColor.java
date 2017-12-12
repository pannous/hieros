/***************************************************************************/
/*                                                                         */
/*  LegendColor.java                                                       */
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

// Part of legend dealing with colors.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.res.Color16;

abstract class LegendColor extends LegendParam implements ItemListener {


    // Old value.
    private Color16 old;
    // Last returned value.
    private Color16 lastReturned;

    // Check whether color present.
    private JCheckBox check;

    // List of colors.
    private JComboBox colorBox;

    // Initial color if any.
    private static final String defaultColor = "red";
    // Other color that will be used often.
    private static final String secondaryColor = "black";

    // Make entry.
    // Put red and black first, as these will be used the most.
    public LegendColor(Color16 old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	check = new JCheckBox();
	check.addItemListener(this);
	add(check);

	Vector colors = new Vector();
	colors.add(defaultColor);
	colors.add(secondaryColor);
	for (int i = 0; i < Color16.SIZE; i++) {
	    Color16 c = new Color16(i);
	    if (!c.toString().equals(defaultColor) &&
		    !c.toString().equals(secondaryColor))
		colors.add(c.toString());
	}
	colorBox = new JComboBox(colors);
	colorBox.setMaximumRowCount(12);
	colorBox.addItemListener(this);
	add(colorBox);

	setValue(old);
    }

    // Set to value.
    private void setValue(Color16 color) {
	if (color.isColor()) {
	    colorBox.setSelectedItem(color.toString());
	    colorBox.setEnabled(true);
	    check.setSelected(true);
	} else {
	    check.setSelected(false);
	    colorBox.setSelectedItem(defaultColor);
	    colorBox.setEnabled(false);
	}
    }

    // Get value.
    private Color16 getValue() {
	if (check.isSelected())
	    return new Color16(colorBox.getSelectedItem().toString());
	else
	    return Color16.NO_COLOR;
    }

    // Restore to no color.
    public void clear() {
	setValue(Color16.NO_COLOR);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Parameter is selected.
    public void itemStateChanged(ItemEvent e) {
	if (e.getItemSelectable() == check) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		colorBox.setEnabled(true);
		processMaybeChanged(getValue());
	    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
		colorBox.setSelectedItem(defaultColor);
		colorBox.setEnabled(false);
		processMaybeChanged(getValue());
	    }
	} else if (e.getItemSelectable() == colorBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED &&
		    check.isSelected()) 
		processMaybeChanged(getValue());
	}
    }

    private void processMaybeChanged(Color16 other) {
        if (!Color16.equal(other, lastReturned)) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(Color16 val);

}
