/***************************************************************************/
/*                                                                         */
/*  LinkViewGenerator.java                                                 */
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

// Generates non-editing panel for hyperlink.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import nederhof.util.*;

public class LinkViewGenerator implements EditorComponentGenerator {

    // Make new panel, with empty fields.
    public Component makeComponent(ChangeListener listener) {
	return new TextLinkPanel("", "", listener);
    }

    // As above, with given values.
    public Component makeComponent(Object object, ChangeListener listener) {
	String[] textLink = (String[]) object;
	return new TextLinkPanel(textLink[0], textLink[1], listener);
    }

    // Extract value.
    public Object extract(Component comp) {
        TextLinkPanel textLinkPanel = (TextLinkPanel) comp;
        String text = textLinkPanel.text;
        String link = textLinkPanel.link;
        return new String[] {text, link};
    }

    // Panel with text and link.
    private class TextLinkPanel extends PropertyEditor.BoldLabel {
        // Two information carrying subpanels.
        public String link;
        public String text;

        // Construct.
        public TextLinkPanel(String link, String text,
                ChangeListener listener) {
	    super(text);
	    this.link = link;
	    this.text = text;
	    setAlignmentY(0.85f);
	    setBorder(BorderFactory.createEmptyBorder());
	    setForeground(Color.BLUE);
	    setToolTipText(link);
	}

    }

}
