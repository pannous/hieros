package nederhof.interlinear.egyptian.ortho;

import java.util.*;

public class OrthoDetWord extends OrthoElem {

    public String word;

    public String name() {
	return "det";
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
    public OrthoDetWord(String word) {
	this.word = word;
    }
    public OrthoDetWord(String word, int[][] signs, int[][] letters) {
	super(signs, letters);
	this.word = word;
    }

}
