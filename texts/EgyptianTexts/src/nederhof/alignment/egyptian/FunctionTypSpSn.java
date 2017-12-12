package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Sp-sn construction (repetition of last consonants).
public class FunctionTypSpSn extends Function implements FunctionTyp {

    // Description
    private String descr;

    // Size of substring to be repeated.
    private int size;

    // Create.
    public FunctionTypSpSn(String[] hiero, String descr, int size) {
	super(hiero);
        this.descr = descr;
        this.size = size;
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	if (size > config.getPos() || config.getPos() != config.getTrans().length())
	    return false;
	else {
	    TransLow last = config.getTrans().substring(config.getPos() - size);
	    return config.validSubstring(config.getPos(), last);
	}
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
        List<Integer> jumps = new ArrayList<Integer>();
        return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
        SimpleConfig next = new SimpleConfig(config);
	TransLow last = next.getTrans().substring(next.getPos() - size);
	TransLow extended = TransLow.concat(next.getTrans(), last);
	next.setTrans(extended);
        next.incrPos(last.length());
        next.setAfterJump(false);
        next.setAfterEpsPhon(false);
        return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(config.getHieroPos(), hiLength());
	elem.addLetters(config.getPos(), size);
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "typspsn";
    }
    public String toString() {
	return super.toString() + "=" + descr;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + descr.hashCode() + size;
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionTypSpSn other = (FunctionTypSpSn) obj;
	    if (descr.compareTo(other.descr) != 0)
		return descr.compareTo(other.descr);
	    else
		return size - other.size;
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) &&
		descr.equals(((FunctionTypSpSn) obj).descr) &&
		size == ((FunctionTypSpSn) obj).size;
    }

}
