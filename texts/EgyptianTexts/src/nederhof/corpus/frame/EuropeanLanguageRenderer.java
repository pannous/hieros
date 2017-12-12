/***************************************************************************/
/*                                                                         */
/*  EuropeanLanguageRenderer.java                                          */
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

// European language alpha-3 code with language name,
// for use in JComboBox.

package nederhof.corpus.frame;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import nederhof.corpus.*;

public class EuropeanLanguageRenderer implements ListCellRenderer {

    // To be used in JComboBox, for labels.
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
	String alpha = (String) value;
        return new JLabel(" " + alpha + " (" + 
	    EuropeanLanguages.getLanguageName(alpha) + ")");
    }

}
