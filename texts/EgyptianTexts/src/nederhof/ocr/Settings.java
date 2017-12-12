// Various constants.

package nederhof.ocr;

import java.awt.*;

class Settings {

    // Initial dimensions of entire screen of viewer.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 740;

    // Maximum image height.
    public static final int maxImageHeight = 1000;

    // Margin around preview, relative to unit size.
    public static final float previewMargin = 0.3f;

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

    // Circle size in image editor.
    public static final int circleSize = 10;
}
