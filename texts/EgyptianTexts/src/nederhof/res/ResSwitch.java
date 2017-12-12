/***************************************************************************/
/*                                                                         */
/*  ResSwitch.java                                                         */
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

// Switch element of RES. 
// Is immutable. However, subclass TreeSwitch is not.

package nederhof.res;

import java.util.*;

public class ResSwitch implements Cloneable {

    // See spec of RES.
    public Color16 color = Color16.NO_COLOR;
    public Boolean shade = null;
    public float sep = Float.NaN;
    public Boolean fit = null;
    public Boolean mirror = null;

    // Direct constructor, taking shallow copy.
    public ResSwitch(
	    Color16 color,
	    Boolean shade,
	    float sep,
	    Boolean fit,
	    Boolean mirror) {
	this.color = color;
	this.shade = shade;
	this.sep = sep;
	this.fit = fit;
	this.mirror = mirror;
    }

    // Create default switch.
    public ResSwitch() {
    }

    // Constructor from parser.
    public ResSwitch(Collection args, IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.isColor())
		color = new Color16(arg.getLhs());
	    else if (arg.is("shade"))
		shade = Boolean.TRUE;
	    else if (arg.is("noshade"))
		shade = Boolean.FALSE;
	    else if (arg.hasLhs("sep") && arg.hasRhsReal())
		 sep = arg.getRhsReal();
	    else if (arg.is("fit"))
		fit = Boolean.TRUE;
	    else if (arg.is("nofit"))
		fit = Boolean.FALSE;
	    else if (arg.is("mirror"))
		mirror = Boolean.TRUE;
	    else if (arg.is("nomirror"))
		mirror = Boolean.FALSE;
	    else 
		context.reportError("Wrong switch_arg", arg.left, arg.right);
	}
    }

    // Constructor that creates switch needed to get global values starting
    // from default values.
    public ResSwitch(GlobalValues globals) {
	if (globals.color.code() != GlobalValues.colorDefault.code())
	    color = globals.color;
	if (globals.shade != GlobalValues.shadeDefault)
	    shade = new Boolean(globals.shade);
	if (globals.sep != GlobalValues.sepDefault)
	    sep = globals.sep;
	if (globals.fit != GlobalValues.fitDefault)
	    fit = new Boolean(globals.fit);
	if (globals.mirror != GlobalValues.mirrorDefault)
	    mirror = new Boolean(globals.mirror);
    }

    // Second switch overrides first switch.
    public ResSwitch join(ResSwitch other) {
	ResSwitch copy = (ResSwitch) makeClone();
	if (other.color.isColor())
	    copy.color = other.color;
	if (other.shade != null)
	    copy.shade = other.shade;
	if (!Float.isNaN(other.sep))
	    copy.sep = other.sep;
	if (other.fit != null)
	    copy.fit = other.fit;
	if (other.mirror != null)
	    copy.mirror = other.mirror;
	return copy;
    }

    // Make copy.
    private ResSwitch makeClone() {
        ResSwitch copy = null;
        try {
            copy = (ResSwitch) super.clone();
        } catch (CloneNotSupportedException e) {
	    System.err.println("ResSwitch clone not supported");
            return null;
        }
	return copy;
    }

    // Make copy of vector of switches.
    public static Vector<ResSwitch> clone(Vector<ResSwitch> switches) {
	return (Vector<ResSwitch>) switches.clone();
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (color.isColor())
	    args.add("" + color);
	if (shade != null) {
	    if (shade.equals(Boolean.TRUE))
		args.add("shade");
	    else
		args.add("noshade");
	}
	if (!Float.isNaN(sep))
	    args.add("sep=" + ResArg.realString(sep));
	if (fit != null) {
	    if (fit.equals(Boolean.TRUE))
		args.add("fit");
	    else
		args.add("nofit");
	}
	if (mirror != null) {
	    if (mirror.equals(Boolean.TRUE))
		args.add("mirror");
	    else
		args.add("nomirror");
	}
	if (args.size() > 0)
	    return "!" + ResArg.toString(args);
	else
	    return "";
    }
    
    // All are default values.
    public boolean isDefault() {
	return !color.isColor() &&
	    shade == null &&
	    Float.isNaN(sep) &&
	    fit == null &&
	    mirror == null;
    }

    // If any global values were previously different from default,
    // then to clear to default values, except where non-defaults 
    // are already set.
    public ResSwitch reset(GlobalValues prev) {
	Color16 resetColor = color;
	Boolean resetShade = shade;
	float resetSep = sep;
	Boolean resetFit = fit;
	Boolean resetMirror = mirror;
	if (!Color16.equal(prev.color, GlobalValues.colorDefault) && !color.isColor())
	    resetColor = GlobalValues.colorDefault;
	if (prev.shade != GlobalValues.shadeDefault && shade == null)
	    resetShade = false;
	if (prev.sep != GlobalValues.sepDefault && Float.isNaN(sep))
	    resetSep = GlobalValues.sepDefault;
	if (prev.fit != GlobalValues.fitDefault && fit == null)
	    resetFit = false;
	if (prev.mirror != GlobalValues.mirrorDefault && mirror == null)
	    resetMirror = false;
	return new ResSwitch(resetColor, resetShade, resetSep, resetFit, resetMirror);
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // Produce new global values after switch.
    public GlobalValues update(GlobalValues globals) {
        if (!color.isColor() &&
                shade == null &&
                Float.isNaN(sep) &&
                fit == null &&
                mirror == null)
            return globals;
        else {
            GlobalValues newVals = (GlobalValues) globals.clone();
            if (color.isColor())
                newVals.color = color;
            if (shade != null)
                newVals.shade = shade.booleanValue();
            if (!Float.isNaN(sep))
                newVals.sep = sep;
            if (fit != null)
                newVals.fit = fit.booleanValue();
            if (mirror != null)
                newVals.mirror = mirror.booleanValue();
            return newVals;
        }
    }

}
