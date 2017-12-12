/***************************************************************************/
/*                                                                         */
/*  ResShadeHelper.java                                                    */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res;

import java.util.*;

public class ResShadeHelper {

    // Make copy of vector of shades.
    public static Vector clone(Vector shades) {
	return (Vector) shades.clone();
    }

}
