/***************************************************************************/
/*                                                                         */
/*  LegendStructure.java                                                   */
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

// Panel within legend, in which structure changes are offered.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.util.*;

class LegendStructure extends JPanel {

    // Collected buttons.
    private Vector leftButtons = new Vector();
    private Vector rightButtons = new Vector();

    // Construct empty panel.
    public LegendStructure() {
	super(new SpringLayout());
        setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Structure"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));
    }

    // Add button on left side.
    public void addButtonLeft(StructureButton but) {
	leftButtons.add(but);
    }

    // Add button on right side.
    public void addButtonRight(StructureButton but) {
	rightButtons.add(but);
    }

    // Format after adding all entries.
    public void format() {
	int rows = Math.max(leftButtons.size(), rightButtons.size());
	for (int i = 0; i < rows; i++) {
	    if (i < leftButtons.size()) {
		StructureButton but = (StructureButton) leftButtons.get(i);
		add(but);
	    } else 
		add(Box.createHorizontalStrut(60));
	    if (i < rightButtons.size()) {
		StructureButton but = (StructureButton) rightButtons.get(i);
		add(but);
	    } else
		add(Box.createHorizontalStrut(60));
	}
	SpringUtilities.makeCompactGrid(this, rows, 2, 5, 5, 5, 5);
    }

    // Push button with character. Return whether any found.
    public boolean push(char c) {
	for (int i = 0; i < leftButtons.size(); i++) {
	    StructureButton but = (StructureButton) leftButtons.get(i);
	    if (but.push(c))
		return true;
	}
	for (int i = 0; i < rightButtons.size(); i++) {
	    StructureButton but = (StructureButton) rightButtons.get(i);
	    if (but.push(c))
		return true;
	}
	return false;
    }

}
