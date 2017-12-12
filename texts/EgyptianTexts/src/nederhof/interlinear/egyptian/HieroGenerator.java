/***************************************************************************/
/*                                                                         */
/*  HieroGenerator.java                                                    */
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;

public class HieroGenerator implements EditorComponentGenerator {

    // Text to which this is added.
    private StyledTextPane text;

    // User of this generator.
    private EditChainElement parent;

    public HieroGenerator(StyledTextPane text, EditChainElement parent) {
	this.text = text;
	this.parent = parent;
    }

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	return makeHieroButton("", listener);
    }
    public Component makeComponent(Object res, ChangeListener listener) {
	return makeHieroButton((String) res, listener);
    }
    public Object extract(Component comp) {
	HieroButton label = (HieroButton) comp;
	return label.getRes();
    }

    private HieroButton makeHieroButton(String res, ChangeListener listener) {
	return new HieroButton(res, listener) {
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

