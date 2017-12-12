/***************************************************************************/
/*                                                                         */
/*  Exporter.java                                                          */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Exporter to PDF.

package nederhof.interlinear.frame.pdf;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
// import com.itextpdf.text.HeaderFooter; // Not used?
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RomanList;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfWriter;

import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

public class Exporter implements FormatListener {

    // The parameters for rendering.
    private PdfRenderParameters params;

    // Name of text.
    private String textName;

    // The resources being viewed.
    private Vector<TextResource> resources;
    // The precedence resources.
    private Vector<ResourcePrecedence> precedences;

    // The tiers.
    private Vector<Tier> tiers = new Vector<Tier>();
    // Numbers of tiers within resources.
    private Vector<Integer> tierNums = new Vector<Integer>();
    // Short names of tiers.
    private Vector<String> labels = new Vector<String>();
    // Versions of tiers.
    private Vector<String> versions = new Vector<String>();
    // Vector of vectors of positions where phrases start.
    private Vector<Vector<Integer>> phraseStarts = new Vector<Vector<Integer>>();
    // Resource for each tier.
    private Vector<TextResource> tierResources = new Vector<TextResource>();

    // Is there more than one tier that is shown?
    private boolean severalTiersShown = false;

    // Where to place labels and versions. Negative if none.
    private float labelLocation = 0;
    private float versionLocation = 0;
    // Where to place text.
    private float textLocation = 0;

    // The sections that result from formatting.
    private Vector sections = new Vector();

    // Page numberer.
    private PageNumberer pageNumberer = new PageNumberer(true);

    public Exporter(String textName, Vector<TextResource> resources, 
	    Vector<ResourcePrecedence> precedences,
	    Vector<Object[]> autoaligns, Autoaligner autoaligner,
	    PdfRenderParameters params) {
	this.textName = textName;
	this.resources = resources;
	this.precedences = precedences;
	this.params = params;
        TierGather gather = new TierGather(resources, precedences, autoaligns,
		autoaligner, params, true, false);
        tiers = gather.tiers;
	tierNums = gather.tierNums;
        labels = gather.labels;
        versions = gather.versions;
        phraseStarts = gather.phraseStarts;
	tierResources = gather.tierResources;
        severalTiersShown = gather.severalTiersShown;
	params.setListener(this);
    }

    // Exporter to PDF without user interaction.
    public static void export(Text text, Vector<ResourceGenerator> resourceGenerators,
	    Autoaligner autoaligner,
	    PdfRenderParameters params) {
	Vector<TextResource> resources = new Vector<TextResource>();
	for (int i = 0; i < text.getResources().size(); i++) {
	    String file = text.getResources().get(i);
	    TextResource resource =
		IndexPane.toResource(file, resourceGenerators);
	    if (resource != null)
		resources.add(resource);
	}

	Vector<ResourcePrecedence> precedences = new Vector<ResourcePrecedence>();
	for (int i = 0; i < text.getPrecedences().size(); i++) {
	    String[] prec = text.getPrecedences().get(i);
	    ResourcePrecedence precedence = 
		IndexPane.toPrecedence(prec, resources);
	    if (precedence != null)
		precedences.add(precedence);
	}

	Vector<Object[]> autoaligns = new Vector<Object[]>();
	for (int i = 0; i < text.getAutoaligns().size(); i++) {
	    String[] autoString = text.getAutoaligns().get(i);
	    Object[] autoResource = IndexPane.toAutoalign(autoString, resources);
	    if (autoResource != null)
		autoaligns.add(autoResource);
	}

	Exporter exporter =
	    new Exporter(text.getName(), resources, precedences, autoaligns, 
		    autoaligner, params);
	exporter.reformat();
    }

    // Format text.
    public void reformat() {
	params.computeFonts();
	try {
	    openPdf();
	    writePreamble();
	    makeLayout();
	    params.initiateSituation();
	    writePages();
	    closePdf();
	} catch (DocumentException e) {
	    JOptionPane.showMessageDialog(null, e.getMessage());
	}
    }

    // Create PDF file.
    private void openPdf() throws DocumentException {
	params.doc =
	    new Document(params.pageSize,
		    params.leftMargin,
		    params.rightMargin,
		    params.topMargin,
		    params.bottomMargin);
	try {
	    params.writer =
		PdfWriter.getInstance(params.doc, 
			new FileOutputStream(params.file));
	    annotatePdf();
	    params.doc.open();
	    params.surface = params.writer.getDirectContent();
	} catch (IOException e) {
	    throw new DocumentException(e.getMessage());
	}
	Rectangle rec = params.doc.getPageSize();
	params.pageWidth = rec.getWidth() - params.rightMargin - params.leftMargin;
	params.pageHeight = rec.getHeight() - params.topMargin - params.bottomMargin;
    }

    // Close PDF file.
    private void closePdf() {
	params.doc.close();
    }

    // Insert meta data in PDF file.
    private void annotatePdf() {
	params.doc.addTitle(params.header);
	String authors = params.getAuthors(resources);
	if (authors != null)
	    params.doc.addAuthor(authors);
	params.doc.addSubject("Formatted resources");
	params.doc.addCreator("Philolog");
    }

    // Write descriptions of resources.
    private void writePreamble() throws DocumentException {
	params.writer.setPageEvent(pageNumberer);
	params.pageY = params.pageHeight;
	params.surface.beginText();
	Vector headerParagraph = new Vector();
	headerParagraph.add(params.getTitlePdfPart(textName));
	writePreambleParagraph(headerParagraph);
        for (int i = 0; i < resources.size(); i++) {
            TextResource resource = (TextResource) resources.get(i);
	    if (isShown(resource)) {
		Vector titleParagraph = new Vector();
		titleParagraph.add(params.getNamePdfPart(resource.getName()));
		writePreambleParagraph(titleParagraph);
		PdfDestination dest = new PdfDestination(PdfDestination.XYZ,
			0, params.pageY, 0);
		params.surface.localDestination("resource" + i, dest);
		Vector contentParagraphs = resource.pdfPreamble(params);
		for (int j = 0; j < contentParagraphs.size(); j++) {
		    Vector contentParagraph = (Vector) contentParagraphs.get(j);
		    writePreambleParagraph(contentParagraph);
		}
	    }
        }
	params.surface.endText();
	params.doc.newPage();
    }

    // Write one paragraph of the preamble.
    private void writePreambleParagraph(Vector par) throws DocumentException {
	if (par.size() == 0)
	    return;
	Vector<Tier> tiers = new Vector<Tier>();
	tiers.add(new Tier(par));
	LineMaker maker = new LineMaker(tiers);
	Vector<Line> lines = maker.lines;
	for (int i = 0; i < lines.size(); i++) {
	    Line line = lines.get(i);
	    if (params.pageY - line.leadingAscent() - line.descent() <
		    params.bottomMargin)
		breakPage();
	    line.print();
	}
	params.pageY -= params.preambleParSep();
    }

    // Write formatted paragraphs on pages.
    private void writePages() throws DocumentException {
	PdfPageLabels pageLabels = new PdfPageLabels();
	pageLabels.addPageLabel(1, PdfPageLabels.LOWERCASE_ROMAN_NUMERALS);
	pageLabels.addPageLabel(params.writer.getPageNumber(),
		PdfPageLabels.DECIMAL_ARABIC_NUMERALS);
	params.writer.setPageLabels(pageLabels);
	params.doc.setPageCount(1);
	pageNumberer.setRoman(false);
	// params.writer.setPageEvent(new PageNumberer(false));
	params.pageY = params.pageHeight + params.bottomMargin;
	params.surface.beginText();
	interlinearFormat();
	params.surface.endText();
    }

    // Do interlinear formatting.
    private void interlinearFormat() throws DocumentException {
	new InterlinearFormatting(tiers) {
	    protected float width() {
		return params.pageWidth - textLocation;
	    }
	    protected boolean processSection(String[] modes, 
		    Vector<TierSpan>[] sectionSpans,
		    TreeMap<Integer,Float>[] sectionSpanLocations) {
		Section sect = makeSection(modes,
			sectionSpans, sectionSpanLocations);
		if (sect != null)
		    sections.add(sect);
		return true;
	    }
	};
	Section footnoteSect = makeFootnoteSection();
	if (footnoteSect != null)
	    sections.add(footnoteSect);
	for (int i = 0; i < sections.size(); i++) {
	    Section sect = (Section) sections.get(i);
	    addSectionToPage(sect);
	}
    }

    // Add section to document.
    // If paragraph fits on page, then add it to page.
    // If it doesn't fit and page is non-empty, go to next page.
    // Else write some of it on present page, and rest on next page.
    private void addSectionToPage(Section sect) throws DocumentException {
	if (params.pageY - sect.height < params.bottomMargin) {
	    if (params.pageY < params.pageHeight + params.bottomMargin) {
		params.surface.endText();
		params.doc.newPage();
		params.pageY = params.pageHeight + params.bottomMargin;
		params.surface.beginText();
	    }
	}
	sect.print();
    }

    // Determine layout of text page.
    public void makeLayout() {
        boolean severalLabels = (new TreeSet(labels)).size() > 1;
        boolean severalVersions = (new TreeSet(versions)).size() > 1;
        float labelWidth = 0;
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            labelWidth = Math.max(labelWidth, widthOf(label));
        }
        float versionWidth = 0;
        for (int i = 0; i < versions.size(); i++) {
            String version = versions.get(i);
            versionWidth = Math.max(versionWidth, widthOf(version));
        }
        float start = params.leftMargin;
        if (severalLabels) {
            labelLocation = start;
            start += labelWidth + params.colSep;
        } else
            labelLocation = -1;
        if (severalVersions) {
            versionLocation = start;
            start += versionWidth + params.colSep;
        } else
            versionLocation = -1;
        textLocation = start;
    }

    // Width of string in label.
    private float widthOf(String s) {
	return params.boldFont.getWidthPointKerned(s, params.boldSize);
    }

    // Combine result from formatting with labels.
    private Section makeSection(String[] modes,
	    Vector<TierSpan>[] sectionSpans, TreeMap<Integer,Float>[] sectionSpanLocations) {
        Vector<Line> lines = new Vector<Line>();
        params.resetMarker();
        for (int i = 0; i < sectionSpans.length; i++) {
	    String mode = modes[i];
            Vector<TierSpan> spans = sectionSpans[i];
            Vector footnotes = FootnoteHelper.footnotes(spans);
	    if (!mode.equals(TextResource.OMITTED)) {
		if (includesContent(spans)) {
		    TreeMap<Integer,Float> locs = sectionSpanLocations[i];
		    TextResource resource = tierResources.get(i);
		    String label = labels.get(i);
		    String version = versions.get(i);
		    lines.add(new Line(resource, label, version, spans, locs));
		}
		params.addFootnotes(footnotes);
	    }
        }
        if (!params.collectNotes) {
            lines.addAll(makeFootnoteLines());
        }
	if (!lines.isEmpty())
	    return new Section(lines);
	else
	    return null;
    }

    // Make section of footnotes.
    private Section makeFootnoteSection() {
        Vector lines = makeFootnoteLines();
        if (lines.isEmpty())
            return null;
        else
            return new Section(lines);
    }

    // Make lines of footnotes.
    private Vector<Line> makeFootnoteLines() {
        Vector<Line> lines = new Vector<Line>();
        Vector notes = params.getPendingNotes();
        params.initiateSituation();
        for (int i = 0; i < notes.size(); i++) {
            Footnote note = (Footnote) notes.get(i);
            Tier tier = note.getTier();
            Vector<Tier> tiers = new Vector<Tier>();
            tiers.add(tier);
            LineMaker maker = new LineMaker(tiers);
            lines.addAll(maker.lines);
        }
        return lines;
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

    // Format tiers and output lines. Used for footnotes.
    private class LineMaker {
        // Collected lines
        public Vector<Line> lines = new Vector<Line>();

        public LineMaker(Vector<Tier> tiers) {
            new InterlinearFormatting(tiers) {
                protected float width() {
		    return params.pageWidth;
                }
                protected boolean processSection(String[] modes,
			Vector<TierSpan>[] sectionSpans,
                        TreeMap<Integer,Float>[] sectionSpanLocations) {
                    for (int i = 0; i < sectionSpans.length; i++) {
                        Vector<TierSpan> spans = sectionSpans[i];
                        if (spans.size() > 0) {
                            TreeMap locs = sectionSpanLocations[i];
                            lines.add(new Line(spans, locs));
                        }
                    }
                    return true;
                }
            };
        }
    }

   // Line contains label, version, and spans.
    private class Line {
	// Footnote and preamble start at left end of page.
        public boolean atLeft = false;
        public TextResource resource;
        public String label;
        public String version;
        public Vector<TierSpan> spans;
        public TreeMap locs;

        // For normal line.
        public Line(TextResource resource,
		String label, String version, Vector<TierSpan> spans, TreeMap locs) {
	    this.resource = resource;
            this.label = label;
            this.version = version;
            this.spans = spans;
            this.locs = locs;
        }
        // For footnote or preamble.
        public Line(Vector<TierSpan> spans, TreeMap locs) {
            this.spans = spans;
            this.locs = locs;
            atLeft = true;
        }

        public float leadingAscent() {
            float max = labelLocation >= 0 ?
		params.boldSize * 0.2f +
		params.boldFont.getFontDescriptor(BaseFont.ASCENT, params.boldSize) : 0;
            for (int i = 0; i < spans.size(); i++) {
                TierSpan span = spans.get(i);
                max = Math.max(max, span.leadingAscent());
            }
            return max;
        }
        public float descent() {
            float max = labelLocation >= 0 ?
		params.boldFont.getFontDescriptor(BaseFont.DESCENT, params.boldSize) : 0;
            for (int i = 0; i < spans.size(); i++) {
                TierSpan span = spans.get(i);
                max = Math.max(max, span.descent());
            }
            return max;
        }
        public void print() {
	    params.pageY -= leadingAscent();
            if (!atLeft && labelLocation >= 0) {
                drawString(label, params.boldFont, params.boldSize,
			labelLocation, params.pageY, 
			Settings.pdfLabelColorDefault);
		float width = textLocation - labelLocation;
		float height = descent() + leadingAscent();
		params.surface.localGoto("resource" + resourceNum(), 
			labelLocation, params.pageY - descent(),
			labelLocation + width, params.pageY + height);
	    }
            if (!atLeft && versionLocation >= 0)
                drawString(version, params.plainFont, params.plainSize,
			versionLocation, params.pageY, 
			Settings.pdfVersionColorDefault);
            for (int i = 0; i < spans.size(); i++) {
                TierSpan span = spans.get(i);
                Float xFloat = (Float) locs.get(new Integer(span.fromPos));
                if (xFloat != null) {
                    float x = xFloat.floatValue();
                    if (atLeft)
                        span.draw(params.surface, params.leftMargin + x, params.pageY);
		    else
                        span.draw(params.surface, textLocation + x, params.pageY);
                }
            }
	    params.pageY -= descent();
        }

	private int resourceNum() {
	    for (int i = 0; i < resources.size(); i++) 
		if (resources.get(i) == resource)
		    return i;
	    return 0;
	}

        private void drawString(String s, BaseFont font, float size, 
		float x, float y, Color color) {
	    params.surface.setFontAndSize(font, size);
	    if (params.color)
		params.surface.setColorFill(toBase(color));
	    else
		params.surface.setColorFill(BaseColor.BLACK);
	    params.surface.setTextMatrix(x, y);
	    params.surface.showTextKerned(s);
        }

    }

    // A section contains a number of lines, the y's are
    // the vertical offset from top. The lines are input
    // as vector.
    private class Section {
        public float height;
        private Vector<Line> lines;
        private int nLines;

        // Prepare section for showing text and buttons.
        public Section(Vector<Line> lines) {
            if (severalTiersShown)
                height = params.sectionSep / 2;
            else
                height = params.lineSep / 2;
            this.lines = lines;
            nLines = lines.size();
            for (int i = 0; i < nLines; i++) {
                Line line = lines.get(i);
                height += line.leadingAscent() + line.descent();
                if (i < nLines - 1) {
                    if (line.atLeft)
                        height += params.footnoteLineSep;
                    else
                        height += params.lineSep;
                }
            }
            if (severalTiersShown)
                height += params.sectionSep / 2;
            else
                height += params.lineSep / 2;
	}

	// Add section to page. In rare cases split over several pages.
        public void print() throws DocumentException {
	    if (params.pageY == params.pageHeight + params.bottomMargin) {
		if (severalTiersShown) {
		    printLine();
		    params.pageY -= params.sectionSep / 2;
		} 
	    } else {
		if (severalTiersShown)
		    params.pageY -= params.sectionSep / 2;
		else
		    params.pageY -= params.lineSep / 2;
	    }
            for (int i = 0; i < nLines; i++) {
                Line line = lines.get(i);
		if (params.pageY - line.leadingAscent() - line.descent() <
			params.bottomMargin)
		    breakPage();
                line.print();
		if (i < nLines - 1) {
		    if (line.atLeft)
			params.pageY -= params.footnoteLineSep;
		    else
			params.pageY -= params.lineSep;
		}
            }
            if (severalTiersShown) {
		params.pageY -= params.sectionSep / 2;
		printLine();
            } else 
		params.pageY -= params.lineSep / 2;
        }

    }

    // Thickness of line separating sections.
    private static final float lineThickness = 1.0f;

    // Print horizontal line to separate sections.
    private void printLine() throws DocumentException {
	params.surface.endText();
	params.surface.setColorStroke(BaseColor.BLACK);
	params.surface.setLineWidth(lineThickness);
	params.surface.moveTo(params.leftMargin, params.pageY);
	params.surface.lineTo(params.leftMargin + params.pageWidth, 
		params.pageY);
	params.surface.stroke();
	params.surface.beginText();
    }

    // Break page in middle of section.
    // Avoid loop by not breaking new page.
    private void breakPage() throws DocumentException {
	if (params.pageY < params.pageHeight + params.bottomMargin) {
	    params.surface.endText();
	    params.doc.newPage();
	    params.pageY = params.pageHeight + params.bottomMargin;
	    params.surface.beginText();
	}
    }

    ///////////////////////////////////////////////
    // Access to resources.

    // At least one tier that is shown?
    private boolean isShown(TextResource resource) {
	for (int i = 0; i < resource.nTiers(); i++) 
	    if (!resource.getMode(i).equals(TextResource.IGNORED) && 
		    !resource.getMode(i).equals(TextResource.OMITTED) && 
		    !resource.isEmptyTier(i)) 
		return true;
	return false;
    }

    ////////////////////////////////////////////////
    // Auxiliary.


    // Colors need to be converted to BaseColor for PDF.
    public BaseColor toBase(Color color) {
        return new BaseColor(color.getRed(),
                color.getGreen(), color.getBlue(), color.getAlpha());
    }

}
