package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Repetition of previous signs, to represent typically
// dual (mult=2) or plural (mult=3), but semantic (not corresponding to
// suffix).
public class FunctionMultSem extends FunctionSem implements FunctionMult {

    // Multiplicity.
    private int mult;

    // Create.
    public FunctionMultSem(String[] hiero, int mult) {
	super(hiero, "");
	this.mult = mult;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(config.getHieroPos(), hiLength());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "multsem";
    }
    public String toString() {
        return super.toString() + "=" + mult;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + mult;
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionMultSem other = (FunctionMultSem) obj;
            return mult - other.mult;
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && mult == ((FunctionMultSem) obj).mult;
    }

}

