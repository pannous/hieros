/***************************************************************************/
/*                                                                         */
/*  ResComposer.java                                                       */
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

import nederhof.res.format.*;
import nederhof.res.operations.*;

// Construction of RES fragments, and normalization.
public class ResComposer {

    // Normalize with rotation.
    // O37 --> O36[rotate=120]
    // etc.
    public boolean normalRotate = false;

    // Normalize with repeated signs versus atomic signs.
    // N19 --> N18:[fix,sep=0.5]N18
    // etc.
    public boolean normalRepeat = false;

    // Normalize with removal of MDC97 mnemonics.
    public boolean normalAberrantMnemonics = false;

    // Normalize with removal of MDC numbers.
    public boolean normalAberrantNumbers = false;

    // Normalize by mapping EGPZ names to RES and Unicode-N3349.
    public boolean normalEgpz = false;

    // Normalize with removal of all mnemonics.
    public boolean normalMnemonics = false;

    // Context for parsing RES.
    private IParsingContext context;

    // Full constructor.
    public ResComposer(IParsingContext context,
	    boolean normalRotate,
	    boolean normalRepeat,
	    boolean normalAberrantMnemonics,
	    boolean normalAberrantNumbers,
	    boolean normalEgpz,
	    boolean normalMnemonics) {
	this.context = context;
	this.normalRotate = normalRotate;
	this.normalRepeat = normalRepeat;
	this.normalAberrantMnemonics = normalAberrantMnemonics;
	this.normalAberrantNumbers = normalAberrantNumbers;
	this.normalEgpz = normalEgpz;
	this.normalMnemonics = normalMnemonics;
    }

    // Constructor.
    public ResComposer(IParsingContext context) {
	this(context, false, false, false, false, false, false);
    }

    // Constructor with default parsing context.
    public ResComposer() {
	this(new ParsingContext());
    }

    // Set properties of normalisation.
    public void setRotate(boolean b) {
	normalRotate = b;
    }
    public void setRepeat(boolean b) {
	normalRepeat = b;
    }
    public void setAberrantMnemonics(boolean b) {
	normalAberrantMnemonics = b;
    }
    public void setAberrantNumbers(boolean b) {
	normalAberrantNumbers = b;
    }
    public void setEgpz(boolean b) {
	normalEgpz = b;
    }
    public void setMnemonics(boolean b) {
	normalMnemonics = b;
    }

    // Normalize existing RES fragment.
    public ResFragment normalize(ResFragment res) {
	ResFragment copy = res.normalizedSwitches();
	if (copy.hiero != null) 
	    normalize(copy.hiero);
	return copy;
    }
    public void normalize(ResHieroglyphic hiero) {
	for (int i = 0; i < hiero.nGroups(); i++) 
	    hiero.groups.set(i, normalize(hiero.group(i)));
    }
    public ResTopgroup normalize(ResTopgroup group) {
	if (group instanceof ResBasicgroup) 
	    return normalize((ResBasicgroup) group);
	else if (group instanceof ResHorgroup)
	    return normalize((ResHorgroup) group);
	else if (group instanceof ResVertgroup)
	    return normalize((ResVertgroup) group);
	else
	    return group;
    }
    public ResTopgroup normalize(ResBasicgroup group) {
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
	else
	    return group;
    }
    public ResTopgroup normalize(ResHorgroup group) {
	float size = group.op(0).size;
	Vector<ResTopgroup> newGroups = new Vector<ResTopgroup>(4,3);
	for (int i = 0; i < group.nGroups(); i++) {
	    ResHorsubgroup sub = group.group(i);
	    ResTopgroup top = normalize(sub.group);
	    newGroups.add(top);
	}
	return ComposeHelper.toHorgroup(newGroups, group.ops, group.switches, size);
    }
    public ResTopgroup normalize(ResVertgroup group) {
	float size = group.op(0).size;
	Vector<ResTopgroup> newGroups = new Vector<ResTopgroup>(4,3);
	for (int i = 0; i < group.nGroups(); i++) {
	    ResVertsubgroup sub = group.group(i);
	    ResTopgroup top = normalize(sub.group);
	    newGroups.add(top);
	}
	return ComposeHelper.toVertgroup(newGroups, group.ops, group.switches, size);
    }
    public ResTopgroup normalize(ResNamedglyph glyph) {
	String gardinerName = context.nameToGardiner(glyph.name);
	if (normalRotate) {
	    String normal = SignNormalisation.rotatedMap(glyph.name);
	    if (normal != null) 
		return normalize(makeTopgroup(normal,
			    glyph.mirror, glyph.rotate, glyph.scale, glyph.color, 
			    glyph.shade, glyph.shades, glyph.notes, glyph.switchs));
	} 
	if (normalRepeat) {
	    String normal = SignNormalisation.repeatedMap(glyph.name);
	    if (normal != null) 
		return normalize(makeTopgroup(normal,
			    glyph.mirror, glyph.rotate, glyph.scale, glyph.color, 
			    glyph.shade, glyph.shades, glyph.notes, glyph.switchs));
	} 
	if (normalAberrantMnemonics) {
	    String normal = SignNormalisation.mdc97MnemomicMap(glyph.name);
	    if (normal != null) 
		return normalize(makeTopgroup(normal,
			    glyph.mirror, glyph.rotate, glyph.scale, glyph.color, 
			    glyph.shade, glyph.shades, glyph.notes, glyph.switchs));
	} 
	if (normalAberrantNumbers) {
	    String normal = SignNormalisation.numberMap(glyph.name);
	    if (normal != null) 
		return normalize(makeTopgroup(normal,
			    glyph.mirror, glyph.rotate, glyph.scale, glyph.color, 
			    glyph.shade, glyph.shades, glyph.notes, glyph.switchs));
	} 
	if (normalEgpz) {
	    String normal = SignNormalisation.egpzMap(glyph.name);
	    if (normal != null) {
		ResTopgroup group = makeTopgroup(normal,
			    glyph.mirror, glyph.rotate, glyph.scale, glyph.color, 
			    glyph.shade, glyph.shades, glyph.notes, glyph.switchs);
		return (group instanceof ResNamedglyph) ? group : normalize(group);
	    }
	} 
	if (normalMnemonics)
	   glyph.name = context.nameToGardiner(glyph.name);
	return glyph;
    }
    public ResTopgroup normalize(ResEmptyglyph empty) {
	return empty;
    }
    public ResTopgroup normalize(ResBox box) {
	if (box.hiero != null) 
	    normalize(box.hiero);
	return box;
    }
    public ResTopgroup normalize(ResStack stack) {
	stack.group1 = normalize(stack.group1);
	stack.group2 = normalize(stack.group2);
	return stack;
    }
    public ResTopgroup normalize(ResInsert insert) {
	insert.group1 = normalize(insert.group1);
	insert.group2 = normalize(insert.group2);
	return insert;
    }
    public ResTopgroup normalize(ResModify modify) {
	modify.group = normalize(modify.group);
	return modify;
    }

    // Rotate more, but for mirrorred, rotation is reversed.
    private static int addRotate(int rotate, int extra, boolean mirror) {
	if (mirror)
	    return (360 + rotate - extra) % 360;
	else
	    return (rotate + extra) % 360;
    }

    // Make topgroup from string.
    public ResTopgroup makeTopgroup(String s) {
	ResFragment frag = ResFragment.parse(s, context);
	if (context.nErrors() > 0 && !context.suppressReporting()) {
	    System.err.println(context.error(0));
	    return null;
	} else if (frag.nGroups() == 1) 
	    return frag.hiero.group(0);
	else {
	    System.err.println("makeTopgroup failed: " + s);
	    return null;
	}
    }

    // Make topgroup from string, and distribute features.
    public ResTopgroup makeTopgroup(String s, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	ResTopgroup group = makeTopgroup(s);
	return distribute(group, 
		mirror, rotate, scale, color, shade, shades, notes, switchs);
    }

    // Distribute features to be interpreted 
    // as if group were single sign.
    public static ResTopgroup distribute(ResTopgroup group, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
        if (group instanceof ResBasicgroup)
            return distribute((ResBasicgroup) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResHorgroup)
            return distribute((ResHorgroup) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
	else if (group instanceof ResVertgroup)
            return distribute((ResVertgroup) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
	else
	    return group;
    }
    public static ResBasicgroup distribute(ResBasicgroup group, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
        if (group instanceof ResNamedglyph)
            return distribute((ResNamedglyph) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResEmptyglyph)
            return distribute((ResEmptyglyph) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResBox)
            return distribute((ResBox) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResStack)
            return distribute((ResStack) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResInsert)
            return distribute((ResInsert) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
        else if (group instanceof ResModify)
            return distribute((ResModify) group,
		    mirror, rotate, scale, color, shade, shades, notes, switchs);
	else
	    return group;
    }
    public static ResTopgroup distribute(ResHorgroup group, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	Vector groups = group.groups; // ignoring hor versus vert
	distribute(groups, group.ops, 0, group.nGroups(), true,
		mirror, rotate, scale, color, shade, shades, notes, switchs);
	float size = group.op(0).size;
	if (isMirroring(mirror, rotate)) {
	    Vector mirrorGroups = new Vector(4,3);
	    Vector<ResOp> mirrorOps = new Vector<ResOp>(3,3);
	    Vector<ResSwitch> mirrorSwitches = new Vector<ResSwitch>(3,3);
	    for (int i = group.nGroups() - 1; i >= 0; i--)
		mirrorGroups.add(groups.get(i));
	    for (int i = group.nOps() - 1; i >= 0; i--)
		mirrorOps.add(group.op(i));
	    for (int i = group.nSwitches() - 1; i >= 0; i--)
		mirrorSwitches.add(group.switchs(i));
	    groups = mirrorGroups;
	    group.ops = mirrorOps;
	    group.switches = mirrorSwitches;
	    group.op(0).size = size;
	}
	if (isHorVertSwap(mirror, rotate))
	    return new ResVertgroup((Vector<ResVertsubgroup>) groups, 
		    group.ops, group.switches, size);
	else 
	    return new ResHorgroup((Vector<ResHorsubgroup>) groups, 
		    group.ops, group.switches, size);
    }
    public static ResTopgroup distribute(ResVertgroup group, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	Vector groups = group.groups; // ignoring hor versus vert
	distribute(groups, group.ops, 0, group.nGroups(), false,
		mirror, rotate, scale, color, shade, shades, notes, switchs);
	float size = group.op(0).size;
	if (isMirroring(mirror, rotate)) {
	    Vector mirrorGroups = new Vector(4,3);
	    Vector<ResOp> mirrorOps = new Vector<ResOp>(3,3);
	    Vector<ResSwitch> mirrorSwitches = new Vector<ResSwitch>(3,3);
	    for (int i = group.nGroups() - 1; i >= 0; i--)
		mirrorGroups.add(groups.get(i));
	    for (int i = group.nOps() - 1; i >= 0; i--)
		mirrorOps.add(group.op(i));
	    for (int i = group.nSwitches() - 1; i >= 0; i--)
		mirrorSwitches.add(group.switchs(i));
	    groups = mirrorGroups;
	    group.ops = mirrorOps;
	    group.switches = mirrorSwitches;
	    group.op(0).size = size;
	}
	if (isHorVertSwap(mirror, rotate)) 
	    return new ResHorgroup((Vector<ResHorsubgroup>) groups, 
		    group.ops, group.switches, size);
	else 
	    return new ResVertgroup((Vector<ResVertsubgroup>) groups,
		    group.ops, group.switches, size);
    }
    public static ResNamedglyph distribute(ResNamedglyph glyph, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	if (glyph.mirror == null)
	    glyph.mirror = mirror;
	else if (mirror != null)
	    glyph.mirror = 
		new Boolean(glyph.mirror.booleanValue() ^ mirror.booleanValue());
	boolean resultMirror = glyph.mirror != null && glyph.mirror.booleanValue();
	glyph.rotate = addRotate(glyph.rotate, rotate, resultMirror);
	glyph.scale *= scale;
	if (!glyph.color.isColor())
	    glyph.color = color;
	if (glyph.shade == null)
	    glyph.shade = shade;
	glyph.shades.addAll(shades);
	glyph.notes.addAll(notes);
	glyph.switchs = glyph.switchs.join(switchs);
	return glyph;
    }
    public static ResEmptyglyph distribute(ResEmptyglyph empty, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	if (empty.shade == null)
	    empty.shade = shade;
	empty.shades.addAll(shades);
	if (notes.size() > 0) {
	    empty.note = (ResNote) notes.get(0);
	    if (notes.size() > 1) {
		System.err.println("WARNING: note ignored in ResComposer.distribute()");
	    }
	}
	empty.switchs = empty.switchs.join(switchs);
	return empty;
    }
    public static ResBox distribute(ResBox box, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	if (box.mirror == null)
	    box.mirror = mirror;
	box.scale *= scale;
	if (!box.color.isColor())
	    box.color = color;
	if (box.shade == null)
	    box.shade = shade;
	box.shades.addAll(shades);
	if (box.hiero != null)
	    box.hiero =
		distribute(box.hiero, mirror, rotate, scale, color, shade, shades, 
		    new Vector<ResNote>(), new ResSwitch());
	box.notes.addAll(notes);
	box.switchs2 = box.switchs2.join(switchs);
	return box;
    }
    public static ResStack distribute(ResStack stack, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	stack.group1 = distribute(stack.group1,
		mirror, rotate, scale, color, shade, shades, 
		notes, new ResSwitch());
	stack.group2 =
	    distribute(stack.group2,
		mirror, rotate, scale, color, null, new Vector<String>(), 
		new Vector<ResNote>(), new ResSwitch());
	stack.switchs2 = stack.switchs2.join(switchs);
	return stack;
    }
    public static ResInsert distribute(ResInsert insert, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	insert.group1 = distribute(insert.group1,
		mirror, rotate, scale, color, shade, shades, 
		notes, new ResSwitch());
	insert.group2 = distribute(insert.group2,
		mirror, rotate, scale, color, null, new Vector<String>(), 
		new Vector<ResNote>(), new ResSwitch());
	insert.switchs2 = insert.switchs2.join(switchs);
	return insert;
    }
    public static ResModify distribute(ResModify modify, Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	if (modify.shade == null)
	    modify.shade = shade;
	modify.shades.addAll(shades);
	modify.group = distribute(modify.group,
		mirror, rotate, scale, color, null, new Vector<String>(), 
		notes, new ResSwitch());
	modify.switchs2 = modify.switchs2.join(switchs);
	return modify;
    }
    public static ResHieroglyphic distribute(ResHieroglyphic hiero, 
	    Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	distribute(hiero.groups, hiero.ops, 0, hiero.nGroups(), 
		ResValues.isH(hiero.direction),
		mirror, rotate, scale, color, shade, shades, notes, switchs);
	return hiero;
    }
    // Treat top groups in range. Binary branching to take care of shade
    // patterns. Horizontal and vertical groups are swapped if needed
    // by rotation.
    public static void distribute(Vector groups, Vector<ResOp> ops, int start, int end, boolean isH,
	    Boolean mirror, int rotate,
	    float scale, Color16 color, Boolean shade, Vector<String> shades,
	    Vector<ResNote> notes, ResSwitch switchs) {
	if (end == start + 1) {
	    Object g = groups.get(start);
	    if (g instanceof ResTopgroup) {
		ResTopgroup top = (ResTopgroup) g;
		top = distribute(top, mirror, rotate, scale, color, shade, shades,
			notes, switchs);
		groups.set(start, top);
	    } else if (g instanceof ResHorsubgroup) {
		ResHorsubgroup hor = (ResHorsubgroup) g;
		ResTopgroup sub = 
		    distribute(hor.group, mirror, rotate, scale, color, shade, shades,
			notes, switchs);
		if (isHorVertSwap(mirror, rotate)) {
		    ResVertsubgroupPart part = (ResVertsubgroupPart) sub;
		    ResVertsubgroup vert = new ResVertsubgroup(hor.switchs1, part, hor.switchs2);
		    groups.set(start, vert);
		} else 
		    hor.group = (ResHorsubgroupPart) sub;
	    } else if (g instanceof ResVertsubgroup) {
		ResVertsubgroup vert = (ResVertsubgroup) g;
		ResTopgroup sub = 
		    distribute(vert.group, mirror, rotate, scale, color, shade, shades,
			notes, switchs);
		if (isHorVertSwap(mirror, rotate)) {
		    ResHorsubgroupPart part = (ResHorsubgroupPart) sub;
		    ResHorsubgroup hor = new ResHorsubgroup(vert.switchs1, part, vert.switchs2);
		    groups.set(start, hor);
		} else 
		    vert.group = (ResVertsubgroupPart) sub;
	    }
	} else if (end > start + 1) {
	    int mid = start + (end-start)/2;
	    ResOp op = (ResOp) ops.get(mid-1);
	    Vector<String> shadesLeft = new Vector<String>();
	    Vector<String> shadesRemain = new Vector<String>();
	    Vector<String> shadesOp = new Vector<String>();
	    Vector<String> shadesRight = new Vector<String>();
	    boolean isShade = shade != null && shade.booleanValue();
	    boolean zoomedLeft = isH ? FormatShadeHelper.zoomInStart(shades, shadesLeft) :
		FormatShadeHelper.zoomInTop(shades, shadesLeft);
	    Boolean shadeLeft = (shade == null && !zoomedLeft) ? null :
		new Boolean(isShade || zoomedLeft);
	    boolean zoomedRemain = isH ? FormatShadeHelper.zoomInEnd(shades, shadesRemain) :
		FormatShadeHelper.zoomInBottom(shades, shadesRemain);
	    Boolean shadeRemain = (shade == null && !zoomedRemain) ? null :
		new Boolean(isShade || zoomedRemain);
	    boolean isShadeRemain = shadeRemain != null && shadeRemain.booleanValue();
	    boolean zoomedOp = isH ? FormatShadeHelper.zoomInStart(shadesRemain, shadesOp) :
		FormatShadeHelper.zoomInTop(shadesRemain, shadesOp);
	    Boolean shadeOp = (shadeRemain == null && !zoomedOp) ? null :
		new Boolean(isShadeRemain || zoomedOp);
	    boolean zoomedRight = isH ? FormatShadeHelper.zoomInEnd(shadesRemain, shadesRight) :
		FormatShadeHelper.zoomInBottom(shadesRemain, shadesRight);
	    Boolean shadeRight = (shadeRemain == null && !zoomedRight) ? null :
		new Boolean(isShadeRemain || zoomedRight);
	    distribute(groups, ops, start, mid, isH,
		    mirror, rotate, scale, color, shadeLeft, shadesLeft,
		    notes, new ResSwitch());
	    distribute(op, shadeOp, shadesOp);
	    distribute(groups, ops, mid, end, isH,
		    mirror, rotate, scale, color, shadeRight, shadesRight,
		    new Vector<ResNote>(), switchs);
	}
    }
    public static void distribute(ResOp op, Boolean shade, Vector<String> shades) {
	if (op.shade == null)
	    op.shade = shade;
	op.shades.addAll(shades);
    }

    // Combine two topgroups combined with vertical operator.
    public static ResVertgroup joinVert(ResTopgroup group1, ResOp op,
	    ResSwitch switchs, ResTopgroup group2) {
	Vector<ResTopgroup> groups = new Vector<ResTopgroup>(2);
	Vector<ResOp> ops = new Vector<ResOp>(1);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(1);
	groups.add(group1);
	groups.add(group2);
	ops.add(op);
	switches.add(switchs);
	return ComposeHelper.toVertgroup(groups, ops, switches, Float.NaN);
    }

    // Combine two topgroups combined with horizontal operator.
    public static ResHorgroup joinHor(ResTopgroup group1, ResOp op,
	    ResSwitch switchs, ResTopgroup group2) {
	Vector<ResTopgroup> groups = new Vector<ResTopgroup>(2);
	Vector<ResOp> ops = new Vector<ResOp>(1);
	Vector<ResSwitch> switches = new Vector<ResSwitch>(1);
	groups.add(group1);
	groups.add(group2);
	ops.add(op);
	switches.add(switchs);
	return ComposeHelper.toHorgroup(groups, ops, switches, Float.NaN);
    }

    // Is transformation of group turning horizontal into vertical?
    private static boolean isHorVertSwap(Boolean mirror, int rotate) {
	return (rotate >= 45 && rotate < 180-45) ||
	    (rotate >= 180+45 && rotate < 360-45);
    }

    // Is mirroring order of subgroups in horgroup or vertgroup?
    private static boolean isMirroring(Boolean mirror, int rotate) {
	boolean mir = (mirror == null ? false : mirror.booleanValue());
	boolean rot = rotate >= 180-45 && rotate < 360-45;
	return mir ^ rot;
    }

    // Get Gardiner name of glyph.
    public String getGardinerName(ResNamedglyph glyph) {
	return context.nameToGardiner(glyph.name);
    }

    // Append two fragments of RES.
    public static ResFragment append(ResFragment frag1, ResFragment frag2) {
	ResFragment combined;
	if (frag1.nGroups() == 0) {
	    combined = (ResFragment) frag2.clone();
	    combined.direction = frag1.direction;
	    combined.size = frag1.size;
	    GlobalValues globals1 = frag1.globalValues();
	    combined.switchs = frag2.switchs.reset(globals1);
	} else if (frag2.nGroups() == 0) {
	    combined = (ResFragment) frag1.clone();
	} else {
	    combined = (ResFragment) frag1.clone();
	    frag2 = (ResFragment) frag2.clone();
	    ResHieroglyphic hiero1 = combined.hiero;
	    ResHieroglyphic hiero2 = frag2.hiero;
	    GlobalValues globals1 = combined.globalValues();
	    ResSwitch clearingSwitch = frag2.switchs.reset(globals1);
	    hiero1.ops.add(new ResOp());
	    hiero1.switches.add(clearingSwitch);
	    hiero1.groups.add(hiero2.groups.get(0));
	    for (int i = 0; i < hiero2.ops.size(); i++) {
		hiero1.ops.add(hiero2.ops.get(i));
		hiero1.switches.add(hiero2.switches.get(i));
		hiero1.groups.add(hiero2.groups.get(i+1));
	    }
	}
	return combined;
    }

    // Put named glyphs together into flat RES.
    public ResFragment composeNames(Vector<String> glyphNames) {
	if (glyphNames.size() == 0)
	    return new ResFragment();
	else {
	    StringBuffer b = new StringBuffer();
	    b.append(glyphNames.get(0));
	    for (int i = 1; i < glyphNames.size(); i++)
		b.append("-" + glyphNames.get(i));
	    return ResFragment.parse(b.toString(), context);
	}
    }
    // As above, but without parsing.
    public static String lazyComposeNames(Vector<String> glyphNames) {
	if (glyphNames.size() == 0)
	    return "";
	else {
	    StringBuffer b = new StringBuffer();
	    b.append(glyphNames.get(0));
	    for (int i = 1; i < glyphNames.size(); i++)
		b.append("-" + glyphNames.get(i));
	    return b.toString();
	}
    }

    // Testing
    public static void main(String[] args) {
	IParsingContext context = new ParsingContext();
	context.setIgnoreWarnings(true);
	ResComposer composer = new ResComposer(context);
	composer.normalRepeat = true;
	composer.normalRotate = true;
	composer.normalMnemonics = true;
	Iterator it = SignNormalisation.repeatedMapping().keySet().iterator();
	while (it.hasNext()) {
	    String fromGlyph = (String) it.next();
	    String resString = "nTr-ir-A1:" + fromGlyph + "[red,rotate=90,s]";
	    ResFragment res = ResFragment.parse(resString, context);
	    System.out.println("Was: " + res);
	    System.out.println(composer.normalize(res));
	}
    }

}
