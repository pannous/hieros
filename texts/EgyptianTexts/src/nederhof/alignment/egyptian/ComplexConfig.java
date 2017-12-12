package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.interlinear.egyptian.ortho.*;
import nederhof.util.*;
import nederhof.util.ngram.*;

// Simple configuration, plus position in hieroglyphic, plus history.
public class ComplexConfig extends SimpleConfig {

    // Hieroglyphic.
    protected String[] hiero;

    // Position in hieroglyphic.
    protected int hieroPos;

    // Last functions (for N-gram models).
    protected Function[] history;

    // Initial configuration.
    public ComplexConfig(String[] hiero, int historySize) {
	this.hiero = new String[hiero.length];
	System.arraycopy(hiero, 0, this.hiero, 0, hiero.length);
	history = new Function[historySize];
	for (int i = 0; i < historySize; i++)
	    history[i] = new FunctionStart();
    }
    // Dummy configuration, including no hieroglyphic.
    public ComplexConfig() {
	this(new String[0], 0);
    }

    // Constructor of configuration, with hieroglyphic and position.
    public ComplexConfig(SimpleConfig config, String[] hiero, int hieroPos, 
		Function[] history) {
	super(config);
	this.hiero = new String[hiero.length];
	System.arraycopy(hiero, 0, this.hiero, 0, hiero.length);
	this.hieroPos = hieroPos;
	this.history = history;
    }

    // Get hieroglyphic.
    public String[] getHiero() {
	return hiero;
    }

    // Get next n signs after current position.
    public String[] getNextHiero(int n) {
	String[] nexts = new String[n];
	for (int i = 0; i < n; i++)
	    nexts[i] = hiero[hieroPos + i];
	return nexts;
    }

    // Get position in hieroglyphic.
    public int getHieroPos() {
	return hieroPos;
    }

    // Get total number of hieroglyphs.
    public int getHieroLength() {
	return hiero.length;
    }

    // Get sign in hieroglyphic at position.
    public String getSign(int p) {
	return hiero[p];
    }

    // Get history.
    public Function[] getHistory() {
	return history;
    }

    // Are X signs repeated Y times?
    public boolean signsRepeated(int nSigns, int nRepeats) {
	if (nSigns > hieroPos || nSigns * nRepeats > hiero.length - hieroPos)
	    return false;
	for (int i = 0; i < nSigns; i++) 
	    for (int j = 0; j < nRepeats; j++)
		if (!hiero[hieroPos-nSigns+i].equals(hiero[hieroPos+nSigns*j+i]))
		    return false;
	return true;
    }

    public boolean canFinish() {
	return super.canFinish() && hieroPos == hiero.length;
    }

    // Translate list of functions to orthographic elements.
    public static Vector<OrthoElem> toOrthoElems(List<Function> functions) {
	Vector<OrthoElem> elems = new Vector<OrthoElem>();
	String[] dummyHiero = new String[0];
	ComplexConfig state = new ComplexConfig(dummyHiero, 0);
	for (Function function : functions) {
	    int hieroPos = state.getHieroPos();
	    SimpleConfig config = function.apply(state);
	    OrthoElem elem = function.orthoElem(state);
	    if (elem != null)
		elems.add(elem);
	    hieroPos += function.hiLength();
	    Function[] newHistory = updateHistory(state.getHistory(), function);
	    state = new ComplexConfig(config, dummyHiero, hieroPos, newHistory);
	}
	return elems;
    }

    // Add new function to history. Forget oldest history.
    public static Function[] updateHistory(Function[] oldHist, Function newFun) {
	Function[] newHist = new Function[oldHist.length];
	System.arraycopy(oldHist, 0, newHist, 0, oldHist.length);
	for (int i = 0; i < oldHist.length - 1; i++) 
	    newHist[i] = oldHist[i+1];
	if (oldHist.length > 0)
	    newHist[oldHist.length-1] = newFun;
	return newHist;
    }

    // As above, but take most recent history of given length, not counting jumps.
    public static Function[] updateHistoryNoJump(Function[] oldHist, List<Function> newFuns, 
	    		int size) {
	Vector<Function> allHist = new Vector<Function>();
	for (int i = 0; i < oldHist.length; i++)
	    allHist.add(oldHist[i]);
	allHist.addAll(newFuns);
	int nonJumps = 0;
	Vector<Function> newHist = new Vector<Function>();
	for (int i = allHist.size()-1; i >= 0; i--) {
	    Function fun = allHist.get(i);
	    if (fun instanceof FunctionJump)
		newHist.add(fun);
	    else if (nonJumps >= size)
		break;
	    else {
		newHist.add(fun);
		nonJumps++;
	    }
	}
	Function[] hist = new Function[newHist.size()];
	for (int i = 0; i < newHist.size(); i++)
	    hist[i] = newHist.get(newHist.size()-i-1);
	return hist;
    }

    public String toString() {
	String hist = "";
	for (int i = 0; i < history.length; i++)
	    hist += " " + history[i];
	return "" + hieroPos + " " + super.toString() + hist;
    }

    public int compareTo(SimpleConfig other) {
	if (other instanceof ComplexConfig) {
	    ComplexConfig otherHi = (ComplexConfig) other;
	    if (compare(hieroPos, otherHi.hieroPos) != 0)
		return compare(hieroPos, otherHi.hieroPos);
	    else if (compare(history, otherHi.history) != 0)
		return compare(history, otherHi.history);
	    else
		return super.compareTo(other);
	} else
	    return -1;
    }

    // Compare two boolean arrays. First on length,
    // then lexicographically.
    protected int compare(Function[] history1, Function[] history2) {
        if (history1.length < history2.length)
            return -1;
        else if (history1.length > history2.length)
            return 1;
        else
            for (int i = 0; i < history1.length; i++) {
                if (history1[i].compareTo(history2[i]) != 0)
                    return history1[i].compareTo(history2[i]);
            }
        return 0;
    }

    // Equality.
    public boolean equals(Object o) {
        if (!(o instanceof ComplexConfig))
            return false;
        else {
            ComplexConfig other = (ComplexConfig) o;
            return compareTo(other) == 0;
        }   
    }

}
