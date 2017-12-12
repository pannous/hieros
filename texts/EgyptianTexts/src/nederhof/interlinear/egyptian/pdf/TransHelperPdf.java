/***************************************************************************/
/*                                                                         */
/*  TransHelperPdf.java                                                    */
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

// Some auxiliary methods for transliteration.

package nederhof.interlinear.egyptian.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

import nederhof.fonts.*;

public class TransHelperPdf {

    // Transliteration font for lower and upper case.
    public static BaseFont translitLower(int translitStyle) {
        switch (translitStyle) {
            case Font.BOLD:
                return FontUtil.baseFont("data/fonts/TranslitLowerSB.ttf");
            case Font.ITALIC:
                return FontUtil.baseFont("data/fonts/TranslitLowerI.ttf");
            case Font.BOLD + Font.ITALIC:
                return FontUtil.baseFont("data/fonts/TranslitLowerIB.ttf");
            default:
                return FontUtil.baseFont("data/fonts/TranslitLowerS.ttf");
        }
    }
    public static BaseFont translitUpper(int translitStyle) {
        switch (translitStyle) {
            case Font.BOLD:
                return FontUtil.baseFont("data/fonts/TranslitUpperSB.ttf");
            case Font.ITALIC:
                return FontUtil.baseFont("data/fonts/TranslitUpperI.ttf");
            case Font.BOLD + Font.ITALIC:
                return FontUtil.baseFont("data/fonts/TranslitUpperIB.ttf");
            default:
                return FontUtil.baseFont("data/fonts/TranslitUpperS.ttf");
        }
    }

}
