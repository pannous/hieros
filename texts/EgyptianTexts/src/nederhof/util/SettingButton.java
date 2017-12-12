/***************************************************************************/
/*                                                                         */
/*  SettingButton.java                                                     */
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

// Button for use in setting windows. Can be clicked, leading to action.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SettingButton extends JButton {
    public SettingButton(ActionListener lis, String label, String action, int mnem) {
	setFocusPainted(true);
	setRolloverEnabled(true);
	setText(label);
	setActionCommand(action);
	setMnemonic(mnem);
	setMaximumSize(getPreferredSize());
	addActionListener(lis);
    }
}

