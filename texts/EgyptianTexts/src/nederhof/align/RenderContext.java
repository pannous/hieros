/***************************************************************************/
/*                                                                         */
/*  RenderContext.java                                                     */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Offers the parameters for printing.

package nederhof.align;

import java.awt.*;

import nederhof.res.*;

interface RenderContext {

    // Left boundary of page.
    public float leftBound();

    // Width of indication of creator, if any.
    public float creatorWidth();

    // Where text starts.
    public float textOffset();

    // Right boundary of page.
    public float rightBound();

    // Should creator be indicated at beginning of lines?
    public boolean mentionCreator();
    // Should version/scheme be indicated at beginning of lines?
    public boolean mentionVersion();

    // The NORMAL_FONT is non-italic, non-bold, for use in preamble.
    // The header fonts are bold, for use in preamble.
    public static final int LATIN_FONT = 0;
    public static final int FOOT_LATIN_FONT = 1;
    public static final int NORMAL_FONT = 10;
    public static final int ITALIC_FONT = 20;
    public static final int FOOT_ITALIC_FONT = 21;
    public static final int HEADER1_FONT = 30;
    public static final int HEADER2_FONT = 31;
    public static final int HEADER3_FONT = 32;
    public static final int EGYPT_FONT = 40;
    public static final int FOOT_EGYPT_FONT = 41;
    public static final int HIERO_FONT = 50;
    public static final int FOOT_HIERO_FONT = 51;
    public static final int LX = 60;

    // Font metrics for above font codes.
    public GeneralFontMetrics getFontMetrics(int f);

    // Get mapping from ASCII to transliteration font.
    public TrMap getEgyptMap();

    // The amount that footnote markers are to be raised above baseline,
    // as factor of ascent.
    public float getFootRaise();

    // Get environments for hieroglyphic.
    public HieroRenderContext getHieroContext();
    public HieroRenderContext getFootHieroContext();

    // Is color allowed in hieroglyphic?
    public boolean hieroColor();
    // Get Color to be used for points.
    public Color getPointColor();
    // Get Color to be used for labels.
    public Color getLabelColor();
    // Get Color to be used for footnote markers.
    public Color getNoteColor();

    // The horizontal separation between lexical entries.
    public float getLxSep();
    // The vertical separation between lexical entries.
    public float getLxLeading();
    // The margin inside lexical entries.
    public float getLxInnerMargin();
    // The width of lines around lexical entries.
    public float getLxLineThickness();
    // Of lexical entries only the textal part is printed.
    public boolean lxAbbreviated();

    // Are lines to be made all uniform ascent, i.e. to be at least
    // as much as point marker.
    public boolean uniformAscent();

    // All footnotes collected at end of text.
    public boolean collectNotes();
}
