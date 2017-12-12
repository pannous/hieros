/***************************************************************************/
/*                                                                         */
/*  FontTypeRenderer.java                                                  */
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

// Print font type as string.

package nederhof.interlinear.frame;

import java.awt.*;
import javax.swing.*;

public class FontTypeRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	String name;
	Integer i = (Integer) value;
	if (i.intValue() == Font.PLAIN)
	    name = "plain";
	else if (i.intValue() == Font.BOLD)
	    name = "bold";
	else if (i.intValue() == Font.ITALIC)
	    name = "italic";
	else
	    name = "bold & italic";
	return new JLabel(name);
    }

    // Codes for font types as Integer.
    public static final Integer plainInt = new Integer(Font.PLAIN);
    public static final Integer boldInt = new Integer(Font.BOLD);
    public static final Integer italicInt = new Integer(Font.ITALIC);
    public static final Integer bolditalicInt = new Integer(Font.BOLD+Font.ITALIC);

    // All font types as array of Integers.
    public static final Integer[] fontTypes =
	new Integer[]{plainInt, boldInt, italicInt, bolditalicInt};
}
