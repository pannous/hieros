// Various constants.

package nederhof.ocr.hiero;

import java.awt.*;

class HieroSettings {

    // Editor of notes at hieroglyphic.
    public static final int hieroNoteEditorWidth = 700;
    public static final int hieroNoteEditorHeight = 400;

    // Size of hieroglyphic font in preview. and notes.
    public static final int previewHieroFontSize = 35;
    public static final int previewNoteFontSize = 14;
    // Size of hieroglyphic font in combobox.
    public static final int comboHieroFontSize = 30;
    // Size of hieroglyphic font in RES editor.
    public static final int editPreviewHieroFontSize = 50;
    public static final int editTreeHieroFontSize = 60;

    // For input text windows.
    public static final String inputTextFontName = "SansSerif";
    public static final int inputTextFontSize = 16;

    // Font in labels.
    public static final Font labelFont =
			new Font("SansSerif", Font.BOLD, 14);

    // Default directory of prototypes.
    public static final String hieroProtoDir = "paleo/sethe";

    // Segment detection starts after selecting lines.
    public static final boolean autoSegment = true;
    // OCR starts after selecting lines.
    public static final boolean autoOcr = true;
    // Formatting done after OCR.
    public static final boolean autoFormat = false;

    // Number of candidates investigated by OCR.
    public static final int beam = 20;
    // Number of candidates returned by OCR.
    public static final int nCandidates = 5;
}
