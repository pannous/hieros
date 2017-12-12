/***************************************************************************/
/*                                                                         */
/*  ResOp.java                                                             */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Subclasses:
// ResFirstop

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

public class ResOp implements Cloneable {

    public static final boolean fixDefault = false;

    // See spec of RES.
    public float sep = Float.NaN;
    public Boolean fit = null;
    public boolean fix = fixDefault;
    public Boolean shade = null;
    public Vector<String> shades = new Vector<String>(0,5);

    // Only used for firstop. In other cases remains Float.NaN.
    public float size = Float.NaN;

    // Direct constructor, taking shallow copy.
    public ResOp(
	    float sep,
	    Boolean fit,
	    boolean fix,
	    Boolean shade,
	    Vector<String> shades) {
        this(sep, fit, fix, shade, shades, Float.NaN);
    }

    // Constructor, includes the case it can be firstop.
    public ResOp(
            float sep,
            Boolean fit,
            boolean fix,
            Boolean shade,
            Vector<String> shades,
            float size) {
	this.sep = sep;
	this.fit = fit;
	this.fix = fix;
	this.shade = shade;
	this.shades = shades;
        this.size = size;
    }

    // Simplified constructor, assuming default values.
    public ResOp() {
    }

    // Only intended for constructor of ResFirstop.
    public ResOp(IParsingContext context) {
    }

    // Constructor from parser.
    public ResOp(Collection args, IParsingContext context) {
	this(context);
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (!processOparg(arg))
		context.reportError("Wrong op_arg", arg.left, arg.right);
	}
    }

    // Process arg. Return true if recognized.
    public boolean processOparg(ResArg arg) {
	if (arg.hasLhs("sep") && arg.hasRhsReal())
	    sep = arg.getRhsReal();
	else if (arg.is("fit"))
	    fit = Boolean.TRUE;
	else if (arg.is("nofit"))
	    fit = Boolean.FALSE;
	else if (arg.is("fix"))
	    fix = true;
	else if (arg.is("shade"))
	    shade = Boolean.TRUE;
	else if (arg.is("noshade"))
	    shade = Boolean.FALSE;
	else if (arg.isPattern())
	    shades.add(arg.getLhs());
	else 
	    return false;
	return true;
    }

    public int nShades() {
        return shades.size();
    }
    public String shade(int i) {
        return shades.get(i);
    }

    // Make copy.
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (!Float.isNaN(sep))
	    args.add("sep=" + ResArg.realString(sep));
	if (fit != null) {
	    if (fit.equals(Boolean.TRUE))
		args.add("fit");
	    else
		args.add("nofit");
	}
	if (fix)
	    args.add("fix");
	if (shade != null) {
	    if (shade.equals(Boolean.TRUE))
		args.add("shade");
	    else
		args.add("noshade");
	}
	for (int i = 0; i < nShades(); i++)
	    args.add(shade(i));
        if (!Float.isNaN(size)) {
            if (size != Float.MAX_VALUE)
                args.add("size=" + ResArg.realString(size));
            else
                args.add("size=inf");
        }
	return ResArg.toString(args);
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // In effect at start.
    public GlobalValues globals;

    public void propagate(GlobalValues globals) {
	this.globals = globals;
    }

    public int dirHeader() {
        return GlobalValues.direction(globals, "ResOp");
    }

    public float sizeHeader() {
        return GlobalValues.sizeHeader(globals, "ResOp");
    }

    // Is colored text at this point.
    public boolean isColored() {
	return GlobalValues.isColored(globals, "ResOp");
    }

    // The sep for this operator. Either specified at operator or global
    // value.
    public float sep() {
	return GlobalValues.sep(sep, globals, "ResOp");
    }

    // Should fitting be applied for this operator?
    public boolean fit() {
	return GlobalValues.fit(fit, globals, "ResOp");
    }

    // Is whole surface to be shaded?
    public boolean shade() {
        return GlobalValues.shade(shade, shades, globals, "ResNamedglyph");
    }

}
