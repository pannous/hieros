/***************************************************************************/
/*                                                                         */
/*  HiGenerator.java                                                       */
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

// Generates buttons containing hieroglyphic.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;

public class HiGenerator implements EditorComponentGenerator {

    // Text to which this is added.
    private StyledTextPane text;

    // User of this generator.
    private EditChainElement parent;

    public HiGenerator(StyledTextPane text, EditChainElement parent) {
	this.text = text;
	this.parent = parent;
    }

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	return makeHieroButton("", new Vector(), new Vector(), listener);
    }
    public Component makeComponent(Object triple, ChangeListener listener) {
	Object[] three = (Object[]) triple;
	String res = (String) three[0];
	Vector posses = (Vector) three[1];
	Vector notes = (Vector) three[2];
	HieroButton b = makeHieroButton((String) res, posses, notes, listener);
	return b;
    }
    public Object extract(Component comp) {
	HieroButton label = (HieroButton) comp;
	String res = label.getRes();
	Vector posses = label.getPosses();
	Vector notes = label.getNotes();
	return new Object[] {res, posses, notes};
    }

    private HieroButton makeHieroButton(String res, 
	    	Vector posses, Vector notes, ChangeListener listener) {
        return new HieroButton(res, posses, notes, listener) {
            protected void startWait() {
                text.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            protected void endWait() {
                text.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            protected void takeFocus() {
                text.requestFocus();
            }
            protected void stopEditing() {
                parent.allowEditing(false);
            }
            protected void resumeEditing() {
                parent.allowEditing(true);
            }
        };
    }

}

