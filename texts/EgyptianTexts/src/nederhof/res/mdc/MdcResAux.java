/***************************************************************************/
/*                                                                         */
/*  MdcResAux.java                                                         */
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

// A number of auxiliary functions for converting from MdC to RES.

package nederhof.res.mdc;

import java.util.*;

import nederhof.res.*;

class MdcResAux {

    // Context for parsing.
    private IParsingContext context = silentContext();

    // Context that doesn't bother about warnings of glyph names.
    private IParsingContext silentContext() {
	IParsingContext context = new ParsingContext();
	context.setIgnoreWarnings(true);
	return context;
    }

    // Composition and normalisation of RES.
    private ResComposer composer = new ResComposer(context, true, true, true, true, true,
	    false);

    public SwitchAndTopgroup normalize(SwitchAndTopgroup group) {
	group.group = composer.normalize(group.group);
	return group;
    }

    // Create ligature from sequence of arguments and operators.
    public SwitchAndTopgroup ligature(Vector e) {
	ResTopgroup group;
	ResNamedglyph e0 = (ResNamedglyph) e.get(0);
	String name0 = composer.getGardinerName(e0);
	String firstOp = (String) e.get(1);
	ResNamedglyph e1 = (ResNamedglyph) e.get(2);
	String name1 = composer.getGardinerName(e1);
	if (firstOp.equals("&&")) {
	    ResTopgroup expr = new ResInsert("bs", e0, e1);
	    for (int i = 4; i < e.size(); i += 2) {
		ResNamedglyph next = (ResNamedglyph) e.get(i);
		expr = new ResInsert("bs", expr, next);
	    }
	    group = expr;
	} else {

	    // Given first argument.
	    if ((name0.equals("A14") ||
			name0.equals("F4") ||
			name0.equals("U2")) &&
		    e.size() == 3)
		group = new ResInsert("ts", e0, e1);
	    else if ((name0.equals("F20") ||
			name0.equals("I10") ||
			name0.equals("V22") ||
			name0.equals("V23")) &&
		    e.size() == 3)
		group = new ResInsert("bs", e0, e1);
	    else if ((name0.equals("D17") ||
			name0.equals("E6") ||
			name0.equals("E10") ||
			name0.equals("E20") ||
			name0.equals("G1") ||
			name0.equals("G2") ||
			name0.equals("G4") ||
			name0.equals("G5") ||
			name0.equals("G9") ||
			name0.equals("G11") ||
			name0.equals("G14") ||
			name0.equals("G17") ||
			name0.equals("G18") ||
			name0.equals("G21") ||
			name0.equals("G25") ||
			name0.equals("G29") ||
			name0.equals("G30") ||
			name0.equals("G31") ||
			name0.equals("G35") ||
			name0.equals("G36") ||
			name0.equals("G37") ||
			name0.equals("G38") ||
			name0.equals("G39") ||
			name0.equals("G43") ||
			name0.equals("G47")) &&
		    e.size() == 3) 
		group = new ResInsert("te", e0, e1);
	    else if ((name0.equals("A17")) &&
		    e.size() == 3)
		group = new ResInsert("be", e0, e1);

	    // Given second argument.
	    else if ((name1.equals("B1") ||
			name1.equals("F4") ||
			name1.equals("Z6")) &&
		    e.size() == 3)
		group = new ResInsert("ts", e0, e1);
	    else if ((name1.equals("R8")) &&
		    e.size() == 3)
		group = new ResInsert("bs", e0, e1);
	    else if ((name1.equals("G43") ||
			name1.equals("G14") ||
			name1.equals("G15")) &&
		    e.size() == 3)
		group = composer.makeTopgroup("insert[b](.*" + e1 + "," + e0 + ")");
	    else if ((name1.equals("D58") ||
			name1.equals("G1") ||
		        name1.equals("U1")) &&
		    e.size() == 3)
		group = composer.makeTopgroup("" + e0 + "*[fit]" + e1);

	    // Catch-all.
	    else {
		ResTopgroup expr = new ResStack(e0, e1);
		for (int i = 4; i < e.size(); i += 2) {
		    ResNamedglyph next = (ResNamedglyph) e.get(i);
		    expr = new ResStack(expr, next);
		}
		group = expr;
	    }
	}
	return new SwitchAndTopgroup(group);
    }

    // Gather arguments and operators of ligature in vector.
    public Vector ligature(ResNamedglyph e1, String op, ResNamedglyph e2) {
	Vector e3 = new Vector();
	e3.add(e1);
	e3.add(op);
	e3.add(e2);
	return e3;
    }

    // Append new operator and argument to ligature expression.
    public Vector ligature(Vector e1, String op, ResNamedglyph e2) {
	e1.add(op);
	e1.add(e2);
	return e1;
    }

    // Normalise sign names from MdC to RES.
    // Turn upper-case suffixes to lower-case.
    public ResNamedglyph sign(String in, Vector args, ResSwitch switchs) {
	if (in.matches(".*[A-Za][0-9].*")) {
	    // Suffixes to lower case.
	    char last = in.charAt(in.length() - 1);
	    if ('A' <= last && last <= 'Z')
		in = in.replaceAll(last + "$", 
			"" + Character.toLowerCase(last));
	} 
	return new ResNamedglyph(in, args, new Vector(), switchs, context);
    }

    // To empty glyph.
    public ResEmptyglyph empty(Vector args, ResSwitch switchs) {
	return new ResEmptyglyph(args, null, switchs, context);
    }
    // To empty glyphs with one argument.
    public ResEmptyglyph empty(String arg, ResSwitch switchs) {
	return empty(args(arg), switchs);
    }

    // To box type.
    public SwitchAndTopgroup box(String pre, String post, 
	    SwitchAndHieroglyphic switchHiero, Vector shades, ResSwitch switchs2) {
	Vector args = new Vector();
	for (int i = 0; i < shades.size(); i++) 
	    args.add(new ResArg("" + shades.get(i)));
	String type = "cartouche";
	ResSwitch switchs1 = switchHiero.switchs;
	ResHieroglyphic hiero = switchHiero.hiero;
	if (hiero != null)
	    hiero = composer.distribute(hiero, 
		    null, 0, 1.0f, Color16.NO_COLOR, null, shades, new Vector(),
		    new ResSwitch());
	if (pre.equals("1") && post.equals("1"))
	    type = "oval";
	else if (pre.equals("2") && post.equals("1")) {
	    type = "oval";
	    args.add(new ResArg("mirror"));
	} else if (pre.matches("^S.*"))
	    type = "serekh";
	else if (pre.equals("s2") && post.equals("s1")) {
	    type = "serekh";
	    args.add(new ResArg("mirror"));
	} else if (pre.matches("^s.*"))
	    type = "serekh";
	else if (pre.matches("^F.*"))
	    type = "inb";
	else if (pre.matches("^f.*"))
	    type = "inb";
	else if (pre.matches("^H.*"))
	    type = "Hwtcloseunder";
	else if (pre.equals("h1") && post.equals("h2")) 
	    type = "Hwtcloseunder";
	else if (pre.equals("h1") && post.equals("h3")) 
	    type = "Hwtcloseover";
	else if (pre.equals("h2") && post.equals("h1")) 
	    type = "Hwtopenunder";
	else if (pre.equals("h3") && post.equals("h1")) 
	    type = "Hwtopenover";
	else if (pre.matches("^h.*"))
	    type = "Hwtcloseunder";
	return new SwitchAndTopgroup(
		new ResBox(type, args, switchs1, hiero, switchs2, context));
    }

    // From 1,2,3,4 to pattern of shading.
    public Vector shading(String in) {
	Vector patterns = new Vector();
	if (in.equals(""))
	    patterns.add("shade");
	if (in.matches(".*1.*"))
	    patterns.add("ts");
	if (in.matches(".*2.*"))
	    patterns.add("te");
	if (in.matches(".*3.*"))
	    patterns.add("bs");
	if (in.matches(".*4.*"))
	    patterns.add("be");
	return patterns;
    }

    // Turn sequence of characters into horizontal group.
    public SwitchAndTopgroup string(String in, ResSwitch switchs) {
	ResTopgroup group;
	if (in.length() == 0)
	    group = new ResEmptyglyph(0.0f, 0.0f, switchs);
	else if (in.length() == 1)
	    group = new ResNamedglyph("\"" + in + "\"", switchs);
	else {
	    Vector fixOnly = new Vector(1);
	    fixOnly.add(new ResArg("fix"));

	    Vector groups = new Vector(in.length(), 3);
	    Vector ops = new Vector(in.length()-1, 3);
	    Vector switches = new Vector(in.length()-1,3);
	    for (int i = 0; i < in.length(); i++) 
		groups.add(new ResNamedglyph("\"" + in.charAt(i) + "\""));
	    for (int i = 0; i < in.length()-1; i++) 
		ops.add(new ResOp(fixOnly, context));
	    for (int i = 0; i < in.length()-2; i++) 
		switches.add(new ResSwitch());
	    switches.add(switchs);

	    group = new ResHorgroup(groups, ops, switches);
	}
	return new SwitchAndTopgroup(group);
    }

    // Create switch for only one argument.
    public ResSwitch switchs(String arg) {
	return new ResSwitch(args(arg), context);
    }

    ///////////////////
    // Arguments.

    // Turn one string into arguments.
    public Vector args(String arg) {
	Vector args = new Vector(1, 3);
	args.add(new ResArg(arg));
	return args;
    }

    // Turn lhs/rhs into arguments.
    public Vector args(String lhs, String rhs) {
	Vector args = new Vector(1, 3);
	args.add(new ResArg(lhs, rhs));
	return args;
    }

    // Make rotation positive.
    public Vector rotateArgs(String in) {
	if (in.charAt(0) == '-') 
	    try {
		int degrees = Integer.parseInt(in.substring(1));
		return args("rotate", "" + (360 - degrees));
	    } catch (Exception e) {
		return new Vector();
	    }
	else
	    return args("rotate", "" + in);
    }

    public Vector mirrorRotateArgs(String in) {
	Vector args = rotateArgs(in);
	args.add(new ResArg("mirror"));
	return args;
    }

    // From 100 being unit to 1 being unit.
    public Vector scaleArgs(String in) {
	try {
	    int percent = Integer.parseInt(in);
	    return args("scale", "" + (percent / 100.0));
	} catch (Exception e) {
	    return new Vector();
	}
    }	

    // As above, but number is to be width and height.
    public Vector scaleBlank(String in) {
	try {
	    int percent = Integer.parseInt(in);
	    double ratio = percent / 100.0;
	    Vector args = new Vector(2);
	    args.add(new ResArg("width", "" + ratio));
	    args.add(new ResArg("height", "" + ratio));
	    return args;
	} catch (Exception e) {
	    return new Vector();
	}
    }

}
