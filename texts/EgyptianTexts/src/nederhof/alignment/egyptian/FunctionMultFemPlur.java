package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Repetition of previous signs, to denote feminine plural.
public class FunctionMultFemPlur extends FunctionFemPlur implements FunctionMult {

    // Multiplicity.
    private int mult;

    // Create.
    public FunctionMultFemPlur(String[] hiero, int mult) {
	super(hiero, "");
	this.mult = mult;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(config.getHieroPos(), hiLength());
	if (config.existingSubstring(config.getPos(), new TransLow("wt")))
	    elem.addLetter(config.getPos()-2);
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoMult elem = new OrthoMult("" + mult);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "multfemplur";
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
            FunctionMultFemPlur other = (FunctionMultFemPlur) obj;
            return mult - other.mult;
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && mult == ((FunctionMultFemPlur) obj).mult;
    }

}
