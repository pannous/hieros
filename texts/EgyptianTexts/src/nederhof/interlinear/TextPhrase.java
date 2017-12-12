/***************************************************************************/
/*                                                                         */
/*  TextPhrase.java                                                        */
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

// A phrase, possibly consisting of several TierParts.

package nederhof.interlinear;

import java.util.*;

public class TextPhrase {

    // The resource in which this is phrase.
    protected TextResource resource;

    // For each tier, the TierParts.
    protected Vector<ResourcePart>[] tiers;

    // Construct new empty phrase in resource.
    public TextPhrase(TextResource resource) {
	this.resource = resource;
	tiers = new Vector[nTiers()];
	for (int i = 0; i < nTiers(); i++)
	    tiers[i] = new Vector<ResourcePart>();
    }

    // Construct new empty phrase in resource,
    // and initialize.
    public TextPhrase(TextResource resource, Vector<ResourcePart>[] tiers) {
	this.resource = resource;
	setTiers(tiers);
    }

    // Set tiers in phrase.
    public void setTiers(Vector<ResourcePart>[] tiers) {
	this.tiers = (Vector<ResourcePart>[]) tiers.clone();
    }

    // Get tier in phrase.
    public Vector<ResourcePart> getTier(int i) {
	return tiers[i];
    }

    // Get number of tiers.
    public int nTiers() {
	return resource.nTiers();
    }

    // Get name of tier.
    public String tierName(int i) {
	return resource.tierName(i);
    }

    // Is tier in phrase empty?
    public boolean isEmptyTier(int i) {
	return tiers[i].isEmpty();
    }

    // Are all tiers in phrase empty?
    public boolean tiersEmpty() {
	for (int i = 0; i < nTiers(); i++) 
	    if (!isEmptyTier(i))
		return false;
	return true;
    }

    // Raw join of two phrases. Assuming same resource.
    public static TextPhrase join(TextPhrase phrase1, TextPhrase phrase2) {
	TextPhrase join = new TextPhrase(phrase1.resource, phrase1.tiers);
	for (int i = 0; i < phrase1.nTiers(); i++)
	    join.tiers[i].addAll(phrase2.tiers[i]);
	return join;
    }


}
