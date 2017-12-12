/***************************************************************************/
/*                                                                         */
/*  ExportWindow.java                                                      */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Window with parameters for export in PDF.

package nederhof.align;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import com.itextpdf.text.FontFactory;

import nederhof.res.*;
import nederhof.util.*;

class ExportWindow extends JFrame implements ActionListener {

    // System to be formatted.
    private StreamSystem system;
    // Default target file.
    private File fileDefault;
    // Default header.
    private String headerDefault;

    // File where pdf to be saved.
    // Header of document.
    private File file;
    private String header;

    // Space between version label and text.
    // Vertical space between paragraphs and between lines within paragraphs.
    // Vertical space between lines of footnotes.
    // All lines to be made of uniform ascent w.r.t. points.
    // Footnotes collected at end.
    // Color in Pdf.
    private int colSep;
    private int parSep;
    private int lineSep;
    private int footnoteLineSep;
    private boolean uniformAscent;
    private boolean collectNotes;
    private boolean pdfColor;

    // PDF Page format.
    private com.itextpdf.text.Rectangle pageSize;
    private int topMargin;
    private int leftMargin;
    private int bottomMargin;
    private int rightMargin;

    // Options for rendering lexical entries.
    private int lxSep;
    private int lxLeading;
    private int lxInnerMargin;
    private int lxLineThickness;
    private boolean lxAbbreviated;

    // Latin.
    private String latinFontName;
    private Integer latinFontSize;

    // Transliteration.
    private Integer egyptFontStyle;
    private Integer egyptFontSize;

    // Hieroglyphic.
    private Integer hieroFontSize;
    private Integer hieroResolution;

    // Footnotes.
    private Integer footFontSizeReduction;

    // Settings.
    private JTextField fileField;
    private JTextField headerField;
    private JTextField colSepField;
    private JTextField parSepField;
    private JTextField lineSepField;
    private JTextField footnoteLineSepField;
    private JTextField lxSepField;
    private JTextField lxLeadingField;
    private JTextField lxInnerMarginField;
    private JTextField lxLineThicknessField;
    private JCheckBox uniformAscentBox;
    private JCheckBox lxAbbreviatedBox;
    private JCheckBox collectNotesBox;
    private JCheckBox pdfColorBox;
    private JComboBox pageSizeCombo;
    private JTextField topMarginField;
    private JTextField leftMarginField;
    private JTextField bottomMarginField;
    private JTextField rightMarginField;
    private JComboBox latinFontNameCombo;
    private JComboBox latinFontSizeCombo;
    private JComboBox egyptFontStyleCombo;
    private JComboBox egyptFontSizeCombo;
    private JComboBox hieroFontSizeCombo;
    private JComboBox hieroResolutionCombo;
    private JComboBox reductionCombo;

    // Chooser for PDF files to be exported.
    private Chooser fileChooseWindow = null;

    // Create window for setting parameters.
    // In case of program inside JAR file, let default location be the
    // location of JAR file. Otherwise the location of an input XML file.
    public ExportWindow(StreamSystem system, String title) { 
	this.system = system;
	File fullPath = new File(system.getFileName() + ".pdf");
	if (fullPath.toString().startsWith("jar:")) {
	    fullPath = new File(fullPath.getName());
	}
	if (Settings.pdfSavePath == null)
	    fileDefault = fullPath;
	else
	    fileDefault = new File(Settings.pdfSavePath, fullPath.getName());
	headerDefault = title;
	setDefaultSettings();

	final int STRUT_SIZE = 6;
	setTitle("Export to PDF");
	setJMenuBar(new QuitMenu(this));
	Container content = getContentPane();
	content.setLayout(new BorderLayout());
	JPanel filePanel = new JPanel();
	JPanel leftPanel = new JPanel();
	JPanel rightPanel = new JPanel();
	JPanel buttonsPanel = new JPanel();
	content.add(filePanel, BorderLayout.NORTH);
	content.add(leftPanel, BorderLayout.WEST);
	content.add(Box.createHorizontalStrut(STRUT_SIZE), BorderLayout.CENTER);
	content.add(rightPanel, BorderLayout.EAST);
	content.add(buttonsPanel, BorderLayout.SOUTH);
	filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
	rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
	buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

	// Location and properties of PDF file.
	JPanel pdfPanel = new JPanel(new SpringLayout());
	pdfPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("PDF file"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	fileField = new JTextField();
	fileField.setColumns(35);
	JButton fileChoose = new SettingButton(this, "<html><u>S</u>elect</html>", 
		"select", KeyEvent.VK_S);
	pdfPanel.add(new JLabel("Location:"));
	pdfPanel.add(fileField);
	pdfPanel.add(fileChoose);
	headerField = new JTextField();
	headerField.setColumns(15);
	pdfPanel.add(new JLabel("Header:"));
	pdfPanel.add(headerField);
	pdfPanel.add(Box.createHorizontalGlue());
	filePanel.add(pdfPanel);
	SpringUtilities.makeCompactGrid(pdfPanel, 2, 3, 5, 5, 5, 5);
	filePanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Paper size.
	JPanel pagePanel = new JPanel(new SpringLayout());
	pagePanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Page format"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	JPanel pageSizePanel = new JPanel(new SpringLayout());
	pageSizeCombo = new JComboBox(PageSizeRenderer.pageSizes);
	pageSizeCombo.setRenderer(new PageSizeRenderer());
	pageSizeCombo.setMaximumRowCount(9);
	pageSizeCombo.setMaximumSize(pageSizeCombo.getPreferredSize());
	pageSizePanel.add(new JLabel("Page size:"));
	pageSizePanel.add(pageSizeCombo);
	pageSizePanel.add(Box.createHorizontalGlue());
	pagePanel.add(pageSizePanel);
	SpringUtilities.makeCompactGrid(pageSizePanel, 1, 3, 0, 0, 5, 5);
	JPanel marginPanel = new JPanel(new SpringLayout());
	topMarginField = new JTextField();
	topMarginField.setColumns(3);
	topMarginField.setMaximumSize(topMarginField.getPreferredSize());
	marginPanel.add(new JLabel("Top margin:"));
	marginPanel.add(topMarginField);
	marginPanel.add(new JLabel("pt"));
	marginPanel.add(Box.createHorizontalGlue());
	leftMarginField = new JTextField();
	leftMarginField.setColumns(3);
	leftMarginField.setMaximumSize(leftMarginField.getPreferredSize());
	marginPanel.add(new JLabel("Left margin:"));
	marginPanel.add(leftMarginField);
	marginPanel.add(new JLabel("pt"));
	marginPanel.add(Box.createHorizontalGlue());
	bottomMarginField = new JTextField();
	bottomMarginField.setColumns(3);
	bottomMarginField.setMaximumSize(bottomMarginField.getPreferredSize());
	marginPanel.add(new JLabel("Bottom margin:"));
	marginPanel.add(bottomMarginField);
	marginPanel.add(new JLabel("pt"));
	marginPanel.add(Box.createHorizontalGlue());
	rightMarginField = new JTextField();
	rightMarginField.setColumns(3);
	rightMarginField.setMaximumSize(rightMarginField.getPreferredSize());
	marginPanel.add(new JLabel("Right margin:"));
	marginPanel.add(rightMarginField);
	marginPanel.add(new JLabel("pt"));
	marginPanel.add(Box.createHorizontalGlue());
	pagePanel.add(marginPanel);
	SpringUtilities.makeCompactGrid(marginPanel, 4, 4, 0, 0, 5, 5);
	leftPanel.add(pagePanel);
	SpringUtilities.makeCompactGrid(pagePanel, 2, 1, 5, 5, 5, 5);
	leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Horizontal distances.
	JPanel horPanel = new JPanel(new SpringLayout());
	horPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Horizontal space"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	colSepField = new JTextField();
	colSepField.setColumns(3);
	colSepField.setMaximumSize(colSepField.getPreferredSize());
	horPanel.add(new JLabel("Column sep:"));
	horPanel.add(colSepField);
	horPanel.add(new JLabel("pt"));
	horPanel.add(Box.createHorizontalGlue());
	leftPanel.add(horPanel);
	SpringUtilities.makeCompactGrid(horPanel, 1, 4, 5, 5, 5, 5);
	leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Vertical distances.
	JPanel vertPanel = new JPanel(new SpringLayout());
	vertPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Vertical space"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	parSepField = new JTextField();
	parSepField.setColumns(3);
	parSepField.setMaximumSize(parSepField.getPreferredSize());
	vertPanel.add(new JLabel("Paragraph sep:"));
	vertPanel.add(parSepField);
	vertPanel.add(new JLabel("pt"));
	vertPanel.add(Box.createHorizontalGlue());
	lineSepField = new JTextField();
	lineSepField.setColumns(3);
	lineSepField.setMaximumSize(lineSepField.getPreferredSize());
	vertPanel.add(new JLabel("Line sep:"));
	vertPanel.add(lineSepField);
	vertPanel.add(new JLabel("pt"));
	vertPanel.add(Box.createHorizontalGlue());
	footnoteLineSepField = new JTextField();
	footnoteLineSepField.setColumns(3);
	footnoteLineSepField.setMaximumSize(footnoteLineSepField.getPreferredSize());
	vertPanel.add(new JLabel("Footnote line sep:"));
	vertPanel.add(footnoteLineSepField);
	vertPanel.add(new JLabel("pt"));
	vertPanel.add(Box.createHorizontalGlue());
	leftPanel.add(vertPanel);
	SpringUtilities.makeCompactGrid(vertPanel, 3, 4, 5, 5, 5, 5);
	leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Lexical entries.
	JPanel lxPanel = new JPanel(new SpringLayout());
	lxPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Lexical entries"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	lxSepField = new JTextField();
	lxSepField.setColumns(3);
	lxSepField.setMaximumSize(lxSepField.getPreferredSize());
	lxPanel.add(new JLabel("Horizontal sep:"));
	lxPanel.add(lxSepField);
	lxPanel.add(new JLabel("pt"));
	lxPanel.add(Box.createHorizontalGlue());
	lxLeadingField = new JTextField();
	lxLeadingField.setColumns(3);
	lxLeadingField.setMaximumSize(lxLeadingField.getPreferredSize());
	lxPanel.add(new JLabel("Leading:"));
	lxPanel.add(lxLeadingField);
	lxPanel.add(new JLabel("pt"));
	lxPanel.add(Box.createHorizontalGlue());
	lxInnerMarginField = new JTextField();
	lxInnerMarginField.setColumns(3);
	lxInnerMarginField.setMaximumSize(lxInnerMarginField.getPreferredSize());
	lxPanel.add(new JLabel("Inner margin:"));
	lxPanel.add(lxInnerMarginField);
	lxPanel.add(new JLabel("pt"));
	lxPanel.add(Box.createHorizontalGlue());
	lxLineThicknessField = new JTextField();
	lxLineThicknessField.setColumns(3);
	lxLineThicknessField.setMaximumSize(lxLineThicknessField.getPreferredSize());
	lxPanel.add(new JLabel("Line thickness:"));
	lxPanel.add(lxLineThicknessField);
	lxPanel.add(new JLabel("pt"));
	lxPanel.add(Box.createHorizontalGlue());
	leftPanel.add(lxPanel);
	SpringUtilities.makeCompactGrid(lxPanel, 4, 4, 5, 5, 5, 5);
	leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Latin font.
	JPanel latinPanel = new JPanel(new SpringLayout());
	latinPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Latin font"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	Set fontNames = FontFactory.getRegisteredFonts();
	Vector fontVector = new Vector(fontNames);
	fontVector.remove(FontFactory.SYMBOL);
	fontVector.remove(FontFactory.ZAPFDINGBATS);
	fontVector.addAll(externalFontNames());
	Collections.sort(fontVector);
	latinFontNameCombo = new JComboBox(fontVector);
	latinFontNameCombo.setMaximumRowCount(9);
	latinFontNameCombo.setMaximumSize(latinFontNameCombo.getPreferredSize());
	latinPanel.add(new JLabel("Font:"));
	latinPanel.add(latinFontNameCombo);
	latinPanel.add(Box.createHorizontalGlue());
	latinPanel.add(Box.createHorizontalGlue());
	latinFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(10, 30, 2));
	latinFontSizeCombo.setRenderer(new FontSizeRenderer());
	latinFontSizeCombo.setMaximumRowCount(9);
	latinFontSizeCombo.setMaximumSize(latinFontSizeCombo.getPreferredSize());
	latinPanel.add(new JLabel("Size:"));
	latinPanel.add(latinFontSizeCombo);
	latinPanel.add(new JLabel("pt"));
	latinPanel.add(Box.createHorizontalGlue());
	rightPanel.add(latinPanel);
	SpringUtilities.makeCompactGrid(latinPanel, 2, 4, 5, 5, 5, 5);
	rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Egyptological font.
	JPanel egyptPanel = new JPanel(new SpringLayout());
	egyptPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Egyptological font"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	egyptFontStyleCombo = new JComboBox(FontTypeRenderer.fontTypes);
	egyptFontStyleCombo.setRenderer(new FontTypeRenderer());
	egyptFontStyleCombo.setMaximumRowCount(9);
	egyptFontStyleCombo.setMaximumSize(egyptFontStyleCombo.getPreferredSize());
	egyptPanel.add(new JLabel("Style:"));
	egyptPanel.add(egyptFontStyleCombo);
	egyptPanel.add(Box.createHorizontalGlue());
	egyptPanel.add(Box.createHorizontalGlue());
	egyptFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(10, 30, 2));
	egyptFontSizeCombo.setRenderer(new FontSizeRenderer());
	egyptFontSizeCombo.setMaximumRowCount(9);
	egyptFontSizeCombo.setMaximumSize(egyptFontSizeCombo.getPreferredSize());
	egyptPanel.add(new JLabel("Size:"));
	egyptPanel.add(egyptFontSizeCombo);
	egyptPanel.add(new JLabel("pt"));
	egyptPanel.add(Box.createHorizontalGlue());
	rightPanel.add(egyptPanel);
	SpringUtilities.makeCompactGrid(egyptPanel, 2, 4, 5, 5, 5, 5);
	rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Hieroglyphic font.
	JPanel hieroPanel = new JPanel(new SpringLayout());
	hieroPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Hieroglyphic font"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	hieroFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(18, 40, 2));
	hieroFontSizeCombo.setRenderer(new FontSizeRenderer());
	hieroFontSizeCombo.setMaximumRowCount(9);
	hieroFontSizeCombo.setMaximumSize(hieroFontSizeCombo.getPreferredSize());
	hieroPanel.add(new JLabel("Size:"));
	hieroPanel.add(hieroFontSizeCombo);
	hieroPanel.add(new JLabel("pt"));
	hieroPanel.add(Box.createHorizontalGlue());
	hieroResolutionCombo = new JComboBox(FontSizeRenderer.getFontSizes(1, 15, 1));
	hieroResolutionCombo.setRenderer(new FontSizeRenderer());
	hieroResolutionCombo.setMaximumRowCount(9);
	hieroResolutionCombo.setMaximumSize(hieroResolutionCombo.getPreferredSize());
	hieroPanel.add(new JLabel("Resolution:"));
	hieroPanel.add(hieroResolutionCombo);
	hieroPanel.add(new JLabel("dots/pt"));
	hieroPanel.add(Box.createHorizontalGlue());
	rightPanel.add(hieroPanel);
	SpringUtilities.makeCompactGrid(hieroPanel, 2, 4, 5, 5, 5, 5);
	rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Reduction in font size for footnotes.
	JPanel reductionPanel = new JPanel(new SpringLayout());
	reductionPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Footnotes"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	reductionCombo = new JComboBox(FontSizeRenderer.getFontSizes(60, 100, 5));
	reductionCombo.setRenderer(new FontSizeRenderer());
	reductionCombo.setMaximumRowCount(9);
	reductionCombo.setMaximumSize(reductionCombo.getPreferredSize());
	reductionPanel.add(new JLabel("Size reduction:"));
	reductionPanel.add(reductionCombo);
	reductionPanel.add(new JLabel("%"));
	reductionPanel.add(Box.createHorizontalGlue());
	rightPanel.add(reductionPanel);
	SpringUtilities.makeCompactGrid(reductionPanel, 1, 4, 5, 5, 5, 5);
	rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Other formatting issues.
	JPanel formatPanel = new JPanel(new SpringLayout());
	formatPanel.setBorder(
		BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Formatting"),
		    BorderFactory.createEmptyBorder(5,5,5,5)));
	uniformAscentBox = new JCheckBox();
	formatPanel.add(uniformAscentBox);
	formatPanel.add(new JLabel("Uniform line ascents"));
	formatPanel.add(Box.createHorizontalGlue());
	lxAbbreviatedBox =  new JCheckBox();
	formatPanel.add(lxAbbreviatedBox);
	formatPanel.add(new JLabel("Lexical entries abbreviated to textal"));
	formatPanel.add(Box.createHorizontalGlue());
	collectNotesBox =  new JCheckBox();
	formatPanel.add(collectNotesBox);
	formatPanel.add(new JLabel("All footnotes together below text"));
	formatPanel.add(Box.createHorizontalGlue());
	pdfColorBox =  new JCheckBox();
	formatPanel.add(pdfColorBox);
	formatPanel.add(new JLabel("Use of color"));
	formatPanel.add(Box.createHorizontalGlue());
	rightPanel.add(formatPanel);
	SpringUtilities.makeCompactGrid(formatPanel, 4, 3, 5, 5, 5, 5);
	formatPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	// Confirmation buttons.
	buttonsPanel.add(Box.createHorizontalGlue());
	buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	buttonsPanel.add(new SettingButton(this,
		    "<html><u>E</u>xport</html>", "export", KeyEvent.VK_E));
	buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	buttonsPanel.add(new SettingButton(this,
		    "<html><u>D</u>efaults</html>", "defaults", KeyEvent.VK_D));
	buttonsPanel.add(Box.createHorizontalGlue());

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new ConservativeListener(this));
	pack();
	makeSettingsVisible();
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("select")) {
	    if (fileChooseWindow == null) 
		fileChooseWindow = new Chooser();
	    fileChooseWindow.setVisible(true);
	} else if (e.getActionCommand().equals("export")) {
	    applySettings();
	    export();
	    setVisible(false);
	} else if (e.getActionCommand().equals("defaults")) {
	    makeDefaultSettingsVisible();
	} else if (e.getActionCommand().equals("quit")) {
	    makeSettingsVisible();
	    setVisible(false);
	} 
    }

    // Kill window and windows depending on it.
    public void dispose() {
	if (fileChooseWindow != null)
	    fileChooseWindow.dispose();
	super.dispose();
    }

    // Get names of external fonts.
    private Set externalFontNames() {
	Set names = new TreeSet();
	for (int i = 0; i < Settings.externalFonts.length; i++)
	    names.add(Settings.externalFonts[i].getName());
	return names;
    }

    // Select target file for export of PDF.
    private class Chooser extends JFrame implements ActionListener {
	// Actual chooser in window.
	private JFileChooser filePanel;

	public Chooser() {
	    setTitle("PDF file selection");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	    filePanel = new JFileChooser(); 
	    filePanel.setDialogType(JFileChooser.OPEN_DIALOG);
	    filePanel.setApproveButtonText("Select");
	    filePanel.addActionListener(this);
	    filePanel.setFileFilter(new PdfFilter());
	    filePanel.setSelectedFile(fileDefault);
	    content.add(filePanel);

	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new ConservativeListener(this));
	    pack();
	    setVisible(true);
	}

	// Actions can be: select, cancel. In both cases, make window
	// invisible.
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
		File selectedFile = filePanel.getSelectedFile();
		fileField.setText(selectedFile.getAbsolutePath());
	    }
	    setVisible(false);
	}
    }

    // Filter that only allows directories and PDF files.
    private static class PdfFilter extends FileFilter {

	// Accept only PDF files. Also directories.
	public boolean accept(File f) {
	    if (f.isDirectory())
		return true;
	    else {
		String name = f.getName().toLowerCase();
		if (name.endsWith(".pdf"))
		    return true;
	    }
	    return false;
	}

	// Description of filter.
	public String getDescription() {
	    return "PDF files";
	}
    }

    // Apply values from screen.
    private void applySettings() {
	file = new File(fileField.getText());
	header = headerField.getText();
	try { 
	    int i = Integer.parseInt(colSepField.getText());
	    colSep = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(parSepField.getText());
	    parSep = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(lineSepField.getText());
	    lineSep = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(footnoteLineSepField.getText());
	    footnoteLineSep = i;
	} catch (NumberFormatException e) {}
	uniformAscent = uniformAscentBox.isSelected();
	collectNotes = collectNotesBox.isSelected();
	pdfColor = pdfColorBox.isSelected();
	pageSize = (com.itextpdf.text.Rectangle) pageSizeCombo.getSelectedItem();
	try { 
	    int i = Integer.parseInt(topMarginField.getText());
	    topMargin = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(leftMarginField.getText());
	    leftMargin = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(bottomMarginField.getText());
	    bottomMargin = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(rightMarginField.getText());
	    rightMargin = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(lxSepField.getText());
	    lxSep = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(lxLeadingField.getText());
	    lxLeading = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(lxInnerMarginField.getText());
	    lxInnerMargin = i;
	} catch (NumberFormatException e) {}
	try { 
	    int i = Integer.parseInt(lxLineThicknessField.getText());
	    lxLineThickness = i;
	} catch (NumberFormatException e) {}
	lxAbbreviated = lxAbbreviatedBox.isSelected();
	footFontSizeReduction = (Integer) reductionCombo.getSelectedItem();
	latinFontName = (String) latinFontNameCombo.getSelectedItem();
	latinFontSize = (Integer) latinFontSizeCombo.getSelectedItem();
	egyptFontStyle = (Integer) egyptFontStyleCombo.getSelectedItem();
	egyptFontSize = (Integer) egyptFontSizeCombo.getSelectedItem();
	hieroFontSize = (Integer) hieroFontSizeCombo.getSelectedItem();
	hieroResolution = (Integer) hieroResolutionCombo.getSelectedItem();
	makeSettingsVisible();
    }

    // Set values in screen to current values.
    private void makeSettingsVisible() {
	fileField.setText(file.getAbsolutePath());
	headerField.setText(header);
	colSepField.setText(Integer.toString(colSep));
	parSepField.setText(Integer.toString(parSep));
	lineSepField.setText(Integer.toString(lineSep));
	footnoteLineSepField.setText(Integer.toString(footnoteLineSep));
	uniformAscentBox.setSelected(uniformAscent);
	collectNotesBox.setSelected(collectNotes);
	pdfColorBox.setSelected(pdfColor);
	pageSizeCombo.setSelectedItem(pageSize);
	topMarginField.setText(Integer.toString(topMargin));
	leftMarginField.setText(Integer.toString(leftMargin));
	bottomMarginField.setText(Integer.toString(bottomMargin));
	rightMarginField.setText(Integer.toString(rightMargin));
	lxSepField.setText(Integer.toString(lxSep));
	lxLeadingField.setText(Integer.toString(lxLeading));
	lxInnerMarginField.setText(Integer.toString(lxInnerMargin));
	lxLineThicknessField.setText(Integer.toString(lxLineThickness));
	lxAbbreviatedBox.setSelected(lxAbbreviated);
	latinFontNameCombo.setSelectedItem(latinFontName);
	latinFontSizeCombo.setSelectedItem(latinFontSize);
	egyptFontStyleCombo.setSelectedItem(egyptFontStyle);
	egyptFontSizeCombo.setSelectedItem(egyptFontSize);
	hieroFontSizeCombo.setSelectedItem(hieroFontSize);
	hieroResolutionCombo.setSelectedItem(hieroResolution);
	reductionCombo.setSelectedItem(footFontSizeReduction);
    }

    // Set values in screen to default values.
    private void makeDefaultSettingsVisible() {
	fileField.setText(fileDefault.getAbsolutePath());
	headerField.setText(headerDefault);
	colSepField.setText(
		Integer.toString(Settings.pdfColSepDefault));
	parSepField.setText(
		Integer.toString(Settings.pdfParSepDefault));
	lineSepField.setText(
		Integer.toString(Settings.pdfLineSepDefault));
	footnoteLineSepField.setText(
		Integer.toString(Settings.pdfFootnoteLineSepDefault));
	uniformAscentBox.setSelected(
		Settings.pdfUniformAscentDefault);
	collectNotesBox.setSelected(
		Settings.pdfCollectNotesDefault);
	pdfColorBox.setSelected(
		Settings.pdfColorDefault);
	pageSizeCombo.setSelectedItem(
		Settings.pdfPageSizeDefault);
	topMarginField.setText(
		Integer.toString(Settings.pdfTopMarginDefault));
	leftMarginField.setText(
		Integer.toString(Settings.pdfLeftMarginDefault));
	bottomMarginField.setText(
		Integer.toString(Settings.pdfBottomMarginDefault));
	rightMarginField.setText(
		Integer.toString(Settings.pdfRightMarginDefault));
	lxSepField.setText(
		Integer.toString(Settings.pdfLxSepDefault));
	lxLeadingField.setText(
		Integer.toString(Settings.pdfLxLeadingDefault));
	lxInnerMarginField.setText(
		Integer.toString(Settings.pdfLxInnerMarginDefault));
	lxLineThicknessField.setText(
		Integer.toString(Settings.pdfLxLineThicknessDefault));
	lxAbbreviatedBox.setSelected(
		Settings.pdfLxAbbreviatedDefault);
	latinFontNameCombo.setSelectedItem(
		Settings.pdfLatinFontNameDefault);
	latinFontSizeCombo.setSelectedItem(
		Settings.pdfLatinFontSizeDefault);
	egyptFontStyleCombo.setSelectedItem(
		Settings.pdfEgyptFontStyleDefault);
	egyptFontSizeCombo.setSelectedItem(
		Settings.pdfEgyptFontSizeDefault);
	hieroFontSizeCombo.setSelectedItem(
		Settings.pdfHieroFontSizeDefault);
	hieroResolutionCombo.setSelectedItem(
		Settings.pdfHieroResolutionDefault);
	reductionCombo.setSelectedItem(
		Settings.pdfFootFontSizeReductionDefault);
    }

    // Set default settings.
    private void setDefaultSettings() {
	file = fileDefault;
	header = headerDefault;
	colSep = Settings.pdfColSepDefault;
	parSep = Settings.pdfParSepDefault;
	lineSep = Settings.pdfLineSepDefault;
	footnoteLineSep = Settings.pdfFootnoteLineSepDefault;
	uniformAscent = Settings.pdfUniformAscentDefault;
	collectNotes = Settings.pdfCollectNotesDefault;
	pdfColor = Settings.pdfColorDefault;
	pageSize = Settings.pdfPageSizeDefault;
	topMargin = Settings.pdfTopMarginDefault;
	leftMargin = Settings.pdfLeftMarginDefault;
	bottomMargin = Settings.pdfBottomMarginDefault;
	rightMargin = Settings.pdfRightMarginDefault;
	lxSep = Settings.pdfLxSepDefault;
	lxLeading = Settings.pdfLxLeadingDefault;
	lxInnerMargin = Settings.pdfLxInnerMarginDefault;
	lxLineThickness = Settings.pdfLxLineThicknessDefault;
	lxAbbreviated = Settings.pdfLxAbbreviatedDefault;
	latinFontName = Settings.pdfLatinFontNameDefault;
	latinFontSize = Settings.pdfLatinFontSizeDefault;
	egyptFontStyle = Settings.pdfEgyptFontStyleDefault;
	egyptFontSize = Settings.pdfEgyptFontSizeDefault;
	hieroFontSize = Settings.pdfHieroFontSizeDefault;
	hieroResolution = Settings.pdfHieroResolutionDefault;
	footFontSizeReduction = Settings.pdfFootFontSizeReductionDefault;
    }

    // Export to PDF.
    private void export() {
	PdfExport.printPdf(this,
		system, file, header,
		colSep, parSep, lineSep, footnoteLineSep,
		uniformAscent, collectNotes, pdfColor,
		pageSize,
		topMargin, leftMargin, bottomMargin, rightMargin,
		lxSep, lxLeading, lxInnerMargin, lxLineThickness,
		lxAbbreviated,
		latinFontName, latinFontSize,
		egyptFontStyle, egyptFontSize, 
		hieroFontSize, hieroResolution);
    }
}
