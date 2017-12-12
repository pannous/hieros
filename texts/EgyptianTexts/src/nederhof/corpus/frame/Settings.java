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

package nederhof.corpus.frame;

import java.awt.*;

class Settings {

    // Initial dimensions of entire screen of viewer.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 740;

    // Default directory where corpus is to be found.
    public static final String defaultCorpusDir = "corpus";
    // Default directory where text files are to be found, relative to corpus
    // directory.
    public static final String defaultTextDir = "texts";
    // Default directory where PDF files are stored.
    public static final String defaultPdfDir = "pdf";

    ////////////////////////
    // Fonts.

    // For input text windows.
    public static final String inputTextFontName = "SansSerif";
    public static final int inputTextFontSize = 16;

    // Labels.
    public static final String labelFontName = "SansSerif";
    public static final int labelFontSize = 16;

    public static Font inputTextFont() {
	return new Font(
                Settings.inputTextFontName,
                Font.PLAIN,
                Settings.inputTextFontSize);
    }
    protected static Font labelFont(int style) {
        return new Font(
                Settings.labelFontName,
                style,
                Settings.labelFontSize);
    }

}
