/***************************************************************************/
/*                                                                         */
/*  EgyptianPdfRenderParameters.java                                       */
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

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;

import nederhof.fonts.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.res.*;
import nederhof.util.*;

public class EgyptianPdfRenderParameters extends PdfRenderParameters {

    // Transliteration.
    public int egyptFontStyle;
    public int egyptFontSize;

    // Hieroglyphic.
    public String hieroFontName;
    public int hieroFontSize;
    public int hieroResolution;

    // Lexical entries.
    public int lxSep;
    public int lxLeading;
    public int lxInnerMargin;
    public int lxLineThickness;
    public boolean lxAbbreviated;

    // Only one window created:
    private SettingsWindow window;

    public EgyptianPdfRenderParameters(String pdfDir, String baseName, String name) {
        super(); // Calls setDefaults.
	setSavePath(pdfDir);
	setNames(baseName, name);
    }

    // When names are not known at time of construction.
    public EgyptianPdfRenderParameters() {
	super();
    }

    public void setDefaults() {
        super.setDefaults();

        egyptFontStyle = egyptFontStyleDefault();
        egyptFontSize = egyptFontSizeDefault();

	hieroFontName = hieroFontNameDefault();
        hieroFontSize = hieroFontSizeDefault();
        hieroResolution = hieroResolutionDefault();

        lxSep = lxSepDefault();
        lxLeading = lxLeadingDefault();
        lxInnerMargin = lxInnerMarginDefault();
        lxLineThickness = lxLineThicknessDefault();
        lxAbbreviated = lxAbbreviatedDefault();
    }

    protected int egyptFontStyleDefault() {
        return Settings.pdfEgyptFontStyleDefault;
    }
    protected int egyptFontSizeDefault() {
        return Settings.pdfEgyptFontSizeDefault;
    }
    protected String hieroFontNameDefault() {
	return Settings.pdfHieroFontNameDefault;
    }
    protected int hieroFontSizeDefault() {
        return Settings.pdfHieroFontSizeDefault;
    }
    protected int hieroResolutionDefault() {
        return Settings.pdfHieroResolutionDefault;
    }
    protected int lxSepDefault() {
        return Settings.pdfLxSepDefault;
    }
    protected int lxLeadingDefault() {
        return Settings.pdfLxLeadingDefault;
    }
    protected int lxInnerMarginDefault() {
        return Settings.pdfLxInnerMarginDefault;
    }
    protected int lxLineThicknessDefault() {
        return Settings.pdfLxLineThicknessDefault;
    }
    protected boolean lxAbbreviatedDefault() {
        return Settings.pdfLxAbbreviatedDefault;
    }

    public void edit() {
        if (window == null)
            window = new SettingsWindow();
        window.setVisible(true);
    }

    // Window for manipulating values.
    private class SettingsWindow extends JFrame implements ActionListener {
	private JTextField fileField;
	private JTextField headerField;
	private JTextField colSepField;
	private JTextField sectionSepField;
	private JTextField lineSepField;
	private JTextField footnoteLineSepField;
	private JTextField lxSepField;
	private JTextField lxLeadingField;
	private JTextField lxInnerMarginField;
	private JTextField lxLineThicknessField;
	private JCheckBox uniformAscentBox;
	private JCheckBox lxAbbreviatedBox;
	private JCheckBox collectNotesBox;
	private JCheckBox colorBox;
	private JComboBox pageSizeCombo;
	private JTextField topMarginField;
	private JTextField leftMarginField;
	private JTextField bottomMarginField;
	private JTextField rightMarginField;
	private JComboBox latinFontNameCombo;
	private JComboBox latinFontSizeCombo;
	private JComboBox egyptFontStyleCombo;
	private JComboBox egyptFontSizeCombo;
	private JComboBox hieroFontNameCombo;
	private JComboBox hieroFontSizeCombo;
	private JComboBox hieroResolutionCombo;
	private JComboBox reductionCombo;

	// Chooser for PDF files to be exported.
	private FileChoosingWindow fileChooseWindow = null;

	public SettingsWindow() {
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
	    sectionSepField = new JTextField();
	    sectionSepField.setColumns(3);
	    sectionSepField.setMaximumSize(sectionSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Section sep:"));
	    vertPanel.add(sectionSepField);
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
            Vector hieroFontNames = new Vector(HieroRenderContext.fontNames());
            hieroFontNameCombo = new JComboBox(hieroFontNames);
            hieroFontNameCombo.setMaximumRowCount(9);
            hieroFontNameCombo.setMaximumSize(hieroFontNameCombo.getPreferredSize());
            hieroPanel.add(new JLabel("Font:"));
            hieroPanel.add(hieroFontNameCombo);
            hieroPanel.add(Box.createHorizontalGlue());
            hieroPanel.add(Box.createHorizontalGlue());
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
	    SpringUtilities.makeCompactGrid(hieroPanel, 3, 4, 5, 5, 5, 5);
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
	    colorBox =  new JCheckBox();
	    formatPanel.add(colorBox);
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
		if (fileChooseWindow == null) {
		    fileChooseWindow = new FileChoosingWindow("PDF file",
			    new String[] {"pdf"}) {
			protected void choose(File file) {
			    fileField.setText(file.getAbsolutePath());
			}
		    };
		}
		fileChooseWindow.setSelectedFile(file);
		fileChooseWindow.setVisible(true);
	    } else if (e.getActionCommand().equals("export")) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		applySettings();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
	    for (int i = 0; i < externalFonts().length; i++)
		names.add(externalFonts()[i].getName());
	    return names;
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
		int i = Integer.parseInt(sectionSepField.getText());
		sectionSep = i;
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
	    color = colorBox.isSelected();
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
	    footFontSizeReduction = ((Integer) reductionCombo.getSelectedItem()).intValue();
	    latinFontName = (String) latinFontNameCombo.getSelectedItem();
	    latinFontSize = ((Integer) latinFontSizeCombo.getSelectedItem()).intValue();
	    egyptFontStyle = ((Integer) egyptFontStyleCombo.getSelectedItem()).intValue();
	    egyptFontSize = ((Integer) egyptFontSizeCombo.getSelectedItem()).intValue();
	    hieroFontName = (String) hieroFontNameCombo.getSelectedItem();
	    hieroFontSize = ((Integer) hieroFontSizeCombo.getSelectedItem()).intValue();
	    hieroResolution = ((Integer) hieroResolutionCombo.getSelectedItem()).intValue();
	    computeFonts();
	    makeSettingsVisible();
	    reformat();
	}

	// Set values in screen to current values.
	private void makeSettingsVisible() {
	    fileField.setText(file.getAbsolutePath());
	    headerField.setText(header);
	    colSepField.setText(Integer.toString(colSep));
	    sectionSepField.setText(Integer.toString(sectionSep));
	    lineSepField.setText(Integer.toString(lineSep));
	    footnoteLineSepField.setText(Integer.toString(footnoteLineSep));
	    uniformAscentBox.setSelected(uniformAscent);
	    collectNotesBox.setSelected(collectNotes);
	    colorBox.setSelected(color);
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
	    latinFontSizeCombo.setSelectedItem(new Integer(latinFontSize));
	    egyptFontStyleCombo.setSelectedItem(new Integer(egyptFontStyle));
	    egyptFontSizeCombo.setSelectedItem(new Integer(egyptFontSize));
	    hieroFontNameCombo.setSelectedItem(hieroFontName);
	    hieroFontSizeCombo.setSelectedItem(new Integer(hieroFontSize));
	    hieroResolutionCombo.setSelectedItem(new Integer(hieroResolution));
	    reductionCombo.setSelectedItem(new Integer(footFontSizeReduction));
	}

	// Set values in screen to default values.
	private void makeDefaultSettingsVisible() {
	    colSepField.setText(
		    Integer.toString(colSepDefault()));
	    sectionSepField.setText(
		    Integer.toString(sectionSepDefault()));
	    lineSepField.setText(
		    Integer.toString(lineSepDefault()));
	    footnoteLineSepField.setText(
		    Integer.toString(footnoteLineSepDefault()));
	    uniformAscentBox.setSelected(
		    uniformAscentDefault());
	    collectNotesBox.setSelected(
		    collectNotesDefault());
	    colorBox.setSelected(
		    colorDefault());
	    pageSizeCombo.setSelectedItem(
		    pageSizeDefault());
	    topMarginField.setText(
		    Integer.toString(topMarginDefault()));
	    leftMarginField.setText(
		    Integer.toString(leftMarginDefault()));
	    bottomMarginField.setText(
		    Integer.toString(bottomMarginDefault()));
	    rightMarginField.setText(
		    Integer.toString(rightMarginDefault()));
	    lxSepField.setText(
		    Integer.toString(lxSepDefault()));
	    lxLeadingField.setText(
		    Integer.toString(lxLeadingDefault()));
	    lxInnerMarginField.setText(
		    Integer.toString(lxInnerMarginDefault()));
	    lxLineThicknessField.setText(
		    Integer.toString(lxLineThicknessDefault()));
	    lxAbbreviatedBox.setSelected(
		    lxAbbreviatedDefault());
	    latinFontNameCombo.setSelectedItem(
		    latinFontNameDefault());
	    latinFontSizeCombo.setSelectedItem(
		    new Integer(latinFontSizeDefault()));
	    egyptFontStyleCombo.setSelectedItem(
		    new Integer(egyptFontStyleDefault()));
	    egyptFontSizeCombo.setSelectedItem(
		    new Integer(egyptFontSizeDefault()));
            hieroFontNameCombo.setSelectedItem(
                    hieroFontNameDefault());
	    hieroFontSizeCombo.setSelectedItem(
		    new Integer(hieroFontSizeDefault()));
	    hieroResolutionCombo.setSelectedItem(
		    new Integer(hieroResolutionDefault()));
	    reductionCombo.setSelectedItem(
		    new Integer(footFontSizeReductionDefault()));
	}

    }

    // Dispose of resources.
    public void dispose() {
	if (window != null)
	    window.dispose();
    }

    //////////////////////////////////////////////////
    // Fonts.

    // For text in tiers. And italic. 
    public BaseFont latinFont;
    public BaseFont italicFont;
    // In footnotes.
    public BaseFont footLatinFont;
    public BaseFont footItalicFont;
    public BaseFont footBoldFont;
    // In headers.
    public BaseFont header1Font;
    public BaseFont header2Font;
    public BaseFont header3Font;
    // Transliteration.
    public BaseFont egyptLowerFont;
    public BaseFont egyptUpperFont;
    public BaseFont footEgyptLowerFont;
    public BaseFont footEgyptUpperFont;

    // Sizes of fonts.
    public float latinSize;
    public float italicSize;
    public float footLatinSize;
    public float footItalicSize;
    public float footBoldSize;
    public float header1Size;
    public float header2Size;
    public float header3Size;
    public float egyptLowerSize;
    public float egyptUpperSize;
    public float footEgyptLowerSize;
    public float footEgyptUpperSize;

    // Hieroglyphic.
    public HieroRenderContext hieroContext;
    public HieroRenderContext footHieroContext;

    // Compute fonts based on settings.
    public void computeFonts() {
	computeFontSizes();
        computeLatinFont();
        computeEgyptFont();
        computeHieroFont();
    }

    // Compute sizes of fonts.
    private void computeFontSizes() {
	latinSize = latinFontSize;
	plainSize = latinFontSize;
	italicSize = latinFontSize;
	boldSize = latinFontSize;
	footLatinSize = latinFontSize * footFontSizeReduction / 100.0f;
	footItalicSize = italicSize * footFontSizeReduction / 100.0f;
	footBoldSize = boldSize * footFontSizeReduction / 100.0f;
	header1Size = latinFontSize * header1Increase();
	header2Size = latinFontSize * header2Increase();
	header3Size = latinFontSize * header3Increase();
	egyptLowerSize = egyptFontSize;
	egyptUpperSize = egyptFontSize;
	footEgyptLowerSize = egyptFontSize * footFontSizeReduction / 100.0f;
	footEgyptUpperSize = egyptFontSize * footFontSizeReduction / 100.0f;
    }

    // Compute Latin font.
    private void computeLatinFont() {
	for (int i = 0; i < externalFonts().length; i++) 
	    if (latinFontName.equals(externalFonts()[i].getName())) {
		ExternalFont external = externalFonts()[i];
		latinFont = getFont(external, latinFontStyle);
		plainFont = getFont(external, Font.NORMAL);
		italicFont = getFont(external, Font.ITALIC);
		boldFont = getFont(external, Font.BOLD);
		footLatinFont = getFont(external, Font.NORMAL);
		footItalicFont = getFont(external, Font.ITALIC);
		footBoldFont = getFont(external, Font.BOLD);
		header1Font = getFont(external, headerFontStyle());
		header2Font = getFont(external, headerFontStyle());
		header3Font = getFont(external, headerFontStyle());
		return;
	    }
	latinFont = getFont(latinFontName);
	plainFont = getFont(latinFontName, Font.NORMAL);
	italicFont = getFont(latinFontName, Font.ITALIC);
	boldFont = getFont(latinFontName, Font.BOLD);
	footLatinFont = getFont(latinFontName, Font.NORMAL);
	footItalicFont = getFont(latinFontName, Font.ITALIC);
	footBoldFont = getFont(latinFontName, Font.BOLD);
	header1Font = getFont(latinFontName, headerFontStyle());
	header2Font = getFont(latinFontName, headerFontStyle());
	header3Font = getFont(latinFontName, headerFontStyle());
    }

    // Compute font for transliteration.
    private void computeEgyptFont() {
	egyptLowerFont = TransHelperPdf.translitLower(egyptFontStyle);
	egyptUpperFont = TransHelperPdf.translitUpper(egyptFontStyle);
	footEgyptLowerFont = TransHelperPdf.translitLower(egyptFontStyle);
	footEgyptUpperFont = TransHelperPdf.translitUpper(egyptFontStyle);
    }

    // Compute hieroglyphic fonts.
    private void computeHieroFont() {
	int footSize = Math.round(hieroFontSize * footFontSizeReduction / 100.0f);
	hieroContext = new HieroRenderContext(hieroFontName,
		hieroFontSize, Math.round(latinSize), 
		hieroResolution, color, true);
	footHieroContext = new HieroRenderContext(hieroFontName,
		footSize, Math.round(footLatinSize), 
		hieroResolution, color, true);
    }

    // Make base font.
    private static BaseFont getFont(String name) {
	return FontFactory.getFont(name).getBaseFont();
    }

    // Make base font from font name and style.
    private static BaseFont getFont(String name, int style) {
	return FontFactory.getFont(name, 
		12.0f /* is dummy value */, style).getBaseFont();
    }

    // Make base font from external font.
    private static BaseFont getFont(ExternalFont external, int style) {
	String file;
	switch (style) {
	    case Font.BOLD:
		file = external.getBold();
		break;
	    case Font.ITALIC:
		file = external.getItalic();
		break;
	    case Font.BOLDITALIC:
		file = external.getBoldItalic();
		break;
	    default:
		file = external.getPlain();
	}
	BaseFont f = FontUtil.baseFont(file);
	if (f == null) 
	    System.err.println("Problem loading external font");
	return f;
    }

    // For leading, factor of font sie.
    public static final float leadingFactor = 0.2f;

    // For footnote markers, raising, factor of font size.
    public static final float raisingFactor = 0.6f;

    // Get authors of viewed resources, separated by semicolons.
    public String getAuthors(Vector resources) {
	TreeSet authors = new TreeSet();
	for (int i = 0; i < resources.size(); i++) 
	    if (resources.get(i) instanceof EgyptianResource) {
		EgyptianResource resource = (EgyptianResource) resources.get(i);
		String name = resource.getStringProperty("name");
		if (!name.matches("\\s*"))
		    for (int j = 0; j < resource.nTiers(); j++) 
			if (!resource.getMode(j).equals(TextResource.IGNORED)) {
			    authors.add(name);
			    break;
			}
	    }
	Vector authorList = new Vector(authors);
	if (authorList.size() == 0)
	    return null;
	String authorString = "";
	for (int i = 0; i < authorList.size(); i++) {
	    authorString += (String) authorList.get(i);
	    if (i < authorList.size() - 1)
		authorString += "; ";
	}
	return authorString;
    }

    // Tier part for title of text.
    public TierPdfPart getTitlePdfPart(String title) {
	EgyptianTierPdfPart p = new Header1Part(title);
	p.setParams(this);
	return p;
    }

    // Tier part for name of resource.
    public TierPdfPart getNamePdfPart(String name) {
	EgyptianTierPdfPart p = new Header2Part(name);
	p.setParams(this);
	return p;
    }

}
