/***************************************************************************/
/*                                                                         */
/*  ResEmptyglyph.java                                                     */
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

public class ResEmptyglyph implements ResBasicgroup {

    public static final float sizePoint = 0.0f;
    public static final float sizeDefault = 1.0f;
    public static final boolean firmDefault = false;

    // See spec of RES.
    public float width = sizeDefault;
    public float height = sizeDefault;
    public Boolean shade = null;
    public Vector<String> shades = new Vector<String>(0,5);
    public boolean firm = firmDefault;
    public ResNote note;
    public ResSwitch switchs;

    // Direct constructor, taking shallow copy.
    public ResEmptyglyph(
	    float width,
	    float height,
	    Boolean shade,
	    Vector<String> shades,
	    boolean firm,
	    ResNote note,
	    ResSwitch switchs) {
	this.width = width;
	this.height = height;
	this.shade = shade;
	this.shades = shades;
	this.firm = firm;
	this.note = note;
	this.switchs = switchs;
    }

    // Constructor with only dimensions and switches.
    public ResEmptyglyph(float width, float height, ResSwitch switchs) {
	this(width, height, null, new Vector<String>(0,5), firmDefault, null, 
		switchs);
    }

    // Constructor with only dimensions.
    public ResEmptyglyph(float width, float height) {
	this(width, height, new ResSwitch());
    }

    // Default constructor.
    public ResEmptyglyph() {
	this(sizeDefault, sizeDefault);
    }

    // Constructor from parser.
    public ResEmptyglyph(Collection args, ResNote note, ResSwitch switchs,
	    IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.hasLhs("width") && arg.hasRhsReal())
		width = arg.getRhsReal();
	    else if (arg.hasLhs("height") && arg.hasRhsReal())
		height = arg.getRhsReal();
	    else if (arg.is("shade"))
		shade = Boolean.TRUE;
	    else if (arg.is("noshade"))
		shade = Boolean.FALSE;
	    else if (arg.isPattern())
		shades.add(arg.getLhs());
	    else if (arg.is("firm"))
		firm = true;
	    else 
		context.reportError("Wrong empty_glyph_arg", arg.left, arg.right);
	}
	this.note = note;
	this.switchs = switchs;
    }

    // Constructor from parser.
    public ResEmptyglyph(ResNote note, ResSwitch switchs,
	    IParsingContext context) {
	this(pointArgs, note, switchs, context);
    }

    public int nShades() {
        return shades.size();
    }
    public String shade(int i) {
        return shades.get(i);
    }

    // Make deep copy.
    public Object clone() {
	ResEmptyglyph copy = null;
	try {
	    copy = (ResEmptyglyph) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.shades = (Vector<String>) shades.clone();
	if (note != null)
	    copy.note = (ResNote) note.clone();
	return copy;
    }

    // Arguments for point glyph.
    private static LinkedList pointArgs;
    static { 
	pointArgs = new LinkedList();
	pointArgs.add(new ResArg("width", "0.0"));
	pointArgs.add(new ResArg("height", "0.0"));
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	boolean noPointArgs = false;
	if (width != sizeDefault)
	    args.add("width=" + ResArg.realString(width));
	if (height != sizeDefault)
	    args.add("height=" + ResArg.realString(height));
	if (shade != null) {
	    if (shade.equals(Boolean.TRUE)) {
		args.add("shade");
		noPointArgs = true;
	    } else {
		args.add("noshade");
		noPointArgs = true;
	    }
	}
	for (int i = 0; i < nShades(); i++) {
	    args.add(shade(i));
	    noPointArgs = true;
	}
	if (firm) {
	    args.add("firm");
	    noPointArgs = true;
	}
	String s;
	if (width == sizePoint && height == sizePoint && !noPointArgs)
	    s = ".";
	else 
	    s = "empty" + ResArg.toString(args);
	if (note != null)
	    s += note.toString();
	s += switchs.toString();
	return s;
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Propagate switch back that occurs after expression.
    public ResSwitch propagateBack(ResSwitch sw) {
        switchs = switchs.join(sw);
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
	if (note != null)
	    note.propagate(globals);
	return switchs.update(globals);
    }

    public int dirHeader() {
        return GlobalValues.direction(globals, "ResEmptyglyph");
    }

    public float sizeHeader() {
        return GlobalValues.sizeHeader(globals, "ResEmptyglyph");
    }

    // Is colored text at this point.
    public boolean isColored() {
        return GlobalValues.isColored(globals, "ResEmptyglyph");
    }

    // Is whole surface to be shaded?
    public boolean shade() {
        return GlobalValues.shade(shade, shades, globals, "ResNamedglyph");
    }

    // Is some part shaded?
    public boolean someShade() {
        return GlobalValues.someShade(shade, shades, globals, "ResNamedglyph");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	return new Vector<ResNamedglyph>();
    }

}
