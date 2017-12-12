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

// Determines most likely functions by unigrams, not considering any context.
public class BaselineAnalyser {

    // Parsing context.
    protected ParsingContext context = new ParsingContext();

    // Unigram model.
    protected NGram<Function> ngram;

    public void train(List<EgyptianOrtho> trainingCorpus) {
        ngram = new NGram<Function>(1, new FunctionStart(), new FunctionEnd());
        for (EgyptianOrtho resource : trainingCorpus) {
            OrthoManipulator manipulator = new OrthoManipulator(resource, 0, context);
            train(manipulator);
        }
        ngram.estimate();
	// ngram.printNGram();
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
            ngram.addGrams(functions);
        }
    }

    public List<Function> analyse(String[] hiero) {
	BaselineAutomaton aut = new BaselineAutomaton(hiero, ngram);
	return aut.success() ? aut.getBest() : null;
    }

    public void printAutomaton(String[] hiero) {
	BaselineAutomaton aut = new BaselineAutomaton(hiero, ngram);
	aut.print();
    }

}
