/***************************************************************************/
/*                                                                         */
/*  NoPart.java                                                            */
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

// Normal text part of tier.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

public class NoPart extends StringPart {

    public NoPart(String string) {
	super(string);
    }

    protected Font font() {
	if (isFootnote())
	    return renderParams.footLatinFont;
	else
	    return renderParams.latinFont;
    }

    protected FontMetrics metrics() {
	if (isFootnote())
	    return renderParams.footLatinFontMetrics;
	else
	    return renderParams.latinFontMetrics;
    }

    //////////////////////////
    // Editing.

    public StringPart prefixPart(int pos) {
        return new NoPart(prefix(pos));
    }
    public StringPart suffixPart(int pos) {
        return new NoPart(suffix(pos));
    }

}
