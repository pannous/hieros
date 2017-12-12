/***************************************************************************/
/*                                                                         */
/*  BoundaryPoint.java                                                     */
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

// An indication of a point with non-empty content, 
// so this can be open or close.
// Such a marker is never printable.
// We record whether content is breakable.
//
// Subclasses:
// OpenPoint
// ClosePoint

package nederhof.align;

class BoundaryPoint extends Point {

    // Should be made true when content consists of single word, so no spaces. 
    // In general, reflects whether content is breakable.
    private boolean shortContent;

    // Constructor.
    public BoundaryPoint(int type, Pos pos, boolean isAlign) {
	super(type, pos, isAlign);
	shortContent = false;
    }

    public void setShortContent() {
	shortContent = true;
    }

    public boolean hasShortContent() {
	return shortContent;
    }

    // See Elem.
    // Never printable.
    public boolean isPrintable() {
	return false;
    }
}
