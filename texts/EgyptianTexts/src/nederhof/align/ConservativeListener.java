/***************************************************************************/
/*                                                                         */
/*  ConservativeListener.java                                              */
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

// Listen if window to be hidden.

package nederhof.align;

import java.awt.*;
import java.awt.event.*;

class ConservativeListener extends WindowAdapter {
    Component frame;
    public ConservativeListener(Component frame) {
	this.frame = frame;
    }

    public void windowClosing(WindowEvent e) {
	frame.setVisible(false);
    }
}
