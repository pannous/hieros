/***************************************************************************/
/*                                                                         */
/*  EgyptianRenderParameters.java                                          */
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

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.util.*;

public class EgyptianRenderParameters extends RenderParameters {

    // For translations and other text.
    public String latinFontName;
    public int latinFontStyle;
    public int latinFontSize;
    // Transliteration.
    public int egyptFontStyle;
    public int egyptFontSize;
    // Hieroglyphic.
    public String hieroFontName;
    public int hieroFontSize;

    // Lexical entries.
    public int lxSep;
    public int lxLeading;
    public int lxInnerMargin;
    public int lxLineThickness;
    public boolean lxAbbreviated;

    // Only one window created:
    private SettingsWindow window;

    // The frame where text is to come.
    // Used for metrics.
    private JFrame targetFrame;

    // Construct with targetFrame.
    public EgyptianRenderParameters(JFrame targetFrame) {
	super(); // Calls setDefaults.
	this.targetFrame = targetFrame;
	computeFonts();
    }
    // Construct without targetFrame.
    public EgyptianRenderParameters() {
	super(); // Calls setDefaults.
    }

    // Set targetFrame, if not done yet.
    public void setTargetFrame(JFrame targetFrame) {
	if (this.targetFrame == null) {
	    this.targetFrame = targetFrame;
	    computeFonts();
	}
    }

    public void setDefaults() {
	super.setDefaults();

	latinFontName = latinFontNameDefault();
	latinFontStyle = latinFontStyleDefault();
	latinFontSize = latinFontSizeDefault();

	egyptFontStyle = egyptFontStyleDefault();
	egyptFontSize = egyptFontSizeDefault();

	hieroFontName = hieroFontNameDefault();
	hieroFontSize = hieroFontSizeDefault();

	lxSep = lxSepDefault();
	lxLeading = lxLeadingDefault();
	lxInnerMargin = lxInnerMarginDefault();
	lxLineThickness = lxLineThicknessDefault();
	lxAbbreviated = lxAbbreviatedDefault();
    }

    protected String latinFontNameDefault() {
	return Settings.latinFontNameDefault;
    }
    protected int latinFontStyleDefault() {
	return Settings.latinFontStyleDefault;
    }
    protected int latinFontSizeDefault() {
	return Settings.latinFontSizeDefault;
    }
    protected int egyptFontStyleDefault() {
	return Settings.egyptFontStyleDefault;
    }
    protected int egyptFontSizeDefault() {
	return Settings.egyptFontSizeDefault;
    }
    protected String hieroFontNameDefault() {
	return Settings.hieroFontNameDefault;
    }
    protected int hieroFontSizeDefault() {
	return Settings.hieroFontSizeDefault;
    }
    protected int lxSepDefault() {
	return Settings.lxSepDefault;
    }
    protected int lxLeadingDefault() {
	return Settings.lxLeadingDefault;
    }
    protected int lxInnerMarginDefault() {
	return Settings.lxInnerMarginDefault;
    }
    protected int lxLineThicknessDefault() {
	return Settings.lxLineThicknessDefault;
    }
    protected boolean lxAbbreviatedDefault() {
	return Settings.lxAbbreviatedDefault;
    }

    public void edit() {
	if (window == null)
	    window = new SettingsWindow();
	window.setVisible(true);
    }

    // Window for manipulating values.
    private class SettingsWindow extends JFrame implements ActionListener {
	private JTextField leftMarginField;
	private JTextField rightMarginField;
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

	private JComboBox latinFontNameCombo;
	private JComboBox latinFontStyleCombo;
	private JComboBox latinFontSizeCombo;
	private JComboBox egyptFontStyleCombo;
	private JComboBox egyptFontSizeCombo;
	private JComboBox hieroFontNameCombo;
	private JComboBox hieroFontSizeCombo;
	private JComboBox reductionCombo;

	public SettingsWindow() {
	    final int STRUT_SIZE = 6;
	    setTitle("Settings");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());
	    JPanel leftPanel = new JPanel();
	    JPanel rightPanel = new JPanel();
	    JPanel buttonsPanel = new JPanel();
	    content.add(leftPanel, BorderLayout.WEST);
	    content.add(Box.createHorizontalStrut(STRUT_SIZE), BorderLayout.CENTER);
	    content.add(rightPanel, BorderLayout.EAST);
	    content.add(buttonsPanel, BorderLayout.SOUTH);
	    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
	    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

	    // Horizontal distances.
	    JPanel horPanel = new JPanel(new SpringLayout());
	    horPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Horizontal space"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    leftMarginField = new JTextField();
	    leftMarginField.setColumns(3);
	    leftMarginField.setMaximumSize(leftMarginField.getPreferredSize());
	    horPanel.add(new JLabel("Left margin:"));
	    horPanel.add(leftMarginField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    rightMarginField = new JTextField();
	    rightMarginField.setColumns(3);
	    rightMarginField.setMaximumSize(rightMarginField.getPreferredSize());
	    horPanel.add(new JLabel("Right margin:"));
	    horPanel.add(rightMarginField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    colSepField = new JTextField();
	    colSepField.setColumns(3);
	    colSepField.setMaximumSize(colSepField.getPreferredSize());
	    horPanel.add(new JLabel("Column sep:"));
	    horPanel.add(colSepField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(horPanel);
	    SpringUtilities.makeCompactGrid(horPanel, 3, 4, 5, 5, 5, 5);
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
	    vertPanel.add(new JLabel("pixels"));
	    vertPanel.add(Box.createHorizontalGlue());
	    lineSepField = new JTextField();
	    lineSepField.setColumns(3);
	    lineSepField.setMaximumSize(lineSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Line sep:"));
	    vertPanel.add(lineSepField);
	    vertPanel.add(new JLabel("pixels"));
	    vertPanel.add(Box.createHorizontalGlue());
	    footnoteLineSepField = new JTextField();
	    footnoteLineSepField.setColumns(3);
	    footnoteLineSepField.setMaximumSize(footnoteLineSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Footnote line sep:"));
	    vertPanel.add(footnoteLineSepField);
	    vertPanel.add(new JLabel("pixels"));
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
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxLeadingField = new JTextField();
	    lxLeadingField.setColumns(3);
	    lxLeadingField.setMaximumSize(lxLeadingField.getPreferredSize());
	    lxPanel.add(new JLabel("Leading:"));
	    lxPanel.add(lxLeadingField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxInnerMarginField = new JTextField();
	    lxInnerMarginField.setColumns(3);
	    lxInnerMarginField.setMaximumSize(lxInnerMarginField.getPreferredSize());
	    lxPanel.add(new JLabel("Inner margin:"));
	    lxPanel.add(lxInnerMarginField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxLineThicknessField = new JTextField();
	    lxLineThicknessField.setColumns(3);
	    lxLineThicknessField.setMaximumSize(lxLineThicknessField.getPreferredSize());
	    lxPanel.add(new JLabel("Line thickness:"));
	    lxPanel.add(lxLineThicknessField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(lxPanel);
	    SpringUtilities.makeCompactGrid(lxPanel, 4, 4, 5, 5, 5, 5);
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

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
	    leftPanel.add(formatPanel);
	    SpringUtilities.makeCompactGrid(formatPanel, 3, 3, 5, 5, 5, 5);
	    formatPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Latin font.
	    JPanel latinPanel = new JPanel(new SpringLayout());
	    latinPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Latin font"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    String envfonts[] = gEnv.getAvailableFontFamilyNames();
	    Vector fontVector = new Vector();
	    for (int i = 0; i < envfonts.length; i++) {
		if (!envfonts[i].matches("Translit.*") &&
			!envfonts[i].matches("Umsch.*") &&
			!envfonts[i].matches("NewGardiner.*") &&
			!envfonts[i].matches("AegyptusSubset.*") &&
			!envfonts[i].matches("HieroglyphicAux.*"))
		    fontVector.addElement(envfonts[i]);
	    }
	    latinFontNameCombo = new JComboBox(fontVector);
	    latinFontNameCombo.setMaximumRowCount(9);
	    latinFontNameCombo.setMaximumSize(latinFontNameCombo.getPreferredSize());
	    latinPanel.add(new JLabel("Font:"));
	    latinPanel.add(latinFontNameCombo);
	    latinPanel.add(Box.createHorizontalGlue());
	    latinPanel.add(Box.createHorizontalGlue());
	    latinFontStyleCombo = new JComboBox(FontTypeRenderer.fontTypes);
	    latinFontStyleCombo.setRenderer(new FontTypeRenderer());
	    latinFontStyleCombo.setMaximumRowCount(9);
	    latinFontStyleCombo.setMaximumSize(latinFontStyleCombo.getPreferredSize());
	    latinPanel.add(new JLabel("Style:"));
	    latinPanel.add(latinFontStyleCombo);
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
	    SpringUtilities.makeCompactGrid(latinPanel, 3, 4, 5, 5, 5, 5);
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

	    // Confirmation buttons.
	    buttonsPanel.add(Box.createHorizontalGlue());
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>O</u>kay</html>", "okay", KeyEvent.VK_O));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>A</u>pply</html>", "apply", KeyEvent.VK_A));
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
	    if (e.getActionCommand().equals("okay")) {
		applySettings();
		setVisible(false);
	    } else if (e.getActionCommand().equals("apply")) {
		applySettings();
	    } else if (e.getActionCommand().equals("defaults")) {
		makeDefaultSettingsVisible();
	    } else if (e.getActionCommand().equals("quit")) {
		makeSettingsVisible();
		setVisible(false);
	    }
	}

	// Apply values from screen.
	private void applySettings() {
	    try { 
		int i = Integer.parseInt(leftMarginField.getText());
		leftMargin = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(rightMarginField.getText());
		rightMargin = i;
	    } catch (NumberFormatException e) {}
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
	    latinFontStyle = ((Integer) latinFontStyleCombo.getSelectedItem()).intValue();
	    latinFontSize = ((Integer) latinFontSizeCombo.getSelectedItem()).intValue();
	    egyptFontStyle = ((Integer) egyptFontStyleCombo.getSelectedItem()).intValue();
	    egyptFontSize = ((Integer) egyptFontSizeCombo.getSelectedItem()).intValue();
	    hieroFontName = (String) hieroFontNameCombo.getSelectedItem();
	    hieroFontSize = ((Integer) hieroFontSizeCombo.getSelectedItem()).intValue();
	    computeFonts();
	    makeSettingsVisible();
	    reformat();
	}

	// Set values in screen to current values.
	private void makeSettingsVisible() {
	    leftMarginField.setText(Integer.toString(leftMargin));
	    rightMarginField.setText(Integer.toString(rightMargin));
	    colSepField.setText(Integer.toString(colSep));
	    sectionSepField.setText(Integer.toString(sectionSep));
	    lineSepField.setText(Integer.toString(lineSep));
	    footnoteLineSepField.setText(Integer.toString(footnoteLineSep));
	    uniformAscentBox.setSelected(uniformAscent);
	    collectNotesBox.setSelected(collectNotes);
	    lxSepField.setText(Integer.toString(lxSep));
	    lxLeadingField.setText(Integer.toString(lxLeading));
	    lxInnerMarginField.setText(Integer.toString(lxInnerMargin));
	    lxLineThicknessField.setText(Integer.toString(lxLineThickness));
	    lxAbbreviatedBox.setSelected(lxAbbreviated);
	    latinFontNameCombo.setSelectedItem(latinFontName);
	    latinFontStyleCombo.setSelectedItem(new Integer(latinFontStyle));
	    latinFontSizeCombo.setSelectedItem(new Integer(latinFontSize));
	    egyptFontStyleCombo.setSelectedItem(new Integer(egyptFontStyle));
	    egyptFontSizeCombo.setSelectedItem(new Integer(egyptFontSize));
	    hieroFontNameCombo.setSelectedItem(hieroFontName);
	    hieroFontSizeCombo.setSelectedItem(new Integer(hieroFontSize));
	    reductionCombo.setSelectedItem(new Integer(footFontSizeReduction));
	}

	// Set values in screen to default values.
	private void makeDefaultSettingsVisible() {
	    leftMarginField.setText(
		    Integer.toString(leftMarginDefault()));
	    rightMarginField.setText(
		    Integer.toString(rightMarginDefault()));
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
	    latinFontStyleCombo.setSelectedItem(
		    new Integer(latinFontStyleDefault()));
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

    // For translations in tiers. And italic.
    public Font latinFont;
    public Font italicFont;
    // In footnotes.
    public Font footLatinFont;
    public Font footItalicFont;
    public Font footBoldFont;
    // Transliteration.
    public Font egyptLowerFont;
    public Font egyptUpperFont;
    public Font footEgyptLowerFont;
    public Font footEgyptUpperFont;

    // Metrics.
    public FontMetrics latinFontMetrics;
    public FontMetrics italicFontMetrics;
    public FontMetrics footLatinFontMetrics;
    public FontMetrics footItalicFontMetrics;
    public FontMetrics footBoldFontMetrics;
    public FontMetrics egyptLowerFontMetrics;
    public FontMetrics egyptUpperFontMetrics;
    public FontMetrics footEgyptLowerFontMetrics;
    public FontMetrics footEgyptUpperFontMetrics;

    // Hieroglyphic render contexts.
    public HieroRenderContext hieroContext;
    public HieroRenderContext footHieroContext;

    // Compute fonts based on settings.
    private void computeFonts() {
        computeLatinFont();
        computeEgyptFont();
        computeHieroFont();
    }

    // Compute Latin font.
    private void computeLatinFont() {
        int footSize = Math.round(latinFontSize * footFontSizeReduction / 100.0f);
        latinFont = new Font(latinFontName, latinFontStyle, latinFontSize);
        latinFontMetrics = targetFrame.getFontMetrics(latinFont);
        footLatinFont = new Font(latinFontName, Font.PLAIN, footSize);
        footLatinFontMetrics = targetFrame.getFontMetrics(footLatinFont);
        italicFont = new Font(latinFontName, Font.ITALIC, latinFontSize);
        italicFontMetrics = targetFrame.getFontMetrics(italicFont);
        footItalicFont = new Font(latinFontName, Font.ITALIC, footSize);
        footItalicFontMetrics = targetFrame.getFontMetrics(footItalicFont);
        footBoldFont = new Font(latinFontName, Font.BOLD, footSize);
        footBoldFontMetrics = targetFrame.getFontMetrics(footBoldFont);
    }

    // Compute font for transliteration.
    private void computeEgyptFont() {
	int footSize = Math.round(egyptFontSize * footFontSizeReduction / 100.0f);
	egyptLowerFont = TransHelper.translitLower(egyptFontStyle, egyptFontSize);
	egyptUpperFont = TransHelper.translitUpper(egyptFontStyle, egyptFontSize);
        egyptLowerFontMetrics = targetFrame.getFontMetrics(egyptLowerFont);
        egyptUpperFontMetrics = targetFrame.getFontMetrics(egyptUpperFont);
	footEgyptLowerFont = TransHelper.translitLower(egyptFontStyle, footSize);
	footEgyptUpperFont = TransHelper.translitUpper(egyptFontStyle, footSize);
        footEgyptLowerFontMetrics = targetFrame.getFontMetrics(footEgyptLowerFont);
        footEgyptUpperFontMetrics = targetFrame.getFontMetrics(footEgyptUpperFont);
    }

    // Compute hieroglyphic fonts.
    private void computeHieroFont() {
        int footSize = Math.round(hieroFontSize * footFontSizeReduction / 100.0f);
        hieroContext = new HieroRenderContext(hieroFontName, hieroFontSize, latinFontSize, true);
        footHieroContext = new HieroRenderContext(hieroFontName, footSize, true);
    }

    // For footnote markers, raising, factor of font size.
    public static final float raisingFactor = 0.4f;

}
