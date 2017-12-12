/***************************************************************************/
/*                                                                         */
/*  PosGenerator.java                                                      */
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

// Generates invisible elements at positions.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public class PosGenerator implements EditorComponentGenerator {

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	// Cannot happen.
	return new PosElement("impossible");
    }
    public Component makeComponent(Object text, ChangeListener listener) {
	String id = (String) text;
	return new PosElement(id);
    }
    public Object extract(Component comp) {
	PosElement elem = (PosElement) comp;
	String id = elem.id;
	return id;
    }

    // Element of size 0.
    private class PosElement extends JPanel {
	public String id;
	public PosElement(String id) {
	    this.id = id;
	}
	public Dimension getMinimumSize() {
	    return new Dimension(0, 0);
	}
	public Dimension getPreferredSize() {
	    return new Dimension(0, 0);
	}
	public Dimension getMaximumSize() {
	    return new Dimension(0, 0);
	}
    }

}

