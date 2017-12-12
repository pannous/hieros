/***************************************************************************/
/*                                                                         */
/*  InterlinearFormatting.java                                             */
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

// Given tiers and bound on page width, make interlinear form.

package nederhof.interlinear;

import java.util.*;

import nederhof.util.*;

public abstract class InterlinearFormatting {

    // Number of tiers.
    private int nTiers;
    // Input tiers.
    private Tier[] tiers;

    // The modes of the tiers.
    private String[] modes;

    // From which positions should we make next section.
    private int[] positions;

    // For current section: spans for each tier.
    protected Vector<TierSpan>[] sectionSpans;
    // Mapping from beginpositions in spans in current section to horizontal locations.
    private TreeMap<Integer,Float>[] sectionSpanLocations;

    // While constructing one section:
    // Penalty of best state so far.
    private double bestPenalty;
    // Copy of best state so far.
    private Vector<TierSpan>[] bestSectionSpans;
    private TreeMap<Integer,Float>[] bestSectionSpanLocations;

    // Create section of interlinear form.
    public InterlinearFormatting(Vector<Tier> tiers) {
	nTiers = tiers.size();
	this.tiers = new Tier[nTiers];
	modes = new String[nTiers];
	positions = new int[nTiers];
	for (int i = 0; i < nTiers; i++) {
	    this.tiers[i] = tiers.get(i);
	    modes[i] = this.tiers[i].getMode();
	    positions[i] = 0;
	}
	sectionSpans = new Vector[nTiers];
	bestSectionSpans = new Vector[nTiers];
	sectionSpanLocations = new TreeMap[nTiers];
	bestSectionSpanLocations = new TreeMap[nTiers];
	while (!tiersExhausted()) {
	    makeEmptySection();
	    fillSection();
	    chooseBestSection();
	}
    }

    // Initialize new section.
    private void makeEmptySection() {
	for (int i = 0; i < nTiers; i++) {
	    int pos = positions[i];
	    clearSpans(i);
	    pushSpan(i, new TierSpan(tiers[i], pos));
	    clearSpanLocations(i);
	    setSpanLocation(i, pos, 0);
	}
	bestPenalty = Double.MAX_VALUE;
    }

    // Fill section.
    private void fillSection() {
	addSpans(true); // must return true
	improvePenalty();
	while (!sectionExhausts() && addSpans(false)) 
	    improvePenalty();
    }

    // Add new batch of spans. Return if successful.
    // Failure is not permitted at the beginning of the section,
    // so the width is ignored and all components are placed.
    private boolean addSpans(boolean ignoreWidth) {
	Vector<TreeSet<TierSpan>> spanSets = minimalSpanComponents();
	if (ignoreWidth) {
	    for (int i = 0; i < spanSets.size(); i++) {
		TreeSet<TierSpan> spanSet = spanSets.get(i);
		TreeMap<TierSpan,Float> locations = solveConstraints(spanSet);
		addSpans(spanSet, locations);
	    }
	    return true;
	} else {
	    TreeSet<TierSpan> spanSet = leftMostSet(spanSets);
	    TreeMap<TierSpan,Float> locations = solveConstraints(spanSet);
	    float rightmost = rightMostLocation(spanSet, locations);
	    if (rightmost < width()) {
		addSpans(spanSet, locations);
		return true;
	    } else 
		return false;
	}
    }

    // Get possible next spans in tiers. These are next unbreakable
    // spans, plus those that they are related to by precedence and
    // break links. We only consider the first so-many spans following
    // the last added span; this is to avoid ill-aligned tiers from
    // leading to inclusion of the complete remainder of the text.
    private Vector<TreeSet<TierSpan>> minimalSpanComponents() {
	TreeSet<TierSpan> nextSpans = new TreeSet<TierSpan>();
	for (int i = 0; i < nTiers; i++) 
	    if (!sectionExhausts(i)) {
		TierSpan span = nextSpan(i);
		nextSpans.add(span);
	    }
	TreeSet<TierSpan> agenda = (TreeSet<TierSpan>) nextSpans.clone();
	DirectedGraph<TierSpan> precedeGraph = new DirectedGraph<TierSpan>();
	closeSpanSet(nextSpans, agenda, precedeGraph);
	return precedeGraph.minimalComponents();
    }

    // Repeatedly take an element from the agenda, add related
    // spans, until agenda is exhausted.
    // Keep precedence and break links between spans. 
    private void closeSpanSet(TreeSet<TierSpan> spans, TreeSet<TierSpan> agenda, 
	    DirectedGraph<TierSpan> precedeGraph) {
	while (!agenda.isEmpty()) {
	    TierSpan el = agenda.last();
	    agenda.remove(el);
	    precedeGraph.addVertex(el);
	    TreeSet<TierSpan> precedings = el.precedingUnbreakableSpans();
	    for (Iterator<TierSpan> it = precedings.iterator(); it.hasNext(); ) {
		TierSpan preceding = it.next();
		TierSpan rightSpan = 
		    inRightContext(preceding, spans, agenda, precedeGraph);
		if (rightSpan != null)
		    precedeGraph.addEdge(el, rightSpan);
	    }
	    TreeSet<TierSpan> linked = el.linkedUnbreakableSpans();
	    for (Iterator<TierSpan> it = linked.iterator(); it.hasNext(); ) {
		TierSpan preceding = it.next();
		TierSpan rightSpan =
		    inRightContext(preceding, spans, agenda, precedeGraph);
		if (rightSpan != null)
		    precedeGraph.addEdge(el, rightSpan);
	    }
	}
    }

    // How many spans we consider next to spans already located.
    private final int NEXT_SPAN_LIMIT = 3;

    // An unbreakable span that is not too far in the right context,
    // ideally equal to argument span.
    // Also gather intermediate spans, with links between them.
    private TierSpan inRightContext(TierSpan span, TreeSet<TierSpan> spans, 
	    TreeSet<TierSpan> agenda, DirectedGraph<TierSpan> precedeGraph) {
	int tier = span.tier.id();
	TierSpan inter = nextSpan(tier);
	if (inter == null || span.fromPos < inter.fromPos)
	    return null; // tier exhausted or span lies in past
	for (int i = 0; i < NEXT_SPAN_LIMIT; i++) 
	    if (inter.equals(span))
		return span;
	    else if (inter.hasNextUnbreakable()) {
		TierSpan nextSpan = inter.nextUnbreakable();
		addSpan(nextSpan, spans, agenda);
		precedeGraph.addEdge(nextSpan, inter);
		inter = nextSpan;
	    } else 
		break;
	return inter;
    }

    // Add span to set. If not present yet, add to agenda.
    private void addSpan(TierSpan span, TreeSet<TierSpan> spans, 
	    TreeSet<TierSpan> agenda) {
	if (!spans.contains(span)) {
	    spans.add(span);
	    agenda.add(span);
	}
    }

    // Of all sets of tiers, that one that is leftmost.
    private TreeSet<TierSpan> leftMostSet(Vector<TreeSet<TierSpan>> spanSets) {
	float bestLoc = Float.MAX_VALUE;
	TreeSet<TierSpan> bestSet = null;
	for (int i = 0; i < spanSets.size(); i++) {
	    TreeSet<TierSpan> spanSet = spanSets.get(i);
	    float loc = rightMostLocation(spanSet);
	    if (loc < bestLoc) {
		bestLoc = loc;
		bestSet = spanSet;
	    }
	}
	return bestSet;
    }

    // Of all tiers represented in spans, take one that is rightmost.
    private float rightMostLocation(TreeSet<TierSpan> spans) {
	float rightmost = 0;
	for (Iterator<TierSpan> it = spans.iterator(); it.hasNext(); ) {
	    TierSpan span = it.next();
	    int tier = span.tier.id();
	    rightmost = Math.max(rightmost, dist(tier));
	}
	return rightmost;
    }

    // Find precedence relations between spans in set, and translate
    // these to inequalities. Constrain spans to be to the right of current
    // position of tier.
    private TreeMap<TierSpan,Float> solveConstraints(TreeSet<TierSpan> spans) {
	situateSpans(spans);
	InequalitySolver<TierSpan> solver = new InequalitySolver<TierSpan>(spans);
	TreeMap<TierSpan,Float> constraints = new TreeMap();
	for (Iterator<TierSpan> it = spans.iterator(); it.hasNext(); ) {
	    TierSpan span = it.next();
	    float minLoc = dist(span.tier.id());
	    for (int pos = span.fromPos; pos < span.toPos; pos++) {
		TreeSet<TierPos> precedes = span.tier.precedings(pos);
		for (Iterator<TierPos> itPrec = precedes.iterator(); itPrec.hasNext(); ) {
		    TierPos precede = itPrec.next();
		    TierSpan precSpan = precede.unbreakableSpan();
		    if (spans.contains(precSpan)) { // in same component
			float precLoc = precSpan.dist(precede.pos);
			if (precede.type.equals("after"))
			    precLoc += precSpan.tier.advance(precede.pos, precede.pos + 1);
			float diff = span.dist(pos) - precLoc;
			solver.addInequality(precSpan, span, diff);
		    } else {
			int precTier = precede.tier.id();
			float precLoc = posLocation(precTier, precede.pos, precede.type);
			if (precLoc > 0) { // already located
			    float loc = precLoc - span.dist(pos);
			    minLoc = Math.max(minLoc, loc);
			}
		    }
		}
	    }
	    if (span.hasPrevUnbreakable()) { // previous span in same tier
		TierSpan prevSpan = span.prevUnbreakable();
		if (spans.contains(prevSpan)) { // and in same component
		    float diff = prevSpan.dist();
		    solver.addInequality(prevSpan, span, -diff);
		} 
	    }
	    constraints.put(span, new Float(minLoc));
	}
	return solver.smallestSolution(constraints);
    }

    // Situate all spans in set.
    private void situateSpans(TreeSet<TierSpan> spans) {
	for (Iterator<TierSpan> it = spans.iterator(); it.hasNext(); ) {
	    TierSpan span = it.next();
	    span.situate();
	}
    }

    // Get rightmost location of spans, given locations of beginning.
    private float rightMostLocation(TreeSet<TierSpan> spans, TreeMap<TierSpan,Float> locations) {
	float rightmost = 0;
	for (Iterator<TierSpan> it = spans.iterator(); it.hasNext(); ) {
	    TierSpan span = it.next();
	    Float beginLoc = locations.get(span);
	    float endLoc = beginLoc.floatValue() + span.width();
	    rightmost = Math.max(rightmost, endLoc);
	}
	return rightmost;
    }

    // Some small number, sufficient to take care of rounding
    // off errors in horizontal positions.
    private final float ROUNDOFFMARGIN = 0.001f;

    // Add spans to section. If not located after advance of previous, 
    // then merge with previous span. Otherwise append.
    private void addSpans(TreeSet<TierSpan> spans, TreeMap<TierSpan,Float> locations) {
	for (Iterator<TierSpan> it = spans.iterator(); it.hasNext(); ) {
	    TierSpan span = it.next();
	    int tier = span.tier.id();
	    Float beginLoc = locations.get(span);
	    float loc = beginLoc.floatValue();
	    if (loc > dist(tier) + ROUNDOFFMARGIN && 
		    loc >= advance(tier)) {
		pushSpan(tier, span);
		setSpanLocation(tier, span.fromPos, loc);
	    } else 
		extendLast(tier, span.toPos);
	}
    }

    // Try improving best penalty so far.
    private void improvePenalty() {
	double penalty = penalty();
	if (penalty <= bestPenalty) {
	    for (int i = 0; i < nTiers; i++) {
		bestSectionSpans[i] = (Vector<TierSpan>) sectionSpans[i].clone();
		bestSectionSpanLocations[i] = (TreeMap<Integer,Float>) sectionSpanLocations[i].clone();
	    }
	    bestPenalty = penalty;
	}
    }

    // Choose best state from among states. 
    private void chooseBestSection() {
	int[] lastPositions = new int[nTiers];
	for (int i = 0; i < nTiers; i++) {
	    sectionSpans[i] = bestSectionSpans[i];
	    sectionSpanLocations[i] = bestSectionSpanLocations[i];
	    lastPositions[i] = lastSpanEnd(i);
	    removeEmptySpans(sectionSpans[i]);
	}
	if (processSection(modes, sectionSpans, sectionSpanLocations)) 
	    for (int i = 0; i < nTiers; i++)
		positions[i] = lastPositions[i];
    }

    // Remove empty spans.
    private void removeEmptySpans(Vector<TierSpan> spans) {
	for (int i = spans.size() - 1; i >= 0; i--) {
	    TierSpan span = spans.get(i);
	    if (span.isEmpty())
		spans.remove(i);
	}
    }

    /////////////////////////////////////////////////////
    // Auxiliaries on tier.

    // Maximum position before tier is exhausted.
    private int tierEnd(int i) {
	return tiers[i].nSymbols();
    }

    // Tier is exhausted. 
    private boolean tierExhausted(int i) {
	return positions[i] >= tierEnd(i);
    }

    // All tiers are exhausted. (To be applied just before constructing
    // section.)
    private boolean tiersExhausted() {
	for (int i = 0; i < nTiers; i++)
	    if (!tierExhausted(i))
		return false;
	return true;
    }

    /////////////////////////////////////////////////////
    // Auxiliaries on section.

    // Remove all spans in section.
    private void clearSpans(int tier) {
	sectionSpans[tier] = new Vector<TierSpan>();
    }

    // Push span onto section.
    private void pushSpan(int tier, TierSpan span) {
	sectionSpans[tier].add(span);
    }

    // Last span in section.
    private TierSpan lastSpan(int tier) {
	return sectionSpans[tier].lastElement();
    }

    // End position of last span in section.
    protected int lastSpanEnd(int tier) {
	return lastSpan(tier).toPos;
    }
    // Begin position of first span in section.
    protected int firstSpanBegin(int tier) {
	return sectionSpans[tier].get(0).fromPos;
    }

    // Current state of section exhausts tier.
    private boolean sectionExhausts(int tier) {
	return lastSpanEnd(tier) >= tierEnd(tier);
    }

    // Current state of section exhausts all tiers.
    private boolean sectionExhausts() {
	for (int i = 0; i < nTiers; i++)
	    if (!sectionExhausts(i))
		return false;
	return true;
    }

    // Replace last element from section.
    private void replaceLast(int tier, TierSpan newSpan) {
	sectionSpans[tier].set(sectionSpans[tier].size()-1, newSpan);
    }

    // Extend last element from tier with new end position.
    private void extendLast(int tier, int pos) {
	TierSpan last = lastSpan(tier);
	TierSpan extended = new TierSpan(last.tier, last.fromPos, pos);
	replaceLast(tier, extended);
    }

    // First unused unbreakable span in section.
    // Is null if section is exhausted.
    private TierSpan nextSpan(int tier) {
	return lastSpan(tier).nextUnbreakable();
    }

    // Remove all span locations in section.
    private void clearSpanLocations(int tier) {
	sectionSpanLocations[tier] = new TreeMap<Integer,Float>();
    }

    // Set location of span beginning at position.
    private void setSpanLocation(int tier, int pos, float loc) {
	sectionSpanLocations[tier].put(new Integer(pos), new Float(loc));
    }

    // Location where last span is.
    private float lastSpanLocation(int tier) {
	TierSpan span = lastSpan(tier);
	return spanLocation(span);
    }

    // Location where span is.
    private float spanLocation(TierSpan span) {
	int tier = span.tier.id();
	Integer previousPos = new Integer(span.fromPos);
	Float start = sectionSpanLocations[tier].get(previousPos);
	return start.floatValue();
    }

    // Location where position in span is. Return 0 if not in section yet.
    private float posLocation(int tier, int pos, String type) {
	Vector<TierSpan> spans = sectionSpans[tier];
	for (int i = spans.size() - 1; i >= 0; i--) {
	    TierSpan previous = spans.get(i);
	    if (pos >= previous.toPos)
		return 0;
	    else if (pos >= previous.fromPos) {
		float loc = previous.dist(pos);
		if (type.equals("after"))
		    loc += previous.tier.advance(pos, pos+1);
		return spanLocation(previous) + loc;
	    }
	}
	return 0;
    }

    // Right location in current section, assuming dist.
    private float dist(int tier) {
	TierSpan span = lastSpan(tier);
	if (span.isEmpty())
	    return 0;
	else 
	    return lastSpanLocation(tier) + span.dist();
    }

    // Right location in current section, assuming advance.
    private float advance(int tier) {
	TierSpan span = lastSpan(tier);
	if (span.isEmpty())
	    return 0;
	else 
	    return lastSpanLocation(tier) + span.advance();
    }

    // Penalty for tier.
    protected double penalty(int tier) {
	return tiers[tier].penalty(lastSpanEnd(tier));
    }

    // Penalties of current state, which is sum for all tiers.
    protected double penalty() {
	double penalty = 0.0;
	for (int i = 0; i < nTiers; i++) 
	    penalty += penalty(i);
	return penalty;
    }

    /////////////////////////////////////////////////////
    // Debugging.

    // Print section.
    private void printSpans() {
	for (int i = 0; i < nTiers; i++) {
	    System.out.println(sectionSpans[i]);
	}
    }

    /////////////////////////////////////////////////////
    // Interface to caller.

    // Ask caller for width of section.
    protected abstract float width();

    // Process section. Return whether this is accepted, or whether
    // it should be done again (assuming width() is changed).
    protected abstract boolean processSection(String[] modes,
	    Vector<TierSpan>[] sectionSpans,
	    TreeMap<Integer,Float>[] sectionSpanLocations);

}
