/***************************************************************************/
/*                                                                         */
/*  BoxPlaces.java                                                         */
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

// Locations of glyphs belonging to box type.

package nederhof.res;

public class BoxPlaces {

    public GlyphPlace open = new GlyphPlace();
    public GlyphPlace segment = new GlyphPlace();
    public GlyphPlace close = new GlyphPlace();

    // Is known?
    public boolean isKnown() {
	return open.isKnown() && segment.isKnown() && close.isKnown();
    }
}

