/***************************************************************************/
/*                                                                         */
/*  EtcGenerator.java                                                      */
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

// Generates buttons containing "etc".

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public class EtcGenerator implements EditorComponentGenerator {

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	return new EtcLabel();
    }
    public Component makeComponent(Object text, ChangeListener listener) {
	return new EtcLabel();
    }
    public Object extract(Component comp) {
	return null;
    }

    private class EtcLabel extends JLabel {
	public EtcLabel() {
	    super("etc.");
	    setForeground(Color.GREEN);
	    setAlignmentY(0.75f);
	}
    }

}

