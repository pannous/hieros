/***************************************************************************/
/*                                                                         */
/*  LegendParams.java                                                      */
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

// Panel within legend, in which parameters can be adjusted.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.util.*;

class LegendParams extends JPanel {

    // Rows.
    private Vector rows = new Vector();

    // Construct empty panel.
    public LegendParams() {
	super(new SpringLayout());
	setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Parameters"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
    }

    // Add row.
    public void addRow(String name, LegendParam param) {
	param.setEncloser(this);
	add(new JLabel(name + ":"));
	add(param);
	rows.add(param);
    }

    // Now rows at all?
    public boolean isEmpty() {
	return rows.isEmpty();
    }

    // Format after adding all entries.
    public void format() {
	SpringUtilities.makeCompactGrid(this, rows.size(), 2, 5, 5, 5, 5);
    }

    // Clear all entries.
    public void clear() {
	for (int i = 0; i < rows.size(); i++) {
	    LegendParam param = (LegendParam) rows.get(i);
	    param.clear();
	}
    }

    // Reset all entries.
    public void reset() {
	for (int i = 0; i < rows.size(); i++) {
	    LegendParam param = (LegendParam) rows.get(i);
	    param.resetValue();
	}
    }

    // Try running command through parameters. Return whether successful.
    public boolean processCommand(char c) {
	for (int i = 0; i < rows.size(); i++) {
	    LegendParam param = (LegendParam) rows.get(i);
	    if (param.processCommand(c))
		return true;
	}
	return false;
    }

    // Receive glyph. Return whether successful.
    public boolean receiveGlyph(String name) {
	for (int i = 0; i < rows.size(); i++) {
	    LegendParam param = (LegendParam) rows.get(i);
	    if (param.receiveGlyph(name))
		return true;
	}
	return false;
    }

}
