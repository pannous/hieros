/***************************************************************************/
/*                                                                         */
/*  LegendInt.java                                                         */
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

// Part of legend dealing with int numbers.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendInt extends LegendParam 
	implements ActionListener, ChangeListener {

    // Initial value.
    private int initial;

    // Old value.
    private int old;
    // Last returned value.
    private int lastReturned;

    // Contains value.
    private SpinnerNumberModel model;
    private JSpinner spinner;

    // Make entry.
    public LegendInt(int initial, int low, int high, int step, int old,
	    int[] increments) {
	this.initial = initial;
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	model = new SpinnerNumberModel(initial, low, high, step);
	spinner = new JSpinner(model);
	JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
	spinner.setEditor(editor);
	add(spinner);
	spinner.addChangeListener(this);

	if (increments != null) 
	    for (int i = 0; i < increments.length; i++) 
		add(new IncrementButton(increments[i]));

	setValue(old);
    }

    // Without increments.
    public LegendInt(int initial, int low, int high, int step, int old) {
	this(initial, low, high, step, old, null);
    }

    // Button that allows increment with large step.
    private class IncrementButton extends JButton {
	public IncrementButton(int incr) {
	    super(incr > 0 ? "+" + incr : "" + incr);
	    addActionListener(LegendInt.this);
	    setMargin(new Insets(5,5,5,5));
	}
    }

    // Set to value.
    private void setValue(int val) {
	model.setValue(new Integer(val));
    }

    // Get value.
    private int getValue() {
	Number num = (Number) model.getNumber();
	return num.intValue();
    }

    // Restore to blank.
    public void clear() {
	setValue(initial);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Process pushed button with increment.
    public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();
	if (action.length() > 0 && action.charAt(0) == '+') 
	    action = action.substring(1); // no leading +
	try {
	    int incr = Integer.parseInt(action);
	    setValue(getValue() + incr);
	} catch (NumberFormatException ex) {
	    // ignore
	}
    }

    // Record change by spinner.
    public void stateChanged(ChangeEvent e) {
	processMaybeChanged(getValue());
    }

    private void processMaybeChanged(int other) {
        if (other != lastReturned) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(int val);

}
