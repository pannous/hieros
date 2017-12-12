package nederhof.interlinear.egyptian.ortho;

import java.util.*;

import nederhof.egyptian.trans.*;

public class OrthoPhon extends OrthoElem {

    public String lit;

    public String name() {
        return "phon";
    }

    public String argName() {
        return "lit";
    }

    public String argValue() {
        return lit;
    }

    public void setValue(String val) {
	lit = val;
    }

    // Constructor.
    public OrthoPhon(TransLow al) {
	this.lit = al.toString();
    }
    public OrthoPhon(String lit, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.lit = lit;
    }

}
