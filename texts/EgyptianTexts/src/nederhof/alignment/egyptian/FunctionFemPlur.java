package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Function (Typ or Mult) corresponding to feminine plural.
public abstract class FunctionFemPlur extends Function {

    // Description. Is missing in some subclasses.
    protected String descr = "";

    // Create.
    public FunctionFemPlur(String[] hiero, String descr) {
	super(hiero);
        this.descr = descr;
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
        return !config.afterEpsPhon() &&
	    pos == config.getTrans().length() && config.femPlur();
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
        next.setWordEnd(true);
        next.setFemPlur(false);
        next.setAfterJump(false);
        next.setAfterEpsPhon(false);
        return next;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + descr.hashCode();
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionFemPlur other = (FunctionFemPlur) obj;
            return descr.compareTo(other.descr);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    	super.equals(obj) && descr.equals(((FunctionFemPlur) obj).descr);
    }

}
