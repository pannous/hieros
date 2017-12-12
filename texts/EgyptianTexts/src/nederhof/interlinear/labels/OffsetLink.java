// Two labelled positions in two different tiers,
// plus one offset each.

package nederhof.interlinear.labels;

public class OffsetLink {

    public String id1;
    public int offset1;
    public String type1;
    public String id2;
    public int offset2;
    public String type2;

    // Constructor.
    public OffsetLink(String id1, int offset1, String type1,
	    String id2, int offset2, String type2) {
	this.id1 = id1;
	this.offset1 = offset1;
	this.type1 = type1;
	this.id2 = id2;
	this.offset2 = offset2;
	this.type2 = type2;
    }

    // Equals if all fields are equal.
    public boolean equals(Object o) {
	if (o instanceof OffsetLink) {
	    OffsetLink other = (OffsetLink) o;
	    return id1.equals(other.id1) &&
		offset1 == other.offset1 &&
		type1.equals(other.type1) &&
		id2.equals(other.id2) &&
		offset2 == other.offset2 &&
		type2.equals(other.type2);
	} else
	    return false;
    }

    // For testing.
    public String toString() {
	return id1 + "," + offset1 + "," + type1 + "," +
	    id2 + "," + offset2 + "," + type2;
    }

}
