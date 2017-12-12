/***************************************************************************/
/*                                                                         */
/*  LinkPart.java                                                          */
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

// Part of tier that is hyperlink.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

class LinkPart extends StringPart {

    // Text and address of hyperlink.
    public String ref;

    public LinkPart(String string, String ref) {
	super(string);
	this.ref = ref;
    }

    protected Font font() {
	return renderParams.footBoldFont;
    }

    protected FontMetrics metrics() {
	return renderParams.footBoldFontMetrics;
    }

    protected Color color() {
	return Color.BLUE;
    }

    //////////////////////////
    // Editing.

    // Should not be called, as this can occur only in footnotes
    // and preamble, where there are no positions.
    public StringPart prefixPart(int pos) {
        return new LinkPart(prefix(pos), ref);
    }
    public StringPart suffixPart(int pos) {
        return new LinkPart(suffix(pos), ref);
    }

}
