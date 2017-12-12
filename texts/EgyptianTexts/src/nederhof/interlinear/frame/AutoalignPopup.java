/***************************************************************************/
/*                                                                         */
/*  AutoalignPopup.java                                                    */
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

// Popup showing tiers for text that are autoaligned.

package nederhof.interlinear.frame;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.util.*;

class AutoalignPopup {

    private static PopupFactory factory = PopupFactory.getSharedInstance();

    public static Popup createAutoalignPopup(Component component, int x, int y,
	    Vector autoaligns) {
	return factory.getPopup(component, autoTable(autoaligns), x, y);
    }

    private static Component autoTable(Vector autoaligns) {
	JPanel panel = new JPanel();
	panel.setBackground(Color.LIGHT_GRAY);
	TitledBorder title = BorderFactory.createTitledBorder(
		new LineBorder(Color.BLUE, 2), "Auto");
	panel.setBorder(BorderFactory.createCompoundBorder(title,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	if (autoaligns.size() < 1) 
	    panel.add(new JLabel("none"));
	else {
	    String[] columnNames = { "Name 1", "Tier 1", "Name 2", "Tier 2" };
	    Object[][] data = new Object[autoaligns.size()][];
	    for (int i = 0; i < autoaligns.size(); i++) {
		Object[] align = (Object[]) autoaligns.get(i);
		TextResource resource1 = (TextResource) align[0];
		String tierName1 = (String) align[1];
		TextResource resource2 = (TextResource) align[2];
		String tierName2 = (String) align[3];
		data[i] = new Object[] {
		    resource1.getName(), tierName1,
		    resource2.getName(), tierName2 };
	    }
	    JTable table = new CompactTable(data, columnNames);
	    panel.add(table);
	}
	return panel;
    }

}
