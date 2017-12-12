/***************************************************************************/
/*                                                                         */
/*  HieroTransMatching.java                                                */
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

// The process of matching hieroglyphic and transliteration.
// For each state in hieroglyphic, an agenda is used with limited
// beam size. States are treated in linear order.

package nederhof.alignment.egyptian.develop;

import java.text.*;
import java.util.*;

import nederhof.alignment.egyptian.*;
import nederhof.hieroutil.*;
import nederhof.util.*;

public class HieroTransMatching {

    // Tracing of computation.
    private static boolean trace = false;

    // Mapping from states of the hieroglyphic to a set of configurations.
    private TreeMap stateToConfigs = new TreeMap();

    // Final configurations (representing final states in both hieroglyphic
    // and transliteration).
    private ConfigurationSet<HiAlConfiguration> finals;

    // Last configuration produced.
    private HiAlConfiguration lastConfig;

    // Create process for hieroglyphic and transliteration.
    // Beam search size indicated.
    public HieroTransMatching(DoubleLinearFiniteAutomatonState hiero,
	    LinearFiniteAutomatonState trans, int beam) {
	makeNoConfigs(hiero, beam);
	makeInitialConfig(hiero, trans);
	finals = new ConfigurationSet<HiAlConfiguration>(beam);
	doHieroStates(hiero);
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

    // Make empty sets of configurations for each state.
    private void makeNoConfigs(LinearFiniteAutomatonState hiero, int beam) {
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
	    if (trace) {
		System.out.println("-------------------");
		printMeanings(state);
		System.out.println("-------------------");
	    }
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
	    if (trace)
		System.out.println("(" + w + ") " + config);
	    doneConfigs.add(config);
	    LinkedList nextConfigs = config.nextConfigs();
	    for (Iterator it = nextConfigs.listIterator(); it.hasNext(); ) {
		WeightConfig<HiAlConfiguration> nextWeightConfig = (WeightConfig<HiAlConfiguration>) it.next();
		double nextW = nextWeightConfig.getWeight();
		HiAlConfiguration nextConfig = nextWeightConfig.getConfig();
		if (!doneConfigs.contains(nextConfig)) {
		    LinearFiniteAutomatonState state = nextConfig.getHieroState();
		    ConfigurationSet<HiAlConfiguration> nextSet = (ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(state);
		    if (trace) {
			System.out.println("    " + nextConfig.action());
			System.out.println("      (" + (w + nextW)+ ") " + nextConfig);
		    }
		    nextSet.add(nextConfig, w + nextW);
		    if (nextConfig.isFinal())
			finals.add(nextConfig, w + nextW);
		    lastConfig = nextConfig;
		}
	    }
	}
    }

    // For tracing, print meanings.
    private void printMeanings(DoubleLinearFiniteAutomatonState state) {
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
    }

    // Replay matching process from Configuration,
    // for next few hieroglyphic signs.
    public static String replay(HiAlConfiguration config, int beam, int nGlyphs) {
	TreeMap stateToConfigs = new TreeMap();
	StringBuffer buf = new StringBuffer();
	buf.append("<html>\n<table>");
	LinearFiniteAutomatonState state = config.getHieroState();
	ConfigurationSet<HiAlConfiguration> initialSet = new ConfigurationSet<HiAlConfiguration>(beam);
	initialSet.add(config, 0);
	stateToConfigs.put(state, initialSet);
	replayStates(state, stateToConfigs, beam, nGlyphs, buf);
	buf.append("</table>\n</html>\n");
	return buf.toString();
    }	

    // Replay matching process from state.
    private static void replayStates(LinearFiniteAutomatonState hiero, 
	    TreeMap stateToConfigs,
	    int beam, int nGlyphs, StringBuffer buf) {
	for (LinearFiniteAutomatonState state = hiero;
		state != null && nGlyphs > 0;
		state = state.getNextState(), nGlyphs--) {
	    buf.append("<tr><th>" + state + "</th></tr>\n");
	    ConfigurationSet<HiAlConfiguration> confs = (ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(state);
	    if (confs != null)
		replayState(confs, stateToConfigs, beam, nGlyphs, buf);
	}
    }

    // Replay matching actions for present state.
    private static void replayState(ConfigurationSet<HiAlConfiguration> confs,
	    TreeMap stateToConfigs,
	    int beam, int nGlyphs, StringBuffer buf) {
	DecimalFormat penaltyFormat = new DecimalFormat("#.#");
	TreeSet doneConfigs = new TreeSet();
	while (!confs.noUntreated()) {
	    WeightConfig<HiAlConfiguration> weightConfig = confs.pop();
	    double w = weightConfig.getWeight();
	    HiAlConfiguration config = weightConfig.getConfig();
	    buf.append("<tr><th> </th><td>[" + 
		penaltyFormat.format(w) + "] " + "</td><td>" +
		config.toHtmlString() +
		"</td></tr>\n");
	    doneConfigs.add(config);
	    LinkedList nextConfigs = config.nextConfigs();
	    for (Iterator it = nextConfigs.listIterator(); it.hasNext(); ) {
		WeightConfig<HiAlConfiguration> nextWeightConfig = (WeightConfig<HiAlConfiguration>) it.next();
		double nextW = nextWeightConfig.getWeight();
		HiAlConfiguration nextConfig = nextWeightConfig.getConfig();
		if (!doneConfigs.contains(nextConfig)) {
		    LinearFiniteAutomatonState state = nextConfig.getHieroState();
		    ConfigurationSet<HiAlConfiguration> nextSet = (ConfigurationSet<HiAlConfiguration>) stateToConfigs.get(state);
		    if (nextSet == null) {
			nextSet = new ConfigurationSet<HiAlConfiguration>(beam);
			stateToConfigs.put(state, nextSet);
		    }
		    buf.append("<tr><th> </th><td>" + nextConfig.action() + 
			    "</td><td>[" + 
			    penaltyFormat.format(w + nextW) + "] " + 
			    nextConfig.toHtmlString() + 
			    "</td></tr>\n");
		    nextSet.add(nextConfig, w + nextW);
		}
	    }
	}
    }

}
