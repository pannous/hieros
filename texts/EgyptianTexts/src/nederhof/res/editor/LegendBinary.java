/***************************************************************************/
/*                                                                         */
/*  LegendBinary.java                                                      */
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

// Part of legend dealing with binary choices.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendBinary extends LegendParam implements ItemListener {

    // Old value.
    private boolean old;
    // Last value returned.
    private boolean lastReturned;

    // Check whether property is on.
    private JCheckBox check;

    // Make entry.
    public LegendBinary(boolean old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	check = new JCheckBox();
	check.addItemListener(this);
	add(check);

	setValue(old);
    }

    // Set to value.
    private void setValue(boolean property) {
	check.setSelected(property);
    }

    // Get value.
    private boolean getValue() {
	return check.isSelected();
    }

    // Set to default.
    public void clear() {
	setValue(false);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
	setValue(old);
	processMaybeChanged(getValue());
    }

    // Parameter is selected.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) 
	    processMaybeChanged(true);
	else if (e.getStateChange() == ItemEvent.DESELECTED)
	    processMaybeChanged(false);
    }

    private void processMaybeChanged(boolean other) {
        if (other != lastReturned) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(boolean val);

}
