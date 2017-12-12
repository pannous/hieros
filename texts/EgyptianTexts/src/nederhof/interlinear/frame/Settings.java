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

package nederhof.interlinear.frame;

import java.awt.*;

class Settings {

    // Initial dimensions of entire screen of viewer.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 740;

    // Default directory where text resources are to be found,
    // relative to directory of corpus.
    public static final String defaultResourceDir = "resources";

    // Default directory where precedence resources are to be found,
    // relative to directory of corpus.
    public static final String defaultAlignDir = "align";

    // Default directory where PDF files are to be written,
    // relative to directory of corpus.
    public static final String defaultPdfDir = "pdf";

    ////////////////////////
    // Fonts.

    // For input text windows.
    public static final String inputTextFontName = "SansSerif";
    public static final int inputTextFontSize = 16;

    // Labels.
    public static final String labelFontName = "SansSerif";
    public static final int labelFontSize = 14;

    //////////////////////////
    // Parameters of rendering of interlinear text.

    public static final int leftMarginDefault = 15;
    public static final int rightMarginDefault = 20;
    public static final int colSepDefault = 5;
    public static final int sectionSepDefault = 30;
    public static final int lineSepDefault = 15;
    public static final int footnoteLineSepDefault = 2;
    public static final boolean uniformAscentDefault = false;
    public static final boolean collectNotesDefault = false;
    public static final int footFontSizeReductionDefault = 85;

    public static final Color labelColor = Color.MAGENTA;
    public static final Color versionColor = Color.BLUE;
    public static final Color coordColor = Color.RED;
    public static final Color footnoteMarkerColor = Color.BLUE;

}
