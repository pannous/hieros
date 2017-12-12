package nederhof.interlinear.egyptian.ortho;

import java.util.*;

// General class of elements of orthographic annotation.

public abstract class OrthoElem {

    // Name of orthographic element.
    public abstract String name();

    // Which argument?
    public abstract String argName();

    // Value of argument, if any.
    public abstract String argValue();

    // Set value.
    public abstract void setValue(String val);

    // Which possible arguments for which name?
    public static Vector<String> argNames(String name) {
	Vector<String> args = new Vector<String>();
	if (name.equals("log")) {
	    args.add("word");
	} else if (name.equals("det")) {
	    args.add("descr");
	    args.add("word");
	} else if (name.equals("phon")) {
	    args.add("lit");
	} else if (name.equals("phondet")) {
	    args.add("lit");
	} else if (name.equals("mult")) {
	    args.add("num");
	} else if (name.equals("typ")) {
	    args.add("descr");
	}
	return args;
    }

    // Empty constructor.
    public OrthoElem() {
    }
    public OrthoElem(int[][] signRanges) {
	addSigns(signRanges);
    }

    // Constructor.
    public OrthoElem(int[][] signRanges, int[][] letterRanges) {
	addSigns(signRanges);
	addLetters(letterRanges);
    }

    // Factory.
    public static OrthoElem makeOrtho(String name, String arg,
	    String val, int[][] signRanges, int[][] letterRanges) {
	if (name.equals("det")) {
	    if (arg.equals("word")) 
		return new OrthoDetWord(val, signRanges, letterRanges);
	    else
		return new OrthoDet(val, signRanges, letterRanges);
	} else if (name.equals("det(word)"))
	    return new OrthoDetWord(val, signRanges, letterRanges);
	else if (name.equals("log"))
	    return new OrthoLog(val, signRanges, letterRanges);
	else if (name.equals("phon"))
	    return new OrthoPhon(val, signRanges, letterRanges);
	else if (name.equals("phondet"))
	    return new OrthoPhondet(val, signRanges, letterRanges);
	else if (name.equals("mult"))
	    return new OrthoMult(val, signRanges, letterRanges);
	else if (name.equals("typ"))
	    return new OrthoTyp(val, signRanges, letterRanges);
	else if (name.equals("spurious"))
	    return new OrthoSpurious(signRanges);
	else
	    return null;
    }

    // Factory.
    public static OrthoElem makeOrtho(String name, String arg,
	    String val, TreeSet<Integer> signs, TreeSet<Integer> letters) {
	return makeOrtho(name, arg, val, ranges(signs), ranges(letters));
    }

    // Copy.
    public static OrthoElem copyOrtho(OrthoElem elem) {
	return makeOrtho(elem.name(), elem.argName(), elem.argValue(),
		elem.signRanges(), elem.letterRanges());
    }
    public static Vector<OrthoElem> copyOrtho(Vector<OrthoElem> elems) {
	Vector<OrthoElem> copy = new Vector<OrthoElem>();
	for (OrthoElem elem : elems) 
	    copy.add(copyOrtho(elem));
	return copy;
    }

    // Has equal values? 
    // (We should keep "equals()" for reference equality.)
    public boolean equalValues(Object o) {
	if (!(o instanceof OrthoElem))
	    return false;
	else {
	    OrthoElem other = (OrthoElem) o;
	    if (!name().equals(other.name()) ||
		    argName() == null && other.argName() != null ||
		    argName() != null && !argName().equals(other.argName()) ||
		    argValue() == null && other.argValue() != null ||
		    argValue() != null && !argValue().equals(other.argValue()))
		return false;
	    else 
	       	return equalSet(signs, other.signs) && equalSet(letters, other.letters);
	}
    }

    // Make extended name (mainly for 'det(word)').
    public static String extendedName(String name, String arg) {
	if (name.equals("det") && arg.equals("al"))
	    return "det(word)";
	else
	    return name;
    }
    // Make extended name.
    public static String extendedName(OrthoElem elem) {
	if (elem instanceof OrthoDetWord) 
	    return "det(word)";
	else
	    return elem.name();
    }

    // Indices of signs this refers to.
    private TreeSet<Integer> signs = new TreeSet<Integer>();

    // Indices of letters this refers to.
    private TreeSet<Integer> letters = new TreeSet<Integer>();

    // Add position and length (possibly empty) of signs.
    public void addSigns(String posStr, String lenStr) {
	add(posStr, lenStr, signs);
    }
    public void addSigns(int pos, int len) {
	add(pos, len, signs);
    }

    // Add position and length (possibly empty) of letters.
    public void addLetters(String posStr, String lenStr) {
	add(posStr, lenStr, letters);
    }
    public void addLetters(int pos, int len) {
	add(pos, len, letters);
    }

    // Add one sign.
    public void addSign(int sign) {
	signs.add(sign);
    }

    // Remove one letter.
    public void removeSign(int sign) {
	signs.remove(sign);
    }

    // Add one letter.
    public void addLetter(int letter) {
	letters.add(letter);
    }

    // Remove one letter.
    public void removeLetter(int letter) {
	letters.remove(letter);
    }

    // Add position and length of signs.
    public void addSigns(int[][] signRanges) {
	add(signRanges, signs);
    }

    // Add position and length of letters.
    public void addLetters(int[][] letterRanges) {
	add(letterRanges, letters);
    }

    // Turn signs into ranges.
    public int[][] signRanges() {
	return ranges(signs);
    }

    // Turn letters into ranges.
    public int[][] letterRanges() {
	return ranges(letters);
    }

    // Return array of signs.
    public int[] signs() {
	return array(signs);
    }
    // Return array of letters.
    public int[] letters() {
	return array(letters);
    }

    // Return set of signs.
    public TreeSet<Integer> signSet() {
	return new TreeSet<Integer>(signs);
    }
    // Return set of letters.
    public TreeSet<Integer> letterSet() {
	return new TreeSet<Integer>(letters);
    }

    ////////////////////////////////////////////////////////
    // Auxiliary.

    // Add position and length to set.
    public void add(String posStr, String lenStr, TreeSet<Integer> set) {
	try {
	    int pos = Integer.parseInt(posStr);
	    int len = 1;
	    if (!lenStr.equals(""))
		len = Integer.parseInt(lenStr);
	    add(pos, len, set);
	} catch (NumberFormatException e) {
	    // ignore
	}
    }
    public void add(int pos, int len, TreeSet<Integer> set) {
	for (int i = 0; i < len; i++)
	    set.add(new Integer(pos + i));
    }

    // Add ranges to set.
    public void add(int[][] ranges, TreeSet<Integer> set) {
	if (ranges != null)
	    for (int i = 0; i < ranges.length; i++) 
		for (int len = 0; len < ranges[i][1]; len++)
		    set.add(new Integer(ranges[i][0] + len));
    }

    // Turn set into ranges. Return null if none.
    private static int[][] ranges(TreeSet<Integer> set) {
	Vector<int[]> rangeVector = new Vector();
	int first = -1;
	int len = 0;
	for (Iterator<Integer> it = set.iterator(); it.hasNext(); ) {
	    int i = it.next().intValue();
	    if (first < 0) {
		first = i;
		len = 1;
	    } else if (i == first + len) {
		len++;
	    } else {
		rangeVector.add(new int[] {first, len});
		first = i;
		len = 1;
	    }
	}
	if (first >= 0) 
	    rangeVector.add(new int[] {first, len});
	if (rangeVector.size() < 1)
	    return null;
	int[][] ranges = new int[rangeVector.size()][];
	for (int i = 0; i < rangeVector.size(); i++)
	    ranges[i] = (int[]) rangeVector.get(i);
	return ranges;
    }

    // Turn set into array. Return null if no elements.
    private int[] array(TreeSet<Integer> set) {
	if (set.size() > 0) {
	    int[] ar = new int[set.size()];
	    int n = 0;
	    for (Iterator<Integer> it = set.iterator(); it.hasNext(); ) 
		ar[n++] = it.next().intValue();
	    return ar;
	} else
	    return null;
    }

    // Are two sets of ranges the same?
    private boolean equalSet(TreeSet<Integer> set1, TreeSet<Integer> set2) {
	int[] ar1 = array(set1);
	int[] ar2 = array(set2);
	if (ar1 == null && ar2 == null)
	    return true;
	else if (ar1 == null && ar2 != null || ar1 != null && ar2 == null)
	    return false;
	else if (ar1.length != ar2.length)
	    return false;
	else {
	    for (int i = 0; i < ar1.length; i++) {
		if (ar1[i] != ar2[i])
		    return false;
	    }
	    return true;
	}
    }

    // To String, for testing.
    public String toString() {
	String s = name() + 
	    (argName() != null ? " " + argName() + "=" + argValue() : "") + "\n";
	int[][] signRanges = signRanges();
	int[][] letterRanges = letterRanges();
	if (signRanges != null) {
	    for (int i = 0; i < signRanges.length; i++)
		s += "" + signRanges[i][0] + "," + signRanges[i][1] + ";";
	    s += "\n";
	}
	if (letterRanges != null) {
	    for (int i = 0; i < letterRanges.length; i++)
		s += "" + letterRanges[i][0] + "," + letterRanges[i][1] + ";";
	    s += "\n";
	}
	return s;
    }

    ////////////////////////////////////////////////////////
    // Testing.

    public static void main(String[] args) {
	/*
	OrthoPhon e = new OrthoPhon();
	e.addSigns("3", "");
	e.addSigns("4", "1");
	e.addSigns("7", "2");
	e.addSigns("8", "2");
	int[][] ranges = e.signRanges();
	for (int i = 0; i < ranges.length; i++) 
	    System.out.println("" + ranges[i][0] + "," + ranges[i][1]);
	OrthoPhon f = new OrthoPhon();
	f.addLetters(ranges);
	ranges = f.letterRanges();
	for (int i = 0; i < ranges.length; i++) 
	    System.out.println("" + ranges[i][0] + "," + ranges[i][1]);
	    */
    }

}
