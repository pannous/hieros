/***************************************************************************/
/*                                                                         */
/*  HieroTransAligner.java                                                 */
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

// Align hieroglyphic and transliteration.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.alignment.*;
import nederhof.fonts.*;
import nederhof.hieroutil.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.*;
import nederhof.res.*;
import nederhof.util.*;

public abstract class HieroTransAligner {

    // Parsing context.
    public static HieroRenderContext hieroContext = null;

    public static void align(final Tier tier1, final Tier tier2,
            TextResource resource1, TextResource resource2,
            int tierNum1, int tierNum2,
            int globalTierNum1, int globalTierNum2,
            Vector<Integer> phraseStarts1, Vector<Integer> phraseStarts2) {
	makeContext();
	// get Gardiner names
	Vector names = new Vector();
	Vector namePositions = new Vector();
	getNames(tier1, names, namePositions);
	String[] namesArray =
	    ArrayAux.toStringArray(names);
	final int[] namePositionsArray = 
	    ArrayAux.toIntArray(namePositions);

	Vector words = new Vector();
	Vector wordPositions = new Vector();
	getWords(tier2, words, wordPositions);
	String[] wordsArray = 
	    ArrayAux.toStringArray(words);
	final int[] wordPositionsArray = 
	    ArrayAux.toIntArray(wordPositions);

	int[] prec1 = AlignConstraints.getRestrictions(tier1, tier2, 
		namePositions, wordPositions);
	int[] prec2 = AlignConstraints.getRestrictions(tier2, tier1, 
		wordPositions, namePositions);

	new HieroTransAligner(namesArray, wordsArray, prec1, prec2) {
	    public void match(int in, int out) {
		if (in < namePositionsArray.length && out < wordPositionsArray.length) {
		    int inPos = namePositionsArray[in];
		    int outPos = wordPositionsArray[out];
		    tier1.addBreak(inPos, new TierPos(tier2, outPos));
		    tier2.addBreak(outPos, new TierPos(tier1, inPos));
		}
	    }
	};
    }

    // Ensure the context exists (only once globally).
    private static void makeContext() {
	if (hieroContext == null)
	    hieroContext =
		new HieroRenderContext(20); // fontsize arbitrary
    }

    // Get Gardiner names in the hieroglyphic.
    private static void getNames(Tier tier1, Vector names, Vector namePositions) {
	TierPart[] parts = tier1.parts;
	int nSymbols = 0;
	for (int i = 0; i < parts.length; i++) {
	    TierPart part = parts[i];
	    Vector glyphs = glyphsOf(part);
	    if (glyphs == null)
		nSymbols += part.nSymbols();
	    else
		for (int j = 0; j < glyphs.size(); j++) {
		    ResNamedglyph named = (ResNamedglyph) glyphs.get(j);
		    names.add(hieroContext.nameToGardiner(named.name));
		    namePositions.add(new Integer(nSymbols));
		    nSymbols++;
		}
	}
    }

    // If part is hieroglyphic, return vector of
    // Gardiner names. Else return null.
    private static Vector glyphsOf(TierPart part) {
	if (part instanceof HiPart) {
	    HiPart hi = (HiPart) part;
	    if (hi.parsed != null)
		return hi.parsed.glyphs();
	} else if (part instanceof HiPdfPart) {
	    HiPdfPart hi = (HiPdfPart) part;
	    if (hi.parsed != null)
		return hi.parsed.glyphs();
	}
	return null;
    }

    // Divide the tier into words. Make note how to map words to
    // positions in tier.
    // A new word is constructed where the tier is breakable.
    private static void getWords(Tier tier2, Vector words, Vector wordPositions) {
	TierPart[] parts = tier2.parts;
	String buffer = "";
	int nSymbols = 0;
	for (int i = 0; i < parts.length; i++) {
	    TierPart part = parts[i];
	    String string = stringOf(part);
	    if (string == null) 
		for (int j = 0; j < part.nSymbols(); j++) {
		    if (tier2.breakable(nSymbols) && !buffer.equals("")) {
			words.add(buffer);
			buffer = "";
		    }
		    nSymbols++;
		}
	    else
		for (int j = 0; j < string.length(); j++) {
		    char c = string.charAt(j);
		    if (!Character.isWhitespace(c)) {
			if (tier2.breakable(nSymbols) && !buffer.equals("")) {
			    words.add(buffer);
			    buffer = "";
			}
			if (buffer.equals(""))
			    wordPositions.add(new Integer(nSymbols));
			buffer = buffer + c;
			nSymbols++;
		    }
		}
	}
	if (!buffer.equals(""))
	    words.add(buffer);
    }

    // If part of transliteration, return string.
    // Otherwise null.
    private static String stringOf(TierPart part) {
	if (part instanceof AlPart) {
	    AlPart al = (AlPart) part;
	    return al.string;
	} else if (part instanceof AlPdfPart) {
	    AlPdfPart al = (AlPdfPart) part;
	    return al.string;
	} else
	    return null;
    }

    ///////////////////////////////////////////////////
    // The actual object of aligner.

    // Mapping signs to meanings.
    private SignMeanings meanings = new SignMeanings("data/fonts/hannigzeichenliste.txt");

    // Array of glyph names.
    private String[] glyphs;
    // Array of words of transliteration.
    private String[] words;

    // Precedences.
    // For position in source text, the minimum position in target text
    // that should be aligned to it. And vice versa.
    private int[] inPrec;
    private int[] outPrec;

    // Mapping from states of the hieroglyphic to a set of configurations.
    private TreeMap stateToConfigs = new TreeMap();

    // Final configurations (representing final states in both hieroglyphic
    // and transliteration).
    private ConfigurationSet<HiAlConfiguration> finals;

    // Last configuration produced.
    private HiAlConfiguration lastConfig;

    // Width of beam in beam search.
    private final int beam = 100;

    public HieroTransAligner(String[] glyphs, String[] words, 
	    int[] inPrec, int[] outPrec) {
	this.glyphs = glyphs;
	this.words = words;
	this.inPrec = inPrec;
	this.outPrec = outPrec;
	DoubleLinearFiniteAutomatonState hiInitial = initialHieroState();
	NumberMeanings.induceRepeat(hiInitial);
	NumberMeanings.induceNumbers(hiInitial);
	meanings.induceMeanings(hiInitial);

	LinearFiniteAutomatonState alInitial = initialTransState();

	makeNoConfigs(hiInitial);
	makeInitialConfig(hiInitial, alInitial);
        finals = new ConfigurationSet<HiAlConfiguration>(beam);
        doHieroStates(hiInitial);
	HiAlConfiguration finalConfig = bestMatch();
	getAutomaticWordStarts(finalConfig);
    }

    // Signal to called the alignment of two positions.
    public abstract void match(int inPos, int outPos);

    // Construct states of hieroglyphic. Return first such state.
    private DoubleLinearFiniteAutomatonState initialHieroState() {
	LabeledDoubleLinearFiniteAutomatonState[] states =
	    new LabeledDoubleLinearFiniteAutomatonState[glyphs.length + 1];
	for (int i = 0; i <= glyphs.length; i++) 
	    states[i] = new LabeledDoubleLinearFiniteAutomatonState(new Integer(i));
	for (int i = 0; i < glyphs.length; i++) {
	    states[i].setNextState(states[i+1]);
	    Set nexts = new TreeSet();
	    nexts.add(states[i+1]);
	    states[i].getOutTransitions().put(glyphs[i], nexts);
	}
	states[glyphs.length].setFinal(true);
	return states[0];
    }

    private LinearFiniteAutomatonState initialTransState() {
	LabeledDoubleLinearFiniteAutomatonState[] states =
	    new LabeledDoubleLinearFiniteAutomatonState[words.length + 1];
	for (int i = 0; i <= words.length; i++) 
	    states[i] = new LabeledDoubleLinearFiniteAutomatonState(new Integer(i));
	for (int i = 0; i < words.length; i++) {
	    states[i].setNextState(states[i+1]);
	    Set nexts = new TreeSet();
	    nexts.add(states[i+1]);
	    states[i].getOutTransitions().put(words[i], nexts);
	}
	states[words.length].setFinal(true);
	return states[0];
    }

    // Make empty sets of configurations for each state.
    private void makeNoConfigs(LinearFiniteAutomatonState hiero) {
        for (LinearFiniteAutomatonState state = hiero;
                state != null;
                state = state.getNextState())
            stateToConfigs.put(state, new ConfigurationSet<HiAlConfiguration>(beam));
    }

    // Make initial configuration.
    private void makeInitialConfig(DoubleLinearFiniteAutomatonState hiero,
            LinearFiniteAutomatonState trans) {
        ConfigurationSet<HiAlConfiguration> initialSet = (ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(hiero);
        HiAlConfiguration initial = new HiAlConfiguration(hiero, trans);
        initialSet.add(initial, 0);
        lastConfig = initial;
    }

    // For each hieroglyphic state, produce more configurations, attached to
    // the same state or to subsequent states.
    private void doHieroStates(DoubleLinearFiniteAutomatonState hiero) {
        for (DoubleLinearFiniteAutomatonState state = hiero;
                state != null;
                state = (DoubleLinearFiniteAutomatonState) state.getNextState()) {
	    tracePrintMeanings(state); // tracing only
            ConfigurationSet<HiAlConfiguration> confs = (ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(state);
            doHieroState(confs);
        }
    }

    // Produce more configurations, by priority queue, choosing lowest
    // weight configurations first.
    // As new configurations may be the same as old configurations,
    // to enforce termination, we maintain set of treated configurations.
    private void doHieroState(ConfigurationSet<HiAlConfiguration> confs) {
        TreeSet doneConfigs = new TreeSet();
        while (!confs.noUntreated()) {
            WeightConfig<HiAlConfiguration> weightConfig = confs.pop();
            double w = weightConfig.getWeight();
            HiAlConfiguration config = weightConfig.getConfig();
	    tracePrintWeight(w, config); // tracing only
            doneConfigs.add(config);
            LinkedList nextConfigs = config.nextConfigs();
            for (Iterator it = nextConfigs.listIterator(); it.hasNext(); ) {
                WeightConfig<HiAlConfiguration> nextWeightConfig = (WeightConfig<HiAlConfiguration>) it.next();
                double nextW = nextWeightConfig.getWeight();
                HiAlConfiguration nextConfig = nextWeightConfig.getConfig();
		if (violates(nextConfig))
		    continue;
                if (!doneConfigs.contains(nextConfig)) {
                    LinearFiniteAutomatonState state = nextConfig.getHieroState();
                    ConfigurationSet<HiAlConfiguration> nextSet = 
			(ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(state);
		    tracePrintNextConfig(w, nextW, nextConfig); // tracing only
                    nextSet.add(nextConfig, w + nextW);
                    if (nextConfig.isFinal())
                        finals.add(nextConfig, w + nextW);
                    lastConfig = nextConfig;
                }
            }
        }
    }

    // Violates constraints? 
    private boolean violates(HiAlConfiguration config) {
	if (config.isWordStart()) {
	    LabeledDoubleLinearFiniteAutomatonState hiState = 
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
	    Integer hiPos = (Integer) hiState.getLabel();
	    int hi = hiPos.intValue();
	    LabeledDoubleLinearFiniteAutomatonState alState = 
		(LabeledDoubleLinearFiniteAutomatonState) config.getTransState();
	    Integer alPos = (Integer) alState.getLabel();
	    int al = alPos.intValue();
	    if (hi < inPrec.length && al < inPrec[hi] ||
		    al < outPrec.length && hi < outPrec[al])
		return true;
	}
	return false;
    }

    // After construction, this gives best final state.
    // (Null if no such state exists.)
    public HiAlConfiguration bestMatch() {
        if (!finals.noUntreated()) {
            WeightConfig<HiAlConfiguration> weightConfig = finals.best();
            return weightConfig.getConfig();
        } else
            return lastConfig;
    }

    // Get automatically computed word start positions.
    private void getAutomaticWordStarts(HiAlConfiguration config) {
        for ( ; config != null; config = config.prev()) {
            if (config.isWordStart()) {
                LabeledDoubleLinearFiniteAutomatonState hiState =
                    (LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
                Integer hiPos = (Integer) hiState.getLabel();
                LabeledDoubleLinearFiniteAutomatonState alState =
                    (LabeledDoubleLinearFiniteAutomatonState) config.getTransState();
                Integer alPos = (Integer) alState.getLabel();
		match(hiPos.intValue(), alPos.intValue());
            }
        }
    }

    //////////////////////////////////////////////
    // Tracing

    // Tracing of computation.
    private static boolean trace = false;

    // Print meanings for state.
    private void tracePrintMeanings(DoubleLinearFiniteAutomatonState state) {
	if (trace) {
	    System.out.println("-------------------");
	    TreeMap meanings = state.getInducedOutTransitions();
	    for (Iterator it = meanings.keySet().iterator();
		    it.hasNext(); ) {
		HieroMeaning meaning = (HieroMeaning) it.next();
		TreeSet states = (TreeSet) meanings.get(meaning);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    DoubleLinearFiniteAutomatonState next =
						(DoubleLinearFiniteAutomatonState) iter.next();
		    String type = meaning.getType();
		    String phon = meaning.getPhonetic();
		    System.out.println("" + next + " " + type + " " + phon);
		}
	    }
	    System.out.println("-------------------");
	}
    }

    // Print weight.
    private void tracePrintWeight(double w, HiAlConfiguration config) {
	if (trace)
	    System.out.println("(" + w + ") " + config);
    }

    // Print next configuration.
    private void tracePrintNextConfig(double w, double nextW, HiAlConfiguration nextConfig) {
	if (trace) {
	    System.out.println("    " + nextConfig.action());
	    System.out.println("      (" + (w + nextW)+ ") " + nextConfig);
	}
    }

}
