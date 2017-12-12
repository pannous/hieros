/***************************************************************************/
/*                                                                         */
/*  Header2Part.java                                                       */
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

// Part of tier for title.

package nederhof.interlinear.egyptian.pdf;

import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;

public class Header2Part extends StringPdfPart {

    public Header2Part(String string) {
        super(string);
    }

    // Make extra leading.
    public float leading() {
	return size() * 0.5f;
    }

    protected BaseFont font() {
	return renderParams.header2Font;
    }

    protected float size() {
	return renderParams.header2Size;
    }

}
