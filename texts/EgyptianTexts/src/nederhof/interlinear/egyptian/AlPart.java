/***************************************************************************/
/*                                                                         */
/*  AlPart.java                                                            */
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

// Transliteration part of tier.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

public class AlPart extends StringPart {

    // Is this uppercase?
    public boolean upper;

    public AlPart(String string, boolean upper, boolean note) {
	super(string);
	this.upper = upper;
	setFootnote(note);
    }

    protected Font font() {
	if (upper) {
	    if (isFootnote())
		return renderParams.footEgyptUpperFont;
	    else
		return renderParams.egyptUpperFont;
	} else {
	    if (isFootnote())
		return renderParams.footEgyptLowerFont;
	    else
		return renderParams.egyptLowerFont;
	}
    }

    protected FontMetrics metrics() {
	if (upper) {
	    if (isFootnote())
		return renderParams.footEgyptUpperFontMetrics;
	    else
		return renderParams.egyptUpperFontMetrics;
	} else {
	    if (isFootnote())
		return renderParams.footEgyptLowerFontMetrics;
	    else
		return renderParams.egyptLowerFontMetrics;
	}
    }

    //////////////////////////
    // Editing.

    public StringPart prefixPart(int pos) {
	return new AlPart(prefix(pos), upper, isFootnote());
    }
    public StringPart suffixPart(int pos) {
	return new AlPart(suffix(pos), upper, isFootnote());
    }

}
