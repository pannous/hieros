package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Function of number.
public class FunctionNum extends Function {

    // Letters in transliteration.
    private TransLow trans;

    // Create.
    public FunctionNum(String[] hiero, TransLow trans) {
	super(hiero);
	this.trans = trans;
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
        return pos == config.getTrans().length() && config.validSuffix(pos, trans);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	return applicable(config, config.getPos());
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
        List<Integer> jumps = new ArrayList<Integer>();
	if (config.getTrans().length() != config.getPos() &&
		!config.afterJump() && applicable(config, config.getTrans().length()))
	    jumps.add(config.getTrans().length() - config.getPos());
        return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
        SimpleConfig next = new SimpleConfig(config);
	TransLow extended = TransLow.concat(next.getTrans(), trans);
	next.setTrans(extended);
	next.incrPos(trans.length());
	next.setWordEnd(true);
	next.setAfterJump(false);
	next.setAfterEpsPhon(false);
        return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoTyp elem = new OrthoTyp("number");
	elem.addSigns(config.getHieroPos(), hiLength());
	elem.addLetters(config.getPos(), trans.length());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoTyp elem = new OrthoTyp("number");
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "num";
    }
    public String toString() {
	return super.toString() + "=" + trans;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + trans.hashCode();
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionNum other = (FunctionNum) obj;
	    return trans.compareTo(other.trans);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && trans.equals(((FunctionNum) obj).trans);
    }

}
