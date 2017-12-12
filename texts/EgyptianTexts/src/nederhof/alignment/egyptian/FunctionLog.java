package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.util.*;

public class FunctionLog extends Function {

    // Letters in transliteration (full lemma).
    private TransMdc lemma;

    // Letters in transliteration.
    private TransLow trans;

    // Feminine plural included in trans. To be resolved later.
    private boolean femPlur;

    // Create.
    public FunctionLog(String[] hiero, TransMdc lemma, TransLow trans, boolean femPlur) {
	super(hiero);
	this.lemma = lemma;
	this.trans = trans;
	this.femPlur = femPlur;
	setWeight(lemma.equals(trans) ? 0 : 1);
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos, List<Integer> starts) {
	if ((trans.length() == 0 || !trans.substring(0,1).isWordSep()) &&
		!starts.contains(pos) &&
		(!starts.contains(pos-1) || config.getTrans().charAt(pos-1) != 's'))
	    return false;
	else
	    return config.validSubstring(pos, trans);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	List<Integer> starts = config.starts();
	return applicable(config, config.getPos(), starts);
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	List<Integer> starts = config.starts();
	List<Integer> jumps = new ArrayList<Integer>();
	if (!config.afterJump() && !config.afterEpsPhon()) 
	    for (int j : starts) {
		if (j != config.getPos() && applicable(config, j, starts))
		    jumps.add(j - config.getPos());
		if (j < config.getTrans().length() && config.getTrans().charAt(j) == 's' &&
			j+1 != config.getPos() && applicable(config, j+1, starts))
		    jumps.add(j+1 - config.getPos());
	    }
	return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
	SimpleConfig next = new SimpleConfig(config);
	if (next.getPos() + trans.length() > next.getTrans().length()) {
	    TransLow extended = TransLow.concat(
		    next.getTrans(),
		    trans.substring(next.getTrans().length() - next.getPos()));
	    next.setTrans(extended);
	    if (femPlur) {
		next.setFemPlur(femPlur);
		next.setWordEnd(true);
	    }
	}
	next.incrPos(trans.length());
	next.setAfterJump(false);
	next.setAfterEpsPhon(false);
	return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoLog elem = new OrthoLog(lemma.toString());
	elem.addSigns(config.getHieroPos(), hiLength());
	TransLow lemmaLow = new TransLow(lemma);
	String common = StringAux.longestCommonPrefix(
		lemmaLow.soundNormalized().toString(), 
		trans.soundNormalized().toString());
	elem.addLetters(config.getPos(), common.length());
	if (lemma.toString().matches(".*t$") && trans.toString().matches(".*t$"))
	    elem.addLetter(config.getPos() + trans.length() - 1);
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoLog elem = new OrthoLog(lemma.toString());
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "log";
    }
    public String toString() {
	return super.toString() + "=" + trans + (lemma.equals(trans) ? "" : "/" + lemma);
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + lemma.hashCode() + trans.hashCode() + (femPlur ? 0 : 1);
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionLog other = (FunctionLog) obj;
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
            FunctionLog other = (FunctionLog) obj;
            return lemma.equals(other.lemma) && 
		    trans.equals(other.trans) && 
		    femPlur == other.femPlur;
        } else
            return false;
    }

}
