/***************************************************************************/
/*                                                                         */
/*  LegendRealInf.java                                                     */
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

// Part of legend dealing with real numbers that can also be
// infinity.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendRealInf extends LegendParam
        implements ItemListener, ChangeListener {

    // Old value.
    private float old;
    // Last returned value.
    private float lastReturned;

    // Check whether visible, and whether infinity.
    private JRadioButton none = new JRadioButton();
    private JRadioButton check = new JRadioButton();
    private JRadioButton inf = new JRadioButton();

    // Contains value.
    private static final float initial = 1.0f;
    private SpinnerNumberModel model = 
	new SpinnerNumberModel(initial, 0.01, 9.99, 0.1);
    private JSpinner spinner = new JSpinner(model);

    // Make entry.
    public LegendRealInf(float old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	add(none);
	add(new JLabel("none"));
	add(check);
	add(spinner);
	add(inf);
	add(new JLabel("inf"));

	ButtonGroup group = new ButtonGroup();
	group.add(none);
	group.add(check);
	group.add(inf);
	spinner.addChangeListener(this);

	none.addItemListener(this);
	check.addItemListener(this);
	inf.addItemListener(this);

	setValue(old);
    }

    // Set to value.
    private void setValue(float val) {
	if (Float.isNaN(val)) {
	    none.setSelected(true);
	    model.setValue(new Float(initial));
	    spinner.setEnabled(false);
	} else if (val == Float.MAX_VALUE) {
	    inf.setSelected(true);
	    model.setValue(new Float(initial));
	    spinner.setEnabled(false);
	} else {
	    model.setValue(new Float(val));
	    check.setSelected(true);
	    spinner.setEnabled(true);
	}
    }

    // Get value.
    private float getValue() {
	if (none.isSelected())
	    return Float.NaN;
	else if (inf.isSelected())
	    return Float.MAX_VALUE;
	else {
	    Number num = (Number) model.getNumber();
	    return num.floatValue();
	}
    }

    // Restore to blank.
    public void clear() {
	setValue(Float.NaN);
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
	    if (e.getItemSelectable() == none) {
		model.setValue(new Float(initial));
		spinner.setEnabled(false);
		processMaybeChanged(getValue());
	    } else if (e.getItemSelectable() == inf) {
		model.setValue(new Float(initial));
		spinner.setEnabled(false);
		processMaybeChanged(getValue());
	    } else if (e.getItemSelectable() == check) {
		spinner.setEnabled(true);
		processMaybeChanged(getValue());
	    }
	}
    }

    // Record change by spinner.
    public void stateChanged(ChangeEvent e) {
	if (spinner.isEnabled())
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
