// Combination of label of position in a resource and offset.

package nederhof.interlinear.labels;

public class LabelOffset implements Comparable {

    // The label is "" if the offset represents
    // the absolute position in the resource.
    public String label;
    public int offset;

    public LabelOffset(String label, int offset) {
	this.label = label;
	this.offset = offset;
    }

    // Equality.
    public boolean equals(Object o) {
	if (o instanceof LabelOffset) {
	    LabelOffset other = (LabelOffset) o;
	    return other.label.equals(label) &&
		other.offset == offset;
	} else
	    return false;
    }

    // Make sure those with lowest absolute offset are first.
    public int compareTo(Object o) {
	if (o instanceof LabelOffset) {
	    LabelOffset other = (LabelOffset) o;
	    if (Math.abs(offset) < Math.abs(other.offset))
		return -1;
	    else if (Math.abs(offset) > Math.abs(other.offset))
		return 1;
	    else
		return label.compareTo(other.label);
	} else
	    return 1;
    }

    // For resting.
    public String toString() {
	return label + "," + offset;
    }

}
