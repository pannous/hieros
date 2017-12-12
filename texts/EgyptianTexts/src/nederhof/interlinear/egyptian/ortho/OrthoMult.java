package nederhof.interlinear.egyptian.ortho;

import java.util.*;

// Represents repeated occurrences of one glyph, to express
// duality of plurality.
public class OrthoMult extends OrthoElem {

    // usually 2 (dual) or 3 (plural)
    public String num;

    public String name() {
        return "mult";
    }

    public String argName() {
        return "num";
    }

    public String argValue() {
        return num;
    }

    public void setValue(String val) {
	num = val;
    }

    // Constructor.
    public OrthoMult(String num) {
	this.num = num;
    }
    public OrthoMult(String num, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.num = num;
    }

}
