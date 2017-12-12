package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.util.*;

// Determinative specific to one word.
public class FunctionDetWord extends Function {

    // Letters in transliteration (full lemma).
    private TransMdc lemma;

    // Letters in transliteration.
    private TransLow trans;

    // Feminine plural included in trans. To be resolved later.
    private boolean femPlur;

    // Create.
    public FunctionDetWord(String[] hiero, TransMdc lemma, TransLow trans,
	    		boolean femPlur) {
	super(hiero);
	this.lemma = lemma;
	this.trans = trans;
	this.femPlur = femPlur;
	setWeight(lemma.equals(trans) ? 0 : 1);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	return applicablePos(config) >= 0;
    }
    // Can we apply function in configuration and where does lemma start.
    // (If not applicable, then return negative.)
    public int applicablePos(SimpleConfig config) {
	if (!config.afterJump()) 
	    for (int j : config.starts()) 
		if (j + trans.length() > config.getTrans().length())
		    break;
		else if (config.validSubstring(j, trans))
		    return j;
		else if (j+1 + trans.length() > config.getTrans().length())
		    break;
		else if (j < config.getTrans().length() && config.getTrans().charAt(j) == 's' &&
			config.validSubstring(j+1, trans))
		    return j+1;
	return -1;
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	return new ArrayList<Integer>();
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
	SimpleConfig next = new SimpleConfig(config);
	next.setAfterJump(false);
	next.setAfterEpsPhon(false);
	if (femPlur) {
	    next.setFemPlur(femPlur);
	    next.setWordEnd(true);
	}
	return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoDetWord elem = new OrthoDetWord(lemma.toString());
	elem.addSigns(config.getHieroPos(), hiLength());
	TransLow lemmaLow = new TransLow(lemma);
	String common = StringAux.longestCommonPrefix(
		lemmaLow.soundNormalized().toString(), 
		trans.soundNormalized().toString());
	int pos = applicablePos(config);
	elem.addLetters(pos, common.length());
	if (lemma.toString().matches(".*t$") && trans.toString().matches(".*t$"))
	    elem.addLetter(pos + trans.length() - 1);
        return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoDetWord elem = new OrthoDetWord(lemma.toString());
	elem.addSigns(posFrom, len);
        return elem;
    }

    public String name() {
	return "detword";
    }
    public String toString() {
	return super.toString() + "=" + trans + (lemma.equals(trans) ? "" : "/" + lemma);
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + lemma.hashCode() + trans.hashCode() +
	    	(femPlur ? 0 : 1);
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionDetWord other = (FunctionDetWord) obj;
	    if (lemma.compareTo(other.lemma) != 0)
		return lemma.compareTo(other.lemma);
	    else if (trans.compareTo(other.trans) != 0)
		return trans.compareTo(other.trans);
	    else
		return (new Boolean(femPlur)).compareTo(other.femPlur);
        } 
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (super.equals(obj)) {
            FunctionDetWord other = (FunctionDetWord) obj;
            return lemma.equals(other.lemma) &&
                    trans.equals(other.trans) &&
                    femPlur == other.femPlur;
        } else
            return false;
    }
}
