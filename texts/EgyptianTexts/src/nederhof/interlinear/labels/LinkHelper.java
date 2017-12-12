/***************************************************************************/
/*                                                                         */
/*  LinkHelper.java                                                        */
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

// Make links between tiers based on labels and positions.

package nederhof.interlinear.labels;

import java.util.*;

import nederhof.interlinear.*;

public class LinkHelper {

    // Merge positions due to schemeMappings.
    public static void transferSchemes(TreeMap toPositions, TreeMap schemeMap) {
	Vector keys = new Vector(toPositions.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    VersionSchemeLabel label = (VersionSchemeLabel) keys.get(i);
	    if (schemeMap.get(label) != null) {
		Vector positions = (Vector) toPositions.get(label);
		VersionSchemeLabel label2 = (VersionSchemeLabel) schemeMap.get(label);
		if (toPositions.get(label2) == null)
		    toPositions.put(label2, new Vector());
		Vector positions2 = (Vector) toPositions.get(label2);
		if (positions2 != positions) {
		    positions2.addAll(positions);
		    positions.clear();
		}
	    }
	}
    }

    // Process various relations between positions into precedence
    // relations.
    public static void compile(Vector tiers, TreeMap labelToPositions,
	    TreeMap labelToPrePositions, TreeMap labelToPostPositions) {
	for (Iterator it = labelToPositions.keySet().iterator(); 
		it.hasNext(); ) {
	    VersionSchemeLabel fullLabel = (VersionSchemeLabel) it.next();
	    Vector positions = (Vector) labelToPositions.get(fullLabel);
	    Vector prePositions = (Vector) labelToPrePositions.get(fullLabel);
	    Vector postPositions = (Vector) labelToPostPositions.get(fullLabel);
	    for (int i = 0; i < positions.size(); i++) 
		for (int j = 0; j < positions.size(); j++) 
		    if (i != j) {
			int[] tierpos1 = (int[]) positions.get(i);
			int[] tierpos2 = (int[]) positions.get(j);
			Tier tier1 = (Tier) tiers.get(tierpos1[0]);
			Tier tier2 = (Tier) tiers.get(tierpos2[0]);
			int pos1 = tierpos1[1];
			int pos2 = tierpos2[1];
			tier1.addPreceding(pos1, 
				new TierPos("start", tier2, pos2));
		    }
	    if (prePositions != null) 
		for (int j = 0; j < positions.size(); j++) {
		    int[] tierpos1 = (int[]) positions.get(j);
		    Tier tier1 = (Tier) tiers.get(tierpos1[0]);
		    int pos1 = tierpos1[1];
		    for (int i = 0; i < prePositions.size(); i++) {
			int[] tierpos2 = (int[]) prePositions.get(i);
			Tier tier2 = (Tier) tiers.get(tierpos2[0]);
			int pos2 = tierpos2[1];
			if (0 <= pos2 && pos2 < tier2.nSymbols())
			    tier1.addPreceding(pos1, 
				    new TierPos("start", tier2, pos2));
		    }
		}
	    if (postPositions != null)
		for (int j = 0; j < positions.size(); j++) {
		    int[] tierpos1 = (int[]) positions.get(j);
		    Tier tier1 = (Tier) tiers.get(tierpos1[0]);
		    int pos1 = tierpos1[1];
		    for (int i = 0; i < prePositions.size(); i++) {
			int[] tierpos2 = (int[]) prePositions.get(i);
			Tier tier2 = (Tier) tiers.get(tierpos2[0]);
			int pos2 = tierpos2[1];
			if (0 <= pos2 && pos2 < tier2.nSymbols())
			    tier2.addPreceding(pos2, 
				    new TierPos("start", tier1, pos1));
		    }
		}
	}
    }

    // Link positions that correspond to the same beginning of phrase.
    public static void alignPhrases(Vector<Tier> tiers, 
	    	TreeMap<Integer,Vector<int[]>> phraseToPositions) {
        for (Iterator<Vector<int[]>> it = phraseToPositions.values().iterator();
                it.hasNext(); ) {
            Vector<int[]> poss = it.next();
            for (int i = 0; i < poss.size() - 1; i++) {
                int[] tierpos1 = poss.get(i);
                int[] tierpos2 = poss.get(i+1);
		Tier tier1 = tiers.get(tierpos1[0]);
		Tier tier2 = tiers.get(tierpos2[0]);
		int pos1 = tierpos1[1];
		int pos2 = tierpos2[1];
		tier1.addPreceding(pos1, new TierPos("start", tier2, pos2));
		tier2.addPreceding(pos2, new TierPos("start", tier1, pos1));
            }
        }
    }

    // Link positions in precedence relations within one resource.
    // Remove links that lead to non-existent positions.
    public static void alignResourcePrecedence(Vector<Tier> tiers, 
	    String location1, String location2,
	    PosPrecedence precedence, 
	    TreeMap<ResourceId,int[]> resourceIdToPositions) {
	LinkedList<Link> missing = new LinkedList<Link>();
	for (Iterator it = precedence.iterator(); it.hasNext(); ) {
	    Link link = (Link) it.next();
	    ResourceId resId1 = new ResourceId(location1, link.id1);
	    ResourceId resId2 = new ResourceId(location2, link.id2);
	    int[] tierpos1 = resourceIdToPositions.get(resId1);
	    int[] tierpos2 = resourceIdToPositions.get(resId2);
	    if (tierpos1 != null && tierpos2 != null) {
		Tier tier1 = tiers.get(tierpos1[0]);
		Tier tier2 = tiers.get(tierpos2[0]);
		int pos1 = tierpos1[1];
		int pos2 = tierpos2[1];
		if (pos1 >= 0 && pos1 < tier1.nSymbols() &&
			pos2 >= 0 && pos2 < tier2.nSymbols())
		    tier2.addManualPreceding(pos2, 
			    new TierPos(link.type1, tier1, pos1));
	    } else 
		missing.add(link);
	}
	precedence.removeAll(missing);
    }

    // Link positions in precedence relations within two resources.
    // Remove links that lead to non-existent positions.
    public static void alignResourcePrecedence(Vector<Tier> tiers, 
	    String location1, String location2,
	    InterPosPrecedence precedence, 
	    TreeMap<ResourceId,int[]> resourceIdToPositions) {
	LinkedList<OffsetLink> missing = new LinkedList<OffsetLink>();
	for (Iterator<OffsetLink> it = precedence.iterator(); it.hasNext(); ) {
	    OffsetLink link = it.next();
	    ResourceId resId1 = new ResourceId(location1, link.id1);
	    ResourceId resId2 = new ResourceId(location2, link.id2);
	    int[] tierpos1 = resourceIdToPositions.get(resId1);
	    int[] tierpos2 = resourceIdToPositions.get(resId2);
	    if (tierpos1 != null && tierpos2 != null) {
		Tier tier1 = tiers.get(tierpos1[0]);
		Tier tier2 = tiers.get(tierpos2[0]);
		int pos1 = tierpos1[1] + link.offset1;
		int pos2 = tierpos2[1] + link.offset2;
		if (pos1 >= 0 && pos1 < tier1.nSymbols() &&
			pos2 >= 0 && pos2 < tier2.nSymbols())
		    tier2.addManualPreceding(pos2, 
			    new TierPos(link.type1, tier1, pos1));
	    } else 
		missing.add(link);
	}
	precedence.removeAll(missing);
    }

}
