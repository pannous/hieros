package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// Phonogram.
public class FunctionPhon extends Function {

    // Letters in transliteration (historical sound value).
    private TransLow hist;

    // Letters in transliteration.
    private TransLow trans;

    // Is there sound change?
    private boolean soundChange;

    // Feminine plural included in trans. To be resolved later.
    private boolean femPlur;

    // Create.
    public FunctionPhon(String[] hiero, TransLow hist, TransLow trans, 
	    boolean soundChange, boolean femPlur) {
	super(hiero);
	this.hist = hist;
	this.trans = trans;
	this.soundChange = soundChange;
	this.femPlur = femPlur;
    }

    public TransLow getTrans() {
	return trans;
    }

    // Change function for feminine plural 'w'.
    public void addFemPlur() {
	trans = TransLow.concat(new TransLow("w"), trans);
	femPlur = true;
    }

    // Can we apply function in configuration if it were at position?
    private boolean applicable(SimpleConfig config, int pos) {
	return config.validSubstring(pos, trans);
    }

    // Can we apply function in configuration?
    public boolean applicable(SimpleConfig config) {
	return applicable(config, config.getPos());
    }

    // Which jumps are needed to apply the function in the configuration?
    public List<Integer> jumpApplicable(SimpleConfig config) {
	List<Integer> jumps = new ArrayList<Integer>();
	if (!config.afterJump() && !config.afterEpsPhon())
	    for (int pos = 0; pos <= config.getTrans().length(); pos++) 
		if (pos != config.getPos() && applicable(config, pos))
		    jumps.add(pos - config.getPos());
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

    // Special treatment for artificial plural 'w' in front of 't',
    // making sure the position of 'w' is omitted.
    public OrthoElem orthoElem(ComplexConfig config) {
	OrthoPhon elem = new OrthoPhon(hist);
	elem.addSigns(config.getHieroPos(), hiLength());
	if (femPlur && trans.length() == 2)
	    elem.addLetters(config.getPos()+1, 1);
	else
	    elem.addLetters(config.getPos(), trans.length());
	return elem;
    }
    public OrthoElem orthoElem(int posFrom, int len) {
	OrthoPhon elem = new OrthoPhon(hist);
	elem.addSigns(posFrom, len);
	return elem;
    }

    public String name() {
	return "phon";
    }
    public String toString() {
	return super.toString() + "=" + trans + 
	    (hist.equals(trans) ? "" : "/" + hist);
    }

    public int hashCode() {
	final int prime = 31;
	return prime * super.hashCode() + hist.hashCode() + trans.hashCode() +
	    (soundChange ? 0 : 1) + (femPlur ? 0 : 2);
    }

    public int compareTo(Function obj) {
        if (super.compareTo(obj) != 0)
            return super.compareTo(obj);
        else {
            FunctionPhon other = (FunctionPhon) obj;
            if (hist.compareTo(other.hist) != 0)
                return hist.compareTo(other.hist);
            else if (trans.compareTo(other.trans) != 0)
                return trans.compareTo(other.trans);
            else if ((new Boolean(soundChange)).compareTo(other.soundChange) != 0)
                return (new Boolean(soundChange)).compareTo(other.soundChange);
	    else
		return (new Boolean(femPlur)).compareTo(other.femPlur);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (super.equals(obj)) {
            FunctionPhon other = (FunctionPhon) obj;
            return hist.equals(other.hist) &&
                    trans.equals(other.trans) &&
                    soundChange == other.soundChange &&
		    femPlur == other.femPlur;
        } else
            return false;
    }

}
