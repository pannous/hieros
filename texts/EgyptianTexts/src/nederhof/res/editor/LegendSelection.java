/***************************************************************************/
/*                                                                         */
/*  LegendSelection.java                                                   */
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

// Part of legend dealing with selection from small set.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendSelection extends LegendParam implements ItemListener {

    // List of options.
    private JComboBox box;

    // Default option.
    private String defaulted;

    // Old value.
    private String old;
    // Last returned value.
    private String lastReturned;

    // Make entry.
    public LegendSelection(Vector options, String defaulted, String old) {
	this.defaulted = defaulted;
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	box = new JComboBox(options);
	box.setMaximumRowCount(20);
	box.addItemListener(this);
	add(box);

        setValue(old);
    }

    // Set to value.
    private void setValue(String option) {
	box.setSelectedItem(option);
    }

    // Get value.
    private String getValue() {
	return box.getSelectedItem().toString();
    }

    // Restore to default.
    public void clear() {
	setValue(defaulted);
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Process changed option.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) 
	    processMaybeChanged(getValue());
    }

    private void processMaybeChanged(String other) {
        if (!other.equals(lastReturned)) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(String val);

}
