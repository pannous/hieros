package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Typographical not corresponding to letters.
public class FunctionTypSem extends FunctionSem implements FunctionTyp {

    // Create.
    public FunctionTypSem(String[] hiero, String descr) {
	super(hiero, descr);
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(config.getHieroPos(), hiLength());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "typsem";
    }
    public String toString() {
	return super.toString() + "=" + descr;
    }

}

