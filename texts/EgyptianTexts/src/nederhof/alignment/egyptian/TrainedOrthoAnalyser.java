package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.res.*;
import nederhof.res.operations.*;
import nederhof.util.*;
import nederhof.util.ngram.*;

// Automaton with N-gram model trained from corpora.
public class TrainedOrthoAnalyser {

	// Parsing context.
	protected ParsingContext context = new ParsingContext();

	// Which N in N-gram.
	protected int N;

	// The model of functions.
	protected NGram<Function> ngram;
	// Model of functions, where jumps are attached to following function.
	protected NGram<String> ngramString;
	// The model of classes of functions.
	protected NGram<String> classGram;
	// Separate unigram models for classes, indexed by class names.
	protected TreeMap<String,NGram<Function>> unigrams;

	// The model used. Can be
	// "functions", "classes", "interpolation".
	protected String model = "functions";

	public TrainedOrthoAnalyser(int n) {
		this.N = n;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void train(List<EgyptianOrtho> trainingCorpus) {
		ngram = new NGram<Function>(N, new FunctionStart(), new FunctionEnd());
		ngramString = new NGram<String>(N, "" + (new FunctionStart()), "" + (new FunctionEnd()));
		classGram = new NGram<String>(N, (new FunctionStart()).name(), 
												(new FunctionEnd()).name());
		unigrams = new TreeMap<String,NGram<Function>>();
		for (EgyptianOrtho resource : trainingCorpus) {
			OrthoManipulator manipulator = new OrthoManipulator(resource, 0, context);
			train(manipulator);
		}
		ngram.estimate();
		ngramString.estimate();
		classGram.estimate();
		for (String name : unigrams.keySet())
			unigrams.get(name).estimate();
	}
	private void train(OrthoManipulator manip) {
		for (int i = 0; i < manip.nSegments(); i++) {
			String word = manip.trans(i);
			Vector<OrthoElem> orthos = manip.orthos(i);
			String hi = manip.hiero(i);
			ResFragment hiParsed = ResFragment.parse(hi, context);
			hiParsed = (new NormalizerMnemonics()).normalize(hiParsed);
			String[] hiero = ArrayAux.toStringArray(hiParsed.glyphNames());
			Linearizer linearizer = new Linearizer(hiero, orthos, new TransLow(word));
			List<Function> functions = linearizer.getFunctions();
			List<String> classes = functionsToClasses(functions);
			List<String> functionsString = partitionFunctions(functions);
			ngram.addGrams(functions);
			ngramString.addGrams(functionsString);
			classGram.addGrams(classes);
			for (Function f : functions) 
				classUnigram(f.name()).addUnigram(f);
		}
	}

	public List<Function> analyse(String[] hiero, TransLow word) {
		ConfigAutomaton aut = makeAutomaton(hiero, word);
		return aut.success() ? aut.getBest() : null;
	}

	public void printAutomaton(String[] hiero, TransLow word) {
		ConfigAutomaton aut = makeAutomaton(hiero, word);
		aut.print();
	}

	private ConfigAutomaton makeAutomaton(String[] hiero, TransLow word) {
		return model.equals("functions") ?
				new ConfigAutomatonNGram(hiero, word, ngram) :
				model.equals("functions strings") ?
				new ConfigAutomatonNGramString(hiero, word, ngramString) :
				model.equals("classes") ?
				new ConfigAutomatonClassGram(hiero, word, ngram, classGram, unigrams) :
				new ConfigAutomatonInterpolation(hiero, word, ngram, classGram, unigrams);
	}

	private List<String> functionsToClasses(List<Function> functions) {
		List<String> classes = new ArrayList<String>();
		for(Function f : functions)
			classes.add(f.name());
		return classes;
	}

	// Tie jumps to following function.
	private List<String> partitionFunctions(List<Function> functions) {
		List<String> ss = new LinkedList<String>();
		String s = "";
		for (Function function : functions) {
			s += "" + function;
			if (!(function instanceof FunctionJump)) {
				ss.add(s);
				s = "";
			}
		}
		if (!s.equals(""))
			ss.add(s);
		return ss;
	}

	// Get unigram model for class. If doesn't exist, then create.
	private NGram<Function> classUnigram(String name) {
		if (unigrams.get(name) == null)
			unigrams.put(name, new NGram<Function>(1, new FunctionStart(), new FunctionEnd()));
		return unigrams.get(name);
	}

}
