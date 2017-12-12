/***************************************************************************/
/*                                                                         */
/*  IntHashMap.java                                                        */
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

// Map from objects to ints.

package nederhof.align;

import java.util.*;

class IntHashMap implements Cloneable {

    private HashMap map;

    public IntHashMap() {
	map = new HashMap();
    }

    public boolean containsKey(Object o) {
	return map.containsKey(o);
    }

    public int get(Object o) {
	if (map.containsKey(o)) {
	    Integer i = (Integer) map.get(o);
	    return i.intValue();
	} else
	    return 0;
    }

    public void put(Object o, int i) {
	map.put(o, new Integer(i));
    }

    public void incr(Object o) {
	put(o, get(o) + 1);
    }

    // Add values from other to present.
    public void add(IntHashMap other) {
	Iterator keys = other.map.keySet().iterator();
	while (keys.hasNext()) {
	    Object o = keys.next();
	    put(o, get(o) + other.get(o));
	}
    }

    // Take maximum of existing and new value.
    public void max(Object o, int other) {
	map.put(o, new Integer(Math.max(get(o), other)));
    }

    // Clone.
    public Object clone() {
	IntHashMap copy = new IntHashMap();
	copy.map = (HashMap) map.clone();
	return copy;
    }
}
