/***************************************************************************/
/*                                                                         */
/*  ResModify.java                                                         */
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

public class ResModify implements ResBasicgroup {

    public static final float boundingDefault = 0.0f;
    public static final boolean omitDefault = false;

    // See spec of RES.
    public float width = Float.NaN;
    public float height = Float.NaN;
    public float above = boundingDefault;
    public float below = boundingDefault;
    public float before = boundingDefault;
    public float after = boundingDefault;
    public boolean omit = omitDefault;
    public Boolean shade = null;
    public Vector<String> shades = new Vector<String>(0,5);
    public ResSwitch switchs1;
    public ResTopgroup group;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResModify(
	    float width,
	    float height,
	    float above,
	    float below,
	    float before,
	    float after,
	    boolean omit,
	    Boolean shade,
	    Vector<String> shades,
	    ResSwitch switchs1,
	    ResTopgroup group,
	    ResSwitch switchs2) {
	this.width = width;
	this.height = height;
	this.above = above;
	this.below = below;
	this.before = before;
	this.after = after;
	this.omit = omit;
	this.shade = shade;
	this.shades = shades;
	this.switchs1 = switchs1;
	this.group = group;
	this.switchs2 = switchs2;
    }

    // Simplified constructor.
    public ResModify(ResSwitch switchs1, ResTopgroup group, ResSwitch switchs2) {
	this.switchs1 = switchs1;
	this.group = group;
	this.switchs2 = switchs2;
    }

    // Constructor from parser.
    public ResModify(Collection args, ResSwitch switchs1, 
	    ResTopgroup group, ResSwitch switchs2, IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.hasLhs("width") && arg.hasRhsNonZeroReal())
		width = arg.getRhsReal();
	    else if (arg.hasLhs("height") && arg.hasRhsNonZeroReal())
		height = arg.getRhsReal();
	    else if (arg.hasLhs("above") && arg.hasRhsReal())
		above = arg.getRhsReal();
	    else if (arg.hasLhs("below") && arg.hasRhsReal())
		below = arg.getRhsReal();
	    else if (arg.hasLhs("before") && arg.hasRhsReal())
		before = arg.getRhsReal();
	    else if (arg.hasLhs("after") && arg.hasRhsReal())
		after = arg.getRhsReal();
	    else if (arg.is("omit"))
		omit = true;
	    else if (arg.is("shade"))
		shade = Boolean.TRUE;
	    else if (arg.is("noshade"))
		shade = Boolean.FALSE;
	    else if (arg.isPattern())
		shades.add(arg.getLhs());
	    else 
		context.reportError("Wrong modify_arg", arg.left, arg.right);
	}
	this.switchs1 = switchs1;
	this.group = group;
	this.switchs2 = switchs2;
    }

    public int nShades() {
        return shades.size();
    }
    public String shade(int i) {
        return shades.get(i);
    }

    // Make deep copy.
    public Object clone() {
	ResModify copy = null;
	try {
	    copy = (ResModify) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.group = (ResTopgroup) group.clone();
	return copy;
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (!Float.isNaN(width))
	    args.add("width=" + ResArg.realString(width));
	if (!Float.isNaN(height))
	    args.add("height=" + ResArg.realString(height));
	if (above != boundingDefault)
	    args.add("above=" + ResArg.realString(above));
	if (below != boundingDefault)
	    args.add("below=" + ResArg.realString(below));
	if (before != boundingDefault)
	    args.add("before=" + ResArg.realString(before));
	if (after != boundingDefault)
	    args.add("after=" + ResArg.realString(after));
	if (omit)
	    args.add("omit");
	if (shade != null) {
	    if (shade.equals(Boolean.TRUE))
		args.add("shade");
	    else
		args.add("noshade");
	}
	for (int i = 0; i < nShades(); i++)
	    args.add(shade(i));
	return "modify" + ResArg.toString(args) + "(" + switchs1.toString() +
	    group.toString() + ")" + switchs1.toString();
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Propagate switch back that occurs after expression.
    // them back, if they occur in the input at all.
    // The argument is what needs to propagate back through this expression.
    // The returned value is what needs to propagated further back.
    public ResSwitch propagateBack(ResSwitch sw) {
        switchs2 = switchs2.join(sw);
        ResSwitch swGroup = group.propagateBack();
        switchs1 = switchs1.join(swGroup);
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
	globals = switchs1.update(globals);
	globals = group.propagate(globals);
	return switchs2.update(globals);
    }

    // Direction.
    public int dirHeader() {
        return GlobalValues.direction(globals, "ResModify");
    }

    // Is whole surface to be shades?
    public boolean shade() {
        return GlobalValues.shade(shade, shades, globals, "ResModify");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	return group.glyphs();
    }

}
