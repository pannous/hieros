/***************************************************************************/
/*                                                                         */
/*  LegendBool.java                                                        */
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

// Part of legend dealing with boolean value.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendBool extends LegendParam implements ItemListener {

    // The three buttons.
    JRadioButton b1 = new JRadioButton();
    JRadioButton b2 = new JRadioButton();
    JRadioButton b3 = new JRadioButton();

    // Old value.
    private Boolean old;
    // Last returned value.
    private Boolean lastReturned;

    // Make entry.
    public LegendBool(Boolean old) {
	this.old = old;
	this.lastReturned = old;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(b1);
        add(new JLabel("none"));
        add(b2);
        add(new JLabel("yes"));
        add(b3);
        add(new JLabel("no"));

        ButtonGroup group = new ButtonGroup();
        group.add(b1);
        group.add(b2);
        group.add(b3);
        b1.addItemListener(this);
        b2.addItemListener(this);
        b3.addItemListener(this);

	setValue(old);
    }

    // Set to value.
    private void setValue(Boolean v) {
        if (v == null)
            b1.setSelected(true);
        else if (v.equals(Boolean.TRUE))
            b2.setSelected(true);
        else if (v.equals(Boolean.FALSE))
            b3.setSelected(true);
    }

    // Get value.
    private Boolean getValue() {
	if (b2.isSelected())
	    return Boolean.TRUE;
	else if (b3.isSelected())
	    return Boolean.FALSE;
	else
	    return null;
    }

    // Clear.
    public void clear() {
	setValue(null);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Value is selected.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED)
	    processMaybeChanged(getValue());
    }

    private void processMaybeChanged(Boolean other) {
	boolean eq;
	if (lastReturned == null && other == null)
	    eq = true;
	else if (lastReturned == null || other == null)
	    eq = false;
	else 
	    eq = (other.booleanValue() == lastReturned.booleanValue());
	if (!eq) {
	    lastReturned = other;
	    processChanged(other);
	}
    }

    // Process that value is changed.
    abstract protected void processChanged(Boolean v);

}
