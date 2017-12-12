/***************************************************************************/
/*                                                                         */
/*  ResNote.java                                                           */
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

public class ResNote implements Cloneable {

    // See spec of RES.
    public String string;
    public Color16 color = Color16.NO_COLOR;

    // Direct constructor, taking shallow copy.
    public ResNote(
	    String string,
	    Color16 color) {
	this.string = string;
	this.color = color;
    }

    // Constructor of default.
    public ResNote() {
	this("\"?\"", Color16.NO_COLOR);
    }

    // Constructor from parser.
    public ResNote(String string, Collection args, IParsingContext context) {
	this.string = string;
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.isColor())
		color = new Color16(arg.getLhs());
	    else 
		context.reportError("Wrong note_arg", arg.left, arg.right);
	}
    }

    // Simplified constructor.
    public ResNote(String string) {
	this(string, Color16.NO_COLOR);
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
	if (color.isColor())
	    args.add("" + color);
	return "^" + string + ResArg.toString(args);
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // In effect at start.
    public GlobalValues globals;

    public void propagate(GlobalValues globals) {
	this.globals = globals;
    }

    // Direction.
    public int dirHeader() {
        return GlobalValues.direction(globals, "ResNote");
    }

    //////////////////////////////////////////////////////////////////
    // Auxiliary routines.

    // Turn into real string by removing initial and final ", and
    // changing \" and \\ into " and \.
    public static String stringify(String str) {
        str = str.replaceFirst("^\"","");
        str = str.replaceFirst("\"$","");
        str = str.replaceAll("\\\\(\\\\|\")","$1");
        return str;
    }

    // Turn into string with escapes.
    public static String escape(String str) {
        str = str.replaceAll("\\\\","\\\\\\\\");
        str = str.replaceAll("\"","\\\\\"");
        return "\"" + str + "\"";
    }

}
