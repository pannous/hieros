/***************************************************************************/
/*                                                                         */
/*  EnabledMenuItem.java                                                   */
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

// Customized menu item that can be enabled/disabled.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class EnabledMenuItem extends JMenuItem {

    // Label of item.
    private String label;

    // Constructor.
    public EnabledMenuItem(ActionListener lis, String label, String action, int mnem) {
	this.label = label;
	setEnabled(true);
	setBackground(Color.LIGHT_GRAY);
	setActionCommand(action);
	setMnemonic(mnem);
	setAccelerator(GuiAux.shortcut(mnem));
	addActionListener(lis);
    }

    // Without mnemonic.
    public EnabledMenuItem(ActionListener lis, String label, String action) {
	this.label = label;
	setEnabled(true);
	setBackground(Color.LIGHT_GRAY);
	setActionCommand(action);
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
