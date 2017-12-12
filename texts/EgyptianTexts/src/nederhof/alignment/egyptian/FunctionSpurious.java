package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Function of hieroglyph not doing anything.
public class FunctionSpurious extends Function {

    // Create.
    public FunctionSpurious(String[] hiero) {
	super(hiero);
	setWeight(10);
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

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoSpurious elem = new OrthoSpurious();
	elem.addSigns(config.getHieroPos(), hiLength());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoSpurious elem = new OrthoSpurious();
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "spurious";
    }

}
