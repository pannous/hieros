/***************************************************************************/
/*                                                                         */
/*  SignMeanings.java                                                      */
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

// Reading file with sign meanings. Two different formats are considered
// here.

package nederhof.hieroutil;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import nederhof.fonts.*;
import nederhof.util.*;

public class SignMeanings {

    // Assuming the empty string has a meaning, a set of meanings.
    private TreeSet meanings = new TreeSet();

    // Mapping from sign to meanings, for suffixes of sequences,
    // starting with the sign.
    private TreeMap meaningMap = new TreeMap();

    // Mapping from numbers to meanings.
    private NumberMeanings numberMeanings = new NumberMeanings();

    // Read file of sign meanings.
    public SignMeanings(String meaningsFile) {
	try {
	    processMeaningsFile(meaningsFile);
	} catch (IOException e) {
	    System.err.println("Cannot interpret " + meaningsFile);
	}
    }

    // Create meanings for suffix of sequences of signs.
    private SignMeanings() {
    }

    // Read file of sign meanings, and store them.
    // Assume file is relative to directory with fonts.
    private void processMeaningsFile(String meaningsFile) throws IOException {
	URL url = null;
	InputStream in = null;
	try {
	    url = FileAux.fromBase(meaningsFile);
	    if (url == null)
		throw new MalformedURLException();
	    in = url.openStream();
	} catch (MalformedURLException e) {
	    System.err.println("File not found " + meaningsFile);
	    System.exit(-1);
	}
	BufferedReader reader =
	    new BufferedReader(new InputStreamReader(in));
	if (meaningsFile.equals("data/fonts/hanniglautwerte.txt"))
	    processMeaningsFileBody1(reader);
	else if (meaningsFile.equals("data/fonts/hannigzeichenliste.txt"))
	    processMeaningsFileBody2(reader);
	else {
	    System.err.println("Unknown file: " + meaningsFile);
	    System.exit(-1);
	}
	try {
	    in.close();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
    }

    // The format for hanniglautwerte.txt
    private void processMeaningsFileBody1(BufferedReader reader) 
    		throws IOException {
	// Phonetic pattern <tab> Gardiner codes or '-' <tab> type.
	Pattern pat = Pattern.compile("^([^\t]+)\t([^\t]+)\t([^\t]+)$");
	String line = reader.readLine();
	while (line != null) {
	    if (!line.startsWith("%") && !line.matches("\\s*")) {
		Matcher m = pat.matcher(line);
		if (m.find()) {
		    String[] signs = m.group(2).split(" ");
		    String normalType = normalize1(m.group(3));
		    storeMeaning1(m.group(1), signs, 0, normalType);
		} else {
		    System.err.println("Cannot make sense of line in meaningsfile:");
		    System.err.println(line);
		    System.exit(-1);
		}
	    }
	    line = reader.readLine();
	}
    }

    // The format for hannigzeichenliste.txt
    private void processMeaningsFileBody2(BufferedReader reader) 
    		throws IOException {
	// For 'sign to be used as sign'.
	Vector asSource = new Vector();
	Vector asTarget = new Vector();
	// Gardiner codes <tab> type <tab> phonetic pattern.
	Pattern pat = Pattern.compile("^(([^\t]+))?\t([^\t]+)(\t([^\t]+))?$");
	String line = reader.readLine();
	String prevSigns = "";
	while (line != null) {
	    if (!line.startsWith("%") && !line.matches("\\s*")) {
		Matcher m = pat.matcher(line);
		if (m.find()) {
		    String curSigns = m.group(1);
		    String curType = m.group(3);
		    String curPatterns = m.group(5);
		    if (curSigns == null)
			curSigns = prevSigns;
		    else 
			prevSigns = curSigns;
		    String[] signs = curSigns.split(" ");
		    String[] patterns = null;
		    if (m.group(4) != null)
			patterns = curPatterns.split(",");
		    String normalType = normalize2(curType, patterns);
		    if (normalType.equals("as") && patterns != null) 
			for (int i = 0; i < patterns.length; i++) {
			    asSource.add(signs);
			    asTarget.add(patterns[i]);
			}
		    else if (patterns != null)
			for (int i = 0; i < patterns.length; i++) {
			    String pattern = patterns[i];
			    if (curType.matches("Log") || curType.matches("Abk")) {
				storeMeaning2(signs, 0, normalType, 
					HieroMeaning.beginMarker + pattern);
				storeMeaning2(signs, 0, normalType, 
					"-" + pattern);
			    } else
				storeMeaning2(signs, 0, normalType, pattern);
			}
		    else 
			storeMeaning2(signs, 0, normalType, null);
		} else {
		    System.err.println("Cannot make sense of line in meaningsfile:");
		    System.err.println(line);
		    System.exit(-1);
		}
	    }
	    line = reader.readLine();
	}
	doSignAsSign(asSource, asTarget);
    }

    // Let asSource elements (arrays) have meanings of asTarget elements.
    // Both vectors must have same length.
    private void doSignAsSign(Vector asSource, Vector asTarget) {
	for (int i = 0; i < asSource.size(); i++) {
	    String[] signs = (String[]) asSource.get(i);
	    String sign = (String) asTarget.get(i);
	    Set targetMeanings = getMeanings(sign);
	    for (Iterator it = targetMeanings.iterator(); it.hasNext(); ) {
		HieroMeaning meaning = (HieroMeaning) it.next();
		storeMeaning2(signs, 0, meaning.getType(), meaning.getPhonetic());
	    }
	}
    }

    // Store meanings, with phonetic pattern, Gardiner sign starting from
    // index, and type of meaning. For hanniglautwerte.txt
    private void storeMeaning1(String phonPattern, 
	    String[] signs, int index, String type) {
	if (index >= signs.length) {
	    HieroMeaning meaning = new HieroMeaning(type, phonPattern);
	    meanings.add(meaning);
	} else {
	    String first = signs[index];
	    if (first.equals("-"))
		return;
	    if (meaningMap.get(first) == null)
		meaningMap.put(first, new SignMeanings());
	    SignMeanings tails = (SignMeanings) meaningMap.get(first);
	    tails.storeMeaning1(phonPattern, signs, index+1, type);
	}
    }

    // Store meanings for hannigzeichenliste.txt
    private void storeMeaning2(String[] signs, int index, 
	    String type, String pattern) {
	if (index >= signs.length) {
	    HieroMeaning meaning = new HieroMeaning(type, pattern);
	    meanings.add(meaning);
	} else {
	    String first = signs[index];
	    if (meaningMap.get(first) == null)
		meaningMap.put(first, new SignMeanings());
	    SignMeanings tails = (SignMeanings) meaningMap.get(first);
	    tails.storeMeaning2(signs, index+1, type, pattern);
	}
    }

    // Map type to normalized type for hanniglautwerte.txt
    private String normalize1(String inType) {
	if (inType.equals("Phon"))
	    return "phon";
	else if (inType.equals("Phon-Det"))
	    return "phon";
	else if (inType.equals("Abk"))
	    return "ideo";
	else {
	    System.err.println("Cannot make sense of type: " + inType);
	    System.exit(-1);
	    return "";
	}
    }

    // Map type to normalized type for hannigzeichenliste.txt
    private String normalize2(String inType, String[] pattern) {
	if (inType.equals("Phon") && pattern != null)
	    return "phon";
	else if (inType.equals("Log")) {
	    if (pattern == null)
		return "det";
	    else
		return "phon";
	} else if (inType.equals("Abk")) {
	    if (pattern == null)
		return "det";
	    else
		return "phon";
	} else if (inType.equals("Det")) {
	    if (pattern == null)
		return "det";
	    else
		return "phon";
	} else if (inType.equals("Phono-Det") && pattern != null) {
	    return "phon";
	} else if (inType.equals("Log/Det") && pattern != null) {
	    return "phon";
	} else if (inType.equals("wie") && pattern != null)
	    return "as";
	System.err.println("Cannot make sense of " + inType + " with " + pattern);
	System.exit(-1);
	return "";
    }

    // Get meanings of sign.
    private TreeSet getMeanings(String sign) {
	if (meaningMap.get(sign) == null)
	    return new TreeSet();
	else {
	    SignMeanings tails = (SignMeanings) meaningMap.get(sign);
	    return tails.meanings;
	}
    }

    // Print meanings (for testing).
    public void print() {
	print("", System.out);
    }

    // Print preceded by indentation.
    private void print(String indent, PrintStream out) {
	for (Iterator iter = meanings.iterator(); iter.hasNext(); ) {
	    HieroMeaning meaning = (HieroMeaning) iter.next();
	    out.println(indent + meaning.getType() + "    " + meaning.getPhonetic());
	}
	for (Iterator it = meaningMap.keySet().iterator(); it.hasNext(); ) {
	    String sign = (String) it.next();
	    out.println(indent + sign);
	    SignMeanings recurMeanings = (SignMeanings) meaningMap.get(sign);
	    recurMeanings.print(indent + " ", out);
	}
    }

    // Add transitions for sign meanings.
    // From the outside, this method is to be called with the initial state,
    // and the process is propagated to other states.
    public void induceMeanings(DoubleLinearFiniteAutomatonState state) {
	while (state != null) {
	    induceMeanings(state, this, state);
	    state = (DoubleLinearFiniteAutomatonState) state.getNextState();
	}
    }

    // The substring between states has so far produced partial meanings.
    private static void induceMeanings(DoubleLinearFiniteAutomatonState fromState,
	    SignMeanings meanings,
	    DoubleLinearFiniteAutomatonState state) {
	if (fromState != state) { // make sure no loops
	    Set meaningsToHere = meanings.meanings;
	    for (Iterator meaningIt = meaningsToHere.iterator(); meaningIt.hasNext(); ) {
		HieroMeaning meaning = (HieroMeaning) meaningIt.next();
		fromState.addInducedTransition(meaning, state);
	    }
	}

	TreeMap outs = state.getOutTransitions();
	for (Iterator iter = outs.keySet().iterator(); iter.hasNext(); ) {
	    String sign = (String) iter.next();
	    Set toStates = (Set) outs.get(sign);
	    SignMeanings nextMeanings = (SignMeanings) meanings.meaningMap.get(sign);
	    if (nextMeanings != null) 
		for (Iterator it = toStates.iterator(); it.hasNext(); ) {
		    DoubleLinearFiniteAutomatonState toState =
			(DoubleLinearFiniteAutomatonState) it.next();
		    induceMeanings(fromState, nextMeanings, toState);
		}
	}
    }

    // For testing.
    public static void main(String[] args) {
	// SignMeanings meanings = new SignMeanings("hanniglautwerte.txt");
	SignMeanings meanings = new SignMeanings("data/fonts/hannigzeichenliste.txt");
	meanings.print();
    }

}
