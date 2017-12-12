/***************************************************************************/
/*                                                                         */
/*  IPart.java                                                             */
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

// Italic text part of tier.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

class IPart extends StringPart {

    public IPart(String string) {
	super(string);
    }

    protected Font font() {
	return renderParams.footItalicFont;
    }

    protected FontMetrics metrics() {
	return renderParams.footItalicFontMetrics;
    }

    //////////////////////////
    // Editing.

    public StringPart prefixPart(int pos) {
        return new IPart(prefix(pos));
    }
    public StringPart suffixPart(int pos) {
        return new IPart(suffix(pos));
    }

}
