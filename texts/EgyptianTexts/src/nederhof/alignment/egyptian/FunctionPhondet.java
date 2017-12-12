package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Phonetic determinative.
public class FunctionPhondet extends Function {

    // Letters in transliteration (historical sound value).
    private TransLow hist;

    // Letters in transliteration
    private TransLow trans;

    // Is there sound change?
    private boolean soundChange;

    // Create.
    public FunctionPhondet(String[] hiero, TransLow hist, TransLow trans,
		boolean soundChange) {
	super(hiero);
	this.hist = hist;
	this.trans = trans;
	this.soundChange = soundChange;
	setWeight(soundChange ? 1 : 0);
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
	return config.existingSubstring(pos, trans);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	return applicable(config, config.getPos());
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	List<Integer> jumps = new ArrayList<Integer>();
	if (!config.afterJump() && !config.afterEpsPhon())
	    for (int pos = trans.length(); pos <= config.getTrans().length(); pos++) 
		if (pos != config.getPos() && applicable(config, pos))
		    jumps.add(pos - config.getPos());
	return jumps;
    }

    // The configuration resulting from function.
    public SimpleConfig apply(SimpleConfig config) {
	SimpleConfig next = new SimpleConfig(config);
	next.setAfterJump(false);
	next.setAfterEpsPhon(false);
	return next;
    }

    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoPhondet elem = new OrthoPhondet(hist);
	elem.addSigns(config.getHieroPos(), hiLength());
	elem.addLetters(config.getPos()-trans.length(), trans.length());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoPhondet elem = new OrthoPhondet(hist);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "phondet";
    }
    public String toString() {
	return super.toString() + "=" + trans + 
	    (hist.equals(trans) ? "" : "/" + hist);
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + hist.hashCode() + trans.hashCode() + 
	    	(soundChange ? 0 : 1);
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionPhondet other = (FunctionPhondet) obj;
            if (hist.compareTo(other.hist) != 0)
                return hist.compareTo(other.hist);
            else if (trans.compareTo(other.trans) != 0)
                return trans.compareTo(other.trans);
            else
                return (new Boolean(soundChange)).compareTo(other.soundChange);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (super.equals(obj)) {
            FunctionPhondet other = (FunctionPhondet) obj;
            return hist.equals(other.hist) &&
                    trans.equals(other.trans) &&
                    soundChange == other.soundChange;
        } else
            return false;
    }


}
