package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Artificial function to jump between positions.
public class FunctionJump extends Function {

    // Distance of jump (non-zero).
    private int dist;

    // Create.
    public FunctionJump(int dist) {
	super(new String[0]);
	this.dist = dist;
	setWeight(5.0 * Math.abs(dist));
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	if (config.afterJump() || config.afterEpsPhon())
	    return false;
	else if (dist < 0) 
	    return config.getPos() + dist >= 0;
	else // dist > 0
	    return config.getPos() + dist <= config.getTrans().length();
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	return new ArrayList<Integer>();
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
	SimpleConfig next = new SimpleConfig(config);
	next.incrPos(dist);
	next.setAfterJump(true);
	next.setAfterEpsPhon(false);
	return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
        return null;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
        return null;
    }

    public String name() {
	return "jump";
    }
    public String toString() {
	return super.toString() + "=" + dist;
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + dist;
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionJump other = (FunctionJump) obj;
            return dist - other.dist;
        }
    }

    public boolean equals(Object obj) {
	return this == obj ||
	    super.equals(obj) && dist == ((FunctionJump) obj).dist;
    }
}
