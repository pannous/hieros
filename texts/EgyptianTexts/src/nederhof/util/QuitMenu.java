/***************************************************************************/
/*                                                                         */
/*  QuitMenu.java                                                          */
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

// Menu containing just quit button.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class QuitMenu extends JMenuBar {

    // Distance between left edge and button.
    private static final int STRUT_SIZE = 10;

    public QuitMenu(ActionListener lis) {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBackground(Color.LIGHT_GRAY);
	add(Box.createHorizontalStrut(STRUT_SIZE));
	add(new ClickButton(lis, "<u>Q</u>uit", "quit", KeyEvent.VK_Q));
    }
}
