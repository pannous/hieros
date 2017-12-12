/***************************************************************************/
/*                                                                         */
/*  ArrayAux.java                                                          */
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

package nederhof.util;

import java.util.*;

public class ArrayAux {

    // The argument is a list of strings of even length.
    // This is converted to a map from every first to every second
    // string.
    public static HashMap arrayToMap(String[] array) {
	HashMap map = new HashMap(array.length / 2);
	if (array.length % 2 != 0)
	    System.err.println("Odd array length in nederhof.util.arrayToMap");
	else 
	    for (int i = 0; i < array.length; i += 2) 
		map.put(array[i], array[i+1]);
	return map;
    }

    // Convert collection of strings to array of string.
    public static String[] toStringArray(Collection coll) {
	String[] ar = new String[coll.size()];
	int i = 0;
	for (Iterator it = coll.iterator(); it.hasNext(); ) {
	    String elem = (String) it.next();
	    ar[i++] = elem;
	}
	return ar;
    }

    // Convert collection of Integers to array of in.
    public static int[] toIntArray(Collection coll) {
	int[] ar = new int[coll.size()];
	int i = 0;
	for (Iterator it = coll.iterator(); it.hasNext(); ) {
	    Integer elem = (Integer) it.next();
	    ar[i++] = elem.intValue();
	}
	return ar;
    }

    // Compare two arrays.
    public static int compareTo(Comparable[] a, Comparable[] b) {
	for (int i = 0; i < Math.min(a.length, b.length); i++)
	    if (a[i].compareTo(b[i]) != 0)
		return a[i].compareTo(b[i]);
	if (a.length < b.length)
	    return -1;
	else if (a.length > b.length)
	    return 1;
	else 
	    return 0;
    }

}
