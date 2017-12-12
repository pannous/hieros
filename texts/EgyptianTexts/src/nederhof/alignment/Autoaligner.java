/***************************************************************************/
/*                                                                         */
/*  Autoaligner.java                                                       */
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

// An autoaligner aligns two tiers.

package nederhof.alignment;

import java.util.*;

import nederhof.interlinear.*;

public interface Autoaligner {

    // Align two tiers. The resources and the numbers of the tiers
    // within the resources are also given, as these may provide
    // extra information e.g. about used languages.
    public void align(Tier tier1, Tier tier2,
	    TextResource resource1, TextResource resource2,
	    int tierNum1, int tierNum2,
	    int globalTierNum1, int globalTierNum2,
	    Vector<Integer> phraseStarts1, Vector<Integer> phraseStarts2);

}
