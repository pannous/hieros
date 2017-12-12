package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Repetition of previous signs, to represent typically
// dual (mult=2) or plural (mult=3). Corresponds to suffix.
public class FunctionMultSuffix extends FunctionSuffix implements FunctionMult {

    // Multiplicity.
    private int mult;

    // Create.
    public FunctionMultSuffix(String[] hiero, int mult, TransLow trans) {
	super(hiero, trans);
	this.mult = mult;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(config.getHieroPos(), hiLength());
	elem.addLetters(config.getPos(), trans.length());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "multsuffix";
    }
    public String toString() {
	return super.toString() + "=" + mult + "/" + trans;
    }

    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + mult;
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionMultSuffix other = (FunctionMultSuffix) obj;
            return mult - other.mult;
        }
    }

    public boolean equals(Object obj) {
        return this == obj ||
            super.equals(obj) && mult == ((FunctionMultSuffix) obj).mult;
    }

}
