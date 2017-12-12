/***************************************************************************/
/*                                                                         */
/*  ContentMatter.java                                                     */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res.mdc;

import nederhof.res.*;

// Content of RES/AELalign in intermediate stage, in translation
// from MdC.
class ContentMatter {

    // Past string.
    StringBuffer past = new StringBuffer();

    // Buffer for RES hieroglyphic under construction, if applicable.
    SwitchAndHieroglyphic current = null;

    public ContentMatter() {
    }

    // Append content, joining hieroglyphic if applicable.
    public ContentMatter add(Object next) {
	if (next instanceof SwitchAndTopgroup) {
	    SwitchAndTopgroup nextExpr = (SwitchAndTopgroup) next;
	    if (current == null)
		current = new SwitchAndHieroglyphic(nextExpr);
	    else 
		current.add(nextExpr);
	} else {
	    emptyCurrent();
	    past.append("" + next);
	} 
	return this;
    }

    // Preferred length of lines containing RES.
    private final static int RES_LENGTH = 70;

    // Clear buffer.
    private void emptyCurrent() {
	if (current != null) {
	    ResFragment res = current.res();
	    String resString = res.toString(RES_LENGTH - lineLength(), RES_LENGTH);
	    past.append(resString);
	}
	current = null;
    }

    // Length of current line, i.e. number of characters until last newline.
    private int lineLength() {
	int count = 0;
	for (int i = past.length() - 1; i >= 0; i--) {
	    if (past.charAt(i) == '\n')
		break;
	    count++;
	}
	return count;
    }

    // Total content as string.
    public String toString() {
	emptyCurrent();
	return past.toString();
    }

}
