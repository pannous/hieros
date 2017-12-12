package nederhof.interlinear.egyptian.ortho;

import java.util.*;

public class OrthoTyp extends OrthoElem {

    public String descr;

    public String name() {
        return "typ";
    }

    public String argName() {
        return "descr";
    }

    public String argValue() {
        return descr;
    }

    public void setValue(String val) {
	descr = val;
    }

    // Constructor.
    public OrthoTyp(String descr) {
	this.descr = descr;
    }
    public OrthoTyp(String descr, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.descr = descr;
    }

}
