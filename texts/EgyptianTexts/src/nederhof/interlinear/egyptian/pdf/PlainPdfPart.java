/***************************************************************************/
/*                                                                         */
/*  PlainPdfPart.java                                                      */
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

package nederhof.interlinear.egyptian.pdf;

import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;

public class PlainPdfPart extends StringPdfPart {

    public PlainPdfPart(String string) {
	super(string);
    }

    protected BaseFont font() {
	return renderParams.plainFont;
    }

    protected float size() {
	return renderParams.plainSize;
    }
}
