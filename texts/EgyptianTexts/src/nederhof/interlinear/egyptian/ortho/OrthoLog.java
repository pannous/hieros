package nederhof.interlinear.egyptian.ortho;

import java.util.*;

public class OrthoLog extends OrthoElem {

    public String word;

    public String name() {
        return "log";
    }

    public String argName() {
        return "word";
    }

    public String argValue() {
        return word;
    }

    public void setValue(String val) {
	word = val;
    }

    // Constructor.
    public OrthoLog(String word) {
	this.word = word;
    }
    public OrthoLog(String word, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.word = word;
    }

}
