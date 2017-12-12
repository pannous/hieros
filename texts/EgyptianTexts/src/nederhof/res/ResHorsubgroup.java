/***************************************************************************/
/*                                                                         */
/*  ResHorsubgroup.java                                                    */
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

public class ResHorsubgroup implements Cloneable {

    // See spec of RES.
    public ResSwitch switchs1;
    public ResHorsubgroupPart group;
    public ResSwitch switchs2;

    // Direct constructor, taking shallow copy.
    public ResHorsubgroup(
            ResSwitch switchs1,
            ResHorsubgroupPart group,
            ResSwitch switchs2) {
        this.switchs1 = switchs1;
        this.group = group;
        this.switchs2 = switchs2;
    }

    // Constructor from parser.
    public ResHorsubgroup(ResHorsubgroupPart group) {
	this.switchs1 = new ResSwitch();
	this.group = group;
	this.switchs2 = new ResSwitch();
    }

    // Make deep copy.
    public Object clone() {
	ResHorsubgroup copy = null;
	try {
	    copy = (ResHorsubgroup) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.group = (ResHorsubgroupPart) group.clone();
	return copy;
    }

    public String toString() {
	if (group instanceof ResVertgroup)
	    return "(" + switchs1.toString() + group.toString() + ")" + 
		switchs2.toString();
	else
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
