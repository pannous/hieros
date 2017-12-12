package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Semantic function (Det, Typ, Mult), not corresponding to letters.
public abstract class FunctionSem extends Function {

    // Description. Is empty in some subclasses.
    protected String descr = "";

    // Create.
    public FunctionSem(String[] hiero, String descr) {
	super(hiero);
        this.descr = descr;
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
        return !config.afterJump() && !config.afterEpsPhon();
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
        return new ArrayList<Integer>();
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
        SimpleConfig next = new SimpleConfig(config);
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
            FunctionSem other = (FunctionSem) obj;
            return descr.compareTo(other.descr);
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
		super.equals(obj) && descr.equals(((FunctionSem) obj).descr);
    }

}
