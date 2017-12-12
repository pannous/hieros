package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.interlinear.egyptian.ortho.*;

// For turning hieroglyphic, orthographic elements, and transliteration into 
// linear sequence of functions, including jumps.
public class Linearizer {

    // Input values.
    // Hieroglyphic.
    private String[] hi;
    // Orthographic elements.
    private Vector<OrthoElem> orthos;
    // Transliteration.
    private TransLow al;

    // Intermediate results.
    // Mapping from positions to orthographic elements.
    private Map<Integer,OrthoElem> hiOrdered = new TreeMap<Integer,OrthoElem>();
    // Positions that contain 'w' for feminine plural, indicated by 
    // mult or typ for plural.
    private TreeSet<Integer> femPlurPoss = new TreeSet<Integer>();

    // Output value.
    // The linear list of functions.
    private List<Function> functions = new LinkedList<Function>();
    // Did the process fail somewhere?
    private boolean failure = false;

    public Linearizer(String[] hi, Vector<OrthoElem> orthos, TransLow al) {
	this.hi = hi;
	this.orthos = orthos;
	this.al = al;
	mapHiToOrtho();
	findFemPlurPoss();
	try {
	    linearize();
	} catch (LinearizeException e) {
	    failure = true;
	    for (String sign : hi)
		System.err.println(sign);
	    for (OrthoElem ortho : orthos)
		System.err.print(ortho);
	    System.err.println(al);
	    System.err.println(e.getMessage());
	    System.err.println("-----");
	}
    }

    // Output value.
    public List<Function> getFunctions() {
	return functions;
    }
    public boolean isFailure() {
	return failure;
    }

    // Create mapping from positions to corresponding orthographic element.
    // Create dummy element for positions that are not covered.
    private void mapHiToOrtho() {
	TreeSet<Integer> covered = new TreeSet<Integer>();
	for (OrthoElem ortho : orthos) {
	    TreeSet<Integer> signPoss = ortho.signSet();
	    covered.addAll(signPoss);
	    int first = signPoss.first();
	    hiOrdered.put(first, ortho);
	}
	for (int i = 0; i < hi.length; i++) 
	    if (!covered.contains(i)) {
		OrthoSpurious spurious = new OrthoSpurious();
		spurious.addSign(i);
		hiOrdered.put(i, spurious);
	    }
    }

    // Find positions that contain 'w' for feminine plural, indicated by
    // mult or typ for plural.
    private void findFemPlurPoss() {
	for (OrthoElem ortho : orthos) 
	    if (ortho instanceof OrthoMult && !ortho.argValue().equals("2") ||
		ortho instanceof OrthoTyp && 
		    	ortho.argValue().equals("plurality or collectivity")) {
		TreeSet<Integer> signPoss = ortho.letterSet();
		if (signPoss.size() != 1)
		    continue;
		int i = signPoss.first();
		if (i < 0 || i+1 >= al.length())
		    continue;
		if (al.charAt(i) == 'w' && al.charAt(i+1) == 't')
		    femPlurPoss.add(i);
	    }
    }

    // Go over all orthographic elements.
    private void linearize() throws LinearizeException {
	SimpleConfig config = new SimpleConfig();
	config.setTarget(al);
	for (int i = 0; i < hi.length; i++) {
	    OrthoElem ortho = hiOrdered.get(i);
	    if (ortho != null)
		config = linearize(ortho, config);
	}
	if (config.femPlur())
	    throw new LinearizeException("femplur");
	if (config.getPos() < al.length()) {
	    FunctionJump jump = new FunctionJump(al.length() - config.getPos());
	    functions.add(jump);
	}
    }

    // Go over one orthographic element. Update configuration.
    private SimpleConfig linearize(OrthoElem ortho, SimpleConfig config) 
		throws LinearizeException {
	String[] signs = signs(hi, ortho);
	TransLow trans = letters(ortho);
	if (ortho instanceof OrthoDet) {
	    OrthoDet det = (OrthoDet) ortho;
	    String descr = det.argValue();
	    FunctionDetDescr function = new FunctionDetDescr(signs, descr);
	    return doFunction(config, function);
	} else if (ortho instanceof OrthoDetWord) {
	    OrthoDetWord det = (OrthoDetWord) ortho;
	    TransMdc lemma = new TransMdc(det.argValue());
	    boolean femPlur = SignTable.isFemPlur(trans, lemma);
	    FunctionDetWord function = new FunctionDetWord(signs, lemma, trans, femPlur);
	    return doEpsPhonFunction(ortho, config, function);
	} else if (ortho instanceof OrthoLog) {
	    OrthoLog log = (OrthoLog) ortho;
	    TransMdc lemma = new TransMdc(log.argValue());
	    boolean femPlur = SignTable.isFemPlur(trans, lemma);
	    FunctionLog function = new FunctionLog(signs, lemma, trans, femPlur);
	    return doEpsPhonJumpFunction(ortho, config, function);
	} else if (ortho instanceof OrthoMult) {
	    OrthoMult mult = (OrthoMult) ortho;
	    int n = 0;
	    try {
		n = Integer.parseInt(mult.argValue());
	    } catch (NumberFormatException e) {
		throw new LinearizeException("Number format " + mult.argValue());
	    }
	    if (config.femPlur()) {
		FunctionMultFemPlur function = new FunctionMultFemPlur(signs, n);
		return doFunction(config, function);
	    } else if (trans.length() == 0) {
		FunctionMultSem function = new FunctionMultSem(signs, n);
		return doFunction(config, function);
	    } else {
		FunctionMultSuffix function = new FunctionMultSuffix(signs, n, trans);
		return doEpsPhonJumpFunction(ortho, config, function);
	    }
	} else if (ortho instanceof OrthoPhondet) {
	    OrthoPhondet phondet = (OrthoPhondet) ortho;
	    TransLow hist = new TransLow(phondet.argValue());
	    boolean soundChange = !hist.equals(trans);
	    FunctionPhondet function = new FunctionPhondet(signs, hist, trans, soundChange);
	    return doEpsPhonJumpEndFunction(ortho, config, function);
	} else if (ortho instanceof OrthoPhon) {
	    OrthoPhon phon = (OrthoPhon) ortho;
	    TransLow hist = new TransLow(phon.argValue());
	    boolean soundChange = false;
	    boolean femPlur = false;
	    if (SignTable.isFemPlurEnding(trans, hist))
		femPlur = true;
	    else if (!hist.equals(trans))
		soundChange = true;
	    FunctionPhon function = new FunctionPhon(signs, hist, trans, soundChange, femPlur);
	    return doEpsPhonFemPlurJumpFunction(ortho, config, function);
	} else if (ortho instanceof OrthoSpurious) {
	    OrthoSpurious spurious = (OrthoSpurious) ortho;
	    FunctionSpurious function = new FunctionSpurious(signs);
	    return doFunction(config, function);
	} else if (ortho instanceof OrthoTyp) {
	    OrthoTyp typ = (OrthoTyp) ortho;
	    String descr = typ.argValue();
	    if (descr.equals("number")) {
		FunctionNum function = new FunctionNum(signs, trans);
		return doEpsPhonJumpFunction(ortho, config, function);
	    } else if (descr.equals("duality")) {
		if (trans.length() == 0) {
		    FunctionTypSem function = new FunctionTypSem(signs, descr);
		    return doFunction(config, function);
		} else {
		    FunctionTypSuffix function = new FunctionTypSuffix(signs, descr, trans);
		    return doEpsPhonJumpFunction(ortho, config, function);
		}
	    } else if (descr.equals("plurality or collectivity")) {
		if (config.femPlur()) {
		    FunctionTypFemPlur function = new FunctionTypFemPlur(signs, descr);
		    return doFunction(config, function);
		} else if (trans.length() == 0) {
		    FunctionTypSem function = new FunctionTypSem(signs, descr);
		    return doFunction(config, function);
		} else {
		    FunctionTypSuffix function = new FunctionTypSuffix(signs, descr, trans);
		    return doEpsPhonJumpFunction(ortho, config, function);
		}
	    } else if (descr.equals("repetition of the preceding sequence of consonants")) {
		int size = trans.length();
		FunctionTypSpSn function = new FunctionTypSpSn(signs, descr, size);
		return doEpsPhonJumpFunction(ortho, config, function);
	    } else {
		FunctionTypSem function = new FunctionTypSem(signs, descr);
		return doFunction(config, function);
	    }
	} 
	throw new LinearizeException("Unexpected OrthoElem");
    }

    // Do function. 
    private SimpleConfig doFunction(SimpleConfig config, Function function) 
    		throws LinearizeException {
	if (function.applicable(config)) {
	    functions.add(function);
	    return function.apply(config);
	} else 
	    throw new LinearizeException("doFunction");
    }

    // Do epsilon phonograms, preceding detword.
    private SimpleConfig doEpsPhonFunction(OrthoElem ortho, SimpleConfig config, Function function)
		throws LinearizeException {
	if (lettersBegin(ortho) < 0)
	    throw new LinearizeException("missing letters in doEpsPhonFunction");
	else {
	    config = doEpsPhon(config, lettersEnd(ortho)+1);
	    return doFunction(config, function);
	}
    }

    // Do epsilon phonograms or jump, preceding scanning function.
    private SimpleConfig doEpsPhonJumpFunction(OrthoElem ortho, SimpleConfig config, Function function) 
    		throws LinearizeException {
	if (lettersBegin(ortho) < 0)
	    throw new LinearizeException("missing letters in doEpsPhonJumpFunction:" + function);
	else {
	    config = doEpsPhon(config, lettersBegin(ortho));
	    return doJumpFunction(ortho, config, function);
	}
    }
    // Do epsilon phonograms or jump, preceding function that covers past letters.
    private SimpleConfig doEpsPhonJumpEndFunction(OrthoElem ortho, SimpleConfig config, Function function) 
    		throws LinearizeException {
	if (lettersBegin(ortho) < 0)
	    throw new LinearizeException("missing letters in doEpsPhonEndJumpFunction:" + function);
	else {
	    config = doEpsPhon(config, lettersEnd(ortho)+1);
	    return doJumpEndFunction(ortho, config, function);
	}
    }


    // Do epsilon phonograms or jump, preceding phonogram.
    // May involve feminine plural 'w'.
    private SimpleConfig doEpsPhonFemPlurJumpFunction(OrthoElem ortho, SimpleConfig config, FunctionPhon function) 
    		throws LinearizeException {
	if (lettersBegin(ortho) < 0)
	    return config;
	else 
	    return doEpsPhonFemPlur(ortho, config, function);
    }

    // Do jump and function.
    private SimpleConfig doJumpFunction(OrthoElem ortho, SimpleConfig config, Function function) 
		throws LinearizeException {
	if (function.applicable(config)) {
	    functions.add(function);
	    return function.apply(config);
	} else {
	    int dist = lettersBegin(ortho) - config.getPos();
	    if (function.jumpApplicable(config).contains(dist)) {
		FunctionJump jump = new FunctionJump(dist);
		functions.add(jump);
		functions.add(function);
		return function.apply(jump.apply(config));
	    } else  
		throw new LinearizeException("doJumpFunction: " + function);
	} 
    }
    // Do jump and function.
    private SimpleConfig doJumpEndFunction(OrthoElem ortho, SimpleConfig config, Function function) 
		throws LinearizeException {
	if (function.applicable(config)) {
	    functions.add(function);
	    return function.apply(config);
	} else {
	    int dist = lettersEnd(ortho)+1 - config.getPos();
	    if (function.jumpApplicable(config).contains(dist)) {
		FunctionJump jump = new FunctionJump(dist);
		functions.add(jump);
		functions.add(function);
		return function.apply(jump.apply(config));
	    } else 
		throw new LinearizeException("doJumpEndFunction: " + function);
	} 
    }

    // Do epsilon phonograms to reach position to right.
    private SimpleConfig doEpsPhon(SimpleConfig config, int pos) {
	while (pos > config.getTrans().length() && 
			config.getTrans().length() < al.length()) {
	    int dist = config.getTrans().length() - config.getPos();
	    if (dist > 0) {
		FunctionJump jump = new FunctionJump(dist);
		functions.add(jump);
		config = jump.apply(config);
	    }
	    int next = config.getTrans().length();
	    FunctionEpsPhon eps = new FunctionEpsPhon(al.letter(next));
	    functions.add(eps);
	    config = eps.apply(config);
	}
	return config;
    }

    // Do epsilon phonograms to reach position to right.
    // If we are to read 't', check whether 'w' is to be produced.
    private SimpleConfig doEpsPhonFemPlur(OrthoElem ortho, SimpleConfig config, FunctionPhon function) 
    		throws LinearizeException {
	while (lettersBegin(ortho) > config.getTrans().length() &&
			config.getTrans().length() < al.length()) {
	    int next = config.getTrans().length();
	    if (next + 1 == lettersBegin(ortho) &&
			function.getTrans().equals(new TransLow("t")) &&
			al.charAt(next) == 'w' &&
			femPlurPoss.contains(next)) {
		int dist = config.getTrans().length() - config.getPos();
		if (dist > 0) {
		    FunctionJump jump = new FunctionJump(dist);
		    functions.add(jump);
		    config = jump.apply(config);
		}
		function.addFemPlur();
		return function.apply(config);
	    } else {
		int dist = config.getTrans().length() - config.getPos();
		if (dist > 0) {
		    FunctionJump jump = new FunctionJump(dist);
		    functions.add(jump);
		    config = jump.apply(config);
		}
		next = config.getTrans().length();
		FunctionEpsPhon eps = new FunctionEpsPhon(al.letter(next));
		functions.add(eps);
		config = eps.apply(config);
	    }
	}
	return doJumpFunction(ortho, config, function);
    }

    // The signs of the element.
    private String[] signs(String[] hi, OrthoElem ortho) {
	int n = 0;
	for (int i : ortho.signSet())
	    if (i < hi.length)
		n++;
	String[] signs = new String[n];
	n = 0;
	for (int i : ortho.signSet())
	    if (i < hi.length)
		signs[n++] = hi[i];
	return signs;
    }

    // The letters of the element. Fill gaps.
    private TransLow letters(OrthoElem ortho) {
	TreeSet<Integer> poss = ortho.letterSet();
	if (poss.isEmpty() || poss.last() >= al.length())
	    return new TransLow("");
	else
	    return al.substring(poss.first(), poss.last()+1);
    }

    // Lowest index of the letters. Or -1 if none.
    private int lettersBegin(OrthoElem ortho) {
	TreeSet<Integer> poss = ortho.letterSet();
	if (poss.isEmpty())
	    return -1;
	else
	    return poss.first();
    }

    // Highest index of the letters. Or -1 if none.
    private int lettersEnd(OrthoElem ortho) {
	TreeSet<Integer> poss = ortho.letterSet();
	if (poss.isEmpty())
	    return -1;
	else
	    return poss.last();
    }

    // Exception class for failure.
    private class LinearizeException extends Exception {
	public LinearizeException(String message) {
	    super(message);
	}
    }

}
