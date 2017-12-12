package nederhof.interlinear.egyptian.lex;

import java.io.*;
import java.util.*;

import nederhof.alignment.generic.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

// Helping access to lexical resource, for editor.
public class LexicoManipulator {

    // Always for EgyptianLexico.
    private static final int nTiers = 1;

    // The resource being edited.
    private EgyptianLexico resource;

    // The current segment. Or negative if none.
    private int current = -1;

    // Constructor.
    public LexicoManipulator(EgyptianLexico resource, int current) {
        this.resource = resource;
        setCurrentSilent(current);
    }

    /////////////////////////////////////////////////////////
    // Feedback to caller.

    // Inform that resource has changed.
    public void recordChange() {
        // caller should override
    }

    // Inform that focus has changed.
    public void showFocus() {
        // caller should override
    }

    // Inform that focus should not be shown for current.
    // (This is preceding change of focus.)
    public void unshowFocus() {
        // caller should override
    }

    // Inform that one should scroll to focus.
    public void scrollToFocus() {
        // caller should override
    }

    // Inform that refresh is needed of fragments.
    public void refreshFragments() {
        // caller should override
    }

    // Inform that refresh is needed (e.g. when segments have been
    // removed).
    public void refreshSegments() {
        // caller should override
    }
    // Inform that refresh is needed for segment.
    public void refreshSegment(int i, LxPart part) {
        // caller should override
    }

    // Inform tbat button belonging to index is to be removed. 
    public void removeButton(int i) {
        // caller should override
    }
    // Inform tbat button belonging to index is to be added. 
    public void addButton(int i, LxPart part) {
        // caller should override
    }
    // Inform that search in lexicon is needed.
    public void searchLexicon() {
        // caller should override
    }

    /////////////////////////////////////////////////////////
    // Access.

    // Safe way of getting fragments.
    private Vector<String> getFragments() {
	Vector<String> fragments = (Vector<String>) resource.getProperty("fragments");
	if (fragments == null)
	    return new Vector<String>();
	else
	    return fragments;
    }

    // The number of fragments in resource.
    public int nFragments() {
	return getFragments().size();
    }

    // A fragment by index.
    public String fragment(int i) {
	Vector<String> fragments = getFragments();
	if (i < 0 || i >= fragments.size())
	    return "";
	else
	    return fragments.get(i);
    }

    // The number of segments in resource.
    public int nSegments() {
        return resource.nPhrases();
    }

    // Give current segment number.
    public int current() {
        return current;
    }

    // Get i-th segment (as part of tier). Or null if none.
    public LxPart segment(int i) {
        if (i < 0 || i >= nSegments())
            return null;
        TextPhrase phrase = resource.getPhrase(i);
        Vector<ResourcePart> tier = phrase.getTier(nTiers - 1);
        if (tier.size() != 0 && tier.get(0) instanceof LxPart)
            return (LxPart) tier.get(0);
	else
	    return null;
    }
    // Current part.
    public LxPart segment() {
        return current >= 0 ? segment(current) : null;
    }
    // Info thereof.
    public LxInfo info() {
	LxPart part = segment();
	return part == null ? null : new LxInfo(part);
    }

    /////////////////////////////////////////////////////////
    // Navigation.

    // Move to position. 
    public void setCurrent(int i) {
        int oldCurrent = current;
        if (i < 0)
            i = nSegments() > 0 ? 0 : -1;
        else if (i >= nSegments())
            i = nSegments() > 0 ? nSegments()-1 : -1;
        if (oldCurrent >= 0 && oldCurrent != i)
            unshowFocus();
        current = i;
        if (current >= 0 && oldCurrent != current) {
	    showFocus();
	    searchLexicon();
	}
    }
    // As above, but do not notify change.
    private void setCurrentSilent(int i) {
        if (i < 0)
            i = nSegments() > 0 ? 0 : -1;
        else if (i >= nSegments())
            i = nSegments() > 0 ? nSegments()-1 : -1;
        current = i;
    }

    // Move to position with segment.
    public void setCurrent(LxPart part) {
        for (int i = 0; i < nSegments(); i++)
            if (segment(i) == part) {
                setCurrent(i);
                break;
            }
    }

    // Move left/right.
    public void left() {
        setCurrent(current-1);
        scrollToFocus();
    }
    public void right() {
        setCurrent(current+1);
        scrollToFocus();
    }

    /////////////////////////////////////////////////////////
    // Removing, adding, updating. Of Segments.

    // Remove i-th segment. Return whether successful.
    private boolean removeSegment(int i) {
        if (i < 0 || i >= nSegments())
            return false;
        resource.removePhrase(i);
        recordChange();
        return true;
    }
    // Remove current.
    public void removeSegment() {
        int oldCurrent = current;
        if (!removeSegment(current))
            return;
        setCurrentSilent(current);
        removeButton(oldCurrent);
        // Less error-prone alternative to above line but much slower:
        // refreshSegments();
    }

    // Swap current and next.
    public void swapSegments() {
        if (current < 0 || current >= nSegments()-1)
            return;
	String id1 = segment(current).id;
	String id2 = segment(current+1).id;
	LxInfo l1 = new LxInfo(segment(current));
	LxInfo l2 = new LxInfo(segment(current+1));
        updateSegment(current, l2, id2);
        updateSegment(current+1, l1, id1);
        refreshSegment(current, segment(current));
        refreshSegment(current+1, segment(current+1));
    }

    // Combine into segment.
    private TextPhrase makeSegment(LxInfo info, String id) {
        Vector<ResourcePart> tier = new Vector<ResourcePart>();
        tier.add(new LxPart(info, id));
        Vector<ResourcePart>[] tiers = new Vector[nTiers];
        tiers[nTiers - 1] = tier;
        return new TextPhrase(resource, tiers);
    }
    private TextPhrase makeSegment(LxInfo info) {
	return makeSegment(info, freshId());
    }

    // Insert before i-th segment.
    // If i is number of existing segment, then append.
    private void insertSegment(int i, LxInfo info) {
        resource.insertPhrase(makeSegment(info), i);
        recordChange();
    }
    // Insert empty.
    public boolean insertSegment(int i) {
        if (i < 0 || i > nSegments())
            return false;
        insertSegment(i, new LxInfo());
        refreshSegments();
        setCurrent(i);
        return true;
    }
    // Insert empty before current.
    public void insertSegment() {
        if (current < 0)
            insertSegment(0, new LxInfo());
        else
            insertSegment(current, new LxInfo());
        refreshSegments();
    }

    // Insert empty after current.
    public void addSegment() {
        if (current < 0) {
            insertSegment(0, new LxInfo());
            setCurrentSilent(0);
            addButton(0, segment());
            // Less error-prone alternative to above line but much slower:
            // refreshSegments();
            // setCurrent(0);
        } else {
            insertSegment(current+1, new LxInfo());
            setCurrentSilent(current+1);
            addButton(current, segment());
            // Less error-prone alternative to above line but much slower:
            // refreshSegments();
            // setCurrent(current+1);
        }
    }
    // Insert empty at beginning.
    public void addInitialSegment() {
        insertSegment(0, new LxInfo());
        setCurrentSilent(0);
        addButton(0, segment());
        // Less error-prone alternative to above line but much slower:
        // refreshSegments();
        // setCurrent(0);
    }

    // Update i-th segment.
    private void updateSegment(int i, LxInfo info, String id) {
        resource.setPhrase(makeSegment(info, id), i);
        recordChange();
    }
    // Update current segment.
    public void updateSegment(LxInfo info) {
        if (current >= 0) {
	    String id = segment(current).id;
            updateSegment(current, info, id);
	}
    }

    // Set texthi of current segment.
    public void setTexthi(ResFragment hi) {
	LxInfo info = info();
	if (info != null) {
	    info.texthi = hi.toString();
	    updateSegment(info);
	    refreshSegment(current, segment(current));
	}
    }

    // Fresh id.
    private String freshId() {
	HashSet<String> ids = new HashSet<String>();
        for (int i = 0; i < nSegments(); i++)
	    ids.add(segment(i).id);
	int id = 0;
	while (ids.contains("" + id))
	    id++;
        return "" + id;
    }

    /////////////////////////////////////////////////////////
    // Removing, adding, updating. Of Fragments.

    // Put new fragments behind old ones.
    private void appendFragments(Vector<String> moreFragments) {
	if (!moreFragments.isEmpty()) {
	    Vector<String> fragments = getFragments();
	    fragments.addAll(moreFragments);
	    resource.setProperty("fragments", fragments);
	    recordChange();
	    refreshFragments();
	}
    }

    // Change first fragment.
    private void setFirstFragment(String fragment) {
	Vector<String> fragments = getFragments();
	if (!fragments.isEmpty()) {
	    fragments.set(0, fragment);
	    resource.setProperty("fragments", fragments);
	    recordChange();
	}
    }

    // Remove first few fragments.
    private void removeFirstFragments(int i) {
	Vector<String> fragments = getFragments();
	int j = Math.min(i, fragments.size());
	if (j > 0) {
	    while (j-- > 0)
		fragments.remove(0);
	    resource.setProperty("fragments", fragments);
	    recordChange();
	}
    }

    // Remove first few fragments, and then first few hieroglyphs from next.
    // Remove that next fragment if no glyphs therein remain.
    public void removeHi(int nFrags, int nGlyphs) {
	HieroRenderContext hieroContext = 
	    new HieroRenderContext(Settings.textHieroFontSize, true);
	ParsingContext parsingContext =
	    new ParsingContext(hieroContext, true);
	removeFirstFragments(nFrags);
	if (nFragments() > 0) {
	    ResFragment parsed = ResFragment.parse(fragment(0), parsingContext);
	    ResFragment suffix = parsed.suffixGlyphs(nGlyphs);
	    if (suffix.nGlyphs() > 0)
		setFirstFragment(suffix.toString());
	    else
		removeFirstFragments(1);
	}
	refreshFragments();
    }

    /////////////////////////////////////////////////////////
    // Import.

    // Put hieroglyphic in fragments at end.
    public void incorporateHiero(EgyptianResource hieroResource) {
	Vector<String> hieros = hieroOf(hieroResource);
	appendFragments(hieros);
    }

    // Put transliteration from resource in segments.
    public void incorporateTrans(EgyptianResource transResource) {
	String joinedTrans = joinTrans(transResource).trim().replaceAll("\\s+", " ");
        int pos = Math.max(current, 0);
	String[] split = joinedTrans.split(" ");
	for (int i = 0; i < split.length; i++) {
	    resource.insertPhrase(makeSegment(simpleEntry(split[i])), pos);
            pos += 1;
	}
        setCurrentSilent(pos);
        recordChange();
        refreshSegments();
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
     * From resource, get hieroglyphic.
     * 
     * @param resource
     * @return
     */
    private static Vector<String> hieroOf(EgyptianResource resource) {
        for (int i = 0; i < resource.nTiers(); i++)
            if (resource.tierName(i).equals("hieroglyphic"))
                return hieroOf(resource, i);
        return new Vector<String>();
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
                    b.append(escapeUpper(alPart.string, alPart.upper));
                }
            }
            if (i < resource.nPhrases()-1)
                b.append(" ");
        }
        return b.toString();
    }

    // Introduce carrets for upper case.
    private static String escapeUpper(String s, boolean upper) {
	if (upper) {
	    String esc = "";
	    for (int i = 0; i < s.length(); i++) {
		if (s.charAt(i) != ' ')
		    esc += "^";
		esc += s.charAt(i);
	    }
	    return esc;
	} else 
	    return s;
    }

    /**
     * From resource and tier number, get hieroglyphic.
     * 
     * @param resource
     * @param tierNo
     * @return
     */
    private static Vector<String> hieroOf(EgyptianResource resource, int tierNo) {
	Vector<String> fragments = new Vector<String>();
        for (int i = 0; i < resource.nPhrases(); i++) {
            TextPhrase phrase = resource.getPhrase(i);
            Vector tier = phrase.getTier(tierNo);
            for (int j = 0; j < tier.size(); j++) {
                ResourcePart part = (ResourcePart) tier.get(j);
                if (part instanceof HiPart) {
                    HiPart hiPart = (HiPart) part;
		    fragments.add(hiPart.hi);
                }
            }
        }
        return fragments;
    }

    ///////////////////////////////////////////////////////////////////
    // Auxiliary.

    // Make entry from word in transliteration.
    // By default assume in basic dictionary.
    // For key, strip of endings.
    private LxInfo simpleEntry(String word) {
	LxInfo lx = new LxInfo();
	word = word.replaceAll("\\[", "");
	word = word.replaceAll("\\]", "");
	lx.textal = word;
	lx.cite = "basic";
	String bare = word;
	bare = bare.replaceAll("=.*", "");
	bare = bare.replaceAll("\\.*.*", "");
	bare = bare.replaceAll("\\(", "");
	bare = bare.replaceAll("\\)", "");
	bare = bare.replaceAll("\\{.*\\}", "");
	if (bare.equals("1000"))
	    bare = "xA";
	lx.keyal = bare;
	return lx;
    }
}
