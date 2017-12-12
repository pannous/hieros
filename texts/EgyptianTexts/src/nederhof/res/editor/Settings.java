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

package nederhof.res.editor;

import java.awt.*;

class Settings {

    // Name of program and version.
    public static final String programName = "AELalignEditor " +
	nederhof.align.Align.versionNumber;

    // Initial dimensions of entire screen of editor.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 800;
    // Same for fragment editor.
    public static final int fragmentDisplayWidthInit = 700;
    public static final int fragmentDisplayHeightInit = 600;

    // How many glyphs next to each other in screen for choosing glyphs.
    public static final int chooserCols = 9;
    // Size of hieroglyphic font in chooser.
    public static final int chooserHieroFontSize = 40;
    // Font to be used for labels in buttons in chooser.
    public static final Font chooserLabelFont = 
	new Font("SansSerif", Font.BOLD, 14);
    // May chooser choose glyphs that are not in the font?
    public static final boolean chooserNonExistentChoose = true;

    // Directory where XML files are to be found.
    public static final String defaultDir = ".";

    // Initial dimensions of screen for info on glyphs.
    public static final int infoWidthInit = 600;
    public static final int infoHeightInit = 400;
    // XML file where information can be found.
    public static final String infoFile = "data/ortho/signinfo.xml";

    // Auto save of text buffer every so many edits.
    public static final int autoSaveInterval = 20;

    // Size of hieroglyphic font in preview.
    public static final int previewHieroFontSize = 50;
    // Margin around preview, relative to unit size.
    public static final float previewMargin = 0.3f;

    // Size of hieroglyphic font in tree.
    public static final int treeHieroFontSize = 60;
    // Margin around view, relative to unit size.
    public static final float treeMargin = 0.0f;

    // Size of hieroglyphic font in composition.
    public static final int compositionHieroFontSize = 70;

    // Length of line of hieroglyphic consisting of top groups,
    // before linebreak is considered. (Header is ignored, and linebreaks 
    // are not within top groups.)
    // A negative number means no linebreaks are used.
    public static final int resLinebreakLimit = 50;

    // Whether RES is taken from an XML file, which means that special
    // characters are escaped.
    public static final boolean ResEscaped = true;
}
