package nederhof.web;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.alignment.egyptian.*;
import nederhof.alignment.*;
import nederhof.corpus.Text;
import nederhof.corpus.egyptian.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.interlinear.labels.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.xml.*;

// Helper with processing of text and (Egyptian) resources.
public class TextHelper {

    // For Egyptian.
    private Vector<ResourceGenerator> resourceGenerators = new Vector<ResourceGenerator>();
    private Autoaligner autoaligner = new EgyptianAutoaligner();
    private EgyptianPdfRenderParameters params = 
	new EgyptianPdfRenderParameters(".", "dont-care", "dont-care");

    // The text, with attached resources.
    private Text text;
    private Vector<TextResource> resources = new Vector<TextResource>();
    private Vector<ResourcePrecedence> precedences = new Vector<ResourcePrecedence>();
    private Vector<Object[]> autoaligns = new Vector<Object[]>();
    // The tiers.
    private Vector<Tier> tiers = new Vector<Tier>();
    // Numbers of tiers within resources.
    private Vector<Integer> tierNums = new Vector<Integer>();
    // Short names of tiers.
    private Vector<String> labels = new Vector<String>();
    // Versions of tiers.
    private Vector<String> versions = new Vector<String>();
    // Vector of arrays of positions with boolean saying where phrases start.
    private Vector<boolean[]> phraseStarts = new Vector<boolean[]>();
    // Resource for each tier.
    private Vector<TextResource> tierResources = new Vector<TextResource>();

    // Formatted sections.
    private Vector<Section> sections = new Vector<Section>();
    // Mapping from marker to section number.
    private TreeMap<String,Integer> markerSecNum = new TreeMap<String,Integer>();

    // Tier number with hieroglyphic, non-negative if any. Same for transliteration,
    // translation, lexical, orthographic, image information. 
    // This is to avoid that parts from different tiers are taken.
    private int hiNum = -1;
    private int alNum = -1;
    private int trNum = -1;
    private int lxNum = -1;
    private int orthoNum = -1;
    private int imageNum = -1;
    // Resource containing images, if any.
    private EgyptianImage imageResource = null;

    private Vocab vocab = new Vocab();

    public TextHelper(String textLocation) throws IOException {
	text = new Text(textLocation);
	getResourceGenerators();
	getResources();
	getPrecedences();
	getAutoaligns();
	makeTiers();
	params.computeFonts();
	params.initiateSituation();
	format();
    }

    private void getResourceGenerators() {
	resourceGenerators.add(new EgyptianResourceGenerator());
	resourceGenerators.add(new EgyptianLexicoGenerator());
	resourceGenerators.add(new EgyptianOrthoGenerator());
	resourceGenerators.add(new EgyptianImageGenerator());
	resourceGenerators.add(new SchemeMapGenerator());
    }

    // Open resources in text.
    private void getResources() {
	Vector<String> resourceLocations = text.getResources();
	for (int i = 0; i < resourceLocations.size(); i++) {
	    String file = resourceLocations.get(i);
	    TextResource resource = IndexPane.toResource(file, resourceGenerators);
	    if (resource != null) {
		resources.add(resource);
		if (imageResource == null && resource instanceof EgyptianImage)
		    imageResource = (EgyptianImage) resource;
	    }
	}
    }

    // Open precedences in text.
    private void getPrecedences() {
	Vector<String[]> precs = text.getPrecedences();
	for (int i = 0; i < precs.size(); i++) {
	    String[] prec = precs.get(i);
	    ResourcePrecedence precedence = IndexPane.toPrecedence(prec, resources);
	    if (precedence != null)
		precedences.add(precedence);
	}
    }

    // Open autoaligns in text.
    private void getAutoaligns() {
	Vector<String[]> aligns = text.getAutoaligns();
	for (int i = 0; i < aligns.size(); i++) {
	    String[] autoString = aligns.get(i);
	    Object[] autoResource = IndexPane.toAutoalign(autoString, resources);
	    if (autoResource != null)
		autoaligns.add(autoResource);
	}
    }

    // Get image resource in text if any.
    public EgyptianImage getImageResource() {
	return imageResource;
    }

    ////////////////////////////////////////////////////////////////
    // Interlinear text.

    // Section.
    private class Section {
	public String hi;
	public String al;
	public String tr;
	public String ortho = "";
	public String lxortho = "";
	public String marker;
	public Vector<Footnote> notes;
	public Section(String hi, String al, String tr, String marker) {
	    this.hi = hi;
	    this.al = al;
	    this.tr = tr;
	    this.marker = marker;
	}
    };

    // Get tiers from resources.
    private void makeTiers() {
	TierGather gather = new TierGather(resources, precedences, autoaligns,
		autoaligner, params, true, false);
	tiers = gather.tiers;
	tierNums = gather.tierNums;
	versions = gather.versions;
	for (int i = 0; i < gather.phraseStarts.size(); i++) {
	    boolean[] starts = new boolean[tiers.get(i).nSymbols()];
	    for (int j : gather.phraseStarts.get(i))
		if (0 <= j && j < tiers.get(i).nSymbols())
		    starts[j] = true;
	    phraseStarts.add(starts);
	}
	tierResources = gather.tierResources;
    }

    // Get sections of interlinear text.
    // The text is to be segmented according to phrases in
    // translation.
    private void format() {
	new InterlinearFormatting(tiers) {
	    protected float width() {
		return 4000; // Some big enough number
	    }
	    protected boolean processSection(String[] modes,
		    Vector<TierSpan>[] sectionSpans,
		    TreeMap<Integer,Float>[] sectionSpanLocations) {
		Section sect = makeSection(modes, sectionSpans);
		if (sect != null) {
		    if (!sect.marker.equals("")) 
			markerSecNum.put(sect.marker, sections.size());
		    sections.add(sect);
		}
		return true;
	    }
	    protected double penalty() {
		double pen = super.penalty();
		if (phraseStartNotAtBeginning())
		    return Double.MAX_VALUE;
		else
		    return pen;
	    }
	    private boolean phraseStartNotAtBeginning() {
		for (int i = 0; i < TextHelper.this.tiers.size(); i++) {
		    TextResource resource = TextHelper.this.tierResources.get(i);
		    Tier tier = TextHelper.this.tiers.get(i);
		    int num = TextHelper.this.tierNums.get(i);
		    String version = TextHelper.this.versions.get(i);
		    String tierName = resource.tierName(num);
		    if (tierName.equals("translation") &&
			    !version.equals("structure") &&
			    !sectionSpans[i].isEmpty() &&
			    containsPhraseStart(i)) {
			return true;
		    }
		}
		return false;
	    }
	    private boolean containsPhraseStart(int i) {
		int fromPos = firstSpanBegin(i);
		int toPos = lastSpanEnd(i);
		for (int j = fromPos+1; j < toPos; j++) 
		    if (0 <= j && j < phraseStarts.get(i).length &&
			    phraseStarts.get(i)[j])
			return true;
		return false;
	    }
	};
    }

    private Section makeSection(String[] modes, Vector<TierSpan>[] sectionSpans) {
	String hi = "";
	String al = "";
	String tr = "";
	Vector<LxPdfPart> lxParts = new Vector<LxPdfPart>();
	Vector<OrthoPdfPart> orthoParts = new Vector<OrthoPdfPart>();
	Vector<ImagePlacePdfPart> imageParts = new Vector<ImagePlacePdfPart>();
	String ortho = "";
	String lxortho = "";
	String marker = "";
	for (int i = 0; i < sectionSpans.length; i++) {
            String mode = modes[i];
            Vector<TierSpan> spans = sectionSpans[i];
	    Vector<Footnote> footnotes = FootnoteHelper.footnotes(spans);
	    if (includesContent(spans)) {
		int first = first(spans);
		int last = last(spans);
		TextResource resource = tierResources.get(i);
		Tier tier = tiers.get(i);
		int num = tierNums.get(i);
		String version = versions.get(i);
		String tierName = resource.tierName(num);
		if (tierName.equals("hieroglyphic") && (hiNum < 0 || hiNum == num)) {
		    hiNum = num;
		    hi = hiOf(resource, tier, first, last);
		} else if (tierName.equals("transliteration") && (alNum < 0 || alNum == num)) {
		    alNum = num;
		    al = textOf(resource, tier, first, last);
		} else if (tierName.equals("translation") && 
			!version.equals("structure") &&
			(trNum < 0 || trNum == num)) {
		    trNum = num;
		    tr = textOf(resource, tier, first, last);
		} else if (tierName.equals("translation") &&
			version.equals("structure")) {
		    marker = textOf(resource, tier, first, last).trim();
		} else if (tierName.equals("lexical") && (lxNum < 0 || lxNum == num)) {
		    lxNum = num;
		    lxParts.addAll(extractLxParts(resource, tier, first, last));
		} else if (tierName.equals("orthographic") && (orthoNum < 0 || orthoNum == num)) {
		    orthoNum = num;
		    orthoParts.addAll(extractOrthoParts(resource, tier, first, last));
		} else if (tierName.equals("signplaces") && (imageNum < 0 || imageNum == num)) {
		    imageNum = num;
		    imageParts.addAll(extractImageParts(resource, tier, first, last));
		}
		params.addFootnotes(footnotes);
	    }
	}
	if (lxParts.isEmpty())
	    ortho = orthoOf(orthoParts);
	else {
	    Vector<AlignedLexOrtho> combined = AlignedLexOrtho.align(lxParts, orthoParts);
	    lxortho = lxorthoOf(combined);
	}

	if (hi.equals("") && al.equals("") && tr.equals(""))
	    return null;
	else {
	    Section sec = new Section(hi, al, tr, marker);
	    sec.ortho = ortho;
	    sec.lxortho = lxortho;
	    sec.notes = params.getPendingNotes();
	    if (imageParts.isEmpty())
		gatherLexical(lxParts);
	    else
		gatherLexical(lxParts, imageParts);
	    return sec;
	}
    }

    // Concatenate all hieroglyphic, into ResLite.
    private String hiOf(TextResource resource, Tier tier, int i, int j) {
	Vector<ResFragment> fragments = new Vector<ResFragment>();
        int startPart = tier.positionToPart[i];
        int fromIndex = i - tier.partToPosition[startPart];
        int endPart = tier.positionToPart[j>0 ? j-1 : j];
        int toIndex = j - tier.partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
            TierPart p = tier.parts[part];
            fragments.add(extractHi(p, fromIndex, p.nSymbols(), tier.nSymbols()));
            fromIndex = 0;
            part++;
        }
        TierPart pLast = tier.parts[part];
        fragments.add(extractHi(pLast, fromIndex, toIndex, tier.nSymbols()));
	ResFragment res = new ResFragment();
	for (ResFragment frag : fragments) 
	    res = ResComposer.append(res, frag);
	FormatFragment formatted = new FormatFragment(res, params.hieroContext);
	return formatted.toResLite().toString();
    }

    // Concatenate all text.
    private String textOf(TextResource resource, Tier tier, int i, int j) {
	StringBuffer buf = new StringBuffer();
        int startPart = tier.positionToPart[i];
        int fromIndex = i - tier.partToPosition[startPart];
        int endPart = tier.positionToPart[j>0 ? j-1 : j];
        int toIndex = j - tier.partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
            TierPart p = tier.parts[part];
	    buf.append(extractText(p, fromIndex, p.nSymbols()));
            fromIndex = 0;
            part++;
        }
        TierPart pLast = tier.parts[part];
	buf.append(extractText(pLast, fromIndex, toIndex));
	return buf.toString();
    }

    private ResFragment extractHi(TierPart part, int i, int j, int len) {
	if (part instanceof HiPdfPart && i != j) {
	    HiPdfPart hiPart = (HiPdfPart) part;
	    // get rid of footnotes.
	    hiPart = new HiPdfPart(hiPart.hi, false);
	    ResFragment res = hiPart.parsed;
	    int groupI = i == 0 ? 0 : res.glyphToGroup(i);
	    int groupJ = res.glyphToGroup(j) + (j == part.nSymbols() ? 1 : 0);
	    ResFragment subRes = res.infixGroups(groupI, groupJ);
	    return subRes;
	} else
	    return new ResFragment();
    }

    private String extractText(TierPart part, int i, int j) {
	if (part instanceof NoPdfPart && i != j) {
	    NoPdfPart trPart = (NoPdfPart) part;
	    int indexI = trPart.indices[i];
	    int indexJ = trPart.indices[j-1] + 1;
	    String str = trPart.string.substring(indexI, indexJ);
	    return str +
		(trPart.hasTrailSpace(i, j) ||
		 trPart.getNext() == null || 
		 trPart.getNext().hasLeadSpace() ?
		 		" " : "");
	} else if (part instanceof AlPdfPart && i != j) {
	    AlPdfPart alPart = (AlPdfPart) part;
	    int indexI = alPart.indices[i];
	    int indexJ = alPart.indices[j-1] + 1;
	    String str = alPart.string.substring(indexI, indexJ);
	    return TransHelper.toUnicode(str, alPart.upper) + 
		(alPart.hasTrailSpace(i, j) ||
		 alPart.getNext() == null || 
		 alPart.getNext().hasLeadSpace() ?
		 		" " : "");
	} else if (part instanceof NotePdfPart && i != j) {
	    NotePdfPart notePart = (NotePdfPart) part;
	    return (notePart.getNext() != null && notePart.getNext().hasLeadSpace()) ? 
				" " : "";
	} else
	    return "";
    }

    private void gatherLexical(Vector<LxPdfPart> lParts) {
	for (LxPdfPart lPart : lParts)
	    vocab.gatherLexical(lPart);
    }
    private void gatherLexical(Vector<LxPdfPart> lParts, Vector<ImagePlacePdfPart> places) {
	Vector<AlignedLexPlaces> combineds = AlignedLexPlaces.align(lParts, places);
	for (AlignedLexPlaces combined : combineds) 
	    vocab.gatherLexical(combined.lxPart, combined.places);
    }

    private Vector<LxPdfPart> extractLxParts(TextResource resource, Tier tier, int i, int j) {
	Vector<LxPdfPart> parts = new Vector<LxPdfPart>();
        int startPart = tier.positionToPart[i];
        int fromIndex = i - tier.partToPosition[startPart];
        int endPart = tier.positionToPart[j>0 ? j-1 : j];
        int toIndex = j - tier.partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
            TierPart p = tier.parts[part];
	    parts.addAll(extractLxParts(p, fromIndex, p.nSymbols()));
            fromIndex = 0;
            part++;
        }
        TierPart pLast = tier.parts[part];
	parts.addAll(extractLxParts(pLast, fromIndex, toIndex));
	return parts;
    }

    private Vector<LxPdfPart> extractLxParts(TierPart part, int i, int j) {
	Vector<LxPdfPart> parts = new Vector<LxPdfPart>();
	if (part instanceof LxPdfPart && i != j)
	    parts.add((LxPdfPart) part);
	return parts;
    }

    private String orthoOf(Vector<OrthoPdfPart> parts) {
        StringBuffer buf = new StringBuffer();
	for (OrthoPdfPart oPart : parts)
	    buf.append(orthoToString(oPart));
	return buf.toString();
    }

    private String lxorthoOf(Vector<AlignedLexOrtho> combineds) {
        StringBuffer buf = new StringBuffer();
	for (AlignedLexOrtho combined : combineds)
	    buf.append(lxorthoToString(combined));
	return buf.toString();
    }

    private Vector<OrthoPdfPart> extractOrthoParts(TextResource resource, Tier tier, int i, int j) {
	Vector<OrthoPdfPart> parts = new Vector<OrthoPdfPart>();
        int startPart = tier.positionToPart[i];
        int fromIndex = i - tier.partToPosition[startPart];
        int endPart = tier.positionToPart[j>0 ? j-1 : j];
        int toIndex = j - tier.partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
            TierPart p = tier.parts[part];
	    parts.addAll(extractOrthoParts(p, fromIndex, p.nSymbols()));
            fromIndex = 0;
            part++;
        }
        TierPart pLast = tier.parts[part];
	parts.addAll(extractOrthoParts(pLast, fromIndex, toIndex));
	return parts;
    }

    private Vector<OrthoPdfPart> extractOrthoParts(TierPart part, int i, int j) {
	Vector<OrthoPdfPart> parts = new Vector<OrthoPdfPart>();
	if (part instanceof OrthoPdfPart && i != j)
	    parts.add((OrthoPdfPart) part);
	return parts;
    }

    private String orthoToString(OrthoPdfPart oPart) {
	StringBuffer buf = new StringBuffer();
	String al = oPart.textal;
	buf.append("<div>\n");
	buf.append("<span class=\"al\">" +
		TransHelper.toUnicode(al) + "</span>");
	buf.append("\n<ul class=\"ortho\">\n");
	buf.append(orthoString(oPart));
	buf.append("</ul>\n");
	buf.append("</div>\n");
	return buf.toString();
    }

    // Convert lexical information plus orthographic information to string.
    private String lxorthoToString(AlignedLexOrtho aligned) {
	StringBuffer buf = new StringBuffer();
	LxPdfPart lxPart = aligned.lxPart;
	Vector<OrthoPdfPart> oParts = aligned.orthoParts;
	buf.append("<div>\n");
	buf.append(lxString(lxPart));
	if (!oParts.isEmpty()) {
	    buf.append("\n<ul class=\"ortho\">\n");
	    for (OrthoPdfPart oPart : oParts)
		buf.append(orthoString(oPart));
	    buf.append("</ul>\n");
	}
	buf.append("</div>\n");
	return buf.toString();
    }

    // Convert lexical information only to string.
    private String lxString(LxPdfPart lx) {
	StringBuffer buf = new StringBuffer();
	String al = lx.textal;
	String tr = !lx.texttr.equals("") ? lx.texttr :
	    !lx.dicttr.equals("") ? lx.dicttr : lx.keytr;
	if (lx.texthiFormat != null) {
	    getHiSpan(lx.texthiFormat.toResLite().toString(), buf);
	    buf.append(" ");
	}
	buf.append("<span class=\"al\">" +
		TransHelper.toUnicode(al) + "</span> ");
	buf.append("<span class=\"tr\">" + tr + "</span>");
	return buf.toString();
    }

    // Convert orthographic information only to string.
    private String orthoString(OrthoPdfPart oPart) {
	StringBuffer buf = new StringBuffer();
	ResFragment hi = oPart.texthiParsed;
	FormatFragment hiForm = new FormatFragment(hi, params.hieroContext);
	Vector<ResNamedglyph> glyphs = hiForm.glyphs();
	for (OrthoElem elem : oPart.textortho) {
	    buf.append("<li>");
	    buf.append(orthoString(elem, glyphs));
	    buf.append("</li>\n");
	}
	return buf.toString();
    }
    private String orthoString(OrthoElem elem, Vector<ResNamedglyph> glyphs) {
	StringBuffer buf = new StringBuffer();
	String name = elem.name();
	int[] signs = elem.signs();
	String arg = elem.argValue();
	String argName = elem.argName();
	if (signs != null) {
	    for (int k = 0; k < signs.length; k++) {
		int sign = signs[k];
		if (sign >= 0 && sign < glyphs.size()) {
		    FormatNamedglyph glyph = (FormatNamedglyph) glyphs.get(sign);
		    int place = glyph.place.index;
		    buf.append("<span class=\"hi\">");
		    buf.append("&#" + place + ";");
		    buf.append("</span> ");
		}
	    }
	    buf.append("<span class=\"fo\">" + name + "</span>"); 
	}
	if (arg != null && !arg.equals("")) {
	    if (argName.equals("lit") || argName.equals("word"))
		buf.append(": <span class=\"al\">" +
			TransHelper.toUnicode(arg) + "</span>");
	    else
		buf.append(": " + arg);
	}
	return buf.toString();
    }

    private Vector<ImagePlacePdfPart> extractImageParts(TextResource resource, Tier tier, int i, int j) {
        Vector<ImagePlacePdfPart> parts = new Vector<ImagePlacePdfPart>();
        int startPart = tier.positionToPart[i];
        int fromIndex = i - tier.partToPosition[startPart];
        int endPart = tier.positionToPart[j>0 ? j-1 : j];
        int toIndex = j - tier.partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
            TierPart p = tier.parts[part];
            parts.addAll(extractImageParts(p, fromIndex, p.nSymbols()));
            fromIndex = 0;
            part++;
        }
        TierPart pLast = tier.parts[part];
        parts.addAll(extractImageParts(pLast, fromIndex, toIndex));
        return parts;
    }

    private Vector<ImagePlacePdfPart> extractImageParts(TierPart part, int i, int j) {
        Vector<ImagePlacePdfPart> parts = new Vector<ImagePlacePdfPart>();
        if (part instanceof ImagePlacePdfPart && i != j)
            parts.add((ImagePlacePdfPart) part);
        return parts;
    }

    // At least one span is real content (not e.g. coordinate).
    private boolean includesContent(Vector<TierSpan> spans) {
        for (int i = 0; i < spans.size(); i++) {
            TierSpan span = spans.get(i);
            if (span.includesContent())
                return true;
        }
        return false;
    }

    // First position of first span.
    private int first(Vector<TierSpan> spans) {
	return spans.get(0).fromPos;
    }
    // Last position of last span.
    private int last(Vector<TierSpan> spans) {
	return spans.get(spans.size()-1).toPos;
    }

    // Get text between markers.
    // For extra material, there will be an id. If no extra
    // material, id is < 0;
    public void getInterlinear(StringBuffer buf, String fromMarker, String toMarker) {
	int fromNum = markerSecNum.get(fromMarker) == null ? 
	    0 : markerSecNum.get(fromMarker);
	int toNum = markerSecNum.get(toMarker) == null ? 
	    sections.size() : markerSecNum.get(toMarker);
	for (int i = fromNum; i < toNum; i++) {
	    Section sec = sections.get(i);
	    boolean popup = !sec.ortho.equals("") || !sec.lxortho.equals("") ||
		!sec.notes.isEmpty();
	    int extraId = popup ? i : -1;
	    getInterlinear(sec, buf, extraId);
	}
    }

    private void getInterlinear(Section section, StringBuffer buf, int i) {
	buf.append("<div class=\"frag\">\n");
	getHi(section.hi, buf, i);
	getAl(section.al, buf);
	getTr(section.tr, buf);
	buf.append("</div>\n");
    }

    // Get hieroglyphic in ResLite.
    // Place reference if there is extra material for section.
    private void getHi(String hi, StringBuffer buf, int i) {
	if (hi.equals(""))
	    return;
	buf.append("<div class=\"hi\">\n");
	buf.append("<canvas class=\"res\">");
	buf.append(hi);
	buf.append("</canvas>\n");
	if (i >= 0) {
	    buf.append("<a href=\"phrase" + i + "\" " +
		    "title=\"show more\" class=\"popupref\">&#9072;</a>");
	}
	buf.append("</div>\n");
    }
    // As above, but in span.
    private void getHiSpan(String hi, StringBuffer buf) {
	buf.append("<span class=\"hi\">");
	buf.append("<canvas class=\"res\">");
	buf.append(hi);
	buf.append("</canvas>");
	buf.append("</span>\n");
    }

    // Get transliteration.
    private void getAl(String al, StringBuffer buf) {
	buf.append("<div class=\"al\">");
	buf.append(al);
	buf.append("</div>\n");
    }

    // Get translation.
    private void getTr(String tr, StringBuffer buf) {
	buf.append("<div class=\"tr\">");
	buf.append(tr);
	buf.append("</div>\n");
    }

    ////////////////////////////////////////////////////
    // Popup text.

    public void getPopupText(StringBuffer buf) {
	for (int i = 0; i < sections.size(); i++) {
	    Section sec = sections.get(i);
	    buf.append("<div id=\"phrase" + i + "\" class=\"hidden_popup\">\n");
	    if (i > 0)
		buf.append("<a href=\"phrase" + (i-1) + "\" " +
			"title=\"previous phrase\" " +
			"class=\"popuparrow\">&#8593;</a>\n");
	    getNotesText(buf, sec.notes);
	    buf.append(sec.lxortho);
	    buf.append(sec.ortho);
	    if (i < sections.size() -1)
		buf.append("<a href=\"phrase" + (i+1) + "\" " +
			"title=\"next phrase\" " +
			"class=\"popuparrow\">&#8595;</a>\n");
	    buf.append("</div>\n");
	}
    }

    // Write all footnotes in section.
    private void getNotesText(StringBuffer buf, Vector<Footnote> notes) {
	if (!notes.isEmpty()) {
	    buf.append("<ul>\n");
	    for (Footnote note : notes) {
		buf.append("<li>");
		Tier tier = note.getTier();
		getNoteText(buf, tier);
		buf.append("</li>\n");
	    }
	    buf.append("</ul>\n");
	}
    }

    // Write elements of footnote.
    private void getNoteText(StringBuffer buf, Tier tier) {
	for (int i = 0; i < tier.parts.length; i++) {
	    TierPart part = tier.parts[i];
	    if (part instanceof NoPdfPart) {
		NoPdfPart noPart = (NoPdfPart) part;
		buf.append(noPart.string);
	    } else if (part instanceof AlPdfPart) {
		AlPdfPart alPart = (AlPdfPart) part;
		buf.append("<span class=\"al\">");
		buf.append(TransHelper.toUnicode(alPart.string, alPart.upper));
		buf.append("</span>");
	    }
	}
    }

    ////////////////////////////////////////////////////
    // Vocabulary.

    public Vocab getVocab() {
	return vocab;
    }

}
