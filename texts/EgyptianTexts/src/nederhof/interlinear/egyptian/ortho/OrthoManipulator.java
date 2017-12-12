package nederhof.interlinear.egyptian.ortho;

import java.io.*;
import java.util.*;

import nederhof.alignment.generic.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.operations.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

// Helping access to orthographic resource, for editor.
public class OrthoManipulator {

    // Always for EgyptianOrtho.
    private static final int nTiers = 1;

    // The resource being edited.
    private EgyptianOrtho resource;

    // The current segment.
    private int current = 0;

    // The function having focus, if any.
    // Is set to null upon many operations.
    private OrthoElem currentFun = null;

    // For hieroglyphic.
    private ParsingContext parsingContext;
    private ResComposer composer;
    private NormalizerRemoveWhite whiteRemover = new NormalizerRemoveWhite();

    // To keep track of existing annotations.
    private ExampleOrtho existing = new ExampleOrtho(Settings.exampleAnnotations);

    // Construct.
    // Ensure there is at least one segment.
    public OrthoManipulator(EgyptianOrtho resource, int current,
	    ParsingContext parsingContext) {
	this.resource = resource;
	this.parsingContext = parsingContext;
	this.composer = new ResComposer(parsingContext);
        if (nSegments() == 0)
            appendSegment();
	loadExisting();
	setCurrent(current);
    }

    // Feedback to caller.
    public void recordChange() {
	// caller should override
    }

    // Initial investigation of all existing annotations.
    private void loadExisting() {
	for (int i = 0; i < nSegments(); i++) {
	    OrthoPart ortho = orthoPart(i);
	    if (!ortho.textortho.isEmpty())
		existing.add(ortho.textal, ortho.texthi, ortho.textortho);
	}
    }

    /////////////////////////////////////////////////////////
    // Access.

    // The number of segments in resource.
    public int nSegments() {
        return resource.nPhrases();
    }

    // Give current segment number.
    public int currentSegment() {
	return current;
    }

    // Give current function if any.
    public OrthoElem currentFun() {
	return currentFun;
    }

    // Get i-th segment. Or null if none.
    private OrthoPart orthoPart(int i) {
        if (i < 0 || i >= nSegments())
            return null;
        TextPhrase phrase = resource.getPhrase(i);
        Vector<ResourcePart> tier = phrase.getTier(nTiers - 1);
        if (tier.size() == 0)
            return null;
        return (OrthoPart) tier.get(0);
    }

    // Get hieroglyphic of i-th segment.
    public String hiero(int i) {
        OrthoPart ortho = orthoPart(i);
        return (ortho == null) ? "" : ortho.texthi;
    }
    // and of current segment.
    public String hiero() {
	return hiero(current);
    }

    // Get transliteration of i-th segment.
    public String trans(int i) {
        OrthoPart ortho = orthoPart(i);
        return (ortho == null) ? "" : ortho.textal;
    }
    // and of current segment.
    public String trans() {
	return trans(current);
    }

    // Get orthographic elements of i-th segment.
    public Vector<OrthoElem> orthos(int i) {
        OrthoPart ortho = orthoPart(i);
        return (ortho == null) ? new Vector<OrthoElem>() : ortho.textortho;
    }
    // and of current segment.
    public Vector<OrthoElem> orthos() {
	return orthos(current);
    }

    // Give length of prefix of letters potentially already covered
    // by functions in current segment.
    public int covered() {
	int last = 0;
	Vector<OrthoElem> orthos = orthos();
	for (OrthoElem ortho : orthos) {
	    int[] letters = ortho.letters();
	    if (letters != null) 
		for (int i = 0; i < letters.length; i++)  {
		    last = Math.max(last, letters[i]+1);
		}
	}
	return last;
    }

    // Get position of second word in transliteration.
    public int secondWordPos() {
	String t = trans();
	int i = 0;
	while (i < t.length() && t.charAt(i) == ' ')
	    i++;
	while (i < t.length() && t.charAt(i) != ' ')
	    i++;
	while (i < t.length() && t.charAt(i) == ' ')
	    i++;
	if (i < t.length())
	    return i;
	else 
	    return 0;
    }

    /////////////////////////////////////////////////////////
    // Navigation.

    // Move to position. If there are no segments, create one.
    // When moving to other segment, there is no longer focussed
    // function.
    public void setCurrent(int i) {
	if (i < 0)
	    return;
	else if (i >= nSegments())
	    return;
	if (i != current)
	    currentFun = null;
        current = i;
    }

    // Move left/right.
    public void left() {
	setCurrent(current-1);
    }
    public void right() {
	setCurrent(current+1);
    }

    // Set current function.
    public void setCurrentFun(OrthoElem currentFun) {
	this.currentFun = currentFun;
    }

    /////////////////////////////////////////////////////////
    // Removing, adding, updating.

    // Remove i-th segment.
    // Ensure at least one is left.
    private void removeSegment(int i) {
	currentFun = null;
        resource.removePhrase(i);
        if (nSegments() == 0)
            appendSegment();
	recordChange();
    }
    // Remove current.
    public void removeSegment() {
        removeSegment(current);
	if (current >= nSegments())
	    left();
    }

    // Combine elements into segment.
    private TextPhrase makeSegment(String hi, String al, Vector<OrthoElem> orthos, String id) {
        Vector tier = new Vector();
        tier.add(new OrthoPart(hi, al, orthos, id));
        Vector[] tiers = new Vector[nTiers];
        tiers[nTiers - 1] = tier;
        return new TextPhrase(resource, tiers);
    }
    private TextPhrase makeSegment(String hi, String al, Vector<OrthoElem> orthos) {
	return makeSegment(hi, al, orthos, freshId());
    }

    // Insert before i-th segment.
    // If i is number of existing segments, then append.
    private void insertSegment(int i, String hi, String al, Vector<OrthoElem> orthos) {
	currentFun = null;
        resource.insertPhrase(makeSegment(hi, al, orthos), i);
	recordChange();
    }
    // Insert empty before current.
    public void insertSegment() {
	insertSegment(current, "", "", new Vector<OrthoElem>());
    }

    // Add a segment at the end.
    private void appendSegment(String hi, String al, Vector<OrthoElem> orthos) {
	currentFun = null;
        resource.addPhrase(makeSegment(hi, al, orthos));
	recordChange();
    }
    // Append empty.
    public void appendSegment() {
	currentFun = null;
	appendSegment("", "", new Vector<OrthoElem>());
	setCurrent(nSegments()-1);
    }

    // Update i-th segment.
    private void updateSegment(int i, String hi, String al, Vector<OrthoElem> orthos) {
	OrthoPart oldPart = orthoPart(i);
	String id = oldPart != null ? oldPart.id : "";
	currentFun = null;
        resource.setPhrase(makeSegment(hi, al, orthos, id), i);
	recordChange();
    }

    private String freshId() {
        HashSet<String> ids = new HashSet<String>();
        for (int i = 0; i < nSegments(); i++)
            ids.add(orthoPart(i).id);
        int id = 0;
        while (ids.contains("" + id))
            id++;
        return "" + id;
    }

    ////////////////////////////////////////////////////////////
    // Editing of current function.

    // Add/remove glyph to/from current function, if any.
    // Make sure the number of glyphs does not become empty.
    // (Not currently used.)
    public void toggleGlyphOfFun(int sign) {
	if (currentFun != null) {
	    TreeSet<Integer> signs = currentFun.signSet();
	    if (signs.contains(sign)) {
		if (signs.size() > 1) 
		    currentFun.removeSign(sign);
	    } else
		currentFun.addSign(sign);
	    recordChange();
	}
    }

    // Add/remove letter position to/from current function, if any.
    public void toggleLetterOfFun(int letter) {
	if (currentFun != null) {
	    TreeSet<Integer> letters = currentFun.letterSet();
	    if (letters.contains(letter))
		currentFun.removeLetter(letter);
	    else
		currentFun.addLetter(letter);
	    recordChange();
	}
    }

    // Add function. Added ortho becomes focus.
    public void addFun(OrthoElem ortho) {
	Vector<OrthoElem> orthos = orthos();
	orthos.add(ortho);
	updateSegment(current, hiero(), trans(), orthos);
	currentFun = ortho;
    }

    // Remove current function.
    public void removeFun() {
	if (currentFun != null) {
	    Vector<OrthoElem> orthos = orthos();
	    orthos.remove(currentFun);
	    updateSegment(current, hiero(), trans(), orthos);
	}
    }

    // Remove all functions.
    public void removeAllFuns() {
	updateSegment(current, hiero(), trans(), new Vector<OrthoElem>());
    }

    // Replace current function. New ortho becomes focus.
    public void replaceFunction(OrthoElem ortho) {
	if (currentFun != null) {
	    Vector<OrthoElem> orthos = orthos();
	    orthos.remove(currentFun);
	    orthos.add(ortho);
	    updateSegment(current, hiero(), trans(), orthos);
	    currentFun = ortho;
	}
    }

    /////////////////////////////////////////////////////////
    // Merging and splitting.

    // Merge current and next segments.
    // If last, then do nothing.
    public void mergeSegments() {
	if (current >= nSegments() - 1)
	    return;
	int next = current + 1;
	String hi1 = hiero(current);
	ResFragment res1 = ResFragment.parse(hi1, parsingContext);
	String al1 = trans(current);
	Vector<OrthoElem> orthos1 = orthos(current);
	String hi2 = hiero(next);
	ResFragment res2 = ResFragment.parse(hi2, parsingContext);
	String al2 = trans(next);
	Vector<OrthoElem> orthos2 = orthos(next);
	int hiLen1 = res1.nGlyphs();
	int alLen1 = TransHelper.charLength(al1);

	ResFragment res = composer.append(res1, res2);
	String hi = res.toString();
	String al;
	if (al1.matches(""))
	    al = al2;
	else if (al2.matches(""))
	    al = al1;
	else {
	    al = al1 + ' ' + al2;
	    alLen1++;
	}
	Vector<OrthoElem> orthos = new Vector<OrthoElem>();
	orthos.addAll(orthos1);
	for (OrthoElem ortho : orthos2) {
	    int[][] signRanges2 = ortho.signRanges();
	    int[][] letterRanges2 = ortho.letterRanges();
	    int[][] signRanges = null;
	    int[][] letterRanges = null;
	    if (signRanges2 != null) {
		signRanges = new int[signRanges2.length][2];
		for (int i = 0; i < signRanges2.length; i++) {
		    signRanges[i][0] = signRanges2[i][0] + hiLen1;
		    signRanges[i][1] = signRanges2[i][1];
		}
	    }
	    if (letterRanges2 != null) {
		letterRanges = new int[letterRanges2.length][2];
		for (int i = 0; i < letterRanges2.length; i++) {
		    letterRanges[i][0] = letterRanges2[i][0] + alLen1;
		    letterRanges[i][1] = letterRanges2[i][1];
		}
	    }
	    OrthoElem newOrtho = OrthoElem.makeOrtho(
			ortho.name(), ortho.argName(), ortho.argValue(), 
			signRanges, letterRanges);
	    orthos.add(newOrtho);
	}
	updateSegment(current, hi, al, orthos);
	removeSegment(current+1);
    }

    // Would a split break some orthographic function on both
    // sides of the split points?
    public boolean splitWouldBreak(int hiPos, int alPos) {
	Vector<OrthoElem> orthos = orthos(current);
	for (OrthoElem ortho : orthos) {
	    boolean inFirst = false;
	    boolean inSecond = false;
	    int[] signs = ortho.signs();
	    int[] letters = ortho.letters();
	    if (signs != null) {
		if (signs[0] < hiPos)
		    inFirst = true;
		else
		    inSecond = true;
		for (int i = 0; i < signs.length; i++) {
		    int sign = signs[i];
		    if (inFirst && sign >= hiPos)
			return true;
		    else if (inSecond && sign < hiPos)
			return true;
		}
	    }
	    if (letters != null) {
		for (int i = 0; i < letters.length; i++) {
		    int letter = letters[i];
		    if (inFirst && letter >= alPos)
			return true;
		    else if (inSecond && letter < alPos)
			return true;
		}
	    }
	}
	return false;
    }

    // Split current segment into two at given positions.
    // The first sign determines in which part a function falls.
    public void splitSegment(int hiPos, int alPos) {
	String hi = hiero(current);
	Vector<OrthoElem> orthos = orthos(current);
	String al = trans(current);

	ResFragment res = ResFragment.parse(hi, parsingContext);
	ResFragment res1 = res.prefixGlyphs(hiPos);
	ResFragment res2 = res.suffixGlyphs(hiPos);
	String hi1 = whiteRemover.normalize(res1).toString();
	String hi2 = whiteRemover.normalize(res2).toString();

	Vector<OrthoElem> orthos1 = new Vector<OrthoElem>();
	Vector<OrthoElem> orthos2 = new Vector<OrthoElem>();
	for (OrthoElem ortho : orthos) {
	    boolean inFirst = false;
	    boolean inSecond = false;
	    int[] signs = ortho.signs();
	    int[] letters = ortho.letters();
	    TreeSet<Integer> newSigns = new TreeSet<Integer>();
	    TreeSet<Integer> newLetters = new TreeSet<Integer>();
	    if (signs != null) {
		if (signs[0] < hiPos)
		    inFirst = true;
		else
		    inSecond = true;
		for (int i = 0; i < signs.length; i++) {
		    int sign = signs[i];
		    if (inFirst && sign < hiPos)
			newSigns.add(sign);
		    else if (inSecond && sign >= hiPos)
			newSigns.add(sign - hiPos);
		}
	    }
	    if (letters != null) {
		for (int i = 0; i < letters.length; i++) {
		    int letter = letters[i];
		    if (inFirst && letter < alPos)
			newLetters.add(letter);
		    else if (inSecond && letter >= alPos)
			newLetters.add(letter - alPos);
		}
	    }
	    OrthoElem newOrtho = OrthoElem.makeOrtho(
		    ortho.name(), ortho.argName(), ortho.argValue(),
		    newSigns, newLetters);
	    if (inFirst) 
		orthos1.add(newOrtho);
	    if (inSecond) 
		orthos2.add(newOrtho);
	}

	StringBuffer al1Buf = new StringBuffer();
	StringBuffer al2Buf = new StringBuffer();
	TransHelper.splitTransWithSpaces(al, alPos, al1Buf, al2Buf);
	String al1 = al1Buf.toString();
	al1 = al1.replaceAll("\\s*$", "");
	String al2 = al2Buf.toString();

	updateSegment(current, hi1, al1, orthos1);
	insertSegment(current+1, hi2, al2, orthos2);
	setCurrent(current+1);
    }

    /////////////////////////////////////////////////////////
    // Normalization.

    // Remove all functions and flatten hieroglyphic.
    public void normalize() {
	String hiero = hiero();
	String trans = trans();
	ResFragment res = ResFragment.parse(hiero, parsingContext);
	res = (new NormalizerMnemonics()).normalize(res);
	res = (new NormalizerGroups()).normalize(res);
	res = (new NormalizerVariants()).normalize(res);
	res = (new NormalizerRemoveBoxes()).normalize(res);
	ResComposer composer = new ResComposer();
	res = composer.composeNames(res.glyphNames());
	hiero = res.toString();
	updateSegment(current, hiero, trans, new Vector<OrthoElem>());
    }

    /////////////////////////////////////////////////////////
    // Examples.

    // Ask for list of existing annotations.
    public LinkedList<Vector<OrthoElem>> retrieve() {
	return existing.retrieve(trans(), hiero());
    }

    /////////////////////////////////////////////////////////
    // Import.

    // Put hieroglyphic from resource in current segment.
    // Keep existing transliteration, but remove functions.
    public void incorporateHiero(EgyptianResource hieroResource) {
	String joinedHiero = joinHiero(hieroResource);
	updateSegment(current, joinedHiero, trans(), new Vector<OrthoElem>());
    }

    // Put transliteration from resource in current segment.
    // Keep existing hieroglyphic, but remove functions.
    public void incorporateTrans(EgyptianResource transResource) {
	String joinedTrans = joinTrans(transResource).trim().replaceAll("\\s+", " ");
	joinedTrans = joinedTrans.replaceAll("\\[\\.\\.\\.\\]", "");
	joinedTrans = joinedTrans.replaceAll("\\[", "");
	joinedTrans = joinedTrans.replaceAll("\\]", "");
	joinedTrans = joinedTrans.replaceAll("\\(", "");
	joinedTrans = joinedTrans.replaceAll("\\)", "");
	joinedTrans = joinedTrans.replaceAll("\\{", "");
	joinedTrans = joinedTrans.replaceAll("\\}", "");
	updateSegment(current, hiero(), joinedTrans, new Vector<OrthoElem>());
    }

    /**
     * From resource, get hieroglyphic.
     * 
     * @param resource
     * @return
     */
    private static String joinHiero(EgyptianResource resource) {
        for (int i = 0; i < resource.nTiers(); i++) 
            if (resource.tierName(i).equals("hieroglyphic"))
                return joinHiero(resource, i);
        return "";
    }

    /**
     * From resource, get transliteration.
     * 
     * @param resource
     * @return
     */
    private static String joinTrans(EgyptianResource resource) {
        for (int i = 0; i < resource.nTiers(); i++) 
            if (resource.tierName(i).equals("transliteration"))
                return joinTrans(resource, i);
        return "";
    }

    /**
     * From resource and tier number, get hieroglyphic.
     * 
     * @param resource
     * @param tierNo
     * @return
     */
    private static String joinHiero(EgyptianResource resource, int tierNo) {
        ResFragment joined = new ResFragment();
        for (int i = 0; i < resource.nPhrases(); i++) {
            TextPhrase phrase = resource.getPhrase(i);
            Vector tier = phrase.getTier(tierNo);
            for (int j = 0; j < tier.size(); j++) {
                ResourcePart part = (ResourcePart) tier.get(j);
                if (part instanceof HiPart) {
                    HiPart hiPart = (HiPart) part;
                    ResFragment next = hiPart.parsed;
                    joined = ResComposer.append(joined, next);
                }
            }
        }
        return joined.toString();
    }

    /**
     * From resource and tier number, get transliteration.
     * 
     * @param resource
     * @param tierNo
     * @return
     */
    private static String joinTrans(EgyptianResource resource, int tierNo) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < resource.nPhrases(); i++) {
            TextPhrase phrase = resource.getPhrase(i);
            Vector tier = phrase.getTier(tierNo);
            for (int j = 0; j < tier.size(); j++) {
                ResourcePart part = (ResourcePart) tier.get(j);
                if (part instanceof AlPart) {
                    AlPart alPart = (AlPart) part;
                    b.append(alPart.string);
                }
            }
	    if (i < resource.nPhrases()-1)
		b.append(" ");
        }
        return b.toString();
    }

    ////////////////////////////////////////////////////////////////
    // Modification.

    // Replace hieroglyphic. Update orthos, based on minimum edit.
    public void replaceHiero(String hi2) {
	String hi1 = hiero(current);
	if (hi1.equals(hi2))
	    return;
	Vector<OrthoElem> orthos1 = orthos(current);
	String al1 = trans(current);

	ResFragment res1 = ResFragment.parse(hi1, parsingContext);
	ResFragment res2 = ResFragment.parse(hi2, parsingContext);
	ResComposer composer = new ResComposer();
	composer.normalMnemonics = true;
	res1 = composer.normalize(res1);
	res2 = composer.normalize(res2);
	Vector<String> names1 = res1.glyphNames();
	Vector<String> names2 = res2.glyphNames();
	MinimumEditUpdater updater = new MinimumEditUpdater(names1, names2);
	Vector<OrthoElem> orthos2 = new Vector<OrthoElem>();
	for (OrthoElem ortho : orthos1) {
	    TreeSet<Integer> signs1 = ortho.signSet();
	    TreeSet<Integer> signs2 = new TreeSet<Integer>();
	    TreeSet<Integer> letters = ortho.letterSet();
	    for (Integer sign1 : signs1) {
		int sign2 = updater.map(sign1);
		if (0 <= sign2 && sign2 < names2.size())
		    signs2.add(sign2);
	    }
	    OrthoElem ortho2 = OrthoElem.makeOrtho(
		    ortho.name(), ortho.argName(), ortho.argValue(),
		    signs2, letters);
	    orthos2.add(ortho2);
	}
	updateSegment(current, hi2, al1, orthos2);
    }

    // Replace transliteration. Update orthos, based on minimum edit.
    public void replaceTrans(String al2) {
	String hi1 = hiero(current);
	Vector<OrthoElem> orthos1 = orthos(current);
	String al1 = trans(current);
	if (al1.equals(al2))
	    return;
	int al2Length = TransHelper.charLength(al2);

	Vector<String> AlLetters1 = TransHelper.letters(al1);
	Vector<String> AlLetters2 = TransHelper.letters(al2);
	MinimumEditUpdater updater = new MinimumEditUpdater(AlLetters1, AlLetters2);
	Vector<OrthoElem> orthos2 = new Vector<OrthoElem>();
	for (OrthoElem ortho : orthos1) {
	    TreeSet<Integer> signs = ortho.signSet();
	    TreeSet<Integer> letters1 = ortho.letterSet();
	    TreeSet<Integer> letters2 = new TreeSet<Integer>();
	    for (Integer letter1 : letters1) {
		int letter2 = updater.map(letter1);
		if (0 <= letter2 && letter2 < al2Length)
		    letters2.add(letter2);
	    }
	    OrthoElem ortho2 = OrthoElem.makeOrtho(
		    ortho.name(), ortho.argName(), ortho.argValue(),
		    signs, letters2);
	    orthos2.add(ortho2);
	}
	updateSegment(current, hi1, al2, orthos2);
    }

}

