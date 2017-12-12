/***************************************************************************/
/*                                                                         */
/*  Settings.java                                                          */
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

// Various constants of PDF output.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

import nederhof.fonts.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;

class Settings {

    // Only takes effect for external fonts.
    public static final int pdfEgyptFontStyleDefault = Font.PLAIN;
    public static final int pdfEgyptFontSizeDefault = 16;

    public static final String pdfHieroFontNameDefault = 
			HieroRenderContext.defaultFontName;
    public static final int pdfHieroFontSizeDefault = 24;
    // If hieroglyphic printed as pixels, how many pixels per point.
    public static final int pdfHieroResolutionDefault = 4;

    // Lexical entries.
    public static final int pdfLxSepDefault = 10;
    public static final int pdfLxLeadingDefault = 10;
    public static final int pdfLxInnerMarginDefault = 5;
    public static final int pdfLxLineThicknessDefault = 1;
    public static final boolean pdfLxAbbreviatedDefault = false;

}
