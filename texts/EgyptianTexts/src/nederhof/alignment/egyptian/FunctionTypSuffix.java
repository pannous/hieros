package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Typographical, corresponding to suffix in transliteration.
public class FunctionTypSuffix extends FunctionSuffix implements FunctionTyp {

    // Description.
    private String descr;

    // Create.
    public FunctionTypSuffix(String[] hiero, String descr, TransLow trans) {
	super(hiero, trans);
        this.descr = descr;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(config.getHieroPos(), hiLength());
	elem.addLetters(config.getPos(), trans.length());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "typsuffix";
    }
    public String toString() {
	return super.toString() + "=" + descr;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + descr.hashCode();
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionTypSuffix other = (FunctionTypSuffix) obj;
            return descr.compareTo(other.descr);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && descr.equals(((FunctionTypSuffix) obj).descr);
    }

}

