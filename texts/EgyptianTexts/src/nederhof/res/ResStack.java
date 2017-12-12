/***************************************************************************/
/*                                                                         */
/*  ResStack.java                                                          */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEg, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res;

import java.util.*;

public class ResStack implements ResBasicgroup {

    public static final float posDefault = 0.5f;

    // See spec of RES.
    public float x = posDefault;
    public float y = posDefault;
    public String onunder = null;
    public ResSwitch switchs0;
    public ResTopgroup group1;
    public ResSwitch switchs1;
    public ResTopgroup group2;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResStack(
	    float x,
	    float y,
	    String onunder,
	    ResSwitch switchs0,
	    ResTopgroup group1,
	    ResSwitch switchs1,
	    ResTopgroup group2,
	    ResSwitch switchs2) {
	this.x = x;
	this.y = y;
	this.onunder = onunder;
	this.switchs0 = switchs0;
	this.group1 = group1;
	this.switchs1 = switchs1;
	this.group2 = group2;
	this.switchs2 = switchs2;
    }
    
    // Simplified constructors.
    public ResStack(ResSwitch switchs0, ResTopgroup group1, 
	    ResSwitch switchs1, ResTopgroup group2,
	    ResSwitch switchs2) {
	this(posDefault, posDefault, null, switchs0, group1,
		switchs1, group2, switchs2);
    }
    public ResStack(ResTopgroup group1, ResTopgroup group2) {
	this(new ResSwitch(), group1, 
		new ResSwitch(), group2, new ResSwitch());
    }
    public ResStack(ResTopgroup group1, ResTopgroup group2,
	    ResSwitch switchs2) {
	this(posDefault, posDefault, null, new ResSwitch(), group1, 
		new ResSwitch(), group2, switchs2);
    }
    public ResStack(ResTopgroup group1, ResSwitch switchs1,
	    ResTopgroup group2) {
	this(posDefault, posDefault, null, new ResSwitch(), group1, 
		switchs1, group2, new ResSwitch());
    }

    // Constructor from parser.
    public ResStack(Collection args, ResSwitch switchs0,
	    ResTopgroup group1, ResSwitch switchs1,
	    ResTopgroup group2, ResSwitch switchs2,
	    IParsingContext context) {
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (arg.hasLhs("x") && arg.hasRhsLowReal())
		x = arg.getRhsReal();
	    else if (arg.hasLhs("y") && arg.hasRhsLowReal())
		y = arg.getRhsReal();
	    else if (arg.is("on") || arg.is("under"))
		onunder = arg.getLhs();
	    else 
		context.reportError("Wrong stack_arg", arg.left, arg.right);
	}
	this.switchs0 = switchs0;
	this.group1 = group1;
	this.switchs1 = switchs1;
	this.group2 = group2;
	this.switchs2 = switchs2;
    }

    // Make deep copy.
    public Object clone() {
	ResStack copy = null;
	try {
	    copy = (ResStack) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.group1 = (ResTopgroup) group1.clone();
	copy.group2 = (ResTopgroup) group2.clone();
	return copy;
    }

    public String toString() {
	Vector<String> args = new Vector<String>();
	if (x != posDefault)
	    args.add("x=" + ResArg.realString(x));
	if (y != posDefault)
	    args.add("y=" + ResArg.realString(y));
	if (onunder != null)
	    args.add(onunder);
	return "stack" + ResArg.toString(args) + "(" + switchs0.toString() +
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

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	Vector<ResNamedglyph> list = group1.glyphs();
	list.addAll(group2.glyphs());
	return list;
    }

}
