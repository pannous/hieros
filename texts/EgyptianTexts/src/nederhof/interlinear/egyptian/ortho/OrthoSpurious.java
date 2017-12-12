package nederhof.interlinear.egyptian.ortho;

import java.util.*;

public class OrthoSpurious extends OrthoElem {

    public String name() {
        return "spurious";
    }

    public String argName() {
        return null;
    }

    public String argValue() {
        return null;
    }

    public void setValue(String val) {
	// ignored
    }

    // Constructor.
    public OrthoSpurious() {
    }
    public OrthoSpurious(int[][] signs) {
	super(signs);
    }

}
