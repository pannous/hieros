/***************************************************************************/
/*                                                                         */
/*  TierGather.java                                                        */
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

// Gathering tiers from resources.

package nederhof.interlinear.frame;

import java.util.*;

import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;

public class TierGather {

    // The tiers.
    public Vector<Tier> tiers = new Vector<Tier>();
    // Numbers of tiers within resources.
    public Vector<Integer> tierNums = new Vector<Integer>();
    // Short names of tiers.
    public Vector<String> labels = new Vector<String>();
    // Versions of tiers.
    public Vector<String> versions = new Vector<String>();
    // Vector of vectors of positions where phrases start.
    public Vector<Vector<Integer>> phraseStarts = new Vector<Vector<Integer>>();
    // Resource for each tier.
    public Vector<TextResource> tierResources = new Vector<TextResource>();

    // Is there more than one tier that is shown?
    public boolean severalTiersShown = false;
    // Is there more than one resource that is shown?
    public boolean severalResourcesShown = false;

    public TierGather(Vector<TextResource> resources, 
	    Vector<ResourcePrecedence> precedences, 
	    Vector<Object[]> autoaligns,
	    Autoaligner aligner,
	    RenderParameters params, boolean pdf, boolean edit) {
        TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPositions = 
	    new TreeMap<VersionSchemeLabel,Vector<int[]>>();
        TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPrePositions = 
	    new TreeMap<VersionSchemeLabel,Vector<int[]>>();
        TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPostPositions = 
	    new TreeMap<VersionSchemeLabel,Vector<int[]>>();
        TreeMap<ResourceId,int[]> resourceIdToPositions = new TreeMap<ResourceId,int[]>();
        TreeMap schemeMappings = new TreeMap();
        for (int i = 0; i < resources.size(); i++) {
            TextResource resource = resources.get(i);
            resource.addTiers(tiers, tierNums, labels, versions, phraseStarts,
                    labelToPositions, labelToPrePositions, labelToPostPositions,
                    resourceIdToPositions, schemeMappings, params, pdf, edit);
	    while (tierResources.size() < tiers.size())
		tierResources.add(resource);
        }
        for (int i = 0; i < precedences.size(); i++) {
            ResourcePrecedence prec = precedences.get(i);
            prec.addTiers(tiers, resourceIdToPositions, params);
        }
        LinkHelper.transferSchemes(labelToPositions, schemeMappings);
        LinkHelper.transferSchemes(labelToPrePositions, schemeMappings);
        LinkHelper.transferSchemes(labelToPostPositions, schemeMappings);
        LinkHelper.compile(tiers, labelToPositions, labelToPrePositions, labelToPostPositions);
	doAutoaligns(autoaligns, aligner);
        severalTiersShown = tiers.size() > 1;
	severalResourcesShown = locations(tierResources).size() > 1;
    }

    // For required tiers, do automatic alignment.
    // Find the tiers among those constructed.
    private void doAutoaligns(Vector<Object[]> autoaligns, Autoaligner aligner) {
	for (int i = 0; i < autoaligns.size(); i++) {
	    Object[] align = autoaligns.get(i);
	    TextResource resource1 = (TextResource) align[0];
	    String tierName1 = (String) align[1];
	    TextResource resource2 = (TextResource) align[2];
	    String tierName2 = (String) align[3];

	    Tier tier1 = null;
	    Tier tier2 = null;
	    int tierNum1 = -1;
	    int tierNum2 = -1;
	    int globalTierNum1 = -1;
	    int globalTierNum2 = -1;
	    Vector<Integer> phraseStarts1 = null;
	    Vector<Integer> phraseStarts2 = null;
	    for (int j = 0; j < tiers.size(); j++) {
		TextResource resource = tierResources.get(j);
		Integer numInt = tierNums.get(j);
		int num = numInt.intValue();
		String tierName = resource.tierName(num);
		if (resource1 == resource && tierName1.equals(tierName)) {
		    tier1 = tiers.get(j);
		    tierNum1 = num;
		    globalTierNum1 = j;
		    phraseStarts1 = phraseStarts.get(j);
		} else if (resource2 == resource && tierName2.equals(tierName)) {
		    tier2 = tiers.get(j);
		    tierNum2 = num;
		    globalTierNum2 = j;
		    phraseStarts2 = phraseStarts.get(j);
		}
	    }
	    if (tier1 != null && tier2 != null)
		aligner.align(tier1, tier2, resource1, resource2,
			tierNum1, tierNum2, globalTierNum1, globalTierNum2,
			phraseStarts1, phraseStarts2);
	}
    }

    // Get set of locations of shown tiers.
    private TreeSet locations(Vector resources) {
	TreeSet locations = new TreeSet();
	for (int i = 0; i < resources.size(); i++) {
	    TextResource resource = (TextResource) resources.get(i);
	    String loc = resource.getLocation();
	    locations.add(loc);
	}
	return locations;
    }

}
