/***************************************************************************/
/*                                                                         */
/*  CharacterAux.java                                                      */
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

package nederhof.util;

import java.awt.event.*;

public class CharacterAux {

    public static boolean isPrintableChar(char c) {
	Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
	return (!Character.isISOControl(c)) &&
	    c != KeyEvent.CHAR_UNDEFINED &&
	    block != null &&
	    block != Character.UnicodeBlock.SPECIALS;
    }

}

