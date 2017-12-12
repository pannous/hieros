/***************************************************************************/
/*                                                                         */
/*  EgyptianAutoaligner.java                                               */
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

// Align tiers.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.alignment.*;
import nederhof.alignment.simple.*;
import nederhof.interlinear.*;

public class EgyptianAutoaligner implements Autoaligner {

    public void align(Tier tier1, Tier tier2,
	    TextResource resource1, TextResource resource2,
	    int tierNum1, int tierNum2,
	    int globalTierNum1, int globalTierNum2,
	    Vector<Integer> phraseStarts1, Vector<Integer> phraseStarts2) {
	String type1 = resource1.tierName(tierNum1);
	String type2 = resource2.tierName(tierNum2);
	if (type1.equals("translation") && type2.equals("translation")) {
	    String lang1 = resource1.getStringProperty("language");
	    String lang2 = resource2.getStringProperty("language");
	    // languages not distinguished at this time
	    WordAligner.align(tier1, tier2, 
		    resource1, resource2,
		    tierNum1, tierNum2,
		    globalTierNum1, globalTierNum2,
		    phraseStarts1, phraseStarts2);
	} else if ((type1.equals("translation") || type1.equals("transliteration")) &&
	    (type2.equals("translation") || type2.equals("transliteration"))) {
	    WordAligner.align(tier1, tier2, 
		    resource1, resource2,
		    tierNum1, tierNum2,
		    globalTierNum1, globalTierNum2,
		    phraseStarts1, phraseStarts2);
	} else if (type1.equals("hieroglyphic") && type2.equals("transliteration")) {
	    HieroTransAligner.align(tier1, tier2,
		    resource1, resource2,
		    tierNum1, tierNum2,
		    globalTierNum1, globalTierNum2,
		    phraseStarts1, phraseStarts2);
	} else if (type1.equals("transliteration") && type2.equals("hieroglyphic")) {
	    HieroTransAligner.align(tier2, tier1,
		    resource2, resource1,
		    tierNum2, tierNum1,
		    globalTierNum2, globalTierNum1,
		    phraseStarts2, phraseStarts1);
	} else if (type1.equals("hieroglyphic") && type2.equals("orthographic")) {
	    HieroOrthoAligner.align(tier1, tier2,
		    resource1, resource2,
		    tierNum1, tierNum2,
		    globalTierNum1, globalTierNum2,
		    phraseStarts1, phraseStarts2);
	} else if (type1.equals("orthographic") && type2.equals("hieroglyphic")) {
	    HieroOrthoAligner.align(tier2, tier1,
		    resource2, resource1,
		    tierNum2, tierNum1,
		    globalTierNum2, globalTierNum1,
		    phraseStarts2, phraseStarts1);
	} else if (type1.equals("hieroglyphic") && type2.equals("signplaces")) {
	    HieroImageAligner.align(tier1, tier2,
		    resource1, resource2,
		    tierNum1, tierNum2,
		    globalTierNum1, globalTierNum2,
		    phraseStarts1, phraseStarts2);
	} else if (type1.equals("signplaces") && type2.equals("hieroglyphic")) {
	    HieroImageAligner.align(tier2, tier1,
		    resource2, resource1,
		    tierNum2, tierNum1,
		    globalTierNum2, globalTierNum1,
		    phraseStarts2, phraseStarts1);
	}
    }

}
