/***************************************************************************/
/*                                                                         */
/*  VectorAux.java                                                         */
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

import java.util.*;

public class VectorAux {

    // Reverse vector.
    public static Vector mirror(Vector in) {
	Vector out = new Vector(in.size());
	for (int i = in.size() - 1; i >=0; i--)
	    out.add(in.get(i));
	return out;
    }

}
