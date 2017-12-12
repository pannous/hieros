package nederhof.interlinear.egyptian.ortho;

import java.util.*;

public class OrthoDet extends OrthoElem {

    public String descr;

    public String name() {
	return "det";
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
    public OrthoDet(String descr) {
	this.descr = descr;
    }
    public OrthoDet(String descr, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.descr = descr;
    }

}
