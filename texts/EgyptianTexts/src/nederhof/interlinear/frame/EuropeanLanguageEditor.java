/***************************************************************************/
/*                                                                         */
/*  EuropeanLanguageEditor.java                                            */
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

// Element used to edit a language of a resource, as combobox.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.corpus.*;
import nederhof.corpus.frame.*;
import nederhof.interlinear.*;

public class EuropeanLanguageEditor extends ComboEditor {

    // Construct editor.
    public EuropeanLanguageEditor(TextResource resource, String name,
	    int nameWidth, String comment) {
	super(resource, name,
	    EuropeanLanguages.getLanguages(),
	    new EuropeanLanguageRenderer(),
	    nameWidth, comment);
    }

    //////////////////////////////
    // Value manipulation.

    // Important enough to propagate to text.
    public boolean isGlobal() {
	return true;
    }

}
