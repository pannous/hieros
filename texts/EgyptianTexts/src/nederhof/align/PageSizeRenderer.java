/***************************************************************************/
/*                                                                         */
/*  PageSizeRenderer.java                                                  */
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

// Print page size as string.

package nederhof.align;

import java.awt.*;
import javax.swing.*;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

class PageSizeRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	String name;
	com.itextpdf.text.Rectangle r = (com.itextpdf.text.Rectangle) value;
	if (r == PageSize.A0)
	    name = "A0";
	else if (r == PageSize.A1)
	    name = "A1";
	else if (r == PageSize.A2)
	    name = "A2";
	else if (r == PageSize.A3)
	    name = "A3";
	else if (r == PageSize.A4)
	    name = "A4";
	else if (r == PageSize.A5)
	    name = "A5";
	else if (r == PageSize.B0)
	    name = "B0";
	else if (r == PageSize.B1)
	    name = "B1";
	else if (r == PageSize.B2)
	    name = "B2";
	else if (r == PageSize.B3)
	    name = "B3";
	else if (r == PageSize.B4)
	    name = "B4";
	else if (r == PageSize.B5)
	    name = "B5";
	else
	    name = "letter";
	return new JLabel(name);
    }

    // All page sizes in array.
    public static final com.itextpdf.text.Rectangle[] pageSizes =
	new com.itextpdf.text.Rectangle[]{
	    PageSize.A0, PageSize.A1, PageSize.A2, PageSize.A3, PageSize.A4, PageSize.A5,
	    PageSize.B0, PageSize.B1, PageSize.B2, PageSize.B3, PageSize.B4, PageSize.B5,
	    PageSize.LETTER};
}
