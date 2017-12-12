/***************************************************************************/
/*                                                                         */
/*  ResVertsubgroup.java                                                   */
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

package nederhof.res;

import java.util.*;

public class ResVertsubgroup implements Cloneable {

    // See spec of RES.
    public ResSwitch switchs1;
    public ResVertsubgroupPart group;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResVertsubgroup(
	    ResSwitch switchs1,
	    ResVertsubgroupPart group,
	    ResSwitch switchs2) {
	this.switchs1 = switchs1;
	this.group = group;
	this.switchs2 = switchs2;
    }

    // Constructor from parser.
    public ResVertsubgroup(ResVertsubgroupPart group) {
	this.switchs1 = new ResSwitch();
	this.group = group;
	this.switchs2 = new ResSwitch();
    }

    // Make deep copy.
    public Object clone() {
	ResVertsubgroup copy = null;
	try {
	    copy = (ResVertsubgroup) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.group = (ResVertsubgroupPart) group.clone();
	return copy;
    }

    public String toString() {
	return switchs1.toString() + group.toString() + switchs2.toString();
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Move back switch after group.
    public ResSwitch propagateBack(ResSwitch sw) {
        ResSwitch swEnd = switchs2.join(sw);
        switchs2 = new ResSwitch();
        ResSwitch swGroup = group.propagateBack(swEnd);
        ResSwitch swStart = switchs1.join(swGroup);
        switchs1 = new ResSwitch();
        return swStart;
    }
    public ResSwitch propagateBack() {
        return propagateBack(new ResSwitch());
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // Propagate globals through all elements.
    public GlobalValues propagate(GlobalValues globals) {
	globals = switchs1.update(globals);
	globals = group.propagate(globals);
	return switchs2.update(globals);
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	return group.glyphs();
    }

}
