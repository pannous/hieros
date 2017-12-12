/***************************************************************************/
/*                                                                         */
/*  AlignConstraints.java                                                  */
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

// Constraints on automatic alignment.

package nederhof.alignment;

import java.util.*;

import nederhof.interlinear.*;

public class AlignConstraints {

    // Get restrictions imposed by precedence relations, between two tiers.
    public static int[] getRestrictions(Tier tier1, Tier tier2,
            Vector phraseStarts1, Vector phraseStarts2) {
        int[] precedence1 = new int[phraseStarts1.size()];
        for (int phrase1 = 0; phrase1 < phraseStarts1.size(); phrase1++)
            precedence1[phrase1] = 0;
        int[] posToPhrase1 = getPositionPhraseMapping(tier1, phraseStarts1);
        for (int phrase2 = 0; phrase2 < phraseStarts2.size(); phrase2++) {
            Integer currentInt = (Integer) phraseStarts2.get(phrase2);
            int current = currentInt.intValue();
            int next;
            if (phrase2 < phraseStarts2.size() - 1) {
                Integer nextInt = (Integer) phraseStarts2.get(phrase2+1);
                next = nextInt.intValue();
            } else
                next = tier2.nSymbols();
            for (int j = current; j < next; j++) {
                TreeSet precedings = tier2.precedings(j);
                for (Iterator it = precedings.iterator(); it.hasNext(); ) {
                    TierPos pos = (TierPos) it.next();
                    if (pos.tier == tier1) {
                        int phrase1 = posToPhrase1[pos.pos];
			if (phrase1 < precedence1.length) 
			    precedence1[phrase1] =
				Math.max(precedence1[phrase1], phrase2);
                    }
                }
            }
        }
        return precedence1;
    }

    // Map positions to number of the next phrase.
    private static int[] getPositionPhraseMapping(Tier tier, Vector phraseStarts) {
        int[] posToPhrase = new int[tier.nSymbols()];
	posToPhrase[0] = 0;
        for (int i = 0; i < phraseStarts.size(); i++) {
            Integer currentInt = (Integer) phraseStarts.get(i);
            int current = currentInt.intValue();
	    if (i == 0) // make sure all are initialized
		current = 0;
            int next;
            if (i < phraseStarts.size() - 1) {
                Integer nextInt = (Integer) phraseStarts.get(i+1);
                next = nextInt.intValue();
            } else
                next = tier.nSymbols() - 1;
            for (int j = current + 1; j <= next; j++)
                posToPhrase[j] = i+1;
        }
        return posToPhrase;
    }

}
