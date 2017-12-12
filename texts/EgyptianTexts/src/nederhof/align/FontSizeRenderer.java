/***************************************************************************/
/*                                                                         */
/*  FontSizeRenderer.java                                                  */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Print font size as string.

package nederhof.align;

import java.awt.*;
import java.util.*;
import javax.swing.*;

class FontSizeRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	Integer i = (Integer) value;
	return new JLabel(i.toString());
    }

    // Get vector of allowable font sizes between min and max.
    public static Vector getFontSizes(int min, int max, int step) {
	Vector vec = new Vector();
	for (int i = min; i <= max; i += step)
	    vec.addElement(new Integer(i));
	return vec;
    }
}
