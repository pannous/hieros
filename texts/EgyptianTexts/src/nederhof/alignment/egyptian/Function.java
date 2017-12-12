// Function of sign(s) or jump.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;
import nederhof.util.*;

// Sign function, including jumps.
public abstract class Function implements Comparable<Function> {

    protected String[] hi;

    // Weight (the higher the less likely).
    protected double weight = 0;

    public Function(String[] hi) {
	this.hi = new String[hi.length];
	System.arraycopy(hi, 0, this.hi, 0, hi.length);
    }

    // Get hieroglyphic.
    public String[] getHi() {
	return hi;
    }

    // Number of signs involved.
    public int hiLength() {
	return hi.length;
    }

    // Get weight.
    public double getWeight() {
	return weight;
    }

    // Set weight.
    public void setWeight(double weight) {
	this.weight = weight;
    }

    // Should be unique for each function.
    public abstract String name();

    // Print hieroglyphs, if any, and trailing space.
    public String toString() {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < hi.length; i++) {
	    buf.append(hi[i]);
	    if (i+1 < hi.length)
		buf.append("-");
	    else
		buf.append(" ");
	}
	buf.append(name());
	return buf.toString();
    }

    public int hashCode() {
	final int prime = 31;
	return prime * name().hashCode() + Arrays.hashCode(hi);
    }

    public int compareTo(Function o) {
	if (name().compareTo(o.name()) != 0)
	    return name().compareTo(o.name());
	else
	    return ArrayAux.compareTo(hi, o.hi);
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    obj != null && 
		obj instanceof Function && 
		name().equals(((Function) obj).name()) &&
		Arrays.equals(hi, ((Function) obj).hi);
    }

    // Can we apply function in configuration?
    public abstract boolean applicable(SimpleConfig config);

    // Which jumps are needed to apply the function in the configuration?
    public abstract List<Integer> jumpApplicable(SimpleConfig config);

    // The configuration resulting from function.
    public abstract SimpleConfig apply(SimpleConfig config);

    // The orthographic element. Null if none.
    public abstract OrthoElem orthoElem(ComplexConfig config);
    // The orthographic element, given position of first sign and length.
    public abstract OrthoElem orthoElem(int posFrom, int len);
}
