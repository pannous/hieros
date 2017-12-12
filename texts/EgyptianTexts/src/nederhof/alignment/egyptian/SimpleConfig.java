// Simple configuration.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;

public class SimpleConfig implements Comparable<SimpleConfig> {

    // Position in transliteration.
    private int pos = 0;

    // Letters of transliteration that have been determined.
    private TransLow trans = new TransLow("");

    // Length of word. 0 if no length specified.
    private int length = 0;

    // Which word is to be achieved? Empty if none.
    private TransLow target = new TransLow("");

    // The current content of trans marks the end of a word.
    private boolean wordEnd = false;

    // Was feminine plural predicted by 'w' but not yet confirmed
    // by plural strokes?
    private boolean femPlur = false;

    // Was previous function a jump?
    private boolean afterJump = false;

    // Was previous function a epsilon phonogram (not corresponding to any sign)?
    private boolean afterEpsPhon = false;

    // Default constructor.
    public SimpleConfig() {
    }

    // Explicit constructor.
    public SimpleConfig(int pos, 
	    TransLow trans, 
	    int length, 
	    TransLow target, 
	    boolean wordEnd,
	    boolean femPlur,
	    boolean afterJump,
	    boolean afterEpsPhon) {
	this.pos = pos;
	this.trans = trans;
	this.length = length;
	this.target = target;
	this.wordEnd = wordEnd;
	this.femPlur = femPlur;
	this.afterJump = afterJump;
	this.afterEpsPhon = afterEpsPhon;
    }

    // Copy constructor.
    public SimpleConfig(SimpleConfig old) {
	this(old.pos, 
		old.trans, 
		old.length, 
		old.target, 
		old.wordEnd,
		old.femPlur,
		old.afterJump,
		old.afterEpsPhon);
    }

    // Position.
    public int getPos() {
	return pos;
    }
    public void setPos(int pos) {
	this.pos = pos;
    }
    public void incrPos(int dist) {
	this.pos += dist;
    }

    // Transliteration.
    public TransLow getTrans() {
	return trans;
    }
    public void setTrans(TransLow trans) {
	this.trans = trans;
	wordEnd = false;
    }

    // Length.
    public int getLength() {
	return length;
    }
    public boolean hasLength() {
	return length > 0;
    }
    public void setLength(int length) {
	this.length = length;
    }

    // Set target.
    public TransLow getTarget() {
	return target;
    }
    public boolean hasTarget() {
	return target.length() > 0;
    }
    public void setTarget(TransLow target) {
	this.target = target;
	this.length = target.length();
    }

    // End of word.
    public boolean wordEnd() {
	return wordEnd;
    }
    public void setWordEnd(boolean b) {
	wordEnd = b;
    }

    // Feminine plural.
    public boolean femPlur() {
	return femPlur;
    }
    public void setFemPlur(boolean b) {
	femPlur = b;
    }

    // Jump.
    public boolean afterJump() {
	return afterJump;
    }
    public void setAfterJump(boolean b) {
	afterJump = b;
    }

    // EpsPhon.
    public boolean afterEpsPhon() {
	return afterEpsPhon;
    }
    public void setAfterEpsPhon(boolean b) {
	afterEpsPhon = b;
    }

    // Can we put this substring s at position p?
    public boolean validSubstring(int p, TransLow s) {
	if (hasLength() && p + s.length() > length)
	    return false;
        for (int i = 0; i < s.length(); i++)
            if (p + i >= trans.length()) {
		if (wordEnd && !s.substring(0,1).isWordSep())
		    return false;
		else if (!hasTarget())
		    return true;
		else if (s.charAt(i) != target.charAt(p + i))
		    return false;
            } else if (s.charAt(i) != trans.charAt(p + i))
                return false;
        return true;
    }

    // Can we put this suffix at position p?
    public boolean validSuffix(int p, TransLow s) {
	if (hasLength() && p + s.length() != length ||
		p + s.length() < trans.length())
	    return false;
        for (int i = 0; i < s.length(); i++)
            if (p + i >= trans.length()) {
		if (!hasTarget())
		    return true;
		else if (s.charAt(i) != target.charAt(p + i))
		    return false;
            } else if (s.charAt(i) != trans.charAt(p + i))
                return false;
        return true;
    }

    // Is it already substring?
    public boolean existingSubstring(int p, TransLow s) {
	if (s.length() > p)
	    return false;
	for (int i = 0; i < s.length(); i++)
	    if (s.charAt(i) != trans.charAt(p - s.length() + i))
		return false;
	return true;
    }

    // Can we finish word here?
    // This might still need final jump if pos < trans.length().
    public boolean canFinish() {
	return 
	    (!hasLength() || length == trans.length()) &&
	    (!hasTarget() || target.equals(trans)) &&
	    !femPlur &&
	    !afterJump;
    }

    // Next letter(s) that can be appended at the end.
    public List<TransLow> nextPhon() {
	List<TransLow> nexts = new ArrayList<TransLow>();
	if (!hasLength() || trans.length() < length) {
	    if (hasTarget()) {
		nexts.add(target.substring(trans.length(), trans.length()+1));
	    } else {
		// TODO
	    }
	}
	return nexts;
    }

    // Where (sub)word can start. This is of course the beginning of the
    // transliteration, but also may be the point just following a hyphen or
    // space.
    public List<Integer> starts() {
	List<Integer> starts = new ArrayList<Integer>();
	starts.add(0);
	int fromIndex = 1;
	while (fromIndex < trans.length()) {
	    int next = Integer.MAX_VALUE;
	    for (int i = 0; i < TransLow.WORD_SEPS.length; i++) {
		int nextSep = trans.indexOf(TransLow.WORD_SEPS[i], fromIndex);
		if (nextSep >= 0)
		    next = Math.min(next, nextSep);
	    }
	    if (next == Integer.MAX_VALUE)
		break;
	    starts.add(next+1);
	    fromIndex = next + 1;
	}
	return starts;
    }

    // For debugging.
    public String toString() {
	return "(" + pos + (hasLength() ? ("<=" + length) : "") + "," + 
	    trans + 
	    (wordEnd ? ",wordEnd" : "") +
	    (femPlur ? ",femPlur" : "") +
	    (afterJump ? ",afterJump" : "") +
	    (afterEpsPhon ? ",afterEpsPhon" : "") +
	    ")";
    }

    // For comparing.
    public int compareTo(SimpleConfig other) {
	if (compare(pos, other.pos) != 0)
	    return compare(pos, other.pos);
	else if (trans.compareTo(other.trans) != 0)
	    return trans.compareTo(other.trans);
	else if (compare(length, other.length) != 0)
	    return compare(length, other.length);
	else if (target.compareTo(other.target) != 0)
	    return target.compareTo(other.target);
	else if (compare(wordEnd, other.wordEnd) != 0)
	    return compare(wordEnd, other.wordEnd);
	else if (compare(femPlur, other.femPlur) != 0)
	    return compare(femPlur, other.femPlur);
	else if (compare(afterJump, other.afterJump) != 0)
	    return compare(afterJump, other.afterJump);
	else 
	    return compare(afterEpsPhon, other.afterEpsPhon);
    }
    protected static int compare(int a, int b) {
	if (a < b) 
	    return -1;
	else if (a > b)
	    return 1;
	else
	    return 0;
    }
    protected static int compare(boolean a, boolean b) {
	int aInt = a ? 1 : 0;
	int bInt = b ? 1 : 0;
	return compare(aInt, bInt);
    }

    // Equality.
    public boolean equals(Object o) {
	if (!(o instanceof SimpleConfig))
	    return false;
	else {
	    SimpleConfig other = (SimpleConfig) o;
	    return compareTo(other) == 0;
	}
    }

    // Testing.
    public static void main(String[] args) {
	SimpleConfig config = new SimpleConfig();
	Function f1 = new FunctionPhon(new String[]{"A1"}, new TransLow("a"), new TransLow("a"), false, false);
	Function f2 = new FunctionPhon(new String[]{"C1"}, new TransLow("A"), new TransLow("A"), false, false);
	Function f3 = new FunctionPhon(new String[]{"D1"}, new TransLow("q"), new TransLow("q"), false, false);
	Function f4 = new FunctionJump(-4);
	Function f5 = new FunctionLog(new String[]{"A1", "B1"}, new TransMdc("aAq"), new TransLow("aAq"), false);
	Function f6 = new FunctionEpsPhon(new TransLow("k"));
	Function f7 = new FunctionJump(1);
	System.out.println(config);
	System.out.println(f1.applicable(config));
	config = f1.apply(config);
	System.out.println(config);
	System.out.println(f2.applicable(config));
	config = f2.apply(config);
	System.out.println(config);
	System.out.println(f3.applicable(config));
	config = f3.apply(config);
	System.out.println(config);
	System.out.println(f6.applicable(config));
	config = f6.apply(config);
	System.out.println(config);
	System.out.println(f4.applicable(config));
	config = f4.apply(config);
	System.out.println(config);
	System.out.println(f5.applicable(config));
	config = f5.apply(config);
	System.out.println(config);
	System.out.println(f7.applicable(config));
	config = f7.apply(config);
	System.out.println(config);
	System.out.println(config.canFinish());
    }

}
