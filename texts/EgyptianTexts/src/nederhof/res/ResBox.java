/***************************************************************************/
/*                                                                         */
/*  ResBox.java                                                            */
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

public class ResBox implements ResBasicgroup {

    public static final String typeDefault = "cartouche";
    public static final float scaleDefault = 1.0f;
    public static final float sizeDefault = 1.0f;

    // See spec of RES.
    public String type;
    public int direction = ResValues.DIR_NONE;
    public Boolean mirror = null;
    public float scale = scaleDefault;
    public Color16 color = Color16.NO_COLOR;
    public Boolean shade = null;
    public Vector<String> shades = new Vector<String>(0,5);
    public float size = sizeDefault;
    public float opensep = Float.NaN;
    public float closesep = Float.NaN;
    public float undersep = Float.NaN;
    public float oversep = Float.NaN;
    public ResSwitch switchs1;
    public ResHieroglyphic hiero;
    public Vector<ResNote> notes;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResBox(
	    String type,
	    int direction,
	    Boolean mirror,
	    float scale,
	    Color16 color,
	    Boolean shade,
	    Vector<String> shades,
	    float size,
	    float opensep,
	    float closesep,
	    float undersep,
	    float oversep,
	    ResSwitch switchs1,
	    ResHieroglyphic hiero,
	    Vector<ResNote> notes,
	    ResSwitch switchs2) {
	this.type = type;
	this.direction = direction;
	this.mirror = mirror;
	this.scale = scale;
	this.color = color;
	this.shade = shade;
	this.shades = shades;
	this.size = size;
	this.opensep = opensep;
	this.closesep = closesep;
	this.undersep = undersep;
	this.oversep = oversep;
	this.switchs1 = switchs1;
	this.hiero = hiero;
	this.notes = notes;
	this.switchs2 = switchs2; 
    }

    // Default constructor.
    public ResBox() {
	type = typeDefault;
	switchs1 = new ResSwitch();
	notes = new Vector<ResNote>();
	switchs2 = new ResSwitch();
    }

    // Constructor from parser.
    public ResBox(String type, Collection args, 
	    ResSwitch switchs1, ResHieroglyphic hiero,
	    Collection notes, ResSwitch switchs2,
	    int left, int right, IParsingContext context) {
	this.type = type;
	BoxPlaces places = context.getBox(type);
	if (!places.isKnown())
	    context.reportError("Unknown box_type", left, right);
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.is("h"))
		direction = ResValues.DIR_H;
	    else if (arg.is("v"))
		direction = ResValues.DIR_V;
	    else if (arg.is("mirror"))
		mirror = Boolean.TRUE;
	    else if (arg.is("nomirror"))
		mirror = Boolean.FALSE;
	    else if (arg.hasLhs("scale") && arg.hasRhsNonZeroReal())
		scale = arg.getRhsReal();
	    else if (arg.isColor()) 
		color = new Color16(arg.getLhs());
	    else if (arg.is("shade"))
		shade = Boolean.TRUE;
	    else if (arg.is("noshade"))
		shade = Boolean.FALSE;
	    else if (arg.isPattern())
		shades.add(arg.getLhs());
	    else if (arg.hasLhs("size") && arg.hasRhsNonZeroReal())
		size = arg.getRhsReal();
	    else if (arg.hasLhs("opensep") && arg.hasRhsReal())
		opensep = arg.getRhsReal();
	    else if (arg.hasLhs("closesep") && arg.hasRhsReal())
		closesep = arg.getRhsReal();
	    else if (arg.hasLhs("undersep") && arg.hasRhsReal())
		undersep = arg.getRhsReal();
	    else if (arg.hasLhs("oversep") && arg.hasRhsReal())
		oversep = arg.getRhsReal();
	    else 
		context.reportError("Wrong box_arg", arg.left, arg.right);
	}
	this.switchs1 = switchs1;
	this.hiero = hiero;
	this.notes = new Vector<ResNote>(notes);
	this.switchs2 = switchs2;
    }

    // Simplified constructor.
    public ResBox(String type, Collection args, ResSwitch switchs1,
	    ResHieroglyphic hiero, ResSwitch switchs2,
	    IParsingContext context) {
	this(type, args, switchs1, hiero, new Vector<ResNote>(),
		switchs2, -1, -1, context);
    }

    public int nShades() {
	return shades.size();
    }
    public String shade(int i) {
	return shades.get(i);
    }
    public int nNotes() {
	return notes.size();
    }
    public ResNote note(int i) {
	return notes.get(i);
    }

    // Make deep copy.
    public Object clone() {
	ResBox copy = null;
	try {
	    copy = (ResBox) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.shades = (Vector<String>) shades.clone();
	if (hiero != null)
	    copy.hiero = (ResHieroglyphic) hiero.clone();
	copy.notes = new Vector<ResNote>(nNotes());
	for (int i = 0; i < nNotes(); i++) 
	    copy.notes.add((ResNote) note(i).clone());
	return copy;
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	switch (direction) {
	    case ResValues.DIR_H:
		args.add("h");
		break;
	    case ResValues.DIR_V:
		args.add("v");
		break;
	}
	if (mirror != null) {
	    if (mirror.equals(Boolean.TRUE))
		args.add("mirror");
	    else
		args.add("nomirror");
	}
	if (scale != scaleDefault) 
	    args.add("scale=" + ResArg.realString(scale));
	if (color.isColor())
	    args.add("" + color);
	if (shade != null) {
	    if (shade.equals(Boolean.TRUE))
		args.add("shade");
	    else
		args.add("noshade");
	}
	for (int i = 0; i < nShades(); i++)
	    args.add(shade(i));
	if (size != sizeDefault) 
	    args.add("size=" + ResArg.realString(size));
	if (!Float.isNaN(opensep))
	    args.add("opensep=" + ResArg.realString(opensep));
	if (!Float.isNaN(closesep))
	    args.add("closesep=" + ResArg.realString(closesep));
	if (!Float.isNaN(undersep))
	    args.add("undersep=" + ResArg.realString(undersep));
	if (!Float.isNaN(oversep))
	    args.add("oversep=" + ResArg.realString(oversep));
	String s = type + ResArg.toString(args) +
	    "(" + switchs1.toString() + 
	    (hiero == null ? "" : hiero.toString()) + 
	    ")";
	for (int i = 0; i < nNotes(); i++) 
	    s += note(i).toString();
	s += switchs2.toString();
	return s;
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Propagate switch back that occurs after expression.
    public ResSwitch propagateBack(ResSwitch sw) {
	switchs2 = switchs2.join(sw);
	if (hiero != null) {
	    ResSwitch swHiero = hiero.propagateBack();
	    switchs1 = switchs1.join(swHiero);
	}
	return new ResSwitch();
    }
    public ResSwitch propagateBack() {
	return propagateBack(new ResSwitch());
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // In effect at start of box.
    public GlobalValues globals;

    public GlobalValues propagate(GlobalValues globals) {
	this.globals = globals;
	globals = switchs1.update(globals);
	if (hiero != null) {
	    float savedSize = globals.size;
	    globals = globals.update(size);
	    globals = hiero.propagate(globals, dir());
	    globals = globals.update(savedSize);
	}
	for (int i = 0; i < nNotes(); i++) 
	    note(i).propagate(globals);
	return switchs2.update(globals);
    }

    // Direction of fragment.
    public int dirHeader() {
	return GlobalValues.direction(globals, "ResBox");
    }

    // Size of fragment.
    public float sizeHeader() {
        return GlobalValues.sizeHeader(globals, "ResBox");
    }

    // If explicit indication of direction for box, adjust direction.
    // Otherwise, take direction dictated by header.
    public int dir() {
	return GlobalValues.direction(direction, globals, "ResBox");
    }

    // Is box to be mirrored? If no indication at box, take global value.
    public boolean mirror() {
	return GlobalValues.mirror(mirror, globals, "ResBox");
    }

    // Color of box. If no indication at box, take global value.
    public Color16 color() {
	return GlobalValues.color(color, globals, "ResBox");
    }
    // Is it colored?
    public boolean isColored() {
	return color().isColored();
    }

    // Is whole surface to be shaded?
    public boolean shade() {
        return GlobalValues.shade(shade, shades, globals, "ResBox");
    }

    // Is some part shaded?
    public boolean someShade() {
        return GlobalValues.someShade(shade, shades, globals, "ResBox");
    }

    // Separation on sides. If no indication at box, take global value.
    public float opensep() {
	return GlobalValues.sep(opensep, globals, "ResBox");
    }
    public float closesep() {
	return GlobalValues.sep(closesep, globals, "ResBox");
    }
    public float undersep() {
	return GlobalValues.sep(undersep, globals, "ResBox");
    }
    public float oversep() {
	return GlobalValues.sep(oversep, globals, "ResBox");
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

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	return hiero == null ? new Vector<ResNamedglyph>() : hiero.glyphs();
    }

}
