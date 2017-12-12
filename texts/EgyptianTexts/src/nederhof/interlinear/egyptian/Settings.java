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

// Various constants of editor.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.res.*;

public class Settings {

    ////////////////////////
    // Fonts.

    // For editing embedded hieroglyphic text.
    public static final int embeddedPreviewHieroFontSize = 50;
    public static final int embeddedTreeHieroFontSize = 60;
    // Hieroglyphic mixed with other text.
    public static final int textHieroFontSize = 35;

    // For transliteration fonts.
    public static final int translitFontSize = 22;
    public static final int translitFontStyle = Font.PLAIN;

    // For input text windows.
    public static final String inputTextFontName = "SansSerif";
    public static final int inputTextFontSize = 16;

    //////////////////////////
    // Parameters of rendering of interlinear text.

    public static final String latinFontNameDefault = "SansSerif";
    public static final int latinFontStyleDefault = Font.PLAIN;
    public static final int latinFontSizeDefault = 16;
    public static final int egyptFontStyleDefault = Font.PLAIN;
    public static final int egyptFontSizeDefault = 22;
    public static final String hieroFontNameDefault = 
				HieroRenderContext.defaultFontName;
    public static final int hieroFontSizeDefault = 36;
    public static final int lxSepDefault = 10;
    public static final int lxLeadingDefault = 10;
    public static final int lxInnerMarginDefault = 5;
    public static final int lxLineThicknessDefault = 2;
    public static final boolean lxAbbreviatedDefault = false;

    ////////////////////////////////
    // Window size.

    public static final int footnoteEditorWidth = 500;
    public static final int footnoteEditorHeight = 300;
    public static final int hieroNoteEditorWidth = 500;
    public static final int hieroNoteEditorHeight = 700;

}
