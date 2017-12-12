/***************************************************************************/
/*                                                                         */
/*  Substring.java                                                         */
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

package nederhof.res.editor;

// Substring in scanned input.
public class Substring {
    public int start;
    public int end;

    public Substring(int start, int end) {
	this.start = start;
	this.end = end;
    }
}
