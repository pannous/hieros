/***************************************************************************/
/*                                                                         */
/*  HieroViewGenerator.java                                                */
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

// Generates labels containing hieroglyphic that cannot be edited.

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

public class HieroViewGenerator implements EditorComponentGenerator {

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
        return makeHieroButton("");
    }
    public Component makeComponent(Object res, ChangeListener listener) {
        return makeHieroButton((String) res);
    }
    public Object extract(Component comp) {
        HieroButton label = (HieroButton) comp;
        return label.getRes();
    }

    private HieroButton makeHieroButton(String res) {
        return new HieroButton(res) {
            protected void startWait() {
		// nothing
            }
            protected void endWait() {
		// nothing
            }
            protected void takeFocus() {
		// nothing
            }
            protected void stopEditing() {
		// nothing
            }
            protected void resumeEditing() {
		// nothing
            }
        };
    }

}
