package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;

// Some function (Typ or Mult) corresponding to a suffix.
public abstract class FunctionSuffix extends Function {

    // Letters in transliteration.
    protected TransLow trans;

    // Create.
    public FunctionSuffix(String[] hiero, TransLow trans) {
	super(hiero);
        this.trans = trans;
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
        return pos + trans.length() >= config.getTrans().length() &&
                config.validSubstring(pos, trans);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
        return applicable(config, config.getPos());
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
        List<Integer> jumps = new ArrayList<Integer>();
	if (!config.afterJump() && !config.afterEpsPhon())
	    for (int pos = Math.max(config.getTrans().length() - trans.length(), 0);
			    pos <= config.getTrans().length(); pos++)
		if (pos != config.getPos() && applicable(config, pos))
		    jumps.add(pos - config.getPos());
        return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
        SimpleConfig next = new SimpleConfig(config);
        if (next.getPos() + trans.length() > next.getTrans().length()) {
            TransLow extended = TransLow.concat(
                    next.getTrans(),
                    trans.substring(next.getTrans().length() - next.getPos()));
            next.setTrans(extended);
        }
        next.incrPos(trans.length());
        next.setWordEnd(true);
        next.setAfterJump(false);
        next.setAfterEpsPhon(false);
        return next;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + trans.hashCode();
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionSuffix other = (FunctionSuffix) obj;
            return trans.compareTo(other.trans);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && trans.equals(((FunctionSuffix) obj).trans);
    }

}
