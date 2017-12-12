/***************************************************************************/
/*                                                                         */
/*  ClickButton.java                                                       */
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

// Button for in menus. Can be clicked, leading to action.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ClickButton extends JButton {

    // Size of borders.
    private static final int BSIZE = 5;

    // Label of item.
    private String label;

    public ClickButton(ActionListener lis, String label, String action, int mnem) {
	this.label = label;
	setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
	setBackground(Color.LIGHT_GRAY);
	setFocusPainted(false);
	setRolloverEnabled(true);
	setText("<html>" + label + "</html>");
	setActionCommand(action);
	setMnemonic(mnem);
	setMaximumSize(getPreferredSize());
	addActionListener(lis);
    }

    // Make gray if not enabled.
    public void setEnabled(boolean b) {
	super.setEnabled(b);
	if (b)
	    setText("<html>" + label + "</html>");
	else
	    setText("<html><font color=\"gray\">" + label + "</font></html>");
    }

}
