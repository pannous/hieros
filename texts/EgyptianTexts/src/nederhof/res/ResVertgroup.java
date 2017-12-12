/***************************************************************************/
/*                                                                         */
/*  ResVertgroup.java                                                      */
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

public class ResVertgroup implements ResHorsubgroupPart {

    // See spec of RES.
    // The number of groups is one greater than number of ops and switches.
    public Vector<ResVertsubgroup> groups = new Vector<ResVertsubgroup>(4,3);
    public Vector<ResOp> ops = new Vector<ResOp>(3,3);
    public Vector<ResSwitch> switches = new Vector<ResSwitch>(3,3);

    // Direct constructor, taking shallow copy.
    public ResVertgroup(
	    Vector<ResVertsubgroup> groups,
	    Vector<ResOp> ops,
	    Vector<ResSwitch> switches) {
	this.groups = groups;
	this.ops = ops;
	this.switches = switches;
    }

    // Simplified constructors.
    public ResVertgroup(ResVertsubgroupPart group1, ResOp op,
            ResSwitch switchs, ResVertsubgroupPart group2) {
        this(new ResVertsubgroup(group1), op, switchs, new ResVertsubgroup(group2));
    }
    public ResVertgroup(ResVertsubgroupPart group1, ResOp op,
            ResVertsubgroupPart group2) {
        this(new ResVertsubgroup(group1), op, new ResSwitch(), new ResVertsubgroup(group2));
    }
    public ResVertgroup(ResVertsubgroup group1, ResOp op,
            ResSwitch switchs, ResVertsubgroup group2) {
        groups.add(group1);
        ops.add(op);
        switches.add(switchs);
        groups.add(group2);
    }
    public ResVertgroup(ResVertsubgroup group1, ResOp op, ResVertsubgroup group2) {
        groups.add(group1);
        ops.add(op);
        switches.add(new ResSwitch());
        groups.add(group2);
    }

    // Constructor with explicit size in first operator.
    public ResVertgroup(
            Vector<ResVertsubgroup> groups,
            Vector<ResOp> ops,
            Vector<ResSwitch> switches,
            float size) {
        this(groups, ops, switches);
        op(0).size = size;
    }

    // Constructor from parser.
    public ResVertgroup(ResVertsubgroup group1, Collection args,
	    ResSwitch switchs, ResVertsubgroup group2, IParsingContext context) {
	groups.add(group1);
	ops.add(new ResFirstop(args, context));
	switches.add(switchs);
	groups.add(group2);
    }

    // From parser, add group at end.
    public ResVertgroup addGroup(Collection args,
	    ResSwitch switchs, ResVertsubgroup group, IParsingContext context) {
	ops.add(new ResOp(args, context));
	switches.add(switchs);
	groups.add(group);
	return this;
    }

    // Add group at index i.
    public void addGroupAt(ResVertsubgroup group, ResOp op, ResSwitch switchs, int i) {
        groups.add(i, group);
        ops.add(i, op);
        switches.add(i, switchs);
    }

    // Add operator and group at end.
    public void addGroup(ResOp op, ResSwitch switchs, ResVertsubgroup group) {
	ops.add(op);
	switches.add(switchs);
	groups.add(group);
    }
    public void addGroup(ResOp op, ResVertsubgroupPart group) {
	ops.add(op);
	switches.add(new ResSwitch());
	groups.add(new ResVertsubgroup(group));
    }

    public int nGroups() {
        return groups.size();
    }
    public ResVertsubgroup group(int i) {
        return groups.get(i);
    }
    public int nOps() {
        return ops.size();
    }
    public ResOp op(int i) {
        return ops.get(i);
    }
    public int nSwitches() {
        return switches.size();
    }
    public ResSwitch switchs(int i) {
        return switches.get(i);
    }
    public void setSwitchs(int i, ResSwitch sw) {
        switches.set(i, sw);
    }

    // Make deep copy.
    public Object clone() {
	ResVertgroup copy = null;
	try {
	    copy = (ResVertgroup) super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
	copy.groups = new Vector<ResVertsubgroup>(nGroups(),3);
	for (int i = 0; i < nGroups(); i++) 
	    copy.groups.add((ResVertsubgroup) group(i).clone());
	copy.ops = new Vector<ResOp>(nOps(),3);
	for (int i = 0; i < nOps(); i++) 
	    copy.ops.add((ResOp) op(i).clone());
        copy.switches = (Vector<ResSwitch>) switches.clone();
	return copy;
    }

    public String toString() {
	String s = group(0).toString();
	for (int i = 0; i < nOps(); i++) {
	    s += ":" + op(i).toString() + switchs(i).toString() + 
		group(i+1).toString();
	}
	return s;
    }

    //////////////////////////////////////////////////////////////////
    // Normalisation.

    // Propagate switch back that occurs after expression.
    public ResSwitch propagateBack(ResSwitch sw) {
        for (int i = 1; i < nGroups(); i++) {
            ResSwitch swGroup = (i == nGroups() - 1) ?
                group(i).propagateBack(sw) :
                    group(i).propagateBack();
	    setSwitchs(i-1, switchs(i-1).join(swGroup));
        }
        return group(0).propagateBack();
    }
    public ResSwitch propagateBack() {
        return propagateBack(new ResSwitch());
    }

    //////////////////////////////////////////////////////////////////
    // Global values.

    // In effect at start.
    public GlobalValues globals;

    // Propagate globals through all elements.
    public GlobalValues propagate(GlobalValues globals) {
	this.globals = globals;
	globals = group(0).propagate(globals);
	for (int i = 0; i < nOps(); i++) {
	    op(i).propagate(globals);
	    globals = switchs(i).update(globals);
	    globals = group(i+1).propagate(globals);
	}
	return globals;
    }

    // Direction.
    public int dirHeader() {
        return GlobalValues.direction(globals, "ResHorgroup");
    }

    // Unit size for height. If the size is specified at the
    // first operator, then take that, otherwise the global size.
    public float size() {
        return GlobalValues.size(op(0).size, globals, "ResVertgroup");
    }

    //////////////////////////////////////////////////////////////////
    // Properties.

    // Number of separations betweengroups without "fix".
    public int nPaddable() {
	int num = 0;
	for (int i = 0; i < nOps(); i++) {
	    if (!op(i).fix)
		num++;
	}
	return num;
    }

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs() {
	Vector<ResNamedglyph> list = new Vector<ResNamedglyph>(5, 5);
        for (int i = 0; i < nGroups(); i++) 
	    list.addAll(group(i).glyphs());
	return list;
    }

}
