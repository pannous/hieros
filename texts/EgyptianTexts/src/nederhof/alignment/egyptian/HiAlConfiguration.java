
// Configuration in matching hieroglyphic against transliteration.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.hieroutil.*;
import nederhof.util.*;

public class HiAlConfiguration implements Comparable {

    // Previous configuration.
    private HiAlConfiguration prev;

    // Action from which current configuration was obtained
    // from previous.
    private String action;

    // Penalty of last action.
    private double penalty;

    // Have we decided to truncate remainder of hieroglyphic?
    private boolean truncateHiero = false;

    // State in hieroglyphic.
    private DoubleLinearFiniteAutomatonState hieroState;

    // Configuration points to transition iff
    // word and translitTo are non-null.
    // Configuration points to state iff
    // word and translitTo are null.
    private LinearFiniteAutomatonState translitFrom;
    private LinearFiniteAutomatonState translitTo;

    // Word of transliteration.
    private WordMatch word;

    // Expecting honorific transposition, an occurrence of e.g. nTr was ignored
    // earlier.
    private String transposed;

    // Constructor for initial configuration.
    public HiAlConfiguration(DoubleLinearFiniteAutomatonState hieroState,
	    LinearFiniteAutomatonState transState) {
	this(null, "", 0, false, hieroState, transState, null, null, null);
    }

    // Constructor for subsequent configuration.
    private HiAlConfiguration(HiAlConfiguration prev, String action, double penalty, boolean truncateHiero,
	    DoubleLinearFiniteAutomatonState hieroState,
	    LinearFiniteAutomatonState translitFrom,
	    LinearFiniteAutomatonState translitTo,
	    WordMatch word,
	    String transposed) {
	this.prev = prev;
	this.action = action;
	this.penalty = penalty;
	this.truncateHiero = truncateHiero;
	this.hieroState = hieroState;
	this.translitFrom = translitFrom;
	this.translitTo = translitTo;
	this.word = word;
	this.transposed = transposed;
    }

    public HiAlConfiguration prev() {
	return prev;
    }

    public String action() {
	return action;
    }

    public double penalty() {
	return penalty;
    }

    public DoubleLinearFiniteAutomatonState getHieroState() {
	return hieroState;
    }

    public LinearFiniteAutomatonState getTransState() {
	return translitFrom;
    }

    public WordMatch getWordMatch() {
	return word;
    }

    public String getTransposed() {
	return transposed;
    }

    public boolean isWordStart() {
	return translitTo == null;
    }

    public boolean isFinal() {
	return hieroState.isFinal() &&
	    translitFrom.isFinal() && translitTo == null &&
	    transposed == null;
    }

    // Go to next configuration.
    public LinkedList nextConfigs() {
	LinkedList configs = new LinkedList();
	skipHiero(configs);
	skipHieroSuffix(configs);
	jumpHiero(configs);
	startWord(configs);
	finishWord(configs);
	pushHonorific(configs);
	popHonorific(configs);
	return configs;
    }

    // Ignore next glyph.
    // If it is nTr, then it may be transposed, if nothing else was transposed
    // before.
    private void skipHiero(LinkedList configs) {
	if (!isWordStart()) {
	    TreeMap nexts = hieroState.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator();
		    it.hasNext(); ) {
		String symbol = (String) it.next();
		TreeSet states = (TreeSet) nexts.get(symbol);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    DoubleLinearFiniteAutomatonState next =
			(DoubleLinearFiniteAutomatonState) iter.next();
		    HiAlConfiguration nextConfig =
			new HiAlConfiguration(this, "skip", WordMatch.hieroSkipPenalty, truncateHiero,
				next, translitFrom, translitTo, word, transposed);
		    configs.add(new WeightConfig<HiAlConfiguration>(WordMatch.hieroSkipPenalty, nextConfig));
		}
	    }
	}
    }

    // Ignore all next glyphs.
    private void skipHieroSuffix(LinkedList configs) {
	if (translitFrom.isFinal()) {
	    TreeMap nexts = hieroState.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator();
		    it.hasNext(); ) {
		String symbol = (String) it.next();
		TreeSet states = (TreeSet) nexts.get(symbol);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    DoubleLinearFiniteAutomatonState next =
			(DoubleLinearFiniteAutomatonState) iter.next();
		    double nextPenalty = truncateHiero ? 0 : WordMatch.truncateHieroPenalty;
		    HiAlConfiguration nextConfig =
			new HiAlConfiguration(this, "truncate", nextPenalty, true,
				next, translitFrom, translitTo, word, transposed);
		    configs.add(new WeightConfig<HiAlConfiguration>(nextPenalty, nextConfig));
		}
	    }
	}
    }

    // Use next meaning.
    private void jumpHiero(LinkedList configs) {
	if (!isWordStart()) {
	    TreeMap meanings = hieroState.getInducedOutTransitions();
	    for (Iterator it = meanings.keySet().iterator();
		    it.hasNext(); ) {
		HieroMeaning meaning = (HieroMeaning) it.next();
		TreeSet states = (TreeSet) meanings.get(meaning);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    DoubleLinearFiniteAutomatonState next =
			(DoubleLinearFiniteAutomatonState) iter.next();
		    jumpHiero(configs, meaning, next);
		}
	    }
	}
    }

    // Use next glyph.
    private void jumpHiero(LinkedList configs, HieroMeaning meaning,
	    DoubleLinearFiniteAutomatonState next) {
	String type = meaning.getType();
	String phon = meaning.getPhonetic();
	if (type.equals("det"))
	    jumpDet(configs, next);
	else if (type.equals("phon"))
	    jumpPhon(configs, next, phon);
	else if (type.equals("num"))
	    jumpNum(configs, next, phon);
	else if (type.equals("dualis"))
	    jumpDualis(configs, next);
	else if (type.equals("pluralis"))
	    jumpPluralis(configs, next);
	else 
	    System.err.println("Unknown type: " + type);
    }

    // Assume next symbol is determinative.
    private void jumpDet(LinkedList configs, DoubleLinearFiniteAutomatonState next) {
	double penalty = word.detPenalty();
	WordMatch nextWord = word.getDetSeen();
	HiAlConfiguration nextConfig = 
	    new HiAlConfiguration(this, "det", penalty, truncateHiero,
		    next, translitFrom, translitTo, nextWord, transposed);
	configs.add(new WeightConfig<HiAlConfiguration>(penalty, nextConfig));
    }

    // Assume next symbol is phoneme. Look at all possible positions in word.
    private void jumpPhon(LinkedList configs, DoubleLinearFiniteAutomatonState next,
	    String phon) {
	LinkedList poss = word.phonPositions(phon);
	for (ListIterator it = poss.listIterator(); it.hasNext(); ) {
	    Integer posInt = (Integer) it.next();
	    int pos = posInt.intValue();
	    double penalty = word.phonPenalty(phon, pos);
	    WordMatch nextWord = word.getPhonSeen(phon, pos);
	    HiAlConfiguration nextConfig =
		new HiAlConfiguration(this, "'" + phon + "'", penalty, truncateHiero,
			next, translitFrom, translitTo, nextWord, transposed);
	    configs.add(new WeightConfig<HiAlConfiguration>(penalty, nextConfig));
	}
    }

    // Assume next word is number.
    private void jumpNum(LinkedList configs, DoubleLinearFiniteAutomatonState next,
	    String phon) {
	double penalty = word.numPenalty(phon);
	WordMatch nextWord = word.getNumSeen(phon);
	HiAlConfiguration nextConfig =
	    new HiAlConfiguration(this, "'" + phon + "'", penalty, truncateHiero,
		    next, translitFrom, translitTo, nextWord, transposed);
	configs.add(new WeightConfig<HiAlConfiguration>(penalty, nextConfig));
    }

    // Assume next symbol is dual marker.
    private void jumpDualis(LinkedList configs, DoubleLinearFiniteAutomatonState next) {
	jumpPhon(configs, next, "j" + HieroMeaning.endMarker); 
	jumpPhon(configs, next, "j" + "="); 
	jumpPhon(configs, next, "j" + "-");  
	jumpPhon(configs, next, "wj" + HieroMeaning.endMarker); 
	jumpPhon(configs, next, "wj" + "=");  
	jumpPhon(configs, next, "wj" + "-"); 
    }

    // Assume next symbol is plural marker.
    private void jumpPluralis(LinkedList configs, DoubleLinearFiniteAutomatonState next) {
	jumpPhon(configs, next, "w" + HieroMeaning.endMarker); 
	jumpPhon(configs, next, "w" + "="); 
	jumpPhon(configs, next, "wt" + HieroMeaning.endMarker); 
	jumpPhon(configs, next, "wt" + "-"); 
    }

    // Start word.
    private void startWord(LinkedList configs) {
	if (isWordStart()) {
	    TreeMap nexts = translitFrom.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator();
		    it.hasNext(); ) {
		String fullWord = (String) it.next();
		WordMatch word = new WordMatch(fullWord);
		TreeSet states = (TreeSet) nexts.get(fullWord);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    LinearFiniteAutomatonState next =
			(LinearFiniteAutomatonState) iter.next();
		    HiAlConfiguration nextConfig =
			new HiAlConfiguration(this, "start", 0, truncateHiero,
				hieroState, translitFrom, next, word, transposed);
		    configs.add(new WeightConfig<HiAlConfiguration>(0, nextConfig));
		}
	    }
	}
    }

    // Let word be at an end.
    private void finishWord(LinkedList configs) {
	if (!isWordStart()) {
	    double penalty = word.endPenalty();
	    HiAlConfiguration nextConfig = new HiAlConfiguration(this, "end", penalty, truncateHiero,
		    hieroState, translitTo, null, null, transposed);
	    configs.add(new WeightConfig<HiAlConfiguration>(penalty, nextConfig));
	}
    }

    // Take next symbol and keep it, assuming honorific transposition.
    private void pushHonorific(LinkedList configs) {
	if (transposed == null && translitTo == null) {
	    TreeMap nexts = hieroState.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator(); it.hasNext(); ) {
		String symbol = (String) it.next();
		String transposedValue = transposedValue(symbol);
		if (transposedValue != null) {
		    TreeSet states = (TreeSet) nexts.get(symbol);
		    for (Iterator iter = states.iterator(); iter.hasNext(); ) {
			DoubleLinearFiniteAutomatonState next =
			    (DoubleLinearFiniteAutomatonState) iter.next();
			HiAlConfiguration nextConfig = new HiAlConfiguration(this, "push", 0, truncateHiero,
				    next, translitFrom, translitTo, word, transposedValue);
			configs.add(new WeightConfig<HiAlConfiguration>(0, nextConfig));
		    }
		}
	    }
	}
    }

    // Sign may be word by itself that can be transposed.
    private static String transposedValue(String sign) {
	if (sign.equals("R8"))
	    return "nTr";
	else if (sign.equals("N5"))
	    return "ra";
	else if (sign.equals("M23"))
	    return "nsw";
	else
	    return null;
    }

    // Given earlier pushed value, try to match here.
    private void popHonorific(LinkedList configs) {
	if (transposed != null && translitTo == null) {
	    TreeMap nexts = translitFrom.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator();
		    it.hasNext(); ) {
		String fullWord = (String) it.next();
		if (transposed.equals(fullWord)) {
		    TreeSet states = (TreeSet) nexts.get(fullWord);
		    for (Iterator iter = states.iterator();
			    iter.hasNext(); ) {
			LinearFiniteAutomatonState next =
			    (LinearFiniteAutomatonState) iter.next();
			HiAlConfiguration nextConfig =
			    new HiAlConfiguration(this, "pop '" + transposed + "'", 0, truncateHiero,
				hieroState, next, null, null, null);
			configs.add(new WeightConfig<HiAlConfiguration>(0, nextConfig));
		    }
		}
	    }
	}
    }

    public boolean equals(Object o) {
	if (o instanceof HiAlConfiguration) {
	    HiAlConfiguration other = (HiAlConfiguration) o;
	    return compareTo(other) == 0;
	} else
	    return false;
    }

    // Compare to other configuration.
    public int compareTo(Object o) {
	if (o instanceof HiAlConfiguration) {
	    HiAlConfiguration other = (HiAlConfiguration) o;
	    if (compareTo(this.translitFrom, other.translitFrom) != 0)
		return compareTo(this.translitFrom, other.translitFrom);
	    else if (compareTo(this.translitTo, other.translitTo) != 0)
		return compareTo(this.translitTo, other.translitTo);
	    else if (compareTo(this.word, other.word) != 0)
		return compareTo(this.word, other.word);
	    else if (compareTo(this.transposed, other.transposed) != 0)
		return compareTo(this.transposed, other.transposed);
	    else if (compareTo(this.truncateHiero, other.truncateHiero) != 0)
		return compareTo(this.truncateHiero, other.truncateHiero);
	    else
		return compareTo(this.hieroState, other.hieroState);
	} else
	    return 1;
    }

    // Compare two objects that may be null. 
    // Null precedes non-null.
    private int compareTo(Comparable o1, Comparable o2) {
	if (o1 == null && o2 == null) 
	    return 0;
	else if (o1 == null)
	    return -1;
	else if (o2 == null)
	    return 1;
	else
	    return o1.compareTo(o2);
    }

    // Compare booleans.
    private int compareTo(boolean b1, boolean b2) {
	if (b1 == b2)
	    return 0;
	else if (!b1)
	    return -1;
	else
	    return 1;
    }

    // For testing.
    public String toString() {
	return "hiero=" + hieroState + " trans=" + 
	    translitFrom + "/" + translitTo + " " + word +
	    (transposed == null ? "" : " " + "(" + transposed + ")");
    }

    // For tracing.
    public String toHtmlString() {
	return "" + hieroState + " " + translitFrom + "/" + translitTo + 
	    (word == null ? "" : " " + word.toHtmlString()) + 
	    (transposed == null ? "" : " " + " <font color=\"green\">(" + transposed + ")</font>");
    }

}
