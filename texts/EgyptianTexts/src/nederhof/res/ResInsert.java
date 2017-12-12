/***************************************************************************/
/*                                                                         */
/*  ResInsert.java                                                         */
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

public class ResInsert implements ResBasicgroup {

    public static final String placeDefault = "";
    public static final float posDefault = 0.5f;
    public static final boolean fixDefault = false;

    // See spec of RES.
    public String place = placeDefault;
    public float x = posDefault;
    public float y = posDefault;
    public boolean fix = fixDefault;
    public float sep = Float.NaN;
    public ResSwitch switchs0;
    public ResTopgroup group1;
    public ResSwitch switchs1;
    public ResTopgroup group2;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResInsert(
	    String place,
	    float x,
	    float y,
	    boolean fix,
	    float sep,
	    ResSwitch switchs0,
	    ResTopgroup group1,
	    ResSwitch switchs1,
	    ResTopgroup group2,
	    ResSwitch switchs2) {
	this.place = place;
	this.x = x;
	this.y = y;
	this.fix = fix;
	this.sep = sep;
	this.switchs0 = switchs0;
	this.group1 = group1;
	this.switchs1 = switchs1;
	this.group2 = group2;
	this.switchs2 = switchs2;
    }

    // Simplified constructors.
    public ResInsert(ResSwitch switchs0, ResTopgroup group1, 
	    ResSwitch switchs1, ResTopgroup group2, 
	    ResSwitch switchs2) {
	this(placeDefault, posDefault, posDefault, fixDefault, Float.NaN,
		switchs0, group1, switchs1, group2, switchs2);
    }
    public ResInsert(String place, ResTopgroup group1, ResTopgroup group2) {
	this(place, posDefault, posDefault, fixDefault, Float.NaN,
		new ResSwitch(), group1, new ResSwitch(), group2, new ResSwitch());
    }
    public ResInsert(ResTopgroup group1, ResSwitch switchs1,
            ResTopgroup group2) {
        this(new ResSwitch(), group1,
                switchs1, group2, new ResSwitch());
    }
    public ResInsert(ResTopgroup group1, ResTopgroup group2) {
        this(new ResSwitch(), group1, new ResSwitch(), group2, new ResSwitch());
    }

    // Constructor from parser.
    public ResInsert(Collection args, ResSwitch switchs0, 
	    ResTopgroup group1, ResSwitch switchs1, 
	    ResTopgroup group2, ResSwitch switchs2,
	    IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.is("t") || arg.is("b") || arg.is("s") || arg.is("e") ||
		    arg.is("ts") || arg.is("te") || arg.is("bs") || arg.is("be"))
		place = arg.getLhs();
	    else if (arg.hasLhs("x") && arg.hasRhsLowReal())
		x = arg.getRhsReal();
	    else if (arg.hasLhs("y") && arg.hasRhsLowReal())
		y = arg.getRhsReal();
	    else if (arg.is("fix"))
		fix = true;
	    else if (arg.hasLhs("sep") && arg.hasRhsReal())
		sep = arg.getRhsReal();
	    else 
		context.reportError("Wrong insert_arg", arg.left, arg.right);
	}
	this.switchs0 = switchs0;
	this.group1 = group1;
	this.switchs1 = switchs1;
	this.group2 = group2;
	this.switchs2 = switchs2;
    }

    // Make deep copy.
    public Object clone() {
	ResInsert copy = null;
	try {
	    copy = (ResInsert) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.group1 = (ResTopgroup) group1.clone();
	copy.group2 = (ResTopgroup) group2.clone();
	return copy;
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (!place.equals(placeDefault))
	    args.add(place);
	if (x != posDefault)
	    args.add("x=" + ResArg.realString(x));
	if (y != posDefault)
	    args.add("y=" + ResArg.realString(y));
	if (fix)
	    args.add("fix");
	if (!Float.isNaN(sep))
	    args.add("sep=" + ResArg.realString(sep));
	return "insert" + ResArg.toString(args) + "(" + switchs0.toString() + 
	    group1.toString() + "," + switchs1.toString() + 
	    group2.toString() + ")" + switchs2.toString();
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Switches after commas are a nuisance for editing, so we propagate
    // them back, if they occur in the input at all.
    // The argument is what needs to propagate back through this expression.
    // The returned value is what needs to propagated further back.
    public ResSwitch propagateBack(ResSwitch sw) {
        switchs2 = switchs2.join(sw);
        ResSwitch swAfter = group2.propagateBack();
        ResSwitch swBefore = switchs1.join(swAfter);
        switchs1 = new ResSwitch();
        ResSwitch swStart = group1.propagateBack(swBefore);
        switchs0 = switchs0.join(swStart);
        return new ResSwitch();
    }
    public ResSwitch propagateBack() {
	return propagateBack(new ResSwitch());
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // In effect at start.
    public GlobalValues globals;

    public GlobalValues propagate(GlobalValues globals) {
	this.globals = globals;
	globals = switchs0.update(globals);
	globals = group1.propagate(globals);
	globals = switchs1.update(globals);
	globals = group2.propagate(globals);
	return switchs2.update(globals);
    }

    // Direction.
    public int dirHeader() {
	return GlobalValues.direction(globals, "ResInsert");
    }

    // Local value overrides global.
    public float sep() {
	return GlobalValues.sep(sep, globals, "ResInsert");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	Vector<ResNamedglyph> list = new Vector<ResNamedglyph>(5, 5);
        if (place.matches("s")) {
	    list.addAll(group2.glyphs());
	    list.addAll(group1.glyphs());
        } else if (place.matches("e")) {
	    list.addAll(group1.glyphs());
	    list.addAll(group2.glyphs());
        } else if (place.matches("t")) {
	    list.addAll(group2.glyphs());
	    list.addAll(group1.glyphs());
        } else {
	    list.addAll(group1.glyphs());
	    list.addAll(group2.glyphs());
        }
	return list;
    }

}
