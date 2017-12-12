/***************************************************************************/
/*                                                                         */
/*  StructureButton.java                                                   */
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

// Button to edit structure.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class StructureButton extends JButton implements ActionListener {

    // Single character for triggering button.
    private char mnemonic;

    public StructureButton(String name, char mnemonic) {
	setText("<html>" + name + "</html>");
	if (name.equals("delete"))
	    setForeground(Color.RED);
	setActionCommand(name);
	this.mnemonic = mnemonic;
	setMaximumSize(getPreferredSize());
	setFocusable(false);
	addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	pushed();
    }

    // Try pushing with character.
    public boolean push(char c) {
	if (c == mnemonic) {
	    pushed();
	    return true;
	} else
	    return false;
    }

    abstract protected void pushed();

}
