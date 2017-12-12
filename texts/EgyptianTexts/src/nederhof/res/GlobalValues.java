/***************************************************************************/
/*                                                                         */
/*  GlobalValues.java                                                      */
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

// Global values in RES.
// Every time a value is changed, a new copy is made, so that 
// objects are effectively immutable.

package nederhof.res;

import java.util.*;

public class GlobalValues implements Cloneable {

    public static final int directionDefault = ResValues.DIR_HLR;
    public static final float sizeDefault = 1.0f;
    public static final Color16 colorDefault = Color16.BLACK;
    public static final boolean shadeDefault = false;
    public static final float sepDefault = 1.0f;
    public static final boolean fitDefault = false;
    public static final boolean mirrorDefault = false;

    // Values determined in header.
    public int direction = directionDefault;
    public float sizeHeader = sizeDefault;
    // Normally equal to sizeHeader, but may be overridden in boxes.
    public float size = sizeHeader;

    // Values affected by switches.
    // Initially the defaults.
    public Color16 color = colorDefault;
    public boolean shade = shadeDefault;
    public float sep = sepDefault;
    public boolean fit = fitDefault;
    public boolean mirror = mirrorDefault;

    // Called once for fragment.
    public GlobalValues(int direction, float size) {
	if (direction != ResValues.DIR_NONE)
	    this.direction = direction;
	if (!Float.isNaN(size))
	    this.size = this.sizeHeader = size;
    }

    // Update or restore size at start or end of box content.
    public GlobalValues update(float size) {
	if (size == this.size)
	    return this;
	else {
	    GlobalValues newVals = (GlobalValues) clone();
	    newVals.size = size;
	    return newVals;
	}
    }

    // Make copy.
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
    }

    // Let local value override global.
    public int direction(int local) {
	switch (local) {
            case ResValues.DIR_H:
		switch (direction) {
		    case ResValues.DIR_VLR:
			return ResValues.DIR_HLR;
		    case ResValues.DIR_VRL:
			return ResValues.DIR_HRL;
		    default:
			return direction;
		}
            case ResValues.DIR_V:
		switch (direction) {
		    case ResValues.DIR_HLR:
			return ResValues.DIR_VLR;
		    case ResValues.DIR_HRL:
			return ResValues.DIR_VRL;
		    default:
			return direction;
		}
	    default: /* ResValues.DIR_NONE */
		return direction;
	}
    }

    // Let local value override global.
    public float size(float local) {
	return !Float.isNaN(local) ? local : size;
    }

    // Let local value override global.
    public Color16 color(Color16 local) {
	return local.isColor() ? local : color;
    }

    // Let any local value override global.
    public boolean shade(Boolean local, Vector locals) {
	return (local != null || !locals.isEmpty()) ? 
	    (local != null && local.booleanValue()) : shade;
    }

    // Is some part shaded?
    public boolean someShade(Boolean local, Vector locals) {
	return shade(local, locals) || !locals.isEmpty();
    }

    // Let local value override global.
    public float sep(float local) {
	return !Float.isNaN(local) ? local : sep;
    }

    // Let local value override global.
    public boolean fit(Boolean local) {
	return local != null ? local.booleanValue() : fit;
    }

    // Let local value override global.
    public boolean mirror(Boolean local) {
	return local != null ? local.booleanValue() : mirror;
    }

    // Let local value override global, if any.
    public static int direction(int local, GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local;
	} else
	    return globals.direction(local);
    }

    // Let local value override global, if any.
    public static float size(float local, GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local;
	} else
	    return globals.size(local);
    }

    // Take size from first global in vector, if any.
    public static float size(Vector<GlobalValues> globalss, String className) {
	if (globalss == null) {
	    reportMissingPropagate(className);
	    return sizeDefault;
	} else {
	    GlobalValues globals = globalss.get(0);
	    return globals.size;
	}
    }

    // Take size in header.
    public static float sizeHeader(GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return sizeDefault;
	} else 
	    return globals.sizeHeader;
    }

    // Let local value override global, if any.
    public static float sep(float local, GlobalValues globals,
	    String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local;
	} else
	    return globals.sep(local);
    }

    // Let local value override global, if any.
    public static Color16 color(Color16 local, GlobalValues globals,
	    String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local;
	} else
	    return globals.color(local);
    }

    // Let local value override global, if any.
    public static boolean shade(Boolean local, Vector locals,
	    GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return false;
	} else
	    return globals.shade(local, locals);
    }

    // Let local value override global, if any.
    public static boolean someShade(Boolean local, Vector locals,
	    GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return false;
	} else
	    return globals.someShade(local, locals);
    }

    // Let local value override global, if any.
    public static boolean fit(Boolean local, GlobalValues globals,
	    String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local.booleanValue();
	} else
	    return globals.fit(local);
    }

    // Let local value override global, if any.
    public static boolean mirror(Boolean local, GlobalValues globals,
	    String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return local.booleanValue();
	} else
	    return globals.mirror(local);
    }

    // Return direction, verifying globals exist.
    public static int direction(GlobalValues globals, String className) {
	return direction(ResValues.DIR_HLR, globals, className);
    }

    // Return whether colored text, verifying globals exist.
    public static boolean isColored(GlobalValues globals, String className) {
	if (globals == null) {
	    reportMissingPropagate(className);
	    return false;
	} else
	    return globals.color.isColored();
    }

    private static void reportMissingPropagate(String className) {
	System.err.println("propagate should have been called first in " +
		className);
    }

}
