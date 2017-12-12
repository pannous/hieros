/***************************************************************************/
/*                                                                         */
/*  LegendReal.java                                                        */
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

// Part of legend dealing with real numbers.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendReal extends LegendParam 
	implements ItemListener, ChangeListener {

    // Different kinds of real.
    public static final int REAL = 0;
    public static final int NON_ZERO_REAL = 1;
    public static final int LOW_REAL = 2;

    // Initial value.
    private float initial;

    // Value to be used if not selected.
    private float unselectVal;

    // Old value.
    private float old;
    // Last returned value.
    private float lastReturned;

    // Check whether visible.
    private JCheckBox check;

    // Contains value.
    private SpinnerNumberModel model;
    private JSpinner spinner;

    // Make entry.
    public LegendReal(float initial, float unselectVal, int kind, float old) {
	this.initial = initial;
	this.unselectVal = unselectVal;
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	check = new JCheckBox();
	check.setSelected(false);
	check.addItemListener(this);
	add(check);

	switch (kind) {
	    case REAL:
		model = new SpinnerNumberModel(initial, 0.0, 9.99, 0.1);
		break;
	    case NON_ZERO_REAL:
		model = new SpinnerNumberModel(initial, 0.01, 9.99, 0.1);
		break;
	    case LOW_REAL:
		model = new SpinnerNumberModel(initial, 0.0, 1.0, 0.1);
		break;
	}

	spinner = new JSpinner(model);
	JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
	spinner.setEditor(editor);
	spinner.addChangeListener(this);
	add(spinner);

	setValue(old);
    }

    // Set to value.
    private void setValue(float val) {
	if (!Float.isNaN(val) && val != unselectVal) {
	    model.setValue(new Float(val));
	    check.setSelected(true);
	    spinner.setEnabled(true);
	} else {
	    check.setSelected(false);
	    model.setValue(new Float(initial));
	    spinner.setEnabled(false);
	}
    }

    // Get value.
    private float getValue() {
	if (check.isSelected()) {
	    Number num = (Number) model.getNumber();
	    return num.floatValue();
	} else
	    return unselectVal;
    }

    // Clear.
    public void clear() {
	setValue(unselectVal);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Parameter is selected.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    spinner.setEnabled(true);
	    processMaybeChanged(getValue());
	} else if (e.getStateChange() == ItemEvent.DESELECTED) {
	    spinner.setEnabled(false);
	    processMaybeChanged(getValue());
	}
    }

    // Record change by spinner.
    public void stateChanged(ChangeEvent e) {
	if (check.isSelected())
	    processMaybeChanged(getValue());
    }

    private void processMaybeChanged(float other) {
        boolean eq;
        if (Float.isNaN(lastReturned) && Float.isNaN(other))
            eq = true;
        else if (Float.isNaN(lastReturned) || Float.isNaN(other))
            eq = false;
        else
            eq = (other == lastReturned);
        if (!eq) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(float val);

}
