package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Normalization of RES.
// This superclass simply makes a deep copy.
// Assumes ResFragment.normalizedSwitches() has been done.
// That is, fresh ResSwitch for:
// ResHorsubgroup.switch1
// ResHorsubgroup.switch2
// ResVertsubgroup.switch1
// ResVertsubgroup.switch2

public class ResNormalizer {

    public ResFragment normalize(ResFragment res) {
	int direction = normalizeDirection(res.direction);
	float size = normalizeSize(res.size);
	ResSwitch switchs = normalize(res.switchs);
	ResHieroglyphic hiero = (res.hiero == null) ? null : normalize(res.hiero);
	return new ResFragment(direction, size, switchs, hiero);
    }

    protected ResHieroglyphic normalize(ResHieroglyphic hiero) {
	Vector<ResTopgroup> groups = new Vector<ResTopgroup>(21,20);
	Vector<ResOp> ops = new Vector<ResOp>(20,20);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(20,20);
        for (int i = 0; i < hiero.nGroups(); i++) {
            groups.add(normalize(hiero.group(i)));
	    if (i < hiero.nOps()) {
		ops.add(normalize(hiero.op(i)));
		switches.add(normalize(hiero.switchs(i)));
	    }
        }
	return new ResHieroglyphic(groups, ops, switches);
    }

    protected ResTopgroup normalize(ResTopgroup group) {
        if (group instanceof ResBasicgroup)
            return normalize((ResBasicgroup) group);
        else if (group instanceof ResHorgroup)
            return normalize((ResHorgroup) group);
        else if (group instanceof ResVertgroup)
            return normalize((ResVertgroup) group);
        else /* cannot happen */
            return group;
    }

    protected ResTopgroup normalize(ResBasicgroup group) {
        if (group instanceof ResNamedglyph)
            return normalize((ResNamedglyph) group);
        else if (group instanceof ResEmptyglyph)
            return normalize((ResEmptyglyph) group);
        else if (group instanceof ResBox)
            return normalize((ResBox) group);
        else if (group instanceof ResStack)
            return normalize((ResStack) group);
        else if (group instanceof ResInsert)
            return normalize((ResInsert) group);
        else if (group instanceof ResModify)
            return normalize((ResModify) group);
        else /* cannot happen */
            return group;
    }

    protected ResTopgroup normalize(ResHorgroup group) {
        float size = normalizeOpSize(group.op(0).size);
        Vector<ResTopgroup> groups = new Vector<ResTopgroup>(4,3);
	Vector<ResOp> ops = new Vector<ResOp>(3,3);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(3,3);
        for (int i = 0; i < group.nGroups(); i++) {
	    ResHorsubgroup sub = group.group(i);
	    ResTopgroup top = normalize(sub.group);
            groups.add(normalize(top));
	    if (i < group.nOps()) {
		ops.add(normalize(group.op(i)));
		switches.add(normalize(group.switchs(i)));
	    }
        }
        return ComposeHelper.toHorgroup(groups, ops, switches, size);
    }

    protected ResTopgroup normalize(ResVertgroup group) {
        float size = normalizeOpSize(group.op(0).size);
        Vector<ResTopgroup> groups = new Vector<ResTopgroup>(4,3);
	Vector<ResOp> ops = new Vector<ResOp>(3,3);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(3,3);
        for (int i = 0; i < group.nGroups(); i++) {
	    ResVertsubgroup sub = group.group(i);
	    ResTopgroup top = normalize(sub.group);
            groups.add(normalize(top));
	    if (i < group.nOps()) {
		ops.add(normalize(group.op(i)));
		switches.add(normalize(group.switchs(i)));
	    }
        }
        return ComposeHelper.toVertgroup(groups, ops, switches, size);
    }

    protected ResTopgroup normalize(ResNamedglyph glyph) {
	String name = normalizeName(glyph.name);
	Boolean mirror = normalizeMirror(glyph.mirror);
	int rotate = normalizeRotate(glyph.rotate);
	float scale = normalizeScale(glyph.scale);
	float xscale = normalizeXscale(glyph.xscale);
	float yscale = normalizeYscale(glyph.yscale);
	Color16 color = normalizeColor(glyph.color);
	Boolean shade = normalizeShade(glyph.shade);
	Vector<String> shades = normalizeShades(glyph.shades);
	Vector<ResNote> notes = normalizeNotes(glyph.notes);
	ResSwitch switchs = normalize(glyph.switchs);
	return new ResNamedglyph(name, mirror, rotate, scale, xscale, yscale, color,
		shade, shades, notes, switchs);
    }

    protected ResTopgroup normalize(ResEmptyglyph empty) {
	float width = normalizeEmptySize(empty.width);
	float height = normalizeEmptySize(empty.height);
	Boolean shade = normalizeShade(empty.shade);
	Vector<String> shades = normalizeShades(empty.shades);
	boolean firm = normalizeFirm(empty.firm);
	ResNote note = normalize(empty.note);
	ResSwitch switchs = normalize(empty.switchs);
	return new ResEmptyglyph(width, height, shade, shades, firm, note, switchs);
    }

    protected ResTopgroup normalize(ResBox box) {
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
	ResSwitch switchs1 = normalize(box.switchs1);
	ResHieroglyphic hiero = normalize(box.hiero);
	Vector<ResNote> notes = normalizeNotes(box.notes);
	ResSwitch switchs2 = normalize(box.switchs2);
	return new ResBox(type, direction, mirror, scale, color, shade, shades, size,
			opensep, closesep, undersep, oversep, switchs1, hiero, notes, switchs2);
    }

    protected ResTopgroup normalize(ResStack stack) {
	float x = normalizeStackPos(stack.x);
	float y = normalizeStackPos(stack.y);
	String onunder = normalizeOnunder(stack.onunder);
	ResSwitch switchs0 = normalize(stack.switchs0);
	ResTopgroup group1 = normalize(stack.group1);
	ResSwitch switchs1 = normalize(stack.switchs1);
	ResTopgroup group2 = normalize(stack.group2);
	ResSwitch switchs2 = normalize(stack.switchs2);
	return new ResStack(x, y, onunder, switchs0, group1, switchs1, group2, switchs2);
    }

    protected ResTopgroup normalize(ResInsert insert) {
	String place = normalizePlace(insert.place);
        float x = normalizeInsertPos(insert.x);
        float y = normalizeInsertPos(insert.y);
	boolean fix = normalizeFix(insert.fix);
	float sep = normalizeSep(insert.sep);
        ResSwitch switchs0 = normalize(insert.switchs0);
        ResTopgroup group1 = normalize(insert.group1);
        ResSwitch switchs1 = normalize(insert.switchs1);
        ResTopgroup group2 = normalize(insert.group2);
        ResSwitch switchs2 = normalize(insert.switchs2);
        return new ResInsert(place, x, y, fix, sep, switchs0, group1, switchs1, group2, switchs2);
    }

    protected ResTopgroup normalize(ResModify modify) {
	float width = normalizeModifySize(modify.width);
	float height = normalizeModifySize(modify.height);
	float above = normalizeOutside(modify.above);
	float below = normalizeOutside(modify.below);
	float before = normalizeOutside(modify.before);
	float after = normalizeOutside(modify.after);
	boolean omit = normalizeOmit(modify.omit);
	Boolean shade = normalizeShade(modify.shade);
	Vector<String> shades = normalizeShades(modify.shades);
	ResSwitch switchs1 = normalize(modify.switchs1);
	ResTopgroup group = normalize(modify.group);
	ResSwitch switchs2 = normalize(modify.switchs2);
	return new ResModify(width, height, above, below, before, after, omit, shade, shades,
			switchs1, group, switchs2);
    }

    protected ResOp normalize(ResOp op) {
	float sep = normalizeSep(op.sep);
	Boolean fit = normalizeFit(op.fit);
	boolean fix = normalizeFix(op.fix);
	Boolean shade = normalizeShade(op.shade);
	Vector<String> shades = normalizeShades(op.shades);
	float size = normalizeOpSize(op.size);
	return new ResOp(sep, fit, fix, shade, shades, size);
    }

    protected ResNote normalize(ResNote note) {
	if (note == null)
	    return note;
	else {
	    String string = normalizeString(note.string);
	    Color16 color = normalizeNoteColor(note.color);
	    return new ResNote(string, color);
	}
    }

    protected ResSwitch normalize(ResSwitch switchs) {
    	Color16 color = normalizeColor(switchs.color);
	Boolean shade = normalizeShade(switchs.shade);
	float sep = normalizeSep(switchs.sep);
	Boolean fit = normalizeFit(switchs.fit);
	Boolean mirror = normalizeMirror(switchs.mirror);
	return new ResSwitch(color, shade, sep, fit, mirror);
    }

    protected Vector<ResNote> normalizeNotes(Vector<ResNote> notes) {
	Vector<ResNote> normNotes = new Vector<ResNote>(1,1);
	for (ResNote note : notes) 
	    normNotes.add(normalize(note));
	return normNotes;
    }

    protected Vector<String> normalizeShades(Vector<String> shades) {
	Vector<String> normShades = new Vector<String>(5,5);
	for (String shade : shades) 
	    normShades.add(normalizeShade(shade));
	return normShades;
    }

    //////////////////////////
    // Atomic values.

    protected int normalizeDirection(int direction) {
	return direction;
    }
    protected int normalizeBoxDirection(int direction) {
	return direction;
    }
    protected int normalizeRotate(int rotate) {
	return rotate;
    }
    protected float normalizeSize(float size) {
	return size;
    }
    protected float normalizeOpSize(float size) {
	return size;
    }
    protected float normalizeEmptySize(float size) {
	return size;
    }
    protected float normalizeOutside(float size) {
	return size;
    }
    protected float normalizeModifySize(float size) {
	return size;
    }
    protected float normalizeScale(float scale) {
	return scale;
    }
    protected float normalizeXscale(float scale) {
	return scale;
    }
    protected float normalizeYscale(float scale) {
	return scale;
    }
    protected float normalizeSep(float sep) {
	return sep;
    }
    protected float normalizeStackPos(float pos) {
	return pos;
    }
    protected float normalizeInsertPos(float pos) {
	return pos;
    }
    protected Boolean normalizeMirror(Boolean mirror) {
	return mirror;
    }
    protected Boolean normalizeBoxMirror(Boolean mirror) {
	return mirror;
    }
    protected Boolean normalizeShade(Boolean shade) {
	return shade;
    }
    protected Boolean normalizeFit(Boolean fit) {
	return fit;
    }
    protected boolean normalizeFirm(boolean firm) {
	return firm;
    }
    protected boolean normalizeFix(boolean fix) {
	return fix;
    }
    protected boolean normalizeOmit(boolean omit) {
	return omit;
    }
    protected Color16 normalizeColor(Color16 color) {
	return color;
    }
    protected Color16 normalizeNoteColor(Color16 color) {
	return color;
    }
    protected String normalizeName(String name) {
	return name;
    }
    protected String normalizeType(String type) {
	return type;
    }
    protected String normalizeString(String string) {
	return string;
    }
    protected String normalizeShade(String shade) {
	return shade;
    }
    protected String normalizeOnunder(String onunder) {
	return onunder;
    }
    protected String normalizePlace(String place) {
	return place;
    }

}
