/***************************************************************************/
/*                                                                         */
/*  OpenPoint.java                                                         */
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

// Open tag for position.

package nederhof.align;

class OpenPoint extends BoundaryPoint {
    public OpenPoint(int type, Pos pos, boolean isAlign) {
	super(type, pos, isAlign);
    }
}
