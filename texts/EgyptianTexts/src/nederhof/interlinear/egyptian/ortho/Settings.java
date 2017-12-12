// Various constants

package nederhof.interlinear.egyptian.ortho;

import java.awt.*;

class Settings {

    // Initial dimensions of orthographic editor.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 740;
    // Initial dimensions of screen for info on glyphs.
    public static final int infoWidthInit = 400;
    public static final int infoHeightInit = 600;

    // Information about signs.
    public static final String infoFile = "data/ortho/signinfo.xml";

    // Information about sign functions.
    public static final String functionsLocation = "data/ortho/functions.xml";

    // Existing orthographic annotations.
    public static final String exampleAnnotations = "data/ortho/OrthoAnnotations.xml";

    ////////////////////////
    // Fonts.

    // Hieroglyphic.
    public static final int textHieroFontSize = 35;
    // For editing embedded hieroglyphic text.
    public static final int embeddedPreviewHieroFontSize = 50;
    public static final int embeddedTreeHieroFontSize = 60;

    // For transliteration fonts.
    public static final int translitFontSize = 26;
    public static final int translitFontStyle = Font.PLAIN;

    // For input text windows.
    public static final String inputTextFontName = "SansSerif";
    public static final int inputTextFontSize = 16;

}
