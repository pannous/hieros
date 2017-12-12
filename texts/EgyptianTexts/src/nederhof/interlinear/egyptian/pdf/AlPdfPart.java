/***************************************************************************/
/*                                                                         */
/*  AlPdfPart.java                                                         */
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

package nederhof.interlinear.egyptian.pdf;

import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;

public class AlPdfPart extends StringPdfPart {

    // Is this uppercase?
    public boolean upper;

    public AlPdfPart(String string, boolean upper, boolean note) {
	super(string);
	this.upper = upper;
	setFootnote(note);
    }

    protected BaseFont font() {
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

    protected float size() {
	if (upper) {
	    if (isFootnote())
		return renderParams.footEgyptUpperSize;
	    else
		return renderParams.egyptUpperSize;
	} else {
	    if (isFootnote())
		return renderParams.footEgyptLowerSize;
	    else
		return renderParams.egyptLowerSize;
	}
    }

}
