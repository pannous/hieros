/***************************************************************************/
/*                                                                         */
/*  GlyphPlace.java                                                        */
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

package nederhof.res;

public class GlyphPlace {

    public int file = 0;
    public int index = 0;

    // Create unknown glyph.
    public GlyphPlace() {
    }

    // Create known glyph.
    public GlyphPlace(int file, int index) {
	this.file = file;
	this.index = index;
    }

    // Is known?
    public boolean isKnown() {
	return file > 0 && index > 0;
    }
}
