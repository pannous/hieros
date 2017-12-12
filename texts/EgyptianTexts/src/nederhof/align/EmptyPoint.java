/***************************************************************************/
/*                                                                         */
/*  EmptyPoint.java                                                        */
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

// Indication of point, i.e. coord or align tag, but with
// empty content.

package nederhof.align;

class EmptyPoint extends Point {

    // Constructor.
    public EmptyPoint(int type, Pos pos, boolean isAlign) {
	super(type, pos, isAlign);
    }

    // See Elem.
    // Printable when not phrasal, is not align tag, and
    // position tag does not start with @ or #.
    public boolean isPrintable() {
	Pos pos = getPos();
	String tag = pos.getTag();
	return !pos.isPhrasal() &&
	    !isAlign() &&
	    !tag.startsWith("@") &&
	    !tag.startsWith("#");
    }

}
