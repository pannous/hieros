package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.align.SimpleTextWindow;
import nederhof.alignment.egyptian.*;
import nederhof.egyptian.trans.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.operations.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

/**
 * Orthographic Editor.
 */
public class OrthoEditor extends ResourceEditor {

    // Unique instance of suggestor.
    private static AnnotationSuggestor suggestor = null;
    // Unique instance of orthographic analyser.
    private static TrainedOrthoAnalyser analyser = null;
    // For hieroglyphic. (Unique instances.)
    protected static HieroRenderContext hieroContext = null;
    protected static ParsingContext parsingContext = null;

    // The mode of operation.
    // EDIT: external edit of hieroglyphic.
    // SPLIT: splitting fragment into fragments.
    // ANNOTATE: orthographic annotation 
    public enum Mode { ANNOTATE, SPLIT };
    // Default.
    private Mode mode = Mode.ANNOTATE;

    // Indicator of no function.
    private final String NO_FUN = "-";

    // Containing annotations.
    private JPanel toolPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT));
    private OrthoSegmentsPane orthoPane;

    // Auxiliary window with info on glyphs.
    protected HieroInfoWindow infoWindow = null;

    // Auxiliary class for manipulating resource.
    private OrthoManipulator manipulator;

    // Constructor.
    public OrthoEditor(EgyptianOrtho resource, int currentSegment) {
	super(resource, currentSegment);
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	extendMenu();
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	if (suggestor == null) { // do only once
	    try {
		suggestor = new AnnotationSuggestor();
		analyser = new TrainedOrthoAnalyser(1);
		LinkedList<EgyptianOrtho> corpus = new LinkedList<EgyptianOrtho>();
		corpus.add(new EgyptianOrtho("corpus/resources/ShipwreckedOrtho.xml"));
		analyser.train(corpus);
	    } catch (Exception e) {
		// ignore
	    }
	    hieroContext = new HieroRenderContext(Settings.textHieroFontSize, true); 
	    parsingContext = new ParsingContext(hieroContext, true);
	}
	manipulator = new OrthoManipulator(resource, currentSegment, parsingContext) {
	    public void recordChange() {
		OrthoEditor.this.recordChange();
	    }
	};

	addToolPanel();
	addOrthoPane();
	addListeners();
	setVisible(true);
	allowEditing(true);
    }

    /**
     * Get the name of the editor.
     */
    public String getName() {
	return "Orthographic Editor";
    }

    // Delegate to auxiliary class to keep track of
    // current segment (overrides superclass).
    public int getCurrentSegment() {
	return manipulator.currentSegment();
    }

    /////////////////////////////////////////////
    // Menu.

    private final JMenu resourceMenu = new EnabledMenu(
	    "<u>R</u>esource", KeyEvent.VK_R);
    private final JMenuItem annotateItem = new EnabledMenuItem(this,
	    "<u>A</u>nnotate", "annotate", KeyEvent.VK_A);
    private final JMenuItem splitItem = new EnabledMenuItem(this,
	    "s<u>P</u>lit", "split", KeyEvent.VK_P);
    private final JMenuItem meaningItem = new EnabledMenuItem(this,
	    "<u>M</u>eanings", "meanings", KeyEvent.VK_M);
    private final JMenuItem hieroItem = new EnabledMenuItem(this,
	    "h<u>I</u>eroglyphic", "hiero", KeyEvent.VK_I);
    private final JMenuItem transItem = new EnabledMenuItem(this,
	    "<u>T</u>ransliteration", "trans", KeyEvent.VK_T);
    private final JMenuItem normalizeItem = new EnabledMenuItem(this,
	    "n<u>O</u>rmalize", "normalize", KeyEvent.VK_O);
    private final JMenuItem mergeItem = new EnabledMenuItem(this,
	    "m<u>E</u>rge", "merge", KeyEvent.VK_E);
    private final JMenuItem wordItem = new EnabledMenuItem(this,
	    "new <u>W</u>ord", "word", KeyEvent.VK_W);
    private final JMenuItem appendItem = new EnabledMenuItem(this,
	    "appe<u>N</u>d word", "append", KeyEvent.VK_N);
    private final JMenuItem deleteItem = new EnabledMenuItem(this,
	    "<u>D</u>elete word", "delete", KeyEvent.VK_D);
    private final JMenuItem leftItem = new EnabledMenuItem(this, 
	    "word left", "word left", KeyEvent.VK_COMMA);
    private final JMenuItem rightItem = new EnabledMenuItem(this, 
	    "word right", "word right", KeyEvent.VK_PERIOD);
    private final JMenuItem glyphLeftItem = new EnabledMenuItem(this, 
	    "glyph left", "glyph left", KeyEvent.VK_OPEN_BRACKET);
    private final JMenuItem glyphRightItem = new EnabledMenuItem(this, 
	    "glyph right", "glyph right", KeyEvent.VK_CLOSE_BRACKET);
    private final JMenuItem glyphRightPlusItem = new EnabledMenuItem(this, 
	    "add glyph", "add glyph", KeyEvent.VK_K);
    private final JMenuItem funLeftItem = new EnabledMenuItem(this, 
	    "function left", "fun left", KeyEvent.VK_LEFT);
    private final JMenuItem funRightItem = new EnabledMenuItem(this, 
	    "function right", "fun right", KeyEvent.VK_RIGHT);

    // Add more buttons to menu.
    private void extendMenu() {
	fileMenu.add(hieroItem);
	fileMenu.add(transItem);
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(resourceMenu);
	resourceMenu.add(annotateItem);
	resourceMenu.add(splitItem);
	resourceMenu.add(meaningItem);
	resourceMenu.addSeparator();
	resourceMenu.add(normalizeItem);
	resourceMenu.add(mergeItem);
	resourceMenu.add(wordItem);
	resourceMenu.add(appendItem);
	resourceMenu.add(deleteItem);
	resourceMenu.add(leftItem);
	resourceMenu.add(rightItem);
	resourceMenu.add(glyphLeftItem);
	resourceMenu.add(glyphRightItem);
	resourceMenu.add(glyphRightPlusItem);
	resourceMenu.add(funLeftItem);
	resourceMenu.add(funRightItem);
	variableJComponents.add(resourceMenu);
	variableJComponents.add(annotateItem);
	variableJComponents.add(splitItem);
	variableJComponents.add(meaningItem);
	variableJComponents.add(hieroItem);
	variableJComponents.add(transItem);
	variableJComponents.add(normalizeItem);
	variableJComponents.add(mergeItem);
	variableJComponents.add(wordItem);
	variableJComponents.add(appendItem);
	variableJComponents.add(deleteItem);
	variableJComponents.add(leftItem);
	variableJComponents.add(rightItem);
	variableJComponents.add(glyphLeftItem);
	variableJComponents.add(glyphRightItem);
	variableJComponents.add(glyphRightPlusItem);
	variableJComponents.add(funLeftItem);
	variableJComponents.add(funRightItem);

	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(new ClickButton(OrthoEditor.this, 
		    "<u>H</u>elp", "help", KeyEvent.VK_H));
    }

    // //////////////////////////////////////
    // Tools: suggestions, ortho functions tabs.

    // For choosing suggestions.
    private JPanel suggestPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT));
    private JButton suggestButton = new EnabledButton(this, "suggest");
    private JButton retrieveButton = new EnabledButton(this, "retrieve");
    private JTextField retrieveDisplay = new JTextField("0",2);
    private JButton computeButton = new EnabledButton(this, "compute");
    private String emptyDescr[] = { "" }; 
    private JComboBox suggestions = new JComboBox(emptyDescr);
    private LinkedList<Vector<OrthoElem>> retrieveList = new LinkedList<Vector<OrthoElem>>();

    // Contains ortho functions and additional inputs.
    private int tabbedSize = 100;
    private JTabbedPane functionTabs = new ConservativeTabbedPane();

    // Fields for additional inputs of ortho functions.
    private AlPlainEditor logWord = new AlPlainEditor("word", 20);
    private JTextField detDescr = new BorderText(50, "descr");
    private AlPlainEditor detWord = new AlPlainEditor("word", 20);
    private AlPlainEditor phonLit = new AlPlainEditor("lit", 20);
    private AlPlainEditor phondetLit = new AlPlainEditor("lit", 20);
    private JTextField multNum = new BorderText(5, "num");
    private JTextField typDescr = new BorderText(50, "descr");

    /**
     * Prepare and add the tool bar.
     */
    private void addToolPanel() {
	// Buttons applicable in certain modes.
	suggestPanel.add(suggestButton);
	suggestPanel.add(suggestions);
	suggestPanel.add(retrieveButton);
	suggestPanel.add(retrieveDisplay);
	suggestPanel.add(computeButton);
	suggestions.setPreferredSize(new Dimension(300, suggestButton.getPreferredSize().height));
	suggestButton.setMnemonic(KeyEvent.VK_G);
	retrieveButton.setMnemonic(KeyEvent.VK_V);
	retrieveDisplay.setEditable(false);
	computeButton.setMnemonic(KeyEvent.VK_C);

	// Functions.
	JPanel logPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	logPanel.add(logWord);
	logWord.setParent(this);
	functionTabs.addTab("log", logPanel);

	JPanel detPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	detPanel.add(detDescr);
	functionTabs.addTab("det", detPanel);

	JPanel detwordPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	detwordPanel.add(detWord);
	detWord.setParent(this);
	functionTabs.addTab("det(word)", detwordPanel);

	JPanel phonPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	phonPanel.add(phonLit);
	phonLit.setParent(this);
	functionTabs.addTab("phon", phonPanel);

	JPanel phondetPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	phondetPanel.add(phondetLit);
	phondetLit.setParent(this);
	functionTabs.addTab("phondet", phondetPanel);

	JPanel multPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	multPanel.add(multNum);
	functionTabs.addTab("mult", multPanel);

	JPanel typPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	typPanel.add(typDescr);
	functionTabs.addTab("typ", typPanel);

	JPanel spuriousPanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	functionTabs.addTab("spurious", spuriousPanel);

	JPanel nonePanel = new ConservativePanel(new FlowLayout(FlowLayout.LEFT), tabbedSize);
	functionTabs.addTab(NO_FUN, nonePanel);
	int noneIndex = functionTabs.indexOfTab(NO_FUN);
	functionTabs.setSelectedIndex(noneIndex);

	// Altogether.
	toolPanel.add(suggestPanel);
	toolPanel.add(functionTabs);
	add(toolPanel);

	variableJComponents.add(toolPanel);
	variableJComponents.add(suggestPanel);
	variableJComponents.add(suggestButton);
	variableJComponents.add(suggestions);
	variableJComponents.add(retrieveButton);
	variableJComponents.add(retrieveDisplay);
	variableJComponents.add(computeButton);
	variableJComponents.add(functionTabs);
	variableJComponents.add(logPanel);
	variableJComponents.add(detPanel);
	variableJComponents.add(detwordPanel);
	variableJComponents.add(phonPanel);
	variableJComponents.add(phondetPanel);
	variableJComponents.add(multPanel);
	variableJComponents.add(typPanel);
	variableJComponents.add(spuriousPanel);

	variableStyledPhraseEditors.add(logWord);
	variableJComponents.add(detDescr);
	variableStyledPhraseEditors.add(detWord);
	variableStyledPhraseEditors.add(phonLit);
	variableStyledPhraseEditors.add(phondetLit);
	variableJComponents.add(multNum);
	variableJComponents.add(typDescr);
    }

    // Add pane with annotations.
    private void addOrthoPane() {
	orthoPane = new OrthoSegmentsPane(manipulator, this,
		hieroContext, parsingContext) {
	    public boolean splitMode() {
		return mode == Mode.SPLIT;
	    }
	    public void querySign(String sign) {
		showMeaning(sign);
	    }
	    public void showOrtho() {
		setOrtho();
	    }
	    public void setFunSelection() {
		setFunctionSelection();
	    }
	    public void saveCurrentFun() {
		saveToResource();
		super.saveCurrentFun();
	    }
	    public void saveFunValue() {
		saveValue();
	    }
	    public boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
		return OrthoEditor.this.isConsistent(ortho, hiNames, al);
	    }
	    public boolean userConfirmation(String message) {
		return userConfirmsLoss(message);
	    }
	};
	add(orthoPane);
	orthoPane.update();
    }

    //////////////////////////////////////////////////////////////
    // Help.

    /**
     * Open help window if not already open.
     */
    private void openHelp() {
	if (helpWindow == null) {
	    URL url = FileAux.fromBase("data/help/text/ortho.html");
	    helpWindow = new HTMLWindow("Orthographic annotation manual", url);
	}
	helpWindow.setVisible(true);
    }

    //////////////////////////////////////////////////////////////
    // Glyph meanings.

    // Open windows for showing meaning. Check whether there is 
    // glyph for which meaning can be shown.
    private void openMeanings() {
        if (infoWindow == null) {
            infoWindow = new HieroInfoWindow(Settings.infoWidthInit,
                    Settings.infoHeightInit);
            Point point = getLocationOnScreen();
            Dimension size = getSize();
            infoWindow.setLocation(point.x + size.width, point.y);
        }
	infoWindow.setVisible(true);
	orthoPane.querySign();
    }

    // For glyph, show meaning, if window is open.
    private void showMeaning(String glyph) {
	if (infoWindow != null) {
	    String normal = parsingContext.nameToGardiner(glyph);
	    infoWindow.lookupSignInfo(normal);
	}
    }

    //////////////////////////////////////////////////////////////
    // Closing and saving.

    // Number of changes since last time.
    private int nChanges = 0;
    // Changes to next save.
    private final int maxChanges = 50;

    // Record number of changes. Save every so often.
    public void recordChange() {
	super.recordChange();
	nChanges++;
	if (nChanges > maxChanges) {
	    trySave();
	    nChanges = 0;
	}
    }

    /**
     * Save the information displayed on GUI to the resource.
     */
    public void saveToResource() {
	clearSuggestions();
	OrthoElem ortho = manipulator.currentFun();
	if (ortho != null) {
	    String fun = getFun();
	    if (fun.equals(NO_FUN)) {
		orthoPane.removeFun();
		return;
	    } 
	    String argName = getArgName(fun);
	    String argValue = getArgValue(fun);
	    if (argValue.equals(ortho.argValue()))
		return;
	    int[][] signs = ortho.signRanges();
	    int[][] letters = ortho.letterRanges();
	    OrthoElem newOrtho = OrthoElem.makeOrtho(fun, argName, argValue, signs, letters);
	    orthoPane.replaceFun(newOrtho);
	}
    }

    // Save the function value.
    public void saveValue() {
	OrthoElem ortho = manipulator.currentFun();
	String fun = getFun();
	if (ortho != null && !fun.equals(NO_FUN)) {
	    String val = getArgValue(fun);
	    ortho.setValue(val);
	}
    }

    public void dispose() {
	super.dispose();
	if (infoWindow != null)
	    infoWindow.dispose();
    }

    //////////////////////////////////////////////////////////////
    // Listeners.

    /**
     * Add listeners to components.
     */
    private void addListeners() {
	// Listen to the function tabs selection.
	functionTabs.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
	        SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			chooseFunction();
		    }
		});
	    }
	});
	// Listen to selected suggestions.
	suggestions.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED &&
			!annoSuggestions.isEmpty()) {
		    int i = suggestions.getSelectedIndex();
		    chooseSuggestion(i);
		}
	    }
	});
    }

    // Get currently selected tab and copy its function.
    private void chooseFunction() {
	String fun = getFun();
	OrthoElem ortho = manipulator.currentFun();
	if (fun == null || fun.equals(NO_FUN)) {
	    if (ortho != null) {
		clearSuggestions();
		orthoPane.removeFun();
	    }
	} else {
	    String argName = getArgName(fun);
	    String argValue = getArgValue(fun);
	    if (ortho != null) {
		String oldName = OrthoElem.extendedName(ortho);
		String oldValue = ortho.argValue();
		if (oldName.equals(fun) && 
			(fun.equals("spurious") || oldValue.equals(argValue)))
		    return;
		int[][] signs = ortho.signRanges();
		orthoPane.removeFun();
		int[][] letters = null;
		if (!fun.equals("spurious") && isAlArg(argName)) {
		    letters = predictLetters(argValue);
		    if (letters != null) 
			letters = predictLetters(argValue.replaceAll("t$", "")); 
		    if (letters != null) 
			letters = predictLetters(argValue.replaceAll("w$", "")); 
		    if (letters != null) 
			letters = predictLetters(argValue.replaceAll("j$", "")); 
		    if (letters != null) 
			letters = predictLetters(argValue.replaceAll("wt$", "")); 
		    if (letters != null) 
			letters = predictLetters(argValue.replaceAll("wj$", "")); 
		}
		OrthoElem newOrtho = OrthoElem.makeOrtho(fun, argName, argValue, signs, letters);
		orthoPane.addFun(newOrtho);
	    } else if (!orthoPane.focussedSigns().isEmpty()) {
		TreeSet<Integer> signs = orthoPane.focussedSigns();
		TreeSet<Integer> letters = new TreeSet<Integer>();
		if (isAlArg(argName))
		    addPredictLetters(argValue, letters);
		OrthoElem newOrtho = OrthoElem.makeOrtho(fun, argName, argValue, 
			signs, letters);
		orthoPane.addFun(newOrtho);
	    }
	}
    }

    // Give letter positions that fit current function.
    private int[][] predictLetters(String value) {
	String word = manipulator.trans();
	int pos = manipulator.covered();
	int i = word.substring(pos).indexOf(value);
	if (i < 0)
	    i = word.lastIndexOf(value);
	else
	    i += pos;
	if (i < 0) {
	    i = soundNormalized(word).substring(pos).indexOf(soundNormalized(value));
	    if (i < 0)
		i = soundNormalized(word).lastIndexOf(soundNormalized(value));
	    else
		i += pos;
	}
	if (i < 0)
	    return null;
	else {
	    // i = i - nSpaces(word.substring(0, i));
	    int[][] ranges = new int[1][];
	    ranges[0] = new int[] {i, value.length()};
	    return ranges;
	}
    }
    // Same, but add positions to set.
    private void addPredictLetters(String value, TreeSet<Integer> letters) {
	int[][] ranges = predictLetters(value);
	if (ranges != null) {
	    int start = ranges[0][0];
	    int len = ranges[0][1];
	    for (int i = 0; i < len; i++) 
		letters.add(start + i);
	}
    }
    // Number of occurrences of spaces.
    private int nSpaces(String s) {
	int n = 0;
	for (int i = 0; i < s.length(); i++) 
	    if (s.charAt(i) == ' ')
		n++;
	return n;
    }
    // Normalize d/D, t/T.
    private String soundNormalized(String s) {
	return s.replaceAll("D", "d").replaceAll("T", "t");
    }

    /**
     * Actions belonging to buttons.
     */
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("annotate")) {
	    setMode(Mode.ANNOTATE);
	} else if (e.getActionCommand().equals("split")) {
	    orthoPane.saveCurrentFun();
	    setMode(Mode.SPLIT);
	} else if (e.getActionCommand().equals("meanings")) {
	    openMeanings();
	} else if (e.getActionCommand().equals("hiero")) {
	    orthoPane.saveCurrentFun();
	    importHiero();
	} else if (e.getActionCommand().equals("trans")) {
	    orthoPane.saveCurrentFun();
	    importTrans();
	} else if (e.getActionCommand().equals("normalize")) {
	    manipulator.normalize();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("merge")) {
	    orthoPane.saveCurrentFun();
	    manipulator.mergeSegments();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("word")) {
	    orthoPane.saveCurrentFun();
	    manipulator.insertSegment();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("append")) {
	    orthoPane.saveCurrentFun();
	    manipulator.appendSegment();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("delete")) {
	    orthoPane.saveCurrentFun();
	    deleteWord();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("word left")) {
	    orthoPane.saveCurrentFun();
	    manipulator.left();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("word right")) {
	    orthoPane.saveCurrentFun();
	    manipulator.right();
	    orthoPane.update();
	} else if (e.getActionCommand().equals("glyph left")) {
	    orthoPane.glyphLeft();
	} else if (e.getActionCommand().equals("glyph right")) {
	    orthoPane.glyphRight();
	    if (mode == Mode.ANNOTATE) {
		extendBySuggestions();
		fillSuggestions();
	    }
	} else if (e.getActionCommand().equals("add glyph")) {
	    addGlyphToFocus();
	} else if (e.getActionCommand().equals("fun left")) {
	    funLeft();
	} else if (e.getActionCommand().equals("fun right")) {
	    funRight();
	} else if (e.getActionCommand().equals("help")) {
	    openHelp();
	} else if (e.getActionCommand().equals("suggest")) {
	    fillSuggestions();
	} else if (e.getActionCommand().equals("retrieve")) {
	    retrieveOrtho();
	} else if (e.getActionCommand().equals("compute")) {
	    computeOrtho();
	} else
	    super.actionPerformed(e);
    }

    ////////////////////////////////////////////////////////////////
    // Appearance.

    // Overrides superclass.
    public void allowEditing(boolean allow) {
	super.allowEditing(allow);
	annotateItem.setEnabled(mode == Mode.SPLIT);
	splitItem.setEnabled(mode == Mode.ANNOTATE);
	meaningItem.setEnabled(mode == Mode.ANNOTATE);
	hieroItem.setEnabled(mode == Mode.ANNOTATE);
	transItem.setEnabled(mode == Mode.ANNOTATE);
	normalizeItem.setEnabled(mode == Mode.SPLIT || mode == Mode.ANNOTATE);
	mergeItem.setEnabled(mode == Mode.SPLIT);
	wordItem.setEnabled(mode == Mode.ANNOTATE);
	appendItem.setEnabled(mode == Mode.ANNOTATE);
	deleteItem.setEnabled(mode == Mode.ANNOTATE);
	leftItem.setEnabled(mode == Mode.SPLIT || mode == Mode.ANNOTATE);
	rightItem.setEnabled(mode == Mode.SPLIT || mode == Mode.ANNOTATE);
	glyphLeftItem.setEnabled(mode == Mode.SPLIT || mode == Mode.ANNOTATE);
	glyphRightItem.setEnabled(mode == Mode.SPLIT || mode == Mode.ANNOTATE);
	glyphRightPlusItem.setEnabled(mode == Mode.ANNOTATE);
	setFunctionSelection();

	if (mode == Mode.SPLIT)
	    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	else
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Switch to mode.
    private void setMode(Mode mode) {
	this.mode = mode;
	allowEditing(true);
	orthoPane.update();
    }

    // Allow selection of tabs. Only when in annotation mode. 
    // Furthermore, either a function must be in focus, or hieroglyphs
    // must be selected.
    private void setFunctionSelection() {
	boolean functionActive = 
		mode == Mode.ANNOTATE && 
		    (manipulator.currentFun() != null ||
		     !orthoPane.focussedSigns().isEmpty());
	enableComponents(functionTabs, functionActive);
	suggestButton.setEnabled(functionActive);
	suggestions.setEnabled(functionActive);
	retrieveButton.setEnabled(functionActive);
	retrieveDisplay.setEnabled(functionActive);
	computeButton.setEnabled(functionActive);
	funLeftItem.setEnabled(functionActive);
	funRightItem.setEnabled(functionActive);
    }

    ////////////////////////////////////////////////////////////////
    // Functions.

    // Get name of current function.
    private String getFun() {
	return functionTabs.getTitleAt(functionTabs.getSelectedIndex());
    }
    // Go to named function.
    private void setFun(String fun) {
	int index = functionTabs.indexOfTab(fun);
	if (index >= 0)
	    functionTabs.setSelectedIndex(index);
    }

    // Make all values of functions empty.
    private void clearFunArgs() {
	logWord.putString("");
	detDescr.setText("");
	detWord.putString("");
	phonLit.putString("");
	phondetLit.putString("");
	multNum.setText("");
	typDescr.setText("");
    }

    // Get argument name.
    private String getArgName(String function) {
	if (function.equals("log")) 
	    return "word";
	else if (function.equals("det")) 
	    return "descr";
	else if (function.equals("det(word)")) 
	    return "word";
	else if (function.equals("phon")) 
	    return "lit";
	else if (function.equals("phondet")) 
	    return "lit";
	else if (function.equals("mult")) 
	    return "num";
	else if (function.equals("typ")) 
	    return "descr";
	else
	    return "";
    }

    // Is argument that is transliteration?
    private boolean isAlArg(String arg) {
	return arg.equals("word") || arg.equals("lit");
    }

    /**
     * Given a function name, get main value from GUI field.
     */
    private String getArgValue(String function) {
	if (function.equals("log")) 
	    return logWord.getString();
	else if (function.equals("det")) 
	    return detDescr.getText();
	else if (function.equals("det(word)")) 
	    return detWord.getString();
	else if (function.equals("phon")) 
	    return phonLit.getString();
	else if (function.equals("phondet")) 
	    return phondetLit.getString();
	else if (function.equals("mult")) 
	    return multNum.getText();
	else if (function.equals("typ")) 
	    return typDescr.getText();
	else
	    return "";
    }

    // Set function value with extended name.
    private void setFunValue(String name, String val) {
	if (name.equals("log") || name.equals("det(word)") ||
	       name.equals("phon") || name.equals("phondet")) {	
	    logWord.putString(val);
	    detWord.putString(val);
	    phonLit.putString(val);
	    phondetLit.putString(val);
	} else if (name.equals("det")) {
	    detDescr.setText(val);
	} else if (name.equals("mult")) {
	    multNum.setText(val);
	} else if (name.equals("typ")) {
	    typDescr.setText(val);
	} else if (name.equals("spurious")) {
	    ; // ignore
	}
    }

    // Set function
    private void setOrtho() {
	clearFunArgs();
	OrthoElem elem = manipulator.currentFun();
	if (elem != null) {
	    String name = OrthoElem.extendedName(elem);
	    setFunValue(name, elem.argValue());
	    setFun(name);
	} else 
	    setFun(NO_FUN);
	setFunctionSelection();
    }

    // Go to previous/next function.
    private void funLeft() {
	int index = functionTabs.getSelectedIndex() - 1;
	if (index < 0)
	    index = functionTabs.getTabCount() - 1;
	functionTabs.setSelectedIndex(index);
    }
    private void funRight() {
	int index = functionTabs.getSelectedIndex() + 1;
	if (index >= functionTabs.getTabCount())
	    index = 0;
	functionTabs.setSelectedIndex(index);
    }

    //////////////////////////////////////////////////////////////////
    // Suggestions.

    private ArrayList<AnnotationSuggestion> annoSuggestions = new ArrayList<AnnotationSuggestion>();

    // If a longer sequence of signs offers
    // suggestions, select more signs.
    private void extendBySuggestions() {
	String word = manipulator.trans();
	int pos = manipulator.covered();
	int maxExtend = 2;
	for (int i = 1; i <= maxExtend; i++) {
	    String hiero = orthoPane.focussedResPlusNext(i);
	    if (hiero.equals("")) 
		return;
	    if (suggestor.getSuggestions(hiero, word, pos).size() > 0) {
		for (int j = 0; j < i; j++)
		    orthoPane.includeNextGlyph();
		extendBySuggestions();
	    }
	}
    }

    // Suggest functions for sign(s).
    private void fillSuggestions() {
	annoSuggestions.clear();
	suggestions.removeAllItems();
	String hiero = orthoPane.focussedRes();
	String word = manipulator.trans();
	int pos = manipulator.covered();
	annoSuggestions.addAll(suggestor.getSuggestions(hiero, word, pos));
	// avoid ConcurrentModificationException by making copy
	ArrayList<AnnotationSuggestion> suggCopy = 
	    new ArrayList<AnnotationSuggestion>(annoSuggestions);
	for (AnnotationSuggestion sugg : suggCopy) {
	    if (sugg.fun.equals("det") && sugg.arg.equals("al"))
		suggestions.addItem("det(word) : " + sugg.val);
	    else if (sugg.val.matches(""))
		suggestions.addItem(sugg.fun);
	    else
		suggestions.addItem(sugg.fun + " : " + sugg.val);
	}
	retrieveList = manipulator.retrieve();
	retrieveDisplay.setText("" + retrieveList.size());
	suggestions.validate();
	retrieveDisplay.validate();
	suggestions.requestFocus();
	suggestions.showPopup();
    }

    // Remove suggestions.
    private void clearSuggestions() {
	annoSuggestions.clear();
	suggestions.removeAllItems();
	suggestions.validate();
    }

    // When user selects suggestion, put this in GUI.
    // If the tab doesn't change, we cannot rely on listener
    // of tab to do update.
    private void chooseSuggestion(int i) {
	if (0 <= i && i < annoSuggestions.size()) {
	    AnnotationSuggestion sugg = annoSuggestions.get(i);
	    String name = OrthoElem.extendedName(sugg.fun, sugg.arg);
	    String oldName = getFun();
	    String val = !sugg.lemma.equals("") ? sugg.lemma : sugg.val;
	    setFunValue(name, val);
	    setFun(name);
	    if (oldName.equals(name))
		chooseFunction();
	}
    }

    // Is element according to sign list?
    private boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
	return CheckerOrtho.isConsistent(ortho, hiNames, al, suggestor);
    }

    // Add to focus one more glyph just to the right of the rightmost glyph
    // under focus.
    private void addGlyphToFocus() {
	orthoPane.includeNextGlyph();
    }

    //////////////////////////////////////////////////////////////////
    // Retrieval and computation of orthographic annotations.

    // Retrieve existing annotations matching the current word.
    private void retrieveOrtho() {
	if (retrieveList.size() > 0) {
	    Vector<OrthoElem> annotation = retrieveList.remove();
	    retrieveDisplay.setText("" + retrieveList.size());
	    manipulator.removeAllFuns();
	    for (OrthoElem elem : annotation)  
		manipulator.addFun(elem);
	    orthoPane.update();
	    clearSuggestions();
	}
    }

    private void computeOrtho() {
	String word = manipulator.trans();
	String hi = manipulator.hiero();
	ResFragment hiParsed = ResFragment.parse(hi, parsingContext);
	hiParsed = (new NormalizerMnemonics()).normalize(hiParsed);
	String[] hiero = ArrayAux.toStringArray(hiParsed.glyphNames());
	java.util.List<Function> bestFunctions = analyser.analyse(hiero, new TransLow(word));
	if (bestFunctions != null) {
	    manipulator.removeAllFuns();
	    Vector<OrthoElem> bestOrtho = ComplexConfig.toOrthoElems(bestFunctions);
	    for (OrthoElem elem : bestOrtho)
		manipulator.addFun(elem);
	    orthoPane.update();
	}
    }

    //////////////////////////////////////////////////////////////////
    // Edit operations.

    // Delete word, but warn if non-empty.
    private void deleteWord() {
	if (!(manipulator.hiero().matches("\\s*") &&
		    manipulator.trans().matches("\\s*") &&
		    manipulator.orthos().isEmpty())
		&& !userConfirmsLoss("Do you want to proceed and remove word?"))
	    return;
	manipulator.removeSegment();
    }

    /////////////////////////////////////////////
    // Import of data.

    /**
     * Get file name and import hieroglyphic from it.
     */
    private void importHiero() {
	if (!manipulator.hiero().matches("\\s*")
		&& !userConfirmsLoss("Do you want to proceed and overwrite current content?"))
	    return;
	orthoPane.allowEditing(false);
	FileChoosingWindow chooser = new FileChoosingWindow(
		"hieroglyphic resource", new String[] { "xml", "txt" }) {
	    public void choose(File file) {
		incorporateHiero(file);
		orthoPane.update();
		orthoPane.allowEditing(true);
		dispose();
	    }

	    public void exit() {
		orthoPane.allowEditing(true);
		dispose();
	    }
	};
	File textFile = new File(resource.getLocation());
	chooser.setCurrentDirectory(textFile.getParentFile());
    }

    /**
     * Get file name and import transliteration from it.
     */
    private void importTrans() {
	if (!manipulator.trans().matches("\\s*")
		&& !userConfirmsLoss("Do you want to proceed and overwrite current content?"))
	    return;
	orthoPane.allowEditing(false);
	FileChoosingWindow chooser = new FileChoosingWindow(
		"transliteration resource", new String[] { "xml", "txt" }) {
	    public void choose(File file) {
		incorporateTrans(file);
		orthoPane.update();
		orthoPane.allowEditing(true);
		dispose();
	    }

	    public void exit() {
		orthoPane.allowEditing(true);
		dispose();
	    }
	};
	File textFile = new File(resource.getLocation());
	chooser.setCurrentDirectory(textFile.getParentFile());
    }

   /**
     * Put hieroglyphic from file in current segment.
     * 
     * @param file
     */
    private void incorporateHiero(File file) {
        try {
            if (file.exists()) {
                EgyptianResource hieroResource = new EgyptianResource(
                        file.getPath());
		manipulator.incorporateHiero(hieroResource);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read:\n" + e.getMessage(), "Reading error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Put transliteration from file in current segment.
     * 
     * @param file
     */
    private void incorporateTrans(File file) {
        try {
            if (file.exists()) {
                EgyptianResource transResource = new EgyptianResource(
                        file.getPath());
		manipulator.incorporateTrans(transResource);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read:\n" + e.getMessage(), "Reading error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //////////////////////////////////////////////
    // Aux GUI element.

    private class BorderText extends JTextField {
	public BorderText(int columns, String borderText) {
	    super(columns);
	    TitledBorder title = BorderFactory.createTitledBorder(
		                new LineBorder(Color.GRAY, 2), borderText);
	    setBorder(BorderFactory.createCompoundBorder(title,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	}
    }

    /**
     * Enable/disable a container and all components in it.
     */
    private static void enableComponents(Container container, boolean enable) {
	container.setEnabled(enable);
	for (Component component : container.getComponents()) {
	    component.setEnabled(enable);
	    if (component instanceof Container) {
		enableComponents((Container) component, enable);
	    }
	}
    }

}
