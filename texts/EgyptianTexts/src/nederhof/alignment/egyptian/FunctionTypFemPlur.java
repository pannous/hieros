package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Typographical, corresponding to feminine plural.
public class FunctionTypFemPlur extends FunctionFemPlur implements FunctionTyp {

    // Create.
    public FunctionTypFemPlur(String[] hiero, String descr) {
	super(hiero, descr);
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(config.getHieroPos(), hiLength());
	if (config.existingSubstring(config.getPos(), new TransLow("wt")))
	    elem.addLetter(config.getPos()-2);
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoTyp elem = new OrthoTyp(descr);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "typfemplur";
    }
    public String toString() {
	return super.toString() + "=" + descr;
    }

}
