/***************************************************************************/
/*                                                                         */
/*  LegendParam.java                                                       */
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

// Parameter within legend.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class LegendParam extends JPanel {

    // Method to clear values.
    abstract public void clear();

    // Method to reset values.
    abstract public void resetValue();

    // Method to process command. Return whether successful.
    // A small number of subclasses override this to process
    // a command under some conditions.
    public boolean processCommand(char c) {
	return false;
    }

    // Method to receive glyph. Return whether successful.
    // A small number of subclasses override this.
    public boolean receiveGlyph(String name) {
	return false;
    }

    // Parent element.
    private LegendParams parent = null;

    // Set parent.
    public void setEncloser(LegendParams parent) {
	this.parent = parent;
    }

}
