package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Changing ResFragment by removing special named or empty glyphs.

public abstract class NormalizerRemoveSpecial extends ResNormalizer {

    /////////////////////////////////////
    // What is to be removed?

    // Which glyphs to be removed? (Subclass to override.)
    protected boolean isSpecial(ResNamedglyph glyph) {
	return false;
    }
    protected boolean isSpecial(ResEmptyglyph empty) {
	return false;
    }

    // General purpose.
    protected boolean isSpecial(ResTopgroup group) {
	if (group instanceof ResNamedglyph)
	    return isSpecial((ResNamedglyph) group);
	else if (group instanceof ResEmptyglyph)
	    return isSpecial((ResEmptyglyph) group);
	else
	    return false;
    }

    // Give trailing switch of special glyph.
    protected ResSwitch after(ResTopgroup group) {
	if (group instanceof ResNamedglyph)
	    return ((ResNamedglyph) group).switchs;
	else if (group instanceof ResEmptyglyph)
	    return ((ResEmptyglyph) group).switchs;
	else
	    return new ResSwitch();
    }

    // Produce special named or empty glyph on which above
    // predicates return true, with given switch.
    // This creates auxiliary object to propagate 
    // groups to be removed upwards in recursion.
    // Subclass to implement.
    protected abstract ResTopgroup makeSpecial(ResSwitch after);

    /////////////////////////////////////////////
    // Normalization.
    // Most methods have two extra arguments.
    // The first collects switches propagated back.
    // The second collects switches propagated forward.
    // These switches come from subgroups that were removed.

    public ResFragment normalize(ResFragment res) {
        int direction = normalizeDirection(res.direction);
        float size = normalizeSize(res.size);
	PendingSwitch before = new PendingSwitch(normalize(res.switchs));
	PendingSwitch after = new PendingSwitch();
	ResHieroglyphic hiero = (res.hiero == null) ? 
	    null : normalize(res.hiero, before, after);
        return new ResFragment(direction, size, before.switchs, hiero);
    }

    protected ResHieroglyphic normalize(ResHieroglyphic hiero, 
		PendingSwitch before, PendingSwitch last) {
	Vector<ResTopgroup> groups = new Vector<ResTopgroup>(21,20);
	Vector<ResOp> ops = new Vector<ResOp>(20,20);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(20,20);
	ResOp pendingOp = null;
	for (int i = 0; i < hiero.nGroups(); i++) {
	    PendingSwitch after = new PendingSwitch();
	    ResTopgroup group = normalize(hiero.group(i), before, after);
	    if (isSpecial(group))
		before.join(after(group)).join(after);
	    else {
		if (pendingOp != null) {
		    ops.add(pendingOp);
		    pendingOp = null;
		    switches.add(before.switchs);
		}
		groups.add(group);
		before = after;
		if (i < hiero.nOps()) 
		    pendingOp = normalize(hiero.op(i));
	    }
	    if (i < hiero.nSwitches()) 
		before.join(normalize(hiero.switchs(i)));
	}
	if (groups.size() > 0) {
	    last.set(before);
	    return new ResHieroglyphic(groups, ops, switches);
	} else
	    return null;
    }

    protected ResTopgroup normalize(ResTopgroup group, 
		PendingSwitch before, PendingSwitch after) {
        if (group instanceof ResBasicgroup)
            return normalize((ResBasicgroup) group, before, after);
        else if (group instanceof ResHorgroup)
            return normalize((ResHorgroup) group, before, after);
        else if (group instanceof ResVertgroup)
            return normalize((ResVertgroup) group, before, after);
        else /* cannot happen */
            return group;
    }

    protected ResTopgroup normalize(ResBasicgroup group,
		PendingSwitch before, PendingSwitch after) {
        if (group instanceof ResNamedglyph)
            return normalize((ResNamedglyph) group, before, after);
        else if (group instanceof ResEmptyglyph)
            return normalize((ResEmptyglyph) group, before, after);
        else if (group instanceof ResBox)
            return normalize((ResBox) group, before, after);
        else if (group instanceof ResStack)
            return normalize((ResStack) group, before, after);
        else if (group instanceof ResInsert)
            return normalize((ResInsert) group, before, after);
        else if (group instanceof ResModify)
            return normalize((ResModify) group, before, after);
        else /* cannot happen */
            return group;
    }

    protected ResTopgroup normalize(ResHorgroup group,
		PendingSwitch before, PendingSwitch last) {
        float size = normalizeOpSize(group.op(0).size);
        Vector<ResTopgroup> groups = new Vector<ResTopgroup>(4,3);
        Vector<ResOp> ops = new Vector<ResOp>(3,3);
        Vector<ResSwitch> switches = new Vector<ResSwitch>(3,3);
	ResOp pendingOp = null;
        for (int i = 0; i < group.nGroups(); i++) {
	    PendingSwitch after = new PendingSwitch();
            ResHorsubgroup sub = group.group(i);
	    before.join(sub.switchs1);
            ResTopgroup top = normalize(sub.group, before, after);
	    after.join(sub.switchs2);
	    if (isSpecial(top))
		before.join(after(top)).join(after);
	    else {
		if (pendingOp != null) {
		    ops.add(pendingOp);
		    pendingOp = null;
		    switches.add(before.switchs);
		}
		groups.add(top);
		before = after;
		if (i < group.nOps())
		    pendingOp = normalize(group.op(i));
	    }
	    if (i < group.nSwitches())
		before.join(normalize(group.switchs(i)));
	}
	if (groups.size() > 0)
	    last.set(before);
        return ComposeHelper.toHorgroupOrTopgroup(groups, ops, switches, size,
		makeSpecial(before.switchs));
    }
    protected ResTopgroup normalize(ResVertgroup group,
		PendingSwitch before, PendingSwitch last) {
        float size = normalizeOpSize(group.op(0).size);
        Vector<ResTopgroup> groups = new Vector<ResTopgroup>(4,3);
        Vector<ResOp> ops = new Vector<ResOp>(3,3);
        Vector<ResSwitch> switches = new Vector<ResSwitch>(3,3);
	ResOp pendingOp = null;
        for (int i = 0; i < group.nGroups(); i++) {
	    PendingSwitch after = new PendingSwitch();
            ResVertsubgroup sub = group.group(i);
	    before.join(sub.switchs1);
            ResTopgroup top = normalize(sub.group, before, after);
	    after.join(sub.switchs2);
	    if (isSpecial(top))
		before.join(after(top)).join(after);
	    else  {
		if (pendingOp != null) {
		    ops.add(pendingOp);
		    pendingOp = null;
		    switches.add(before.switchs);
		}
		groups.add(top);
		before = after;
		if (i < group.nOps())
		    pendingOp = normalize(group.op(i));
	    }
	    if (i < group.nSwitches())
		before.join(normalize(group.switchs(i)));
	}
	if (groups.size() > 0)
	    last.set(before);
	return ComposeHelper.toVertgroupOrTopgroup(groups, ops, switches, size,
		makeSpecial(before.switchs));
    }

    protected ResTopgroup normalize(ResNamedglyph glyph,
		PendingSwitch before, PendingSwitch last) {
	return super.normalize(glyph);
    }

    protected ResTopgroup normalize(ResEmptyglyph empty,
		PendingSwitch before, PendingSwitch last) {
	return super.normalize(empty);
    }

    protected ResTopgroup normalize(ResBox box,
		PendingSwitch before, PendingSwitch last) {
        String type = normalizeType(box.type);
        int direction = normalizeBoxDirection(box.direction);
        Boolean mirror = normalizeBoxMirror(box.mirror);
        float scale = normalizeScale(box.scale);
        Color16 color = normalizeColor(box.color);
        Boolean shade = normalizeShade(box.shade);
        Vector<String> shades = normalizeShades(box.shades);
        float size = normalizeSize(box.size);
        float opensep = normalizeSep(box.opensep);
        float closesep = normalizeSep(box.closesep);
        float undersep = normalizeSep(box.undersep);
        float oversep = normalizeSep(box.oversep);
	PendingSwitch boxBefore = new PendingSwitch(normalize(box.switchs1));
	PendingSwitch boxAfter = new PendingSwitch();
        ResHieroglyphic hiero = normalize(box.hiero, boxBefore, boxAfter);
	boxAfter = boxAfter.join(normalize(box.switchs2));
        Vector<ResNote> notes = normalizeNotes(box.notes);
        return new ResBox(type, direction, mirror, scale, color, shade, shades, size,
                        opensep, closesep, undersep, oversep, 
			boxBefore.switchs, hiero, notes, boxAfter.switchs);
    }

    protected ResTopgroup normalize(ResStack stack,
		PendingSwitch before, PendingSwitch last) {
        float x = normalizeStackPos(stack.x);
        float y = normalizeStackPos(stack.y);
        String onunder = normalizeOnunder(stack.onunder);
	PendingSwitch pending0 = new PendingSwitch(normalize(stack.switchs0));
	PendingSwitch pending1 = new PendingSwitch();
        ResTopgroup group1 = normalize(stack.group1, pending0, pending1);
	pending1 = pending1.join(normalize(stack.switchs1));
	PendingSwitch pending2 = new PendingSwitch();
        ResTopgroup group2 = normalize(stack.group2, pending1, pending2);
	pending2 = pending2.join(normalize(stack.switchs2));
	if (isSpecial(group1)) {
	    before.join(pending0).join(after(group1)).join(pending1);
	    last.set(pending2.switchs);
	    return group2;
	} else if (isSpecial(group2)) {
	    before.join(pending0);
	    last.set(pending1.join(after(group2)).join(pending2).switchs);
	    return group1;
	} else
	    return new ResStack(x, y, onunder, pending0.switchs, group1, 
		    pending1.switchs, group2, pending2.switchs);
    }

    protected ResTopgroup normalize(ResInsert insert,
		PendingSwitch before, PendingSwitch last) {
        String place = normalizePlace(insert.place);
        float x = normalizeInsertPos(insert.x);
        float y = normalizeInsertPos(insert.y);
        boolean fix = normalizeFix(insert.fix);
        float sep = normalizeSep(insert.sep);
	PendingSwitch pending0 = new PendingSwitch(normalize(insert.switchs0));
	PendingSwitch pending1 = new PendingSwitch();
	ResTopgroup group1 = normalize(insert.group1, pending0, pending1);
	pending1 = pending1.join(normalize(insert.switchs1));
	PendingSwitch pending2 = new PendingSwitch();
	ResTopgroup group2 = normalize(insert.group2, pending1, pending2);
	pending2 = pending2.join(normalize(insert.switchs2));
	if (isSpecial(group1)) {
	    before.join(pending0).join(after(group1)).join(pending1);
	    last.set(pending2.switchs);
	    return group2;
	} else if (isSpecial(group2)) {
	    before.join(pending0);
	    last.set(pending1.join(after(group2)).join(pending2).switchs);
	    return group1;
	} else
	    return new ResInsert(place, x, y, fix, sep, pending0.switchs, group1,
		    pending1.switchs, group2, pending2.switchs);
    }

    protected ResTopgroup normalize(ResModify modify,
		PendingSwitch before, PendingSwitch last) {
        float width = normalizeModifySize(modify.width);
        float height = normalizeModifySize(modify.height);
        float aboveOut = normalizeOutside(modify.above);
        float belowOut = normalizeOutside(modify.below);
        float beforeOut = normalizeOutside(modify.before);
        float afterOut = normalizeOutside(modify.after);
        boolean omit = normalizeOmit(modify.omit);
        Boolean shade = normalizeShade(modify.shade);
        Vector<String> shades = normalizeShades(modify.shades);
	PendingSwitch pending1 = new PendingSwitch(normalize(modify.switchs1));
	PendingSwitch pending2 = new PendingSwitch();
        ResTopgroup group = normalize(modify.group, pending1, pending2);
	pending2 = pending2.join(normalize(modify.switchs2));
	if (isSpecial(group)) {
	    before.join(pending1);
	    last.set(pending2.switchs);
	    return group;
	} else
	    return new ResModify(width, height, 
		    aboveOut, belowOut, beforeOut, afterOut, 
		    omit, shade, shades,
                        pending1.switchs, group, pending2.switchs);
    }

    /////////////////////////
    // Auxiliary.

    // Switch from empty group that is to be removed.
    // Is to be joined to preceding or following switch.
    protected class PendingSwitch {
	public ResSwitch switchs;
	public PendingSwitch() {
	    this(new ResSwitch());
	}
	public PendingSwitch(ResSwitch switchs) {
	    this.switchs = switchs;
	}
	public PendingSwitch join(ResSwitch other) {
	    switchs = switchs.join(other);
	    return this;
	}
	public PendingSwitch join(PendingSwitch other) {
	    join(other.switchs);
	    return this;
	}
	public void set(ResSwitch other) {
	    switchs = other;
	}
	public void set(PendingSwitch other) {
	    set(other.switchs);
	}
    }

}
