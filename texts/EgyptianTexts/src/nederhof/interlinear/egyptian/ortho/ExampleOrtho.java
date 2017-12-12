package nederhof.interlinear.egyptian.ortho;

import java.io.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.lexicon.egyptian.*;

// Retrieving orthographic annotations from existing resources.
public class ExampleOrtho {

    // Always for EgyptianOrtho.
    private static final int nTiers = 1;

    // Orthographic annotation with count.
    private static class CountedOrtho {
	public String hi;
	public Vector<OrthoElem> orthos;
	public int n = 1;
	public CountedOrtho(String hi, Vector<OrthoElem> orthos) {
	    this.hi = hi;
	    this.orthos = OrthoElem.copyOrtho(orthos);
	}
	public void incr() {
	    n++;
	}
	public void decr() {
	    n--;
	}
	public boolean equalValues(String hi, Vector<OrthoElem> orthos) {
	    if (hi == null && this.hi != null)
		return false;
	    else if (hi != null && this.hi == null)
		return false;
	    else if (hi != null && !hi.equals(this.hi))
		return false;
	    else if (orthos.size() != this.orthos.size())
		return false;
	    int nMatch = 0;
	    for (OrthoElem ortho : orthos) {
		for (OrthoElem old : this.orthos) 
		    if (ortho.equalValues(old)) {
			nMatch++;
			break;
		    }
	    }
	    return nMatch == orthos.size();
	}
    }

    // Load with existing database of annotations.
    public ExampleOrtho(String path) {
	this();
	try {
	    addResource(path);
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	}
    }

    // Create empty database.
    public ExampleOrtho() {
    }

    // Add annotations from resource.
    public void addResource(String path) throws IOException {
	EgyptianOrtho database = new EgyptianOrtho(path);
	for (int i = 0; i < database.nPhrases(); i++) {
	     TextPhrase phrase = database.getPhrase(i);
	     Vector tier = phrase.getTier(nTiers - 1);
	     if (tier.size() != 0) {
		 OrthoPart ortho = (OrthoPart) tier.get(0);
		 if (ortho != null)
		     add(ortho.textal, ortho.texthi, ortho.textortho);
	     }
	}
    }

    // Save annotations.
    public void save(String path) throws IOException {
	EgyptianOrtho database = new EgyptianOrtho(path);
	database.clearPhrases();
	List<String> als = new LinkedList<String>(alToOrtho.keySet());
	Collections.sort(als, new TranslitComparator());
	int id = 0;
	for (String al : als) 
	    for (CountedOrtho counted : alToOrtho.get(al)) {
		String hi = counted.hi;
		Vector<OrthoElem> orthos = counted.orthos;
		String idLab = "" + (id++);
		Vector tier = new Vector();
		tier.add(new OrthoPart(hi, al, orthos, idLab));
		Vector[] tiers = new Vector[nTiers];
		tiers[nTiers - 1] = tier;
		database.addPhrase(new TextPhrase(database, tiers));
	    }
	database.save();
    }

    // Mapping from transliteration to list of orthographic annotations.
    private TreeMap<String,LinkedList<CountedOrtho>> alToOrtho = new
	TreeMap<String,LinkedList<CountedOrtho>>();

    // Add, if not already there.
    public void add(String al, String hi, Vector<OrthoElem> orthos) {
	if (orthos.size() == 0 || isNumber(al, hi, orthos))
	    return;
	if (alToOrtho.get(al) == null)
	    alToOrtho.put(al, new LinkedList<CountedOrtho>());
	LinkedList<CountedOrtho> old = alToOrtho.get(al);
	for (CountedOrtho counted : old) 
	    if (counted.equalValues(hi, orthos)) {
		counted.incr();
		return;
	    }
	old.add(new CountedOrtho(hi, orthos));
    }

    // Remove.
    public void remove(String al, String hi, Vector<OrthoElem> orthos) {
	LinkedList<CountedOrtho> old = alToOrtho.get(al);
	if (old == null) 
	    return;
	for (CountedOrtho counted : old) 
	    if (counted.equalValues(hi, orthos)) {
		counted.decr();
		if (counted.n == 0)
		    old.remove(counted);
		return;
	    }
    }

    // Return candidates.
    public LinkedList<Vector<OrthoElem>> retrieve(String al, String hi) {
	LinkedList<CountedOrtho> old = alToOrtho.get(al);
	if (old == null) 
	    old = new LinkedList<CountedOrtho>();
	LinkedList<Vector<OrthoElem>> olds = new LinkedList<Vector<OrthoElem>>();
	for (CountedOrtho counted : old)
	    if (counted.hi.equals(hi) && counted.orthos.size() > 0) 
		olds.add(OrthoElem.copyOrtho(counted.orthos));
	if (olds.size() > 0)
	    return olds;
	for (CountedOrtho counted : old) {
	    Vector<OrthoElem> extraction = extractFunctions(al, hi, counted.hi, counted.orthos);
	    if (extraction.size() > 0)
		olds.add(extraction);
	}
	if (olds.size() > 0)
	    return olds;
	if (isNumberAl(al) && isNumberHi(hi)) {
	    Vector<OrthoElem> numberAnnotation = new Vector<OrthoElem>();
	    OrthoElem numberOrtho = makeNumberOrtho(al, hi);
	    numberAnnotation.add(numberOrtho);
	    olds.add(numberAnnotation);
	}
	return olds;
    }

    // Find similar functions that would fit the al/hi pair.
    private Vector<OrthoElem> extractFunctions(String al, String hi, String oldHi, 
	    Vector<OrthoElem> oldOrthos) {
	Vector<OrthoElem> elems = new Vector<OrthoElem>();
	String[] names = hi.split("-");
	String[] oldNames = oldHi.split("-");
	Set<Integer> forbidden = new TreeSet<Integer>();
	for (OrthoElem elem : oldOrthos) {
	    int[] signs = elem.signs();
	    Vector<String> signNames = new Vector<String>();
	    for (int i = 0; i < signs.length; i++) {
		if (signs[i] < oldNames.length)
		    signNames.add(oldNames[signs[i]]);
	    }
	    TreeSet<Integer> subseq = subsequence(signNames, names, forbidden);
	    if (subseq != null) {
		OrthoElem newElem = OrthoElem.makeOrtho(elem.name(),
			elem.argName(), elem.argValue(),
			subseq, elem.letterSet());
		elems.add(newElem);
		forbidden.addAll(subseq);
	    }
	}
	return elems;
    }

    // Is it number?
    public static boolean isNumber(String al, String hi, Vector<OrthoElem> orthos) {
	return isNumberAl(al) && isNumberHi(hi) && isNumber(orthos);
    }
    public static boolean isNumberAl(String al) {
	return al.matches("^[0-9]+$");
    }
    public static boolean isNumberHi(String hi) {
	String[] names = hi.split("-");
	for (int i = 0; i < names.length; i++) 
	    if (!isNum(names[i]))
		return false;
	return true;
    }
    public static boolean isNum(String name) {
	return 
	    name.equals("Z15") || // 1
	    name.equals("Z16") || // 1
	    name.equals("V20") || // 10
	    name.equals("V40") || // 10
	    name.equals("V1") || // 100
	    name.equals("M12") || // 1000
	    name.equals("D50") || // 10000
	    name.equals("I8"); // 100000
    }
    public static boolean isNumber(Vector<OrthoElem> orthos) {
	if (orthos.size() != 1)
	    return false;
	OrthoElem elem = orthos.get(0);
	return elem.name().equals("typ") && elem.argValue().equals("number");
    }

    public static OrthoElem makeNumberOrtho(String al, String hi) {
	String[] names = hi.split("-");
	TreeSet<Integer> signs = new TreeSet<Integer>();
	TreeSet<Integer> letters = new TreeSet<Integer>();
	for (int i = 0; i < names.length; i++)
	    signs.add(i);
	for (int i = 0; i < al.length(); i++)
	    letters.add(i);
	return OrthoElem.makeOrtho("typ", "descr", "number", signs, letters);
    }

    // Find sequence of sign positions for sign names. Or null if no
    // subsequence is found.
    private static TreeSet<Integer> subsequence(Vector<String> signNames, String[] names,
	    Set<Integer> forbidden) {
	TreeSet<Integer> positions = new TreeSet<Integer>();
	int firstPos = 0;
	for (String sign : signNames) {
	    int pos = posFrom(sign, firstPos, names, forbidden);
	    if (pos < 0)
		return null;
	    else {
		positions.add(pos);
		firstPos = pos + 1;
	    }
	}
	return positions;
    }

    // Find position of sign from position onwards. But ignore forbidden
    // positions.
    private static int posFrom(String sign, int firstPos, String[] names, Set<Integer> forbidden) {
	for (int i = firstPos; i < names.length; i++) 
	    if (!forbidden.contains(i) && names[i].equals(sign))
		return i;
	return -1;
    }

    // Create new database from resources.
    public static void main(String[] args) {
	ExampleOrtho example = new ExampleOrtho();
	try {
	    example.addResource("corpus/resources/ShipwreckedOrtho.xml");
	    example.save("data/ortho/OrthoAnnotations.xml");
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	}
    }

}
