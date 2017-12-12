/***************************************************************************/
/*                                                                         */
/*  ResNamedglyph.java                                                     */
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

public class ResNamedglyph implements ResBasicgroup {

    public static final int rotateDefault = 0;
    public static final float scaleDefault = 1.0f;

    // See spec of RES.
    public String name;
    public Boolean mirror = null;
    public int rotate = rotateDefault;
    public float scale = scaleDefault;
    public float xscale = scaleDefault;
    public float yscale = scaleDefault;
    public Color16 color = Color16.NO_COLOR;
    public Boolean shade = null;
    public Vector<String> shades = new Vector<String>(0,5);
    public Vector<ResNote> notes;
    public ResSwitch switchs;

    // Direct constructor, taking shallow copy.
    public ResNamedglyph(
	    String name,
	    Boolean mirror,
	    int rotate,
	    float scale,
	    float xscale,
	    float yscale,
	    Color16 color,
	    Boolean shade,
	    Vector<String> shades,
	    Vector<ResNote> notes,
	    ResSwitch switchs) {
	this.name = name;
	this.mirror = mirror;
	this.rotate = rotate;
	this.scale = scale;
	this.xscale = xscale;
	this.yscale = yscale;
	this.color = color;
	this.shade = shade;
	this.shades = shades;
	this.notes = notes;
	this.switchs = switchs;
    }

    // Constructor with only name and switchs.
    public ResNamedglyph(String name, ResSwitch switchs) {
	this(name, null, rotateDefault, 
		scaleDefault, scaleDefault, scaleDefault, Color16.NO_COLOR,
		null, new Vector<String>(0,5), new Vector<ResNote>(1,2), switchs);
    }

    // Constructor with only name.
    public ResNamedglyph(String name) {
	this(name, new ResSwitch());
    }

    // Constructor from parser.
    public ResNamedglyph(String name, Collection args,
	    Collection notes, ResSwitch switchs,
	    int left, int right, IParsingContext context) {
	this.name = name;
	GlyphPlace place = context.getGlyph(name);
	if (!place.isKnown()) 
	    context.reportWarning("Unknown glyph_name", left, right);
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.is("mirror"))
		mirror = Boolean.TRUE;
	    else if (arg.is("nomirror"))
		mirror = Boolean.FALSE;
	    else if (arg.hasLhs("rotate") && arg.hasRhsNatNum())
		rotate = arg.getRhsNatNum() % 360;
	    else if (arg.hasLhs("scale") && arg.hasRhsNonZeroReal())
		scale = arg.getRhsReal();
	    else if (arg.hasLhs("xscale") && arg.hasRhsNonZeroReal())
		xscale = arg.getRhsReal();
	    else if (arg.hasLhs("yscale") && arg.hasRhsNonZeroReal())
		yscale = arg.getRhsReal();
	    else if (arg.isColor())
		color = new Color16(arg.getLhs());
	    else if (arg.is("shade"))
		shade = Boolean.TRUE;
	    else if (arg.is("noshade"))
		shade = Boolean.FALSE;
	    else if (arg.isPattern())
		shades.add(arg.getLhs());
	    else 
		context.reportError("Wrong named_glyph_arg", arg.left, arg.right);
	}
	this.notes = new Vector<ResNote>(notes);
	this.switchs = switchs;
    }

    // As above, but without positions in the input.
    public ResNamedglyph(String name, Collection args,
	    Collection notes, ResSwitch switchs, IParsingContext context) {
	this(name, args, notes, switchs, -1, -1, context);
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
	ResNamedglyph copy = null;
	try {
	    copy = (ResNamedglyph) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.notes = new Vector<ResNote>(1, 2);
	for (int i = 0; i < nNotes(); i++) 
	    copy.notes.add((ResNote) note(i).clone());
	return copy;
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (mirror != null) {
	    if (mirror.equals(Boolean.TRUE))
		args.add("mirror");
	    else
		args.add("nomirror");
	}
	if (rotate != rotateDefault)
	    args.add("rotate=" + rotate);
	if (scale != scaleDefault)
	    args.add("scale=" + ResArg.realString(scale));
	if (xscale != scaleDefault)
	    args.add("xscale=" + ResArg.realString(xscale));
	if (yscale != scaleDefault)
	    args.add("yscale=" + ResArg.realString(yscale));
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
	String s = name + ResArg.toString(args);
	for (int i = 0; i < nNotes(); i++) 
	    s += note(i).toString();
	s += switchs.toString();
	return s;
    }


    // Add note.
    public void addNote(ResNote note) {
	notes.add(note);
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
	for (int i = 0; i < nNotes(); i++) 
	    note(i).propagate(globals);
	return switchs.update(globals);
    }

    // Direction of fragment.
    public int dirHeader() {
        return GlobalValues.direction(globals, "ResNamedglyph");
    }

    // Size of fragment.
    public float sizeHeader() {
        return GlobalValues.sizeHeader(globals, "ResNamedglyph");
    }

    // Is text to be mirrored? If no indication at glyph, take global value.
    public boolean mirror() {
	return GlobalValues.mirror(mirror, globals, "ResNamedglyph");
    }

    // Color. If no indication at glyph, take global value.
    public Color16 color() {
	return GlobalValues.color(color, globals, "ResNamedglyph");
    }
    // Is it colored?
    public boolean isColored() {
        return color().isColored();
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
    // A white glyph is regarded as a space filler.
    public Vector<ResNamedglyph> glyphs() {
	Vector<ResNamedglyph> list = new Vector<ResNamedglyph>(1, 1);
	if (!Color16.equal(color, Color16.WHITE))
	    list.add(this);
	return list;
    }

}
