package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.alignment.egyptian.*;
import nederhof.interlinear.egyptian.ortho.*;

// Artificial function for word start. For N-gram model.
public class FunctionEnd extends Function {

    public FunctionEnd() {
	super(new String[0]);
    }
    public boolean applicable(SimpleConfig config) {
	return true;
    }
    public List<Integer> jumpApplicable(SimpleConfig config) {
	return null;
    }
    public SimpleConfig apply(SimpleConfig config) {
	return null;
    }
    public OrthoElem orthoElem(ComplexConfig config) {
	return null;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	return null;
    }
    public String name() {
	return "end";
    }

}
