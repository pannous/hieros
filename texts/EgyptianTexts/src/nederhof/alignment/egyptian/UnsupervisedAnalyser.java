package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.res.*;
import nederhof.res.operations.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// Model obtained by unsupervised training.
public class UnsupervisedAnalyser {

    // Parsing context.
    protected ParsingContext context = new ParsingContext();

    // Which N in N-gram.
    protected int N;

	// How many iterations of EM.
	protected int nIter = 1;

	// The model of functions.
	protected FloatNGram<Function> ngram;

	// Constructor.
    public UnsupervisedAnalyser(int n) {
        this.N = n;
    }

	// Set number of iterations.
	public void setIterations(int nIter) {
		this.nIter = nIter;
	}

    public void train(List<EgyptianOrtho> trainingCorpus) {
		ngram = new FloatNGram<Function>(N, new FunctionStart(), new FunctionEnd());
        for (EgyptianOrtho resource : trainingCorpus) {
            OrthoManipulator manipulator = new OrthoManipulator(resource, 0, context);
            train(manipulator);
        }
		ngram.estimateUniform();
		for (int i = 0; i < nIter; i++) {
			System.out.println("Iteration " + i);
			for (EgyptianOrtho resource : trainingCorpus) {
				OrthoManipulator manipulator = new OrthoManipulator(resource, 0, context);
				train(manipulator);
			}
		}
    }
    private void train(OrthoManipulator manip) {
        // for (int i = 0; i < manip.nSegments(); i++) {
        for (int i = 8; i < 9; i++) {
            String word = manip.trans(i);
            String hi = manip.hiero(i);
			System.out.println("==================================================================");
			System.out.println(word);
            ResFragment hiParsed = ResFragment.parse(hi, context);
            hiParsed = (new NormalizerMnemonics()).normalize(hiParsed);
            String[] hiero = ArrayAux.toStringArray(hiParsed.glyphNames());
			for (int j = 0; j < hiero.length; j++)
				System.out.println(hiero[j]);
			ConfigAutomaton aut = makeAutomaton(hiero, new TransLow(word));
			// aut.print();
			count(aut.getFsa());
        }
    }

	private void count(Fsa<ComplexConfig,List<Function>> fsa) {
		FsaExpectation<ComplexConfig,List<Function>> exp = new FsaExpectation(fsa);
		for (FsaTrans<ComplexConfig,List<Function>> trans : exp.transCounts()) {
			Function[] history = trans.fromState().getHistory();
			List<Function> funs = trans.label();
			List<Function> lastFuns = append(history, funs);
			System.out.println("LAST FUNS");
			for (int i = 0; i < lastFuns.size(); i++) {
				System.out.println(lastFuns.get(i));
			}
			double f = NegLogProb.from(trans.weight());
			for (int i = 0; i < funs.size(); i++) {
				int diff = lastFuns.size() - funs.size();
				List<Function> seq = lastFuns.subList(diff + i - (N-1), diff + i);
				ngram.addGrams(seq, f);
			}
			if (fsa.getFinalStates().contains(trans.toState())) {
				List<Function> finalFuns = extendFinal(lastFuns);
				ngram.addGrams(finalFuns, f);
			}
			if (f > 0)
				System.out.println(f);
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
		return new ConfigAutomatonFloatNGram(hiero, word, ngram);
	}

	private List<Function> append(Function[] history, List<Function> funs) {
		List<Function> newFuns = new LinkedList<Function>();
		for (int i = 0; i < N-1 - history.length; i++)
			newFuns.add(new FunctionStart());
		for (int i = 0; i < history.length; i++)
			newFuns.add(history[i]);
		newFuns.addAll(funs);
		return newFuns;
	}

	private List<Function> extendFinal(List<Function> funs) {
		List<Function> newFuns = new LinkedList<Function>(funs);
		newFuns.add(new FunctionEnd());
		return newFuns.subList(newFuns.size()-N, newFuns.size());
	}

}
