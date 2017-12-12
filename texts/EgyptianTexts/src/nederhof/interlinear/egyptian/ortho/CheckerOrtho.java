package nederhof.interlinear.egyptian.ortho;

import java.io.*;
import java.util.*;

import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.lexicon.egyptian.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.operations.*;
import nederhof.util.*;

// Check consistency of orthographic annotation.
// Also allow database of annotations to be manipulated.
public class CheckerOrtho {

    // Contexts.
    protected HieroRenderContext hieroContext;
    protected ParsingContext parsingContext;

    // Various tools.
    protected AnnotationSuggestor suggestor;
    protected ResComposer composer;

    // Mapping from hieroglyphic and transliteration (separated by space)
    // to orthographic annotations.
    protected HashMap<OrthoKey, Vector<OrthoRecord>> annotations =
	new HashMap<OrthoKey, Vector<OrthoRecord>>();

    // Key consisting of hieroglyphic and transliteration.
    protected class OrthoKey {
	// The hieroglyphic.
	public String hi;
	// The transliteration.
	public String al;
	// Constructor.
	public OrthoKey(String hi, String al) {
	    this.hi = hi;
	    this.al = al;
	}
	// Equality if both fields are the same.
	public boolean equals(Object o) {
	    if (!(o instanceof OrthoKey))
		return false;
	    else {
		OrthoKey other = (OrthoKey) o;
		return hi.equals(other.hi) && al.equals(other.al);
	    }
	}
	// Hashcode derived from both strings together.
	public int hashCode() {
	    return (hi + " " + al).hashCode();
	}
    }

    // Comparator of keys, with transliteration first, according to
    // lexicographical ordering.
    private class KeyComparator implements Comparator<OrthoKey> {
	private TranslitComparator translitComp = new TranslitComparator();
	public int compare(OrthoKey k1, OrthoKey k2) {
	    if (translitComp.compare(k1.al, k2.al) != 0)
		return translitComp.compare(k1.al, k2.al);
	    else
		return k1.hi.compareTo(k2.hi);
	}
	// Unused.
	public boolean equals(Object o) {
	    return false;
	}
    }

    // Represents one annotation.
    protected class OrthoRecord {
	// The hieroglyphic.
	public String hi;
	// The transliteration.
	public String al;
	// List of functions.
	public Vector<OrthoElem> orthos = new Vector<OrthoElem>();

	// Constructor.
	public OrthoRecord(String hi, String al, Vector<OrthoElem> orthos) {
	    this.hi = hi;
	    this.al = al;
	    for (OrthoElem ortho : orthos)
		this.orthos.add(OrthoElem.copyOrtho(ortho));
	}
    }

    // Create checker.
    public CheckerOrtho() {
	hieroContext =
	            new HieroRenderContext(Settings.textHieroFontSize, true);
	parsingContext =
	            new ParsingContext(hieroContext, true);
	try {
	    suggestor = new AnnotationSuggestor();
	    suggestor.resetCounts();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
	composer = new ResComposer();
	composer.normalMnemonics = true;
    }

    // Check the resource.
    public void check(EgyptianOrtho resource) {
	OrthoManipulator manipulator = 
	    new OrthoManipulator(resource, 0, parsingContext);
	for (int i = 0; i < manipulator.nSegments(); i++) {
	    String hi = manipulator.hiero(i);
	    Vector<OrthoElem> orthos = manipulator.orthos(i);
	    String al = manipulator.trans(i);
	    ResFragment hiParsed = ResFragment.parse(hi, parsingContext);
	    hiParsed = (new NormalizerMnemonics()).normalize(hiParsed);
	    Vector<String> hiNames = hiParsed.glyphNames();
	    for (OrthoElem ortho : orthos) 
		checkElem(ortho, hiNames, al, suggestor);
	    recordAnnotation(hi, al, orthos);
	}
    }
    // Get function.
    public static void checkElem(OrthoElem ortho, Vector<String> hiNames, String al,
	    AnnotationSuggestor suggestor) {
	String name = ortho.name();
	String val = ortho.argValue();
	int[] signs = ortho.signs();
	Vector<String> names = getRes(hiNames, signs);
	String jointNames = ResComposer.lazyComposeNames(names);
	ArrayList<AnnotationSuggestion> readings = suggestor.getReadings(jointNames);

	if (readingMatch(name, val, readings) || 
		name.equals("typ") && isNum(names) || 
		name.equals("mult") && isMult(names))
	    ; // System.out.println(hiero + " " + name + " " + val);
	else {
	    System.out.println("*** " + jointNames + " " + name + " " + val);
	    if (readings.size() > 0) {
		System.out.println("  EXPECTED");
		for (AnnotationSuggestion reading : readings) 
		    System.out.println("  --> " + reading.toShortString());
	    }
	}
    }
    public static boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al,
		AnnotationSuggestor suggestor) {
	String name = ortho.name();
	String val = ortho.argValue();
	int[] signs = ortho.signs();
	Vector<String> names = getRes(hiNames, signs);
	String jointNames = ResComposer.lazyComposeNames(names);
	ArrayList<AnnotationSuggestion> readings = suggestor.getReadings(jointNames);
	return (readingMatch(name, val, readings) || 
		name.equals("typ") && isNum(names) || 
		name.equals("mult") && isMult(names));
    }

    // Count the functions in the corpus.
    public void count(Corpus corpus) {
	EgyptianOrthoGenerator gen = new EgyptianOrthoGenerator();
	Vector<ResourceGenerator> gens = new Vector<ResourceGenerator>();
	gens.add(gen);
	for (Text text : corpus.getTexts()) {
	    for (String loc : text.getResources()) {
		TextResource resource = IndexPane.toResource(loc, gens);
		if (resource != null && resource instanceof EgyptianOrtho) {
		    count((EgyptianOrtho) resource);
		}
	    }
	}
    }

    // Count the functions in the resource.
    public void count(EgyptianOrtho resource) {
	OrthoManipulator manipulator = 
	    new OrthoManipulator(resource, 0, parsingContext);
	for (int i = 0; i < manipulator.nSegments(); i++) {
	    String hi = manipulator.hiero(i);
	    Vector<OrthoElem> orthos = manipulator.orthos(i);
	    ResFragment hiParsed = ResFragment.parse(hi, parsingContext);
	    for (OrthoElem ortho : orthos) {
		int[] signs = ortho.signs();
		String hiero = getRes(hiParsed, signs);
		ArrayList<AnnotationSuggestion> readings = suggestor.getReadings(hiero);
		String name = ortho.name();
		String val = ortho.argValue();
		countMatch(name, val, readings);
	    }
	}
    }

    // Record annotation, as long as it is not yet present.
    private void recordAnnotation(String hi, String al, Vector<OrthoElem> orthos) {
	OrthoKey key = new OrthoKey(hi, al);
	if (annotations.get(key) == null)
	    annotations.put(key, new Vector<OrthoRecord>());
	for (OrthoRecord record : annotations.get(key)) 
	    if (record.orthos.size() == orthos.size()) {
		boolean same = true;
		for (int i = 0; i < record.orthos.size(); i++) 
		    if (!record.orthos.get(i).equalValues(orthos.get(i))) {
			same = false;
			break;
		    }
		if (same)
		    return;
	    }
	annotations.get(key).add(new OrthoRecord(hi, al, orthos));
    }

    // Output annotations.
    private void writeAnnotations() {
	List<OrthoKey> keys = new LinkedList<OrthoKey>(annotations.keySet());
	Collections.sort(keys, new KeyComparator());
	for (OrthoKey key : keys) {
	    Vector<OrthoRecord> records = annotations.get(key);
	    if (records.size() < 2)
		continue;
	    System.out.println(key.al + " " + key.hi);
	    write(records.get(0));
	    for (int i = 1; i < records.size(); i++) {
		System.out.println("************ OR");
		write(records.get(i));
	    }
	}
    }

    // Output one record.
    private void write(OrthoRecord rec) {
	Vector<OrthoElem> orthos = rec.orthos;
	for (int i = 0; i < orthos.size(); i++) {
	    OrthoElem ortho = orthos.get(i);
	    System.out.print(ortho.name() + " " + ortho.argName() + "=" + ortho.argValue());
	    if (i < orthos.size()-1)
		System.out.print(",");
	}
	System.out.println();
    }

    public void writeCounts() {
	try {
	    suggestor.write();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

    // Get RES related to orthographic element.
    private String getRes(ResFragment hiParsed, int[] signs) {
	Vector<String> hiNames = hiParsed.glyphNames();
	Vector<String> glyphs = new Vector<String>();
	if (signs != null)
	    for (int i = 0; i < signs.length; i++)
		if (signs[i] < hiNames.size())
		    glyphs.add(hiNames.get(signs[i]));
	return composer.normalize(composer.composeNames(glyphs)).toString();
    }
    // As above, but for vector of normalized names.
    private static Vector<String> getRes(Vector<String> hiNames, int[] signs) {
	Vector<String> glyphs = new Vector<String>();
	if (signs != null)
	    for (int i = 0; i < signs.length; i++)
		if (signs[i] < hiNames.size())
		    glyphs.add(hiNames.get(signs[i]));
	return glyphs;
    }

    // Is reading matched by some in list?
    private static boolean readingMatch(String name, String val,
	    ArrayList<AnnotationSuggestion> readings) {
	for (AnnotationSuggestion reading : readings) 
	    if (reading.fun.equals(name) && reading.val.equals(val))
		return true;
	return false;
    }

    // Increase count of matching reading.
    private void countMatch(String name, String val,
	    ArrayList<AnnotationSuggestion> readings) {
	for (AnnotationSuggestion reading : readings) 
	    if (reading.fun.equals(name) && reading.val.equals(val)) 
		reading.count++;
    }

    // Does hiero consist of numerals only.
    private static boolean isNum(Vector<String> names) {
	for (String name : names)
	    if (!name.equals("Z15") && // 1
		    !name.equals("V20") && // 10
		    !name.equals("Z16") && // 10
		    !name.equals("V40") && // 10
		    !name.equals("V1") && // 100
		    !name.equals("M12") && // 1000
		    !name.equals("D50") && // 10000
		    !name.equals("I8")) // 100000
		return false;
	return true;
    }

    // Is hieroglyphic a repeated sign?
    private static boolean isMult(Vector<String> names) {
	return (new TreeSet<String>(names)).size() == 1;
    }

    // For testing.
    public static void main(String[] args) {
	if (args.length == 0) {
	    try {
		CheckerOrtho checker = new CheckerOrtho();
		EgyptianOrtho resource = new EgyptianOrtho("corpus/resources/ShipwreckedOrtho.xml");
		checker.check(resource);
		checker.writeAnnotations();
		/*
		Corpus corpus = new Corpus("corpus/corpus.xml");
		checker.count(corpus);
		checker.writeCounts();
		*/
	    } catch (IOException e) {
		System.err.println(e.getMessage());
	    }
	}
    }
}
