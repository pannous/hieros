/***************************************************************************/
/*                                                                         */
/*  CompactTable.java                                                      */
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

// JTable where the widths of columns is determined on the
// widths of the cells.

package nederhof.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class CompactTable extends JTable {

    private final static int margin = 5;

    // Construct.
    public CompactTable(Object[][] rowData, Object[] columnNames) {
	super(rowData, columnNames);
	setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	for (int col = 0; col < getColumnCount(); col++) 
	    packColumn(col);
    }

    private void packColumn(int c) {
	DefaultTableColumnModel colModel = 
	    (DefaultTableColumnModel) getColumnModel();
	TableColumn col = colModel.getColumn(c);

	TableCellRenderer renderer = col.getHeaderRenderer();
	if (renderer == null) 
	    renderer = getTableHeader().getDefaultRenderer();

	Component comp = renderer.getTableCellRendererComponent(
		this, col.getHeaderValue(), false, false, 0, 0);
	int width = comp.getPreferredSize().width;

	for (int r = 0; r < getRowCount(); r++) {
	    renderer = getCellRenderer(r, c);
	    comp = renderer.getTableCellRendererComponent(
		    this, getValueAt(r, c), false, false, r, c);
	    width = Math.max(width, comp.getPreferredSize().width);
	}

	width += 2 * margin;
	col.setPreferredWidth(width);
    }

}
