/***************************************************************************/
/*                                                                         */
/*  PdfExport.java                                                         */
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

// Format text and export to PDF.

package nederhof.align;

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
import com.itextpdf.text.factories.RomanNumberFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfWriter;

import nederhof.fonts.*;
import nederhof.res.*;

class PdfExport {

    // While formatting and writing to PDF, we need a need a number
    // of values, some of which remain constant, others change while
    // writing the pages of the PDF file.
    private static class ExportVariables implements RenderContext, ITextFontMapper {

	// The window from which the export was called.
	// May be null if no GUI was used.
	public JFrame callingWindow = null;

	// System to be formatted.
	// File where pdf to be saved.
	// Header of document.
	public StreamSystem system;
	public File file;
	public String header;

	// Space between version label and text.
	// Vertical space between paragraphs and between lines within paragraphs.
	// Vertical space between lines of footnotes.
	// All lines to be made of uniform ascent w.r.t. points.
	// Footnotes collected at end.
	// Color in Pdf.
	public int colSep = Settings.pdfColSepDefault;
	public int parSep = Settings.pdfParSepDefault;
	public int lineSep = Settings.pdfLineSepDefault;
	public int footnoteLineSep = Settings.pdfFootnoteLineSepDefault;
	public boolean uniformAscent = Settings.pdfUniformAscentDefault;
	public boolean collectNotes = Settings.pdfCollectNotesDefault;
	public boolean pdfColor = Settings.pdfColorDefault;

	// PDF Page format.
	public com.itextpdf.text.Rectangle pageSize = Settings.pdfPageSizeDefault;
	public int topMargin = Settings.pdfTopMarginDefault;
	public int leftMargin = Settings.pdfLeftMarginDefault;
	public int bottomMargin = Settings.pdfBottomMarginDefault;
	public int rightMargin = Settings.pdfRightMarginDefault;

	// Options for rendering lexical entries.
	public int lxSep = Settings.pdfLxSepDefault;
	public int lxLeading = Settings.pdfLxLeadingDefault;
	public int lxInnerMargin = Settings.pdfLxInnerMarginDefault;
	public int lxLineThickness = Settings.pdfLxLineThicknessDefault;
	public boolean lxAbbreviated = Settings.pdfLxAbbreviatedDefault;

	// Latin.
	public String latinFontName = Settings.pdfLatinFontNameDefault;
	public Integer latinFontSize = Settings.pdfLatinFontSizeDefault;
	public BaseFont latinFont;
	public GeneralFontMetrics latinFontMetrics;
	public BaseFont footLatinFont;
	public GeneralFontMetrics footLatinFontMetrics;
	public BaseFont normalFont;
	public GeneralFontMetrics normalFontMetrics;
	public BaseFont italicFont;
	public GeneralFontMetrics italicFontMetrics;
	public BaseFont footItalicFont;
	public GeneralFontMetrics footItalicFontMetrics;
	public BaseFont header1Font;
	public GeneralFontMetrics header1FontMetrics;
	public BaseFont header2Font;
	public GeneralFontMetrics header2FontMetrics;
	public BaseFont header3Font;
	public GeneralFontMetrics header3FontMetrics;

	// Transliteration.
	public Integer egyptFontStyle = Settings.pdfEgyptFontStyleDefault;
	public Integer egyptFontSize = Settings.pdfEgyptFontSizeDefault;
	public BaseFont egyptFont;
	public GeneralFontMetrics egyptFontMetrics;
	public BaseFont footEgyptFont;
	public GeneralFontMetrics footEgyptFontMetrics;
	public TrMap egyptMap;

	// Hieroglyphic.
	public Integer hieroFontSize = Settings.pdfHieroFontSizeDefault;
	public HieroRenderContext hieroContext;
	public HieroRenderContext footHieroContext;
	public Integer hieroResolution = Settings.pdfHieroResolutionDefault;
	public DefaultFontMapper hieroFontMapper;

	// Footnotes.
	public Integer footFontSizeReduction = Settings.pdfFootFontSizeReductionDefault;

	// Get font metrics belonging to given font.
	public GeneralFontMetrics getFontMetrics(int f) {
	    switch (f) {
		case RenderContext.LATIN_FONT:
		    return latinFontMetrics;
		case RenderContext.FOOT_LATIN_FONT:
		    return footLatinFontMetrics;
		case RenderContext.NORMAL_FONT:
		    return normalFontMetrics;
		case RenderContext.ITALIC_FONT:
		    return italicFontMetrics;
		case RenderContext.FOOT_ITALIC_FONT:
		    return footItalicFontMetrics;
		case RenderContext.HEADER1_FONT:
		    return header1FontMetrics;
		case RenderContext.HEADER2_FONT:
		    return header2FontMetrics;
		case RenderContext.HEADER3_FONT:
		    return header3FontMetrics;
		case RenderContext.EGYPT_FONT:
		    return egyptFontMetrics;
		case RenderContext.FOOT_EGYPT_FONT:
		    return footEgyptFontMetrics;
		default:
		    return latinFontMetrics;
	    }
	}

	// Get basefont belonging to given font code.
	public BaseFont getBaseFont(int f) {
	    switch (f) {
		case RenderContext.LATIN_FONT:
		    return latinFont;
		case RenderContext.FOOT_LATIN_FONT:
		    return footLatinFont;
		case RenderContext.NORMAL_FONT:
		    return normalFont;
		case RenderContext.ITALIC_FONT:
		    return italicFont;
		case RenderContext.FOOT_ITALIC_FONT:
		    return footItalicFont;
		case RenderContext.HEADER1_FONT:
		    return header1Font;
		case RenderContext.HEADER2_FONT:
		    return header2Font;
		case RenderContext.HEADER3_FONT:
		    return header3Font;
		case RenderContext.EGYPT_FONT:
		    return egyptFont;
		case RenderContext.FOOT_EGYPT_FONT:
		    return footEgyptFont;
		default:
		    return latinFont;
	    }
	}

	// Get font size belonging to given font code.
	public float getFontSize(int f) {
	    switch (f) {
		case RenderContext.LATIN_FONT:
		    return latinFontSize.intValue();
		case RenderContext.FOOT_LATIN_FONT:
		    return latinFontSize.intValue() *
			footFontSizeReduction.intValue() / 100.0f;
		case RenderContext.NORMAL_FONT:
		    return latinFontSize.intValue();
		case RenderContext.ITALIC_FONT:
		    return latinFontSize.intValue();
		case RenderContext.FOOT_ITALIC_FONT:
		    return latinFontSize.intValue() *
			footFontSizeReduction.intValue() / 100.0f;
		case RenderContext.HEADER1_FONT:
		    return latinFontSize.intValue() * Settings.pdfHeader1Increase;
		case RenderContext.HEADER2_FONT:
		    return latinFontSize.intValue() * Settings.pdfHeader2Increase;
		case RenderContext.HEADER3_FONT:
		    return latinFontSize.intValue() * Settings.pdfHeader3Increase;
		case RenderContext.EGYPT_FONT:
		    return egyptFontSize.intValue();
		case RenderContext.FOOT_EGYPT_FONT:
		    return egyptFontSize.intValue() *
			footFontSizeReduction.intValue() / 100.0f;
		default:
		    return latinFontSize.intValue();
	    }
	}

	// Get mapping from ASCII to transliteration font.
	public TrMap getEgyptMap() {
	    return egyptMap;
	}

	// The amount that footnote markers are to be raised above baseline,
	// as factor of ascent.
	public float getFootRaise() {
	    return Settings.pdfFootRaiseDefault;
	}

	// Get environments for hieroglyphic.
	public HieroRenderContext getHieroContext() {
	    return hieroContext;
	}
	public HieroRenderContext getFootHieroContext() {
	    return footHieroContext;
	}

	// Get resolution for hieroglyphic pixel representation.
	public int getHieroResolution() {
	    return hieroResolution.intValue();
	}

	// Get mapper from fonts to basefonts.
	public DefaultFontMapper getHieroFontMapper() {
	    return hieroFontMapper;
	}

	// Is color allowed in hieroglyphic?
	public boolean hieroColor() {
	    return Settings.pdfColorDefault;
	}

	// Get Color to be used for points.
	public Color getPointColor() {
	    if (pdfColor)
		return Settings.pdfPointColorDefault;
	    else
		return Color.BLACK;
	}
	// Get Color to be used for labels.
	public Color getLabelColor() {
	    if (pdfColor)
		return Settings.pdfLabelColorDefault;
	    else
		return Color.BLACK;
	}
	// Get Color to be used for footnote markers.
	public Color getNoteColor() {
	    if (pdfColor)
		return Settings.pdfNoteColorDefault;
	    else
		return Color.BLACK;
	}

	// The horizontal separation between lexical entries.
	public float getLxSep() {
	    return lxSep;
	}
	// The vertical separation between lexical entries.
	public float getLxLeading() {
	    return lxLeading;
	}
	// The margin inside lexical entries.
	public float getLxInnerMargin() {
	    return lxInnerMargin;
	}
	// The width of lines around lexical entries.
	public float getLxLineThickness() {
	    return lxLineThickness;
	}
	// Of lexical entries only the textal part is printed.
	public boolean lxAbbreviated() {
	    return lxAbbreviated;
	}

	// Are lines to be made all uniform ascent, i.e. to be at least
	// as much as point marker.
	public boolean uniformAscent() {
	    return uniformAscent;
	}

	// All footnotes collected at end of text.
	public boolean collectNotes() {
	    return collectNotes;
	}

	//////////////////////////////////////////////////////////////////////////////
	
	// The PDF document and the writer.
	public Document doc;
	public PdfWriter writer;
	public PdfContentByte surface;

	// Dimensions of pages.
	public float pageWidth;
	public float pageHeight;

	// Width of indication of creator if present.
	public float creatorWidth;
	// Width of indication of version if present.
	public float versionWidth;

	// Position on current page.
	public float pageY;

	// Left boundary of page.
	public float leftBound() {
	    return leftMargin;
	}

	// Width of indication of creator, if any.
	public float creatorWidth() {
	    return creatorWidth;
	}

	// Where text starts.
	public float textOffset() {
	    if (creatorWidth + versionWidth > 0)
		return leftBound() + creatorWidth + versionWidth + colSep;
	    else
		return leftBound();
	}

	// Right boundary of page.
	public float rightBound() {
	    return pageWidth;
	}

	// Is there more than one file among selected streams?
	public boolean mentionCreator() {
	    return system.nSelectedFiles() > 1;
	}

	// Are labels of versions to be printed? Is there only one stream?
	public boolean mentionVersion() {
	    return system.nSelected() > 1;
	}

    }

    // Call that takes default values.
    public static void printPdf(StreamSystem system, String fileName, String header) {
	ExportVariables vars = new ExportVariables();
	vars.system = system;
	vars.file = new File(fileName + ".pdf");
	vars.header = header;
	export(vars);
    }

    // Call that overrides default values.
    public static void printPdf(JFrame callingWindow,
	    StreamSystem system, File file, String header,
	    int colSep, int parSep, int lineSep, int footnoteLineSep,
	    boolean uniformAscent, boolean collectNotes, boolean pdfColor,
	    com.itextpdf.text.Rectangle pageSize,
	    int topMargin, int leftMargin, int bottomMargin, int rightMargin,
	    int lxSep, int lxLeading, int lxInnerMargin, int lxLineThickness, 
	    boolean lxAbbreviated,
	    String latinFontName, Integer latinFontSize,
	    Integer egyptFontStyle, Integer egyptFontSize, 
	    Integer hieroFontSize, Integer hieroResolution) {
	ExportVariables vars = new ExportVariables();
	vars.callingWindow = callingWindow;
	vars.system = system;
	vars.file = file;
	vars.header = header;
	vars.colSep = colSep;
	vars.parSep = parSep;
	vars.lineSep = lineSep;
	vars.footnoteLineSep = footnoteLineSep;
	vars.uniformAscent = uniformAscent;
	vars.collectNotes = collectNotes;
	vars.pdfColor = pdfColor;
	vars.pageSize = pageSize;
	vars.topMargin = topMargin;
	vars.leftMargin = leftMargin;
	vars.bottomMargin = bottomMargin;
	vars.rightMargin = rightMargin;
	vars.lxSep = lxSep;
	vars.lxLeading = lxLeading;
	vars.lxInnerMargin = lxInnerMargin;
	vars.lxLineThickness = lxLineThickness;
	vars.lxAbbreviated = lxAbbreviated;
	vars.latinFontName = latinFontName;
	vars.latinFontSize = latinFontSize;
	vars.egyptFontStyle = egyptFontStyle;
	vars.egyptFontSize = egyptFontSize;
	vars.hieroFontSize = hieroFontSize;
	vars.hieroResolution = hieroResolution;
	export(vars);
    }

    // Prepare fonts, format, and write to file.
    private static void export(ExportVariables vars) {
	computeLatinFont(vars);
	computeEgyptFont(vars);
	vars.egyptMap = new TrMap(Settings.egyptMapFile);
	computeHieroFont(vars);
	computePreludeWidth(vars);
	try {
	    openPdf(vars);
	    writePreamble(vars);
	    writePages(vars);
	    closePdf(vars);
	} catch (DocumentException e) {
	    if (vars.callingWindow != null)
		JOptionPane.showMessageDialog(vars.callingWindow, e.getMessage());
	    else
		System.err.println(e.getMessage());
	}
    }

    // Compute Latin font. If the font is external, give special treatment.
    private static void computeLatinFont(ExportVariables vars) {
	int normalSize = vars.latinFontSize.intValue();
	float footSize = normalSize * vars.footFontSizeReduction.intValue() / 100.0f;
	float header1Size = normalSize * Settings.pdfHeader1Increase;
	float header2Size = normalSize * Settings.pdfHeader2Increase;
	float header3Size = normalSize * Settings.pdfHeader3Increase;

	computeLatinBaseFont(vars);

	vars.latinFontMetrics = new ITextFontMetrics(vars.latinFont, normalSize);
	vars.footLatinFontMetrics = new ITextFontMetrics(vars.footLatinFont, footSize);
	vars.normalFontMetrics = new ITextFontMetrics(vars.normalFont, normalSize);
	vars.italicFontMetrics = new ITextFontMetrics(vars.italicFont, normalSize);
	vars.footItalicFontMetrics = new ITextFontMetrics(vars.footItalicFont, footSize);
	vars.header1FontMetrics = new ITextFontMetrics(vars.header1Font, header1Size);
	vars.header2FontMetrics = new ITextFontMetrics(vars.header2Font, header2Size);
	vars.header3FontMetrics = new ITextFontMetrics(vars.header3Font, header3Size);
    }

    // Get base fonts for Latin. Give special treatment for external fonts.
    private static void computeLatinBaseFont(ExportVariables vars) {
	for (int i = 0; i < Settings.externalFonts.length; i++)
	    if (vars.latinFontName.equals(Settings.externalFonts[i].getName())) {
		ExternalFont external = Settings.externalFonts[i];
		String fFile = external.getPlain();
		String fItalicFile = external.getItalic();
		String fHeadFile;
		switch (Settings.pdfHeaderFontStyle) {
		    case com.itextpdf.text.Font.BOLD:
			fHeadFile = external.getBold();
		    case com.itextpdf.text.Font.ITALIC:
			fHeadFile = external.getItalic();
		    case com.itextpdf.text.Font.BOLDITALIC:
			fHeadFile = external.getBoldItalic();
		    default:
			fHeadFile = external.getPlain();
		}
		BaseFont f = FontUtil.getBaseFontFrom(fFile);
		BaseFont fItalic = FontUtil.getBaseFontFrom(fItalicFile);
		BaseFont fHead = FontUtil.getBaseFontFrom(fHeadFile);
		if (f == null || fItalic == null || fHead == null) {
		    System.err.println("Problem with external font: " + vars.latinFontName);
		    // use internal font if external font is no good
		    vars.latinFontName = com.itextpdf.text.FontFactory.HELVETICA;
		    continue;
		}
		vars.latinFont = f;
		vars.footLatinFont = f;
		vars.normalFont = f;
		vars.italicFont = fItalic;
		vars.footItalicFont = fItalic;
		vars.header1Font = fHead;
		vars.header2Font = fHead;
		vars.header3Font = fHead;
		return;
	    }
	com.itextpdf.text.Font f = FontFactory.getFont(vars.latinFontName);
	com.itextpdf.text.Font fNormal = FontFactory.getFont(vars.latinFontName,
		12.0f /* is dummy value */, Font.NORMAL);
	com.itextpdf.text.Font fItalic = FontFactory.getFont(vars.latinFontName,
		12.0f /* is dummy value */, Font.ITALIC);
	com.itextpdf.text.Font fHead = FontFactory.getFont(vars.latinFontName,
		12.0f /* is dummy value */, Settings.pdfHeaderFontStyle);
	vars.latinFont = f.getBaseFont();
	vars.footLatinFont = f.getBaseFont();
	vars.normalFont = fNormal.getBaseFont();
	vars.italicFont = fItalic.getBaseFont();
	vars.footItalicFont = fItalic.getBaseFont();
	vars.header1Font = fHead.getBaseFont();
	vars.header2Font = fHead.getBaseFont();
	vars.header3Font = fHead.getBaseFont();
    }

    // Compute font for transliteration.
    private static void computeEgyptFont(ExportVariables vars) {
	BaseFont f;
	if (vars.egyptFontStyle.equals(FontTypeRenderer.plainInt)) 
	    f = FontUtil.baseFont(Settings.egyptFontFilePlain);
	else if (vars.egyptFontStyle.equals(FontTypeRenderer.boldInt)) 
	    f = FontUtil.baseFont(Settings.egyptFontFileBold);
	else if (vars.egyptFontStyle.equals(FontTypeRenderer.italicInt)) 
	    f = FontUtil.baseFont(Settings.egyptFontFileIt);
	else 
	    f = FontUtil.baseFont(Settings.egyptFontFileBoldIt);
	if (f == null) {
	    vars.egyptFont = vars.latinFont;
	    vars.footEgyptFont = vars.footLatinFont;
	} else {
	    vars.egyptFont = f;
	    vars.footEgyptFont = f;
	}
	float footSize = vars.egyptFontSize.intValue() *
	    vars.footFontSizeReduction.intValue() / 100.0f;
	vars.egyptFontMetrics = new ITextFontMetrics(vars.egyptFont,
		vars.egyptFontSize.intValue());
	vars.footEgyptFontMetrics = new ITextFontMetrics(vars.footEgyptFont,
		footSize);
    }

    // Compute hieroglyphic fonts.
    private static void computeHieroFont(ExportVariables vars) {
	int footSize = Math.round(vars.hieroFontSize.intValue() *
		vars.footFontSizeReduction.intValue() / 100.0f);
	vars.hieroContext = new HieroRenderContext(vars.hieroFontSize.intValue(), 
		vars.getHieroResolution(), vars.pdfColor, true);
	vars.footHieroContext = new HieroRenderContext(footSize, 
		vars.getHieroResolution(), vars.pdfColor, true);
	vars.hieroFontMapper = vars.hieroContext.pdfMapper();
    }

    // Compute size of material before actual text.
    // The main issue is the sizes of the labels at the beginning of lines.
    // There are no labels if only one stream is selected.
    private static void computePreludeWidth(ExportVariables vars) {
	vars.creatorWidth = 0.0f;
	if (vars.mentionCreator()) {
	    int nFiles = vars.system.nFiles();
	    for (int file = 0; file < nFiles; file++) {
		String name = vars.system.getName(file);
		String shortName = Link.shortName(name);
		float width = Link.getButtonWidth(vars,
			RenderContext.NORMAL_FONT, shortName);
		vars.creatorWidth = Math.max(vars.creatorWidth, width);
	    }
	}
	vars.versionWidth = 0.0f;
	if (vars.mentionVersion()) {
	    int nStreams = vars.system.nStreams();
	    for (int str = 0; str < nStreams; str++) {
		StreamId id = vars.system.getStreamId(str);
		String version = id.getVersion();
		String scheme = id.getScheme();
		VersionLabel lab = new VersionLabel(version, scheme);
		float width = lab.getWidth(vars);
		vars.versionWidth = Math.max(vars.versionWidth, width);
	    }
	}
    }

    // Create PDF file.
    private static void openPdf(ExportVariables vars) throws DocumentException {
	vars.doc = new Document(vars.pageSize,
		vars.leftMargin, vars.rightMargin, vars.topMargin, vars.bottomMargin);
	try {
	    vars.writer = PdfWriter.getInstance(vars.doc, new FileOutputStream(vars.file));
	    annotatePdf(vars);
	    vars.doc.open();
	    vars.surface = vars.writer.getDirectContent();
	} catch (IOException e) {
	    if (vars.callingWindow != null)
		JOptionPane.showMessageDialog(vars.callingWindow, e.getMessage());
	    else
		System.err.println(e.getMessage());
	    return;
	}
	Rectangle rec = vars.doc.getPageSize();
	vars.pageWidth = rec.getWidth() - vars.rightMargin;
	vars.pageHeight = rec.getHeight() - vars.topMargin;
    }

    // Insert meta data in PDF file.
    private static void annotatePdf(ExportVariables vars) {
	vars.doc.addTitle(vars.header);
	LinkedList list = new LinkedList();
	for (int file = 0; file < vars.system.nFiles(); file++) {
	    String author = vars.system.getName(file);
	    if (list.indexOf(author) < 0)
		list.addLast(author);
	}
	String authors = "";
	while (!list.isEmpty()) {
	    authors += (String) list.removeFirst();
	    if (!list.isEmpty()) 
		authors += ", ";
	}
	vars.doc.addAuthor(authors);
	vars.doc.addSubject("Formatted resources on Egyptian text");
	vars.doc.addCreator("AELalignViewer " + Align.versionNumber);
    }

    // Write descriptions of resources.
    private static void writePreamble(ExportVariables vars) throws DocumentException {
	vars.writer.setPageEvent(new pageNumberer(true));
	vars.pageY = vars.pageHeight;
	vars.surface.beginText();
	PreamblePars headPars = XMLfiles.getPreambleHead(vars.header);
	writePreamblePars(vars, headPars);
	for (int file = 0; file < vars.system.nFiles(); file++) {
	    PdfDestination dest = new PdfDestination(PdfDestination.XYZ, 
		    0, vars.pageY, 0);
	    vars.surface.localDestination("resource" + file, dest);
	    PreamblePars resourcePars = 
		XMLfiles.getPreamble(vars.system.getName(file), 
		    vars.system.getCreated(file),
		    vars.system.getHeader(file), 
		    vars.system.getBibl(file)); 
	    writePreamblePars(vars, resourcePars);
	}
	vars.surface.endText();
	vars.doc.newPage();
    }

    // Write several paragraphs for one resource.
    private static void writePreamblePars(ExportVariables vars, PreamblePars pars) 
    	throws DocumentException {
	pars.normalize();
	for (int i = 0; i < pars.size(); i++) {
	    LinkedList elems = pars.getPar(i);
	    if (Elem.isContent(elems))
		writePreamblePar(vars, elems);
	}
    }

    // Write one paragraph.
    private static void writePreamblePar(ExportVariables vars, LinkedList stream) 
	throws DocumentException {
	while (!stream.isEmpty()) {
	    Line line = StreamSystem.splitOffLine(vars, stream, vars.leftBound());
	    float ascent = line.getAscent(vars);
	    float descent = line.getDescent(vars);
	    maybePageBreak(vars, ascent + descent);
	    ITextDraw drawer = new ITextDraw(vars, vars.surface, vars.pageY,
		    vars.pageWidth + vars.rightMargin, vars.pageHeight);
	    line.draw(vars, drawer, ascent);
	    vars.pageY -= line.getHeight(vars);
	}
	vars.pageY -= Settings.pdfPreambleParSep;
    }

    // Write formatted paragraphs on pages.
    private static void writePages(ExportVariables vars) throws DocumentException {
	PdfPageLabels pageLabels = new PdfPageLabels();
	pageLabels.addPageLabel(1, PdfPageLabels.LOWERCASE_ROMAN_NUMERALS);
	pageLabels.addPageLabel(vars.writer.getPageNumber(), 
		PdfPageLabels.DECIMAL_ARABIC_NUMERALS);
	vars.writer.setPageLabels(pageLabels);
	vars.doc.setPageCount(1);
	vars.writer.setPageEvent(new pageNumberer(false));
	vars.pageY = vars.pageHeight;
	vars.surface.beginText();
	LinkedList formatted = vars.system.format(vars);
	ListIterator iter = formatted.listIterator();
	while (iter.hasNext()) {
	    Vector lines = (Vector) iter.next();
	    addParagraphToPage(vars, lines);
	}
	vars.surface.endText();
    }

    // If paragraph fits on page, then add it to page.
    // If it doesn't fit and page is non-empty, go to next page.
    // Else write some of it on present page, and rest on next page.
    private static void addParagraphToPage(ExportVariables vars, Vector lines) 
	throws DocumentException {
	    float parHeight = paragraphHeight(vars, lines);
	    if (vars.pageY - parHeight < vars.bottomMargin) {
		if (vars.pageY < vars.pageHeight) {
		    vars.surface.endText();
		    vars.doc.newPage();
		    vars.pageY = vars.pageHeight;
		    vars.surface.beginText();
		}
	    }
	    addParagraph(vars, lines);
    }

    // Compute height of paragraph.
    private static float paragraphHeight(ExportVariables vars, Vector lines) {
	float height = 0;
	if (vars.system.nSelected() > 1)
	    height = vars.parSep / 2;
	else
	    height = vars.lineSep / 2;
	int nLines = lines.size();
	for (int i = 0; i < nLines; i++) {
	    Line line = (Line) lines.elementAt(i);
	    float ascent = line.getAscent(vars);
	    float descent = line.getDescent(vars);
	    float leading = line.getLeading(vars);
	    height += ascent + descent;
	    if (i < nLines - 1) {
		height += leading;
		if (line.isFootnote())
		    height += vars.footnoteLineSep;
		else
		    height += vars.lineSep;
	    }
	}
	if (vars.system.nSelected() > 1)
	    height += vars.parSep / 2;
	else
	    height += vars.lineSep / 2;
	return height;
    }

    // Add paragraph to page. In rare cases split over several pages.
    private static void addParagraph(ExportVariables vars, Vector lines) 
	throws DocumentException {
	    ITextDraw drawer = new ITextDraw(vars, vars.surface, vars.pageY, 
		    vars.pageWidth + vars.rightMargin, vars.pageHeight);
	    float height = 0;
	    if (vars.system.nSelected() > 1)
		height = vars.parSep / 2;
	    else
		height = vars.lineSep / 2;
	    int nLines = lines.size();
	    for (int i = 0; i < nLines; i++) {
		Line line = (Line) lines.elementAt(i);
		float ascent = line.getAscent(vars);
		float descent = line.getDescent(vars);
		float leading = line.getLeading(vars);
		height = maybePageBreak(vars, height, ascent + descent);
		line.draw(vars, drawer, height + ascent);
		height += ascent + descent;
		if (i < nLines - 1) {
		    height += leading;
		    if (line.isFootnote())
			height += vars.footnoteLineSep;
		    else
			height += vars.lineSep;
		}
	    }
	    if (vars.system.nSelected() > 1)
		height += vars.parSep / 2;
	    else
		height += vars.lineSep / 2;
	    height = maybePageBreak(vars, height, 0);
	    vars.pageY -= height;
	    vars.surface.endText();
	    vars.surface.setColorStroke(BaseColor.BLACK);
	    vars.surface.setLineWidth(1.0f);
	    vars.surface.moveTo(vars.leftMargin, vars.pageY);
	    vars.surface.lineTo(vars.rightBound(), vars.pageY);
	    vars.surface.stroke();
	    vars.surface.beginText();
    }

    // If paragraph is higher than page, it is broken when needed.
    // We avoid loops by not breaking if no line has yet been written on
    // the page.
    private static float maybePageBreak(ExportVariables vars,
	    float height, float addedHeight) throws DocumentException {
	if (vars.pageY - height - addedHeight < vars.bottomMargin) {
	    if (vars.pageY - height < vars.pageHeight) {
		vars.surface.endText();
		vars.doc.newPage();
		vars.pageY = vars.pageHeight;
		vars.surface.beginText();
		return 0;
	    }
	} 
	return height;
    }

    // If line is higher than what is left on page, then page break.
    // We avoid loops by not breaking is nothing has been written on page.
    private static void maybePageBreak(ExportVariables vars, 
	    float addedHeight) throws DocumentException {
	if (vars.pageY - addedHeight < vars.bottomMargin &&
		vars.pageY < vars.pageHeight) {
	    vars.surface.endText();
	    vars.doc.newPage();
	    vars.pageY = vars.pageHeight;
	    vars.surface.beginText();
	}
    }

    // Close PDF file.
    private static void closePdf(ExportVariables vars) {
	vars.doc.close();
    }

    /////////////////////////////////////////////////////////////////////////////
    // Page numbers.

    private static class pageNumberer extends PdfPageEventHelper {
	private boolean romanNumbers;

	// To be printed at Roman numbers, or Arabic?
	public pageNumberer(boolean roman) {
	    romanNumbers = roman;
	}

	// Print page number in middle of lower margin.
	// May be Roman or Arabic.
	public void onEndPage(PdfWriter writer, Document doc) {
	    String page;
	    if (romanNumbers)
		page = RomanNumberFactory.getString(doc.getPageNumber());
	    else
		page = Integer.toString(doc.getPageNumber());
	    PdfContentByte surface = writer.getDirectContent();
	    BaseFont font = FontFactory.getFont(
		    Settings.pdfPageNumberFontNameDefault).getBaseFont();
	    Rectangle rect = doc.getPageSize();
	    float fontSize = Settings.pdfPageNumberFontSizeDefault;
	    float width = font.getWidthPointKerned(page, fontSize);
	    float x = (rect.getWidth() - 
		    width - doc.leftMargin() - doc.rightMargin()) / 2 + doc.leftMargin();
	    float height = font.getAscentPoint(page, fontSize) -
		font.getDescentPoint(page, fontSize);
	    float y = (doc.bottomMargin() - height) * 0.66f;
	    y = Math.abs(y);
	    y -= font.getDescentPoint(page, fontSize);
	    surface.beginText();
	    surface.setFontAndSize(font, fontSize);
	    surface.setColorFill(BaseColor.BLACK);
	    surface.setTextMatrix(x, y);
	    surface.showTextKerned(page);
	    surface.endText();
	}
    }
}
