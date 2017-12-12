/***************************************************************************/
/*                                                                         */
/*  ResHieroglyphic.java                                                   */
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

package nederhof.res;

import java.util.*;

public class ResHieroglyphic implements Cloneable {

    // See spec of RES.
    // The number of groups is one greater than number of ops and switches.
    public Vector<ResTopgroup> groups = new Vector<ResTopgroup>(21,20);
    public Vector<ResOp> ops = new Vector<ResOp>(20,20);
    public Vector<ResSwitch> switches = new Vector<ResSwitch>(20,20);

    // Direct constructor, taking shallow copy.
    public ResHieroglyphic(
	    Vector<ResTopgroup> groups,
	    Vector<ResOp> ops,
	    Vector<ResSwitch> switches) {
	this.groups = groups;
	this.ops = ops;
	this.switches = switches;
    }

    // Constructor from parser.
    public ResHieroglyphic(ResTopgroup group, IParsingContext context) {
	groups.add(0, group);
    }

    // Simplified constructor.
    public ResHieroglyphic(ResTopgroup group) {
	groups.add(0, group);
    }

    // From parser, add group at beginning.
    public ResHieroglyphic addGroup(ResTopgroup group, Collection args,
	    ResSwitch switchs, IParsingContext context) {
	groups.add(0, group);
	ops.add(0, new ResOp(args, context));
	switches.add(0, switchs);
	return this;
    }

    // Simplified methods to add group (at end).
    public ResHieroglyphic addGroup(ResSwitch switchs, ResTopgroup group) {
	ops.add(new ResOp());
	switches.add(switchs);
	groups.add(group);
	return this;
    }
    public ResHieroglyphic addGroup(ResOp op, ResTopgroup group) {
	ops.add(op);
	switches.add(new ResSwitch());
	groups.add(group);
	return this;
    }

    // Add group at index i.
    public void addGroupAt(ResTopgroup group, ResOp op, ResSwitch switchs, int i) {
	if (i > 0) {
	    ops.add(i-1, op);
	    switches.add(i-1, switchs);
	    groups.add(i, group);
	} else {
	    groups.add(i, group);
	    ops.add(i, op);
	    switches.add(i, switchs);
	}
    }

    public int nGroups() {
	return groups.size();
    }
    public ResTopgroup group(int i) {
	return groups.get(i);
    }
    public int nOps() {
	return ops.size();
    }
    public ResOp op(int i) {
	return ops.get(i);
    }
    public int nSwitches() {
	return switches.size();
    }
    public ResSwitch switchs(int i) {
	return switches.get(i);
    }
    public void setSwitchs(int i, ResSwitch sw) {
        switches.set(i, sw);
    }

    // Make deep copy.
    public Object clone() {
	ResHieroglyphic copy = null;
	try {
	    copy = (ResHieroglyphic) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.groups = new Vector<ResTopgroup>(nGroups(),20);
	for (int i = 0; i < nGroups(); i++) 
	    copy.groups.add((ResTopgroup) group(i).clone());
	copy.ops = new Vector<ResOp>(nOps(),20);
	for (int i = 0; i < nOps(); i++) 
	    copy.ops.add((ResOp) op(i).clone());
	copy.switches = (Vector<ResSwitch>) switches.clone();
	return copy;
    }

    public String toString() {
	String s = group(0).toString();
	for (int i = 0; i < nOps(); i++) 
	    s += "-" + op(i).toString() + switchs(i).toString() + 
		group(i+1).toString();
	return s;
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Propagate switches back.
    public ResSwitch propagateBack() {
	for (int i = 0; i < nSwitches(); i++) {
	    ResSwitch swGroup = group(i+1).propagateBack();
	    setSwitchs(i, switchs(i).join(swGroup));
	}
        return group(0).propagateBack();
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // For each group there is set of global values.
    // These represent the values that hold right at the beginning of the
    // groups.
    public Vector<GlobalValues> globalss;

    // Direction as specified by encoding, not considering forced direction.
    public int direction;

    // Propagate globals through all elements.
    // Store values per beginning of group.
    public GlobalValues propagate(GlobalValues globals, int direction) {
	globalss = new Vector<GlobalValues>(20,20);
	globalss.add(globals);
	globals = group(0).propagate(globals);
	for (int i = 0; i < nOps(); i++) {
	    op(i).propagate(globals);
	    globals = switchs(i).update(globals);
	    globalss.add(globals);
	    globals = group(i+1).propagate(globals);
	}
	this.direction = direction;
	return globals;
    }

    // Unit size for height of line or width or column.
    // (Taken from first globals-record; any other would give same value.)
    public float size() {
	return GlobalValues.size(globalss, "ResHieroglyphic");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Number of separations between groups without "fix".
    public int nPaddable() {
	return nPaddable(Integer.MAX_VALUE);
    }
    // Number of separations between the first n groups without "fix".
    public int nPaddable(int n) {
	int num = 0;
	for (int i = 0; i < nOps() && i+1 < n; i++) {
	    if (!op(i).fix) 
		num++;
	}
	return num;
    }

    // Number of groups in first chunk (groups connected with "fix").
    public int firstChunkLength() {
	int num = 1;
	for (int i = 0; i < nOps(); i++) {
	    if (!op(i).fix) 
		break;
	    num++;
	}
	return num;
    }

    // Indices of glyphs occurring after paddable positions.
    // In array, which is null if none.
    public boolean[] glyphAfterPaddable() {
	int size = glyphs().size();
	if (size == 0)
	    return null;
	else {
	    boolean[] positions = new boolean[size];
	    for (int i = 0; i < size; i++) 
		positions[i] = false;
	    int nGlyphs = 0;
	    for (int i = 0; i < nOps(); i++) {
		nGlyphs += group(i).glyphs().size();
		if (!op(i).fix && nGlyphs < size) 
		    positions[nGlyphs] = true;
	    }
	    return positions;
	}
    }

    // Get list of glyphs.
    public Vector<ResNamedglyph> glyphs() {
	Vector<ResNamedglyph> list = new Vector<ResNamedglyph>(50, 50);
	for (int i = 0; i < nGroups(); i++) 
	    list.addAll(group(i).glyphs());
	return list;
    }

    // Get length of shortest prefix that contains so many glyphs.
    public int nGlyphsToGroups(int nGlyphs) {
	for (int i = 0; i < nGroups(); i++) {
	    if (nGlyphs <= 0)
		return i;
	    nGlyphs -= group(i).glyphs().size();
	}
	return nGroups();
    }

    // Get index of group containing glyph with index.
    // For index past last glyph, return last group.
    public int glyphToGroup(int glyph) {
	int nGlyphs = 0;
	for (int i = 0; i < nGroups(); i++) {
	    int size = group(i).glyphs().size();
	    if (glyph < nGlyphs + size)
		return i;
	    nGlyphs += size;
	}
	return nGroups() - 1; 
    }

    // Create hieroglyphic consisting of the first groups.
    // We assume 0 < nGroups <= # groups.
    public ResHieroglyphic prefixGroups(int nGroups) {
	ResHieroglyphic pref = (ResHieroglyphic) clone();
	pref.groups = new Vector<ResTopgroup>(pref.groups.subList(0, nGroups));
	pref.ops = new Vector<ResOp>(pref.ops.subList(0, nGroups-1));
	pref.switches = new Vector<ResSwitch>(pref.switches.subList(0, nGroups-1));
	return pref;
    }

    // Create hieroglyphic excluding the first groups.
    // We assume 0 < nGroups < # groups.
    public ResHieroglyphic suffixGroups(int nGroups) {
	ResHieroglyphic suff = (ResHieroglyphic) clone();
	suff.groups = new Vector<ResTopgroup>(suff.groups.subList(nGroups, nGroups()));
	suff.ops = new Vector<ResOp>(suff.ops.subList(nGroups, nOps()));
	suff.switches = new Vector<ResSwitch>(suff.switches.subList(nGroups, nSwitches()));
	return suff;
    }

}
