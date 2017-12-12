/***************************************************************************/
/*                                                                         */
/*  LegendThreeValue.java                                                  */
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

// Part of legend dealing with three values.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendThreeValue extends LegendParam implements ItemListener {

    // The number values of the three options.
    private int v1;
    private int v2;
    private int v3;

    // The old value.
    private int old;
    // Last returned value.
    private int lastReturned;

    // The three buttons.
    JRadioButton b1 = new JRadioButton();
    JRadioButton b2 = new JRadioButton();
    JRadioButton b3 = new JRadioButton();

    // Make entry.
    public LegendThreeValue(String s1, int v1,
	    String s2, int v2, String s3, int v3,
	    int old) {
	this.v1 = v1;
	this.v2 = v2;
	this.v3 = v3;
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	add(b1);
	add(new JLabel(s1));
	add(b2);
	add(new JLabel(s2));
	add(b3);
	add(new JLabel(s3));

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
    private void setValue(int v) {
	if (v == v1)
	    b1.setSelected(true);
	else if (v == v2)
	    b2.setSelected(true);
	else if (v == v3)
	    b3.setSelected(true);
    }

    // Get value.
    private int getValue() {
	if (b2.isSelected())
	    return v2;
	else if (b3.isSelected())
	    return v3;
	else
	    return v1;
    }

    // Clear.
    public void clear() {
	setValue(v1);
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

    private void processMaybeChanged(int other) {
        if (other != lastReturned) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(int v);

}
