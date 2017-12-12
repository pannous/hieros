// Alignment between named positions within two different resources,
// together with offset.

package nederhof.interlinear.labels;

import java.util.*;

public class InterPosPrecedence extends LinkedList<OffsetLink> {

    // Add mapping. 
    public void add(
	    String id1, int offset1, String type1, 
	    String id2, int offset2, String type2) {
	add(new OffsetLink(id1, offset1, type1, id2, offset2, type2));
    }

    // Remove mapping for carthesian product.
    public void remove(TreeSet positions1, TreeSet positions2) {
	LinkedList<OffsetLink> toRemove = new LinkedList<OffsetLink>();
	for (Iterator<OffsetLink> it = iterator(); it.hasNext(); ) {
	    OffsetLink link = it.next();
	    LabelOffset first = new LabelOffset(link.id1, link.offset1);
	    LabelOffset second = new LabelOffset(link.id2, link.offset2);
	    if (positions1.contains(first) &&
		    positions2.contains(second)) 
		toRemove.add(link);
	}
	removeAll(toRemove);
    }

}
