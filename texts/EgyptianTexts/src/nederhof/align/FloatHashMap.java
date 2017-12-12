/***************************************************************************/
/*                                                                         */
/*  FloatHashMap.java                                                      */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Map from objects to floats.

package nederhof.align;

import java.util.*;

class FloatHashMap implements Cloneable {

    private HashMap map;

    public FloatHashMap() {
	map = new HashMap();
    }

    public boolean containsKey(Object o) {
	return map.containsKey(o);
    }

    public float get(Object o) {
	if (map.containsKey(o)) {
	    Float f = (Float) map.get(o);
	    return f.floatValue();
	} else
	    return 0;
    }

    public void put(Object o, float f) {
	map.put(o, new Float(f));
    }

    // Add values from other to present.
    public void add(FloatHashMap other) {
	Iterator keys = other.map.keySet().iterator();
	while (keys.hasNext()) {
	    Object o = keys.next();
	    put(o, get(o) + other.get(o));
	}
    }

    // Take maximum of existing and new value.
    public void max(Object o, float other) {
	map.put(o, new Float(Math.max(get(o), other)));
    }

    // Clone.
    public Object clone() {
	FloatHashMap copy = new FloatHashMap();
	copy.map = (HashMap) map.clone();
	return copy;
    }
}
