package nederhof.alignment.egyptian.experiments;

import java.io.*;
import java.util.*;

import nederhof.alignment.egyptian.*;
import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.res.*;
import nederhof.util.ngram.*;
import nederhof.util.eval.*;

public class Experiment {

	// Should traces be printed?
	private static boolean verbose = false;

	// Training corpus.
	private static LinkedList<EgyptianOrtho> trainingCorpus() throws IOException {
		LinkedList<EgyptianOrtho> corpus = new LinkedList<EgyptianOrtho>();
		corpus.add(new EgyptianOrtho("corpus/resources/WestcarOrtho.xml"));
		return corpus;
	}
	// Test corpus.
	private static LinkedList<EgyptianOrtho> testCorpus() throws IOException {
		LinkedList<EgyptianOrtho> corpus = new LinkedList<EgyptianOrtho>();
		corpus.add(new EgyptianOrtho("corpus/resources/ShipwreckedOrtho.xml"));
		return corpus;
	}
	// Words in test corpus.
	private static LinkedList<Word> testWords() throws IOException {
		LinkedList<Word> words = new LinkedList<Word>();
		for (EgyptianOrtho resource : testCorpus()) {
			OrthoManipulator manipulator = new OrthoManipulator(resource, 0, new ParsingContext());
			int begin = 0;
			int end = manipulator.nSegments();
			// begin = 18;
			// end = begin+1;
			for (int i = begin; i < end; i++) {
				String hi = manipulator.hiero(i);
				String[] hiero = hi.split("-");
				String al = manipulator.trans(i);
				Vector<OrthoElem> orthos = manipulator.orthos(i);
				words.add(new Word(hiero, new TransLow(al), orthos));
			}
		}
		return words;
	}

	// One word for testing or training.
	private static class Word {
		public String[] hiero;
		public TransLow al;
		public Vector<OrthoElem> orthos;
		public Word(String[] hiero, TransLow al, Vector<OrthoElem> orthos) {
			this.hiero = hiero;
			this.al = al;
			this.orthos = orthos;
		}
	}

	private static void testText() throws IOException {
		for (Word word : testWords()) {
			ConfigAutomaton aut = new ConfigAutomaton(word.hiero, word.al);
			System.out.println("======");
			System.out.println(word.hiero);
			System.out.println(word.al);
			printOrthos(word.orthos);
			System.out.println("------");
			if (aut.success()) {
				List<Function> bestFunctions = aut.getBest();
				Vector<OrthoElem> bestOrtho = ComplexConfig.toOrthoElems(bestFunctions);
				printOrthos(bestOrtho);
				// aut.print();
				// aut.printBest();
			} else {
				System.out.println("CANNOT PARSE");
			}
		}
	}

	private static void testLinearize() throws IOException {
		NGram<Function> ngram = new NGram<Function>(2, new FunctionStart(), new FunctionEnd());
		for (Word word : testWords()) {
			Linearizer linearizer = new Linearizer(word.hiero, word.orthos, word.al);
			/*
			System.out.println("======");
			System.out.println(hi);
			System.out.println(al);
			printOrthos(orthos);
			System.out.println("------");
			*/
			List<Function> functions = linearizer.getFunctions();
			ngram.addGrams(functions);
			// printFunctions(functions);
		}
		ngram.estimate();
		ngram.printNGram();
	}

	// Compute by n-gram.
	private static void testAnalyser(int N, String model) throws IOException {
		testAnalyser(N, model, -1);
	}
	private static void testAnalyser(int N, String model, int index) throws IOException {
		TrainedOrthoAnalyser analyser = new TrainedOrthoAnalyser(N);
		analyser.setModel(model);
		analyser.train(trainingCorpus());
		Evaluation<String> eval = new Evaluation<String>();

		int i = 0;
		for (Word word : testWords()) {
			if (index < 0 || i == index)
				testWord(word, analyser, eval, "" + i);
			i++;
		}

		System.out.println("NGRAM " + N + " " + model);
		System.out.println("RECALL: ");
		System.out.format("%.3f%n", eval.recall());
		System.out.println("PREC: ");
		System.out.format("%.3f%n", eval.precision());
		System.out.println("FSCORE: ");
		System.out.format("%.3f%n", eval.fscore());
	}

	// Test unsupervised learning.
	private static void testUnsupervisedAnalyser(int N, int index) throws IOException {
		UnsupervisedAnalyser analyser = new UnsupervisedAnalyser(N);
		analyser.train(trainingCorpus());
		Evaluation<String> eval = new Evaluation<String>();

		int i = 0;
		for (Word word : testWords()) {
			if (index < 0 || i == index)
				testWord(word, analyser, eval, "" + i);
			i++;
		}
	}

	// Determine baseline accuracy.
	private static void testBaseline() throws IOException {
		testBaseline(-1);
	}
	private static void testBaseline(int index) throws IOException {
		BaselineAnalyser analyser = new BaselineAnalyser();
		analyser.train(trainingCorpus());
		Evaluation<String> eval = new Evaluation<String>();

		int i = 0;
		for (Word word : testWords()) {
			if (index < 0 || i == index)
				testWord(word, analyser, eval, "" + i);
			i++;
		}

		System.out.println("BASELINE");
		System.out.println("RECALL: ");
		System.out.format("%.3f%n", eval.recall());
		System.out.println("PREC: ");
		System.out.format("%.3f%n", eval.precision());
		System.out.println("FSCORE: ");
		System.out.format("%.3f%n", eval.fscore());
	}

	// Test word manually.
	private static void testWord(int N, String model, String label) throws IOException {
		TrainedOrthoAnalyser analyser = new TrainedOrthoAnalyser(N);
		analyser.setModel(model);
		analyser.train(trainingCorpus());
		Evaluation<String> eval = new Evaluation<String>();
		String[] hiero = new String[] {"N31", "X1", "Z1", "Z1", "Z1"};
		String al = "wAwt";
		Word word = new Word(hiero, new TransLow(al), new Vector<OrthoElem>());
		testWord(word, analyser, eval, label);
	}

	// Test word by applying N-gram model.
	private static void testWord(Word word, TrainedOrthoAnalyser analyser,
					Evaluation<String> eval, String label) {
		Set<String> relevant = orthosToStrings(word.orthos);
		Set<String> retrieved = new TreeSet<String>();
		List<Function> bestFunctions = analyser.analyse(word.hiero, word.al);
		if (bestFunctions != null) {
			Vector<OrthoElem> bestOrtho = ComplexConfig.toOrthoElems(bestFunctions);
			retrieved = orthosToStrings(bestOrtho);
		}
		if (verbose && !relevant.equals(retrieved)) {
			printResults(word.hiero, word.al, relevant, retrieved, label);
			analyser.printAutomaton(word.hiero, word.al);
			printFunctions(bestFunctions);
		}
		eval.addObservation(retrieved, relevant);
	}

	// Test word by applying unsupervised N-gram model.
	private static void testWord(Word word, UnsupervisedAnalyser analyser,
					Evaluation<String> eval, String label) {
		Set<String> relevant = orthosToStrings(word.orthos);
		Set<String> retrieved = new TreeSet<String>();
		List<Function> bestFunctions = analyser.analyse(word.hiero, word.al);
		if (bestFunctions != null) {
			Vector<OrthoElem> bestOrtho = ComplexConfig.toOrthoElems(bestFunctions);
			retrieved = orthosToStrings(bestOrtho);
		}
		if (verbose && !relevant.equals(retrieved)) {
			printResults(word.hiero, word.al, relevant, retrieved, label);
			analyser.printAutomaton(word.hiero, word.al);
			printFunctions(bestFunctions);
		}
		eval.addObservation(retrieved, relevant);
	}

	// Test word by baseline model.
	private static void testWord(Word word, BaselineAnalyser analyser,
			Evaluation<String> eval, String label) {
		Set<String> relevant = orthosToStrings(word.orthos);
		List<Function> bestFunctions = analyser.analyse(word.hiero);
		List<OrthoElem> bestOrthos = functionsToOrthos(bestFunctions);
		Set<String> retrieved = orthosToStrings(bestOrthos);
		if (verbose) {
			printResults(word.hiero, word.al, relevant, retrieved, label);
			// analyser.printAutomaton(word.hiero);
		}
		eval.addObservation(retrieved, relevant);
	}

	private static Set<String> orthosToStrings(Vector<OrthoElem> elems) {
		Set<String> strings = new TreeSet<String>();
		for (OrthoElem elem : elems)
			strings.add(orthoToString(elem));
		return strings;
	}
	private static Set<String> orthosToStrings(List<OrthoElem> elems) {
		Set<String> strings = new TreeSet<String>();
		for (OrthoElem elem : elems)
			strings.add(orthoToString(elem));
		return strings;
	}
	private static String orthoToString(OrthoElem elem) {
		String s = "";
		int[][] signRanges = elem.signRanges();
		if (signRanges != null) {
			for (int i = 0; i < signRanges.length; i++)
				s += "" + signRanges[i][0] + "," + signRanges[i][1] + ";";
			s += "\n";
		}
		s += elem.name() +
			(elem.argName() != null ? " " + elem.argName() + "=" + elem.argValue() : "") + "\n";
		return s;
	}

	private static List<OrthoElem> functionsToOrthos(List<Function> functions) {
		List<OrthoElem> elems = new LinkedList<OrthoElem>();
		int pos = 0;
		for (Function fun : functions) {
			OrthoElem elem = fun.orthoElem(pos, fun.hiLength());
			if (elem != null)
				elems.add(elem);
			pos += fun.hiLength();
		}
		return elems;
	}

	/////////////
	// Printing.

	private static void printHiero(String[] hiero) {
		for (int i = 0; i < hiero.length; i++) {
			System.out.print(hiero[i]);
			if (i+1 < hiero.length)
				System.out.print("-");
		}
		System.out.println();
	}

	private static void printOrthos(Vector<OrthoElem> elems) {
		for (OrthoElem elem : elems)
			System.out.print(elem);
	}

	private static void printFunctions(List<Function> functions) {
		for (Function fun : functions)
			System.out.println(fun);
	}

	private static void printStrings(Set<String> elems) {
		for (String elem : elems)
			System.out.print(elem);
	}

	private static void printResults(String[] hiero, TransLow al, 
			Set<String> relevant, Set<String> retrieved, String label) {
		System.out.println("====== " + label);
		printHiero(hiero);
		System.out.println(al);
		printStrings(relevant);
		System.out.println("---");
		printStrings(retrieved);
	}

	/////////////
	// Main.

	public static void main(String[] args) {
		try {
			// int index = 54;
			int index = -1;
			if (index >= 0)
				verbose = true;
			// verbose = true;
			// testText();
			// testLinearize();
			// testBaseline(index);
			// testAnalyser(1, "functions", index);
			// testAnalyser(2, "functions", index);
			// testAnalyser(3, "functions", index);
			// testAnalyser(4, "functions", index);
			// testAnalyser(5, "functions", index);
			// testAnalyser(1, "classes", index);
			// testAnalyser(2, "classes", index);
			// testAnalyser(3, "classes", index);
			// testAnalyser(4, "classes", index);
			// testAnalyser(5, "classes", index);
			// testAnalyser(1, "interpolation", index);
			// testAnalyser(2, "interpolation", index);
			// testAnalyser(3, "interpolation", index);
			// testAnalyser(4, "interpolation", index);
			// testAnalyser(1, "functions strings", index);
			// testAnalyser(2, "functions strings", index);
			// testAnalyser(3, "functions strings", index);
			// testAnalyser(4, "functions strings", index);
			testUnsupervisedAnalyser(3, index);
			// testWord();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
