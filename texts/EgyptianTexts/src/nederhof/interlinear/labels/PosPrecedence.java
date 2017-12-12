/***************************************************************************/
/*                                                                         */
/*  PosPrecedence.java                                                     */
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

// Alignment between named positions within one resource.
// The first precedes the second.
// Map from id to set of ids.

package nederhof.interlinear.labels;

import java.util.*;

public class PosPrecedence extends LinkedList {

    // Add mapping. 
    public void add(String id1, String type1, String id2, String type2) {
	add(new Link(id1, type1, id2, type2));
    }

    // Remove mapping.
    public void remove(String id1, String id2) {
	LinkedList toRemove = new LinkedList();
	for (Iterator it = iterator(); it.hasNext(); ) {
	    Link link = (Link) it.next();
	    if (link.id1.equals(id1) &
		    link.id2.equals(id2))
		toRemove.add(link);
	}
	removeAll(toRemove);
    }

    // Get links for from position.
    public LinkedList getFrom(String id1) {
	LinkedList links = new LinkedList();
	for (Iterator it = iterator(); it.hasNext(); ) {
	    Link link = (Link) it.next();
	    if (link.id1.equals(id1))
		links.add(link);
	}
	return links;
    }

}
