/***************************************************************************/
/*                                                                         */
/*  EnabledButton.java                                                     */
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

// Customized button that can be enabled/disabled.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class EnabledButton extends JButton {

    // Normal color.
    private Color color;

    // Constructor.
    public EnabledButton(ActionListener lis, String action, Color color) {
	setFocusPainted(false);
	setRolloverEnabled(true);
	setText(action);
	setActionCommand(action);
	setMaximumSize(getPreferredSize());
	addActionListener(lis);
    }
    // With black.
    public EnabledButton(ActionListener lis, String action) {
	this(lis, action, Color.BLACK);
    }

    // Make gray if not enabled.
    public void setEnabled(boolean b) {
	super.setEnabled(b);
	if (b) 
	    setForeground(color);
	else
	    setForeground(Color.GRAY);
    }

}
