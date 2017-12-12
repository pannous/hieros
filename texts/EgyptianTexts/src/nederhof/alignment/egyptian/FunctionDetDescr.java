package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;

// Determinative, not specific to one word.
public class FunctionDetDescr extends FunctionSem {

    // Create.
    public FunctionDetDescr(String[] hiero, String descr) {
	super(hiero, descr);
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoDet elem = new OrthoDet(descr);
	elem.addSigns(config.getHieroPos(), hiLength());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoDet elem = new OrthoDet(descr);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "detdescr";
    }
    public String toString() {
	return super.toString() + "=" + descr;
    }

}
