// Align hieroglyphic and orthography.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.alignment.*;
import nederhof.fonts.*;
import nederhof.hieroutil.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.res.*;
import nederhof.util.*;

public abstract class HieroImageAligner {

    // Parsing context.
    public static HieroRenderContext hieroContext = null;

    public static void align(final Tier tier1, final Tier tier2,
            TextResource resource1, TextResource resource2,
            int tierNum1, int tierNum2,
            int globalTierNum1, int globalTierNum2,
            Vector phraseStarts1, Vector phraseStarts2) {
        makeContext();

        // get Gardiner names
        Vector<String> names1 = new Vector<String>();
        Vector<Integer> namePositions1 = new Vector<Integer>();
        getNamesHiero(tier1, names1, namePositions1);
        String[] namesArray1 =
            ArrayAux.toStringArray(names1);
        final int[] namePositionsArray1 =
            ArrayAux.toIntArray(namePositions1);

        Vector<String> names2 = new Vector<String>();
        Vector<Integer> namePositions2 = new Vector<Integer>();
        getNamesImage(tier2, names2, namePositions2);
        String[] namesArray2 =
            ArrayAux.toStringArray(names2);
        final int[] namePositionsArray2 =
            ArrayAux.toIntArray(namePositions2);

        new HieroImageAligner(namesArray1, namesArray2) {
            public void match(int in, int out) {
                if (in < namePositionsArray1.length && out < namePositionsArray2.length) {
                    int inPos = namePositionsArray1[in];
                    int outPos = namePositionsArray2[out];
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
    private static void getNamesHiero(Tier tier, Vector<String> names, 
	    Vector<Integer> namePositions) {
        TierPart[] parts = tier.parts;
        int nSymbols = 0;
        for (int i = 0; i < parts.length; i++) {
            TierPart part = parts[i];
            Vector glyphs = glyphsOfHi(part);
            if (glyphs == null)
                nSymbols += part.nSymbols();
            else
                for (int j = 0; j < glyphs.size(); j++) {
                    ResNamedglyph named = (ResNamedglyph) glyphs.get(j);
                    names.add(hieroContext.nameToGardiner(named.name));
                    namePositions.add(nSymbols);
                    nSymbols++;
                }
        }
    }
    // Get Gardiner names in the orthography.
    private static void getNamesImage(Tier tier, Vector<String> names, 
	    Vector<Integer> namePositions) {
	TierPart[] parts = tier.parts;
	int nSymbols = 0;
	for (int i = 0; i < parts.length; i++) {
	    TierPart part = parts[i];
	    Vector<String> glyphs = glyphsOfImage(part);
	    if (glyphs != null)
		for (int j = 0; j < glyphs.size(); j++) {
		    String name = glyphs.get(j);
		    names.add(hieroContext.nameToGardiner(name));
		    namePositions.add(nSymbols);
		}
	    nSymbols++;
	}
    }

    // If part is hieroglyphic, return vector of
    // Gardiner names. Else return null.
    private static Vector glyphsOfHi(TierPart part) {
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
    private static Vector<String> glyphsOfImage(TierPart part) {
        if (part instanceof ImagePlacePart) {
	    Vector<String> glyphs = new Vector<String>();
            ImagePlacePart imagePlace = (ImagePlacePart) part;
	    ImageSign info = imagePlace.info;
	    glyphs.add(info.getName());
	    return glyphs;
        }
        return null;
    }

    ///////////////////////////////////////////////////
    // The actual object of aligner.

    // Array of glyph names.
    private String[] glyphs1;
    private String[] glyphs2;

    // Mapping from states of the hieroglyphic to a set of configurations.
    private TreeMap stateToConfigs = new TreeMap();

    // Final configurations (representing final states in both hieroglyphic
    // and orthography).
    private ConfigurationSet<HiConfiguration> finals;

    // Last configuration produced.
    private HiConfiguration lastConfig;

    // Width of beam in beam search.
    private final int beam = 20;

    public HieroImageAligner(String[] glyphs1, String[] glyphs2) {
        this.glyphs1 = glyphs1;
        this.glyphs2 = glyphs2;

        LinearFiniteAutomatonState hiInitial1 = initialHieroState(glyphs1);
        LinearFiniteAutomatonState hiInitial2 = initialHieroState(glyphs2);

        makeNoConfigs(hiInitial1);
        makeInitialConfig(hiInitial1, hiInitial2);
        finals = new ConfigurationSet<HiConfiguration>(beam);
        doHieroStates(hiInitial1);
        HiConfiguration finalConfig = bestMatch();
        reportAlignment(finalConfig);
    }

    // Signal to called the alignment of two positions.
    public abstract void match(int inPos, int outPos);

    // Construct states of hieroglyphic. Return first such state.
    private static LinearFiniteAutomatonState initialHieroState(String[] glyphs) {
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

    // Make empty sets of configurations for each state.
    private void makeNoConfigs(LinearFiniteAutomatonState hiero) {
        for (LinearFiniteAutomatonState state = hiero;
                state != null;
                state = state.getNextState())
            stateToConfigs.put(state, new ConfigurationSet<HiConfiguration>(beam));
    }

    // Make initial configuration.
    private void makeInitialConfig(LinearFiniteAutomatonState hiero1,
            LinearFiniteAutomatonState hiero2) {
        ConfigurationSet<HiConfiguration> initialSet = 
		(ConfigurationSet<HiConfiguration>) stateToConfigs.get(hiero1);
        HiConfiguration initial = new HiConfiguration(hiero1, hiero2);
        initialSet.add(initial, 0);
        lastConfig = initial;
    }

    // For each hieroglyphic state, produce more configurations, attached to
    // the same state or to subsequent states.
    private void doHieroStates(LinearFiniteAutomatonState hiero) {
        for (LinearFiniteAutomatonState state = hiero;
                state != null;
                state = (LinearFiniteAutomatonState) state.getNextState()) {
            ConfigurationSet<HiConfiguration> confs = 
			(ConfigurationSet<HiConfiguration>) stateToConfigs.get(state);
            doHieroState(confs);
        }
    }

    // Produce more configurations, by priority queue, choosing lowest
    // weight configurations first.
    // As new configurations may be the same as old configurations,
    // to enforce termination, we maintain set of treated configurations.
    private void doHieroState(ConfigurationSet<HiConfiguration> confs) {
        TreeSet doneConfigs = new TreeSet();
        while (!confs.noUntreated()) {
            WeightConfig<HiConfiguration> weightConfig = confs.pop();
            double w = weightConfig.getWeight();
            HiConfiguration config = weightConfig.getConfig();
            tracePrintWeight(w, config); // tracing only
            doneConfigs.add(config);
            LinkedList nextConfigs = config.nextConfigs();
            for (Iterator it = nextConfigs.listIterator(); it.hasNext(); ) {
                WeightConfig<HiConfiguration> nextWeightConfig = 
			(WeightConfig<HiConfiguration>) it.next();
                double nextW = nextWeightConfig.getWeight();
                HiConfiguration nextConfig = nextWeightConfig.getConfig();
                if (!doneConfigs.contains(nextConfig)) {
                    LinearFiniteAutomatonState state = nextConfig.getHieroState1();
                    ConfigurationSet<HiConfiguration> nextSet =
                        (ConfigurationSet<HiConfiguration>) stateToConfigs.get(state);
                    tracePrintNextConfig(w, nextW, nextConfig); // tracing only
                    nextSet.add(nextConfig, w + nextW);
                    if (nextConfig.isFinal())
                        finals.add(nextConfig, w + nextW);
                    lastConfig = nextConfig;
                }
            }
        }
    }

    // After construction, this gives best final state.
    // (Null if no such state exists.)
    public HiConfiguration bestMatch() {
        if (!finals.noUntreated()) {
            WeightConfig<HiConfiguration> weightConfig = finals.best();
            return weightConfig.getConfig();
        } else
            return lastConfig;
    }

    private void reportAlignment(HiConfiguration config) {
        for ( ; config != null; config = config.prev()) {
	    LabeledDoubleLinearFiniteAutomatonState hiState1 =
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState1();
	    Integer hiPos1 = (Integer) hiState1.getLabel();
	    LabeledDoubleLinearFiniteAutomatonState hiState2 =
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState2();
	    Integer hiPos2 = (Integer) hiState2.getLabel();
	    match(hiPos1.intValue(), hiPos2.intValue());
        }
    }

    //////////////////////////////////////////////
    // Tracing

    // Tracing of computation.
    private static final boolean trace = false;

    // Print weight.
    private void tracePrintWeight(double w, HiConfiguration config) {
        if (trace)
            System.out.println("(" + w + ") " + config);
    }

    // Print next configuration.
    private void tracePrintNextConfig(double w, double nextW, HiConfiguration nextConfig) {
        if (trace) {
            System.out.println("    " + nextConfig.action());
            System.out.println("      (" + (w + nextW)+ ") " + nextConfig);
        }
    }

}
