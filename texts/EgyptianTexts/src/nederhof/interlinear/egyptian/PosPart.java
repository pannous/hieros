/***************************************************************************/
/*                                                                         */
/*  PosPart.java                                                           */
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

// Position part of resource.

package nederhof.interlinear.egyptian;

import nederhof.interlinear.*;

class PosPart implements ResourcePart {

    public int symbol;
    public String id;

    public PosPart(int symbol, String id) {
	this.symbol = symbol;
	this.id = id;
    }

}
