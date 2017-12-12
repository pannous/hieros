/***************************************************************************/
/*                                                                         */
/*  ResFragment.java                                                       */
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

// A fragment of RES.

package nederhof.res;

import java.util.*;

public class ResFragment extends ResOrLite implements Cloneable {

    // See spec of RES.
    // hiero may be null. This is when there are 0 groups, or in case of error.
    public int direction = ResValues.DIR_NONE;
    public float size = Float.NaN;
    public ResSwitch switchs;
    public ResHieroglyphic hiero;

    // Direct constructor, taking shallow copy.
    public ResFragment(
	    int direction,
	    float size,
	    ResSwitch switchs,
	    ResHieroglyphic hiero) {
	this.direction = direction;
	this.size = size;
	this.switchs = switchs;
	this.hiero = hiero;
	propagate();
    }

    // Simplified constructor.
    public ResFragment(ResSwitch switchs, ResHieroglyphic hiero) {
	this(ResValues.DIR_NONE, Float.NaN, switchs, hiero);
    }
    public ResFragment(ResHieroglyphic hiero) {
	this(ResValues.DIR_NONE, Float.NaN, new ResSwitch(), hiero);
    }

    // Create fragment. 
    protected ResFragment(Collection args, ResSwitch switchs, ResHieroglyphic hiero,
	    IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.is("hlr"))
		direction = ResValues.DIR_HLR;
	    else if (arg.is("hrl"))
		direction = ResValues.DIR_HRL;
	    else if (arg.is("vlr"))
		direction = ResValues.DIR_VLR;
	    else if (arg.is("vrl"))
		direction = ResValues.DIR_VRL;
	    else if (arg.hasLhs("size") && arg.hasRhsNonZeroReal())
		size = arg.getRhsReal();
	    else
		context.reportError("Wrong header_arg", arg.left, arg.right);
	}
	this.switchs = switchs;
	this.hiero = hiero;
	propagate();
    }

    // Create empty fragment with specified properties.
    protected ResFragment(int direction, float size) {
	this(direction, size, new ResSwitch(), null);
    }

    // Create default empty fragment.
    public ResFragment() {
	this(ResValues.DIR_NONE, Float.NaN);
    }

    // Create RES from string and context.
    // If errors occur in parsing, report them.
    public static ResFragment parse(String s, IParsingContext context) {
	parser p = new parser(s, context);
        Object result = null;
        try {
            result = p.parse().value;
        } catch (Exception e) {
            result = null;
        }
        if (context.nErrors() > 0 && !context.suppressReporting()) {
            System.err.println("In " + s);
            for (int i = 0; i < context.nErrors(); i++)
                System.err.print("" + i + ") " + context.error(i));
        }
        if (result == null || !(result instanceof ResFragment))
            return new ResFragment();
        else
            return (ResFragment) result;
    }

    // Make deep copy.
    public Object clone() {
	ResFragment copy = null;
	try {
	    copy = (ResFragment) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	if (hiero != null)
	    copy.hiero = (ResHieroglyphic) hiero.clone();
	return copy;
    }

    // Convert fragment to string.
    public String toString() {
	return argsToString() + switchs.toString() +
	    (hiero == null ? "" : hiero.toString());
    }

    // Convert fragment to string, making first line not longer
    // than first limit, and subsequent lines not longer than second limit.
    // Line break only at '-' and after header.
    public String toString(int remain, int followLimit) {
	StringBuffer buf = new StringBuffer();
	String argsSwitch = argsToString() + switchs.toString();
	if (argsSwitch.length() > remain &&
		argsSwitch.length() <= followLimit) {
	    buf.append("\n");
	    remain = followLimit;
	} 
	buf.append(argsSwitch);
	remain -= argsSwitch.length();
	for (int i = 0; i < nGroups(); i++) {
	    String s = group(i).toString();
	    if (i < nOps())
		s += "-" + op(i).toString() + switchs(i).toString();
	    if (s.length() > remain && s.length() <= followLimit) {
		buf.append("\n");
		remain = followLimit;
	    }
	    buf.append(s); 
	    remain -= s.length();
	}
	return buf.toString();
    }

    public String argsToString() {
	return argsToString(direction, size);
    }

    public static String argsToString(int dir, float size) {
	Vector<String> args = new Vector<String>();
	switch (dir) {
	    case ResValues.DIR_HLR:
		args.add("hlr");
		break;
	    case ResValues.DIR_HRL:
		args.add("hrl");
		break;
	    case ResValues.DIR_VLR:
		args.add("vlr");
		break;
	    case ResValues.DIR_VRL:
		args.add("vrl");
		break;
	}
	if (!Float.isNaN(size))
	    args.add("size=" + ResArg.realString(size));
	return ResArg.toString(args);
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Switches after commas are a nuisance for editing, so we propagate
    // them back, if they occur in the input at all.
    public ResFragment normalizedSwitches() {
	ResFragment copy = (ResFragment) clone();
	copy.propagateBack();
	return copy;
    }

    private void propagateBack() {
        if (hiero != null) {
            ResSwitch sw = hiero.propagateBack();
            switchs = switchs.join(sw);
        }
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // Global values that hold at end of hieroglyphic.
    protected GlobalValues globals;

    // Propagate global values recursively through switches.
    public void propagate() {
	globals = new GlobalValues(direction, size);
	globals = switchs.update(globals);
	if (hiero != null)
	    globals = hiero.propagate(globals, globals.direction);
    }

    public GlobalValues globalValues() {
	return (GlobalValues) globals.clone();
    }

    public int dir() {
	return GlobalValues.direction(globals, "ResFragment");
    }

    public float size() {
	return GlobalValues.sizeHeader(globals, "ResFragment");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // All passed on to hiero if any.
    public int nGroups() {
	return hiero == null ? 0 : hiero.nGroups();
    }
    public ResTopgroup group(int i) {
	return hiero.group(i);
    }
    public int nOps() {
	return hiero == null ? 0 : hiero.nOps();
    }
    public ResOp op(int i) {
	return hiero.op(i);
    }
    public int nSwitches() {
	return hiero == null ? 0 : hiero.nSwitches();
    }
    public ResSwitch switchs(int i) {
	return hiero.switchs(i);
    }

    // Are there zero groups?
    public boolean isEmpty() {
	return nGroups() == 0;
    }

    // Number of separations between the first n groups without "fix".
    public int nPaddable(int n) {
	return hiero == null ? 0 : hiero.nPaddable(n);
    }
    // For all groups.
    public int nPaddable() {
	return hiero == null ? 0 : hiero.nPaddable();
    }

    // Number of groups, taking groups together if there is "fix" in between.
    public int nChunks() {
	return hiero == null ? 0 : hiero.nPaddable() + 1;
    }

    // Number of groups in first chunk (groups connected with "fix").
    public int firstChunkLength() {
	return hiero == null ? 0 : hiero.firstChunkLength();
    }

    // Whether glyphs occur after paddable positions.
    public boolean[] glyphAfterPaddable() {
	return hiero == null ? null : hiero.glyphAfterPaddable();
    }

    // Get list of glyphs with Gardiner names.
    public Vector<ResNamedglyph> glyphs() {
	return hiero == null ? new Vector<ResNamedglyph>() : hiero.glyphs();
    }

    // Get number of glyphs with Gardiner names.
    public int nGlyphs() {
	return glyphs().size();
    }

    // Get the Gardiner names.
    public Vector<String> glyphNames() {
	Vector<String> list = new Vector<String>(50, 50);
	Vector<ResNamedglyph> glyphs = glyphs();
	for (int i = 0; i < glyphs.size(); i++) {
	    ResNamedglyph named = glyphs.get(i);
	    list.add(named.name);
	}
	return list;
    }

    // Change color of glyphs between indices.
    public void makeGlyphs(int start, int end, Color16 color) {
	Vector<ResNamedglyph> glyphs = glyphs();
	for (int i = start; i < Math.min(end, glyphs.size()); i++) {
	    ResNamedglyph named = glyphs.get(i);
	    named.color = color;
	}
    }

    // Get length of shortest prefix that contains so many glyphs.
    public int nGlyphsToGroups(int nGlyphs) {
	return hiero == null ? 0 : hiero.nGlyphsToGroups(nGlyphs);
    }

    // Get index of group containing glyph with index.
    public int glyphToGroup(int glyph) {
	return hiero == null ? 0 : hiero.glyphToGroup(glyph);
    }

    // Get index of group containing glyph with index, and increment as long
    // as next group is entirely empty.
    public int glyphToGroupAndEmpty(int glyph) {
	int i = glyphToGroup(glyph);
	if (hiero != null) 
	    while (i < hiero.nGroups() - 1 && 
		    hiero.group(i+1).glyphs().isEmpty())
		i++;
	return i;
    }

    // Make a new fragment containing the first few groups from the present
    // fragment. nGroups should be less than or equal to the number of groups.
    public ResFragment prefixGroups(int nGroups) {
	ResSwitch switchsPrefix = switchs;
	if (hiero != null && nGroups > 0) {
	    nGroups = Math.min(nGroups, nGroups());
	    ResHieroglyphic hieroPrefix = hiero.prefixGroups(nGroups);
	    return new ResFragment(direction, size, switchsPrefix, hieroPrefix);
	} else 
	    return new ResFragment(direction, size, switchsPrefix, null);
    }

    // Make a new fragment excluding the first few groups from the present
    // fragment. nGroups should be less than or equal to the number of groups.
    public ResFragment suffixGroups(int nGroups) {
	if (hiero != null && nGroups > 0) {
	    nGroups = Math.min(nGroups, nGroups());
	    if (nGroups == nGroups()) {
		ResSwitch switchsSuffix = new ResSwitch(globals);
		return new ResFragment(direction, size, switchsSuffix, null);
	    } else {
		GlobalValues last = (GlobalValues) hiero.globalss.get(nGroups);
		ResSwitch switchsSuffix = new ResSwitch(last);
		ResHieroglyphic hieroSuffix = hiero.suffixGroups(nGroups);
		return new ResFragment(direction, size, switchsSuffix, hieroSuffix);
	    }
	} else
	    return (ResFragment) this.clone();
    }

    // Make a new fragment consisting from given first group to last
    // group.
    public ResFragment infixGroups(int first, int last) {
	ResFragment pref = prefixGroups(last);
	return pref.suffixGroups(first);
    }

    // Make a new fragment containing the first few glyphs.
    // The groups necessary are taken, and the remaining glyphs are
    // made white.
    public ResFragment prefixGlyphs(int nGlyphs) {
	int nGroups = nGlyphsToGroups(nGlyphs);
	ResFragment pref = prefixGroups(nGroups);
	pref.makeGlyphs(nGlyphs, pref.nGlyphs(), Color16.WHITE);
	return pref;
    }

    // Make a new fragment containing glyphs after first few.
    // The required groups are taken, and the remaining glyphs 
    // at the beginning are made white.
    public ResFragment suffixGlyphs(int nGlyphs) {
	int nGroups = nGlyphsToGroups(nGlyphs);
	ResFragment suff = suffixGroups(nGroups);
	if (nGlyphs + suff.nGlyphs() < nGlyphs())
	    suff = suffixGroups(nGroups-1);
	int nErase = nGlyphs + suff.nGlyphs() - nGlyphs();
	suff.makeGlyphs(0, nErase, Color16.WHITE);
	return suff;
    }

    // Add note to existing fragment, at numbered glyph.
    public void addNote(int glyphIndex, String string, Color16 color) {
	Vector<ResNamedglyph> glyphs = glyphs();
	if (glyphIndex < 0 || glyphIndex >= glyphs.size())
	    return;
	ResNamedglyph named = glyphs.get(glyphIndex);
	named.addNote(new ResNote("\"" + string + "\"", color));
	propagate();
    }

    ///////////////////////////////////////////////////////////////
    // Testing.

    public static void main(String[] args) {
	String s = "[hrl]![red]i*r-[sep=0.5]U2:[fit]ir-A*A-[fit]k[mirror]-Xr:r-.:Z4";
	// s = "A1-B1";
	ResFragment frag = parse(s, new ParsingContext());
	System.out.println(s);
	System.out.println("" + frag);
	// RES res = RES.createRES(s, new HieroRenderContext(14));
	// System.out.println("res " + res);
    }

}
