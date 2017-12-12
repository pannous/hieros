/***************************************************************************/
/*                                                                         */
/*  WordMatch.java                                                         */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Stage at recognizing word, after reading a number of hieroglyphs.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.hieroutil.*;

public class WordMatch implements Comparable {

    // Penalty of glyph unmatched.
    public static final double hieroSkipPenalty = 20;
    // Penalty of truncating suffix of hieroglyphic.
    public static final int truncateHieroPenalty = 100;
    // Penalty of not seeing expected substring at position.
    private static final double nonOccurPenalty = 20;
    // Penalty of seeing determinative before any phonemes have been seen.
    private static final double initialDetPenalty = 6;
    // Penalty of seeing phoneme after determinative.
    private static final double phonemeAfterDetPenalty = 8;
    // Penalty for seeing letter to the right of unseen letters.
    private static final double jumpLetterPenalty = 4;
    // Penalty for seeing weak letter to the right of unseen letters.
    private static final double jumpWeakLetterPenalty = 3;
    // Penalty of seeing number but wrong number.
    private static final double wrongNumberPenalty = 10;
    // Penalty of seeing no number where number expected.
    private static final double noNumberPenalty = 20;
    // Penalty of not matching weak phoneme.
    private static final double unmatchedWeakPhonPenalty = 2;
    // Penalty of not matching other phoneme to letter.
    private static final double unmatchedPhonPenalty = 5;
    // Penalty of removing weak letter from phoneme.
    private static final double weakPrunePenalty = 1;
    // Penalty of replacing t by T or d by D.
    private static final double similarPenalty = 2;

    // Letters without brackets.
    private String letters;

    // Letters already matched to phonograms.
    private boolean[] matchedLetters;

    // If one determinative already processed,
    // what was position beyond last matched letter? (1 if none.)
    private int detSeenAt = 0;

    // Strings between round brackets are removed.
    // Square brackets are removed.
    // Replace 'i' by 'j', 'K' by 'q', 'z' by 's'.
    public WordMatch(String word) {
	letters = word;
	letters = letters.replaceAll("\\([^()]\\)", "");
	letters = letters.replaceAll("\\[", "");
	letters = letters.replaceAll("\\]", "");
	letters = letters.replaceAll("i", "j");
	letters = letters.replaceAll("K", "q");
	letters = letters.replaceAll("z", "s");
	letters = letters.replaceAll("^", HieroMeaning.beginMarker);
	letters = letters.replaceAll("$", HieroMeaning.endMarker);
	matchedLetters = new boolean[letters.length()];
	for (int i = 1; i < matchedLetters.length - 1; i++)
	    matchedLetters[i] = false;
    }

    // Create when all member values are known.
    public WordMatch(String letters, boolean[] matchedLetters, int detSeenAt) {
	this.letters = letters;
	this.matchedLetters = (boolean[]) matchedLetters.clone();
	this.detSeenAt = detSeenAt;
    }

    // Copy of this class.
    private WordMatch copyOfThis() {
	return new WordMatch(letters, matchedLetters, detSeenAt);
    }

    // Phonetic symbols.

    // Return position(s) where substring matches.
    // If sub ends on weak character, omit last character.
    public LinkedList phonPositions(String sub) {
	sub = normaliseSimilar(pruneWeak(sub));
	String lettersNormal = normaliseSimilar(letters);
	LinkedList startPoss = new LinkedList();
	int pos = 0;
	while (true) {
	    pos = lettersNormal.indexOf(sub, pos);
	    if (pos < 0) 
		break;
	    startPoss.add(new Integer(pos));
	    pos++;
	}
	return startPoss;
    }

    // Penalty of seeing phoneme at position. Extra penalty if determinatives
    // were seen.
    public double phonPenalty(String sub, int pos) {
	boolean match = letters.startsWith(sub, pos);
	double transformPenalty = 0;
	if (!match) {
	    sub = pruneWeak(sub);
	    match = letters.startsWith(sub, pos);
	    transformPenalty += weakPrunePenalty;
	}
	if (!match) {
	    sub = normaliseSimilar(sub);
	    String lettersNormal = normaliseSimilar(letters);
	    match = lettersNormal.startsWith(sub, pos);
	    transformPenalty += similarPenalty;
	}
	if (match) {
	    double prefixPenalty = 0;
	    for (int i = 1; i < pos; i++) {
		char c = letters.charAt(i);
		if (isPhon(c) && !matchedLetters[i]) {
		    if (isWeak(c))
			prefixPenalty += jumpLetterPenalty;
		    else
			prefixPenalty += jumpWeakLetterPenalty;
		}
	    }
	    double detPenalty = (pos < detSeenAt) ? phonemeAfterDetPenalty : 0;
	    return prefixPenalty + detPenalty + transformPenalty;
	} else 
	    return nonOccurPenalty;
    }

    // Note that substring was seen.
    private void makePhonSeen(String sub, int pos) {
	for (int i = pos; i < Math.min(pos + sub.length(), matchedLetters.length); i++)
	    matchedLetters[i] = true;
    }
    
    // Give modified word with substring seen.
    public WordMatch getPhonSeen(String sub, int pos) {
	WordMatch other = copyOfThis();
	other.makePhonSeen(sub, pos);
	return other;
    }

    // Numbers.

    // Penalty of seeing number at position. 
    public double numPenalty(String sub) {
	if (letters.equals(HieroMeaning.beginMarker + sub + HieroMeaning.endMarker))
	    return 0;
	else if (letters.matches(HieroMeaning.beginMarker + "[0-9]+" + HieroMeaning.endMarker))
	    return wrongNumberPenalty;
	else
	    return noNumberPenalty;
    }

    // Note that number was seen.
    private void makeNumSeen() {
	for (int i = 1; i < matchedLetters.length - 1; i++)
	    matchedLetters[i] = true;
    }

    // Give modified word with number seen.
    public WordMatch getNumSeen(String sub) {
	WordMatch other = copyOfThis();
	other.makeNumSeen();
	return other;
    }

    // Determinatives.

    // Penalty of seeing determinative.
    public double detPenalty() {
	if (beyondPhonemes() == 0)
	    return initialDetPenalty;
	else
	    return 0;
    }

    // Note that determinative was seen.
    private void makeDetSeen() {
	detSeenAt = beyondPhonemes();
    }

    // Give modified word with determinative seen.
    public WordMatch getDetSeen() {
	WordMatch other = copyOfThis();
	other.makeDetSeen();
	return other;
    }

    // Penalty of ending word here. Depends on letters not
    // yet matched by phonemes.
    public double endPenalty() {
	double penalty = 0;
	for (int i = 1; i < matchedLetters.length - 1; i++) {
	    char c = letters.charAt(i);
	    if (isPhon(c) && !matchedLetters[i]) {
		if (isWeak(letters.charAt(i)))
		    penalty += unmatchedWeakPhonPenalty;
		else
		    penalty += unmatchedPhonPenalty;
	    }
	}
	return penalty;
    }

    // Position beyond last phoneme.
    private int beyondPhonemes() {
	for (int i = letters.length() - 2; i >= 1; i--)
	    if (matchedLetters[i])
		return i+1;
	return 0;
    }

    // Prune weak consonants at end.
    private static String pruneWeak(String s) {
	while (s.length() > 1 && isWeak(s.charAt(s.length()-1))) 
	    s = s.substring(0, s.length()-1);
	return s;
    }

    // Is w, j, y.
    private static boolean isWeak(char c) {
	return c == 'w' || c == 'j' || c == 'y';
    }

    // Is phonemic, i.e. not punctuation.
    private static boolean isPhon(char c) {
	return ('a' <= c && c <= 'z') || 
	    ('A' <= c && c <= 'Z') ||
	    ('0' <= c && c <= '9');
    }

    // Turn D into d and T into t.
    private static String normaliseSimilar(String s) {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < s.length(); i++) 
	    buf.append(normaliseSimilar(s.charAt(i)));
	return buf.toString();
    }

    // Turn D into d and T into t.
    private static char normaliseSimilar(char c) {
	if (c == 'T')
	    return 't';
	else if (c == 'D')
	    return 'd';
	else
	    return c;
    }

    public int compareTo(Object o) {
	if (o instanceof WordMatch) {
	    WordMatch other = (WordMatch) o;
	    if (this.letters.compareTo(other.letters) != 0)
		return this.letters.compareTo(other.letters);
	    else if (compareTo(this.matchedLetters, other.matchedLetters) != 0)
		return compareTo(this.matchedLetters, other.matchedLetters);
	    else if (compareTo(this.detSeenAt, other.detSeenAt) != 0)
		return compareTo(this.detSeenAt, other.detSeenAt);
	    else
		return 0;
	} else
	    return 1;
    }

    // Compare two boolean arrays. First on length,
    // then lexicographically.
    private int compareTo(boolean[] a1, boolean[] a2) {
	if (a1.length < a2.length)
	    return -1;
	else if (a1.length > a2.length)
	    return 1;
	else 
	    for (int i = 0; i < a1.length; i++) {
		if (compareTo(a1[i], a2[i]) != 0)
		    return compareTo(a1[i], a2[i]);
	    }
	return 0;
    }

    // Compare two booleans.
    private int compareTo(boolean b1, boolean b2) {
	if (!b1 && b2)
	    return -1;
	else if (b1 && !b2)
	    return 1;
	else
	    return 0;
    }

    // Compare two numbers.
    private int compareTo(int i1, int i2) {
	if (i1 < i2)
	    return -1;
	else if (i1 > i2)
	    return 1;
	else
	    return 0;
    }

    // For testing.
    public String toString() {
	String matched = " ";
	for (int i = 1; i < matchedLetters.length - 1; i++)
	    matched += matchedLetters[i] ? "X" : "-";
	return "letters=" + letters + matched + " detSeenAt=" + detSeenAt;
    }

    // HTML representation of word.
    // Matched letters are bold. 
    // Determinative position indicated by blue exclamation mark.
    public String toHtmlString() {
	StringBuffer str = new StringBuffer();
	str.append("<html>");
	for (int i = 1; i < matchedLetters.length - 1; i++) {
	    if (detSeenAt == i)
		str.append("<font color=\"red\">!</font>");
	    char c = letters.charAt(i);
	    if (matchedLetters[i])
		str.append("<font color=\"blue\">" + c + "</font>");
	    else
		str.append(c);
	}
	if (detSeenAt == matchedLetters.length - 1)
	    str.append("<font color=\"red\">!</font>");
	str.append("</html>");
	return str.toString();
    }

}
