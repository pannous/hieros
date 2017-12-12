/***************************************************************************/
/*                                                                         */
/*  WordAligner.java                                                       */
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

// Divide text of two tiers into phrases and words.
// Align the two tiers.

package nederhof.alignment.simple;

import java.util.*;

import nederhof.alignment.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.*;

public class WordAligner {

    public static void align(final Tier tier1, final Tier tier2,
            TextResource resource1, TextResource resource2,
            int tierNum1, int tierNum2,
            int globalTierNum1, int globalTierNum2,
            final Vector<Integer> phraseStarts1, final Vector<Integer> phraseStarts2) {
	int[] phraseSizes1 = getPhraseSizes(tier1, phraseStarts1);
	int[] phraseSizes2 = getPhraseSizes(tier2, phraseStarts2);
	int[] prec1 = AlignConstraints.getRestrictions(tier1, tier2,
		phraseStarts1, phraseStarts2);
	int[] prec2 = AlignConstraints.getRestrictions(tier2, tier1,
		phraseStarts2, phraseStarts1);
	new SimpleAligner(phraseSizes1, phraseSizes2, prec1, prec2) {
	    public void matchPhrases(int inNum, int outNum) {
		Integer inInt = phraseStarts1.get(inNum);
		Integer outInt = phraseStarts2.get(outNum);
		int inPos = inInt.intValue();
		int outPos = outInt.intValue();
		tier1.addBreak(inPos, new TierPos(tier2, outPos));
		tier2.addBreak(outPos, new TierPos(tier1, inPos));
	    }
	};
    }

    // Take tier, divide it into phrases and counts word in each phrase.
    private static int[] getPhraseSizes(Tier tier, Vector<Integer> phraseStarts) {
	int[] phraseSizes = new int[phraseStarts.size()];
	for (int i = 0; i < phraseStarts.size(); i++) {
	    Integer currentInt = phraseStarts.get(i);
	    int current = currentInt.intValue();
	    int next;
	    if (i < phraseStarts.size() - 1) {
		Integer nextInt = phraseStarts.get(i+1);
		next = nextInt.intValue();
	    } else
		next = tier.nSymbols();
	    int nBreakable = 1;
	    for (int j = current + 1; j < next - 1; j++)
		if (tier.breakable(j))
		    nBreakable++;
	    phraseSizes[i] = nBreakable;
	}
	return phraseSizes;
    }

}
