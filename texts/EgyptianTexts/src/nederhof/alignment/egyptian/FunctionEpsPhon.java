package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Artifical function to produce letter, not coming from hieroglyph.
public class FunctionEpsPhon extends Function {

    // Letters in transliteration not corresponding to hieroglyphs.
    // Normally this is one letter.
    private TransLow trans;

    // Create.
    public FunctionEpsPhon(TransLow trans) {
	super(new String[0]);
	this.trans = trans;
	if (trans.isPunctuation())
	    setWeight(1);
	else if (trans.isWeak())
	    setWeight(2);
	else
	    setWeight(10);
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
	return pos == config.getTrans().length() && config.validSubstring(pos, trans) &&
	    (!config.femPlur() || !trans.isWordSep());
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	return applicable(config, config.getPos());
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	List<Integer> jumps = new ArrayList<Integer>();
	if (!config.afterJump() && 
			config.getPos() < config.getTrans().length() && 
			applicable(config, config.getTrans().length()))
	    jumps.add(config.getTrans().length() - config.getPos());
	return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
	SimpleConfig next = new SimpleConfig(config);
	TransLow extended = TransLow.concat(next.getTrans(), trans);
	next.setTrans(extended);
	next.incrPos(trans.length());
	next.setAfterJump(false);
	next.setAfterEpsPhon(true);
	return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
        return null;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
        return null;
    }

    public String name() {
	return "epsphon";
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
            FunctionEpsPhon other = (FunctionEpsPhon) obj;
	    return trans.compareTo(other.trans);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && trans.equals(((FunctionEpsPhon) obj).trans);
    }

}
