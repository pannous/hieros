/***************************************************************************/
/*                                                                         */
/*  StyledTextPane.java                                                    */
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

// A text pane with different styles, allowing editing.
// Two paragraphs are separated by 2 newlines.

package nederhof.util;

import java.awt.*; 
import java.awt.event.*;  
import java.net.*; 
import java.util.*;  
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public class StyledTextPane extends JTextPane 
		implements UndoableEditListener, 
		ChangeListener, ActionListener {

    // Bullet in 'ul' environments.
    public static String itemStart = "\u2022";

    // The styled document.
    private StyledDocument doc;

    // Custom fonts. Mapping from font family
    // and font style to font.
    private TreeMap customFonts = new TreeMap();

    // Custom components in text, name maps to generators.
    private TreeMap customComponents = new TreeMap();

    // The undo manager of text.
    private UndoManager undoManager = new UndoManager();

    // Undo manager to be respected.
    // This is false upon programmatic edits.
    private boolean respectUndoManager = true;

    // Window showing mapping to special characters.
    private HTMLWindow mappingWindow = null;

    // Normally formation of special characters is guided from
    // outside, e.g. through popup menu. If not, then following
    // is true, which allows special characters through direct
    // use of alt keys.
    protected boolean directSpecials = false;

    // Make pane. With given font family and size for regular text.
    public StyledTextPane(String fontFamily, int fontSize) {
	doc = new CustomStyledDocument();
	setStyledDocument(doc);
	addStyles(fontFamily, fontSize);
	doc.addUndoableEditListener(this);
	// Needed for Windows:
	doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n" );

	if (directSpecials) {
	    // Detect when special characters need to be merged.
	    registerKeyboardAction(this, "merge",
		    GuiAux.shortcut(KeyEvent.VK_Z),
		    JComponent.WHEN_FOCUSED);
	    // Detect calling help page.
	    registerKeyboardAction(this, "show",
		    GuiAux.shortcut(KeyEvent.VK_X),
		    JComponent.WHEN_FOCUSED);
	}
    }
    // Default font family and size.
    public StyledTextPane() {
	this("SansSerif", 14);
    }

    // Get decent rendering.
    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
	super.paintComponent(g2);
    }

    // Document allowing custom fonts.
    private class CustomStyledDocument extends DefaultStyledDocument {
        public CustomStyledDocument() {
	    super(new CustomStyleContext());
	}
    }
    // Style context allowing custom fonts.
    private class CustomStyleContext extends StyleContext {
    	public Font getFont(AttributeSet attr) {
	    Font f = getCustomFont(attr);
	    if (f == null)
		f = super.getFont(attr);
	    return f;
	}
    }

    // Get custom font from attribute set, if it is custom,
    // otherwise null.
    private Font getCustomFont(AttributeSet attr) {
	String family = StyleConstants.getFontFamily(attr);
	boolean bold = StyleConstants.isBold(attr);
	boolean italic = StyleConstants.isItalic(attr);
	String key = familyStyle(family, bold, italic);
	return (Font) customFonts.get(key);
    }

    // Add custom font.
    public void addFont(String family, int style, Font font) {
	boolean bold = (style == Font.BOLD || style == Font.BOLD+Font.ITALIC);
	boolean italic = (style == Font.ITALIC || style == Font.BOLD+Font.ITALIC);
	String key = familyStyle(family, bold, italic);
	customFonts.put(key, font);
    }

    // Add component type.
    public void addComponent(String name, EditorComponentGenerator generator) {
	customComponents.put(name, generator);
	doc.addStyle(name, doc.getStyle("plain"));
    }

    // Merge family and style into one string.
    private String familyStyle(String family, boolean bold, boolean italic) {
	return (family == null ? "SansSerif " : "" + family + " ") +
	    (!bold && italic ? "italic" :
	     bold && !italic ? "bold" :
	     bold && italic ? "bolditalic" : "plain");
    }

    // Define regular style, italics, hyperlink.
    private void addStyles(String fontFamily, int fontSize) {
	Style def = StyleContext.getDefaultStyleContext().
	    getStyle(StyleContext.DEFAULT_STYLE);
	StyleConstants.setFontFamily(def, "SansSerif");
	StyleConstants.setFontSize(def, fontSize);

	Style regular = doc.addStyle("plain", def);

	Style italic = doc.addStyle("italic", regular);
	StyleConstants.setItalic(italic, true);
    }

    // Add custom style.
    public void addStyle(String styleName, String fontFamily, int fontStyle) {
	Style newStyle = doc.addStyle(styleName, doc.getStyle("plain"));
	StyleConstants.setFontFamily(newStyle, fontFamily);
	switch (fontStyle) {
	    case Font.BOLD:
		StyleConstants.setBold(newStyle, true);
		break;
	    case Font.ITALIC:
		StyleConstants.setItalic(newStyle, true);
		break;
	    case Font.BOLD+Font.ITALIC:
		StyleConstants.setBold(newStyle, true);
		StyleConstants.setItalic(newStyle, true);
		break;
	    default:
		break;
	}
    }

    ///////////////////////////////////////////////
    // Initial content.

    // For vector of paragraphs, each as vector of substring/style
    // pairs in array of length 2. Instead of substring there
    // can be component, with style "component".
    // Delete existing text.
    // If no text, make regular style.
    public void setParagraphs(Vector paragraphs) {
	undoManager.setLimit(0); // disable undo/redo.
	setText("");
	for (int i = 0; i < paragraphs.size(); i++) {
	    Vector par = (Vector) paragraphs.get(i);
	    for (int j = 0; j < par.size(); j++) {
		Object[] parElem = (Object[]) par.get(j);
		String style = (String) parElem[0];
		Object info = parElem[1];
		EditorComponentGenerator gen = 
		    (EditorComponentGenerator) customComponents.get(style);
		if (gen != null) {
		    Component comp = gen.makeComponent(info, this);
		    int pos = getText().length();
		    setCaretPosition(pos);
		    insertComponent(comp);
		    doc.setCharacterAttributes(pos, 1, 
			    doc.getStyle(style), false);
		} else {
		    String string = (String) info;
		    if (j == 0) // remove leading whitespace
			string = string.replaceFirst("^\\s*","");
		    if (j == par.size()-1) // remove trailing whitespace
			string = string.replaceFirst("\\s*$","");
		    insertInStyle(string, style);
		}
	    }
	    if (i < paragraphs.size() - 1) // between paragraphs
		insertInStyle("\n\n", "plain");
	}
	if (getText().length() == 0) 
	    doc.setLogicalStyle(0, doc.getStyle("plain"));
	undoManager.discardAllEdits();
	undoManager.setLimit(100); // enable undo/redo.
	makeStyle(defaultStyle);
	if (textDisabled)
	    componentOnlyNormalize();
    }

    ///////////////////////////////////////////////
    // Editing content.

    // Revert to this style after component creation,
    // and initially.
    private String defaultStyle = "plain";

    // We give the illusion of disabling text by
    // having foreground color the same as background color.
    // Further at least two spaces between components.
    private boolean textDisabled = false;

    // Set default style.
    public void setDefaultStyle(String style) {
	defaultStyle = style;
    }

    // Disallow text input.
    public void enableTextInput(boolean b) {
	if (!b)
	    setForeground(getBackground());
	else
	    setForeground(Color.BLACK);
	textDisabled = !b;
    }

    // Make selected text plain, italic.
    public void makePlain() {
	makeStyle("plain");
    }
    public void makeItalic() {
	makeStyle("italic");
    }
    public void makeStyle(String style) {
	setCharacterAttributes(doc.getStyle(style), true);
    }

    // Insert at position.
    private void insertInStyle(int pos, String text, String style) {
	try {
	    doc.insertString(pos, text, doc.getStyle(style));
	} catch (BadLocationException ex) {
	    System.err.println("StyledTextPane.initializeText cannot insert");
	}
    }
    // Insert at end.
    private void insertInStyle(String text, String style) {
	insertInStyle(doc.getLength(), text, style);
    }

    // Make present paragraph to item.
    public void makeItem() {
	int pos = getCaretPosition();
	Element el = doc.getParagraphElement(pos);
	if (el.getStartOffset() == el.getEndOffset())
	    return;
	pos = el.getStartOffset();
	insertInStyle(pos, itemStart + " ", "plain");
    }

    // Insert fresh component at present position.
    public void insertFreshComponent(String style) {
	EditorComponentGenerator gen = 
	    (EditorComponentGenerator) customComponents.get(style);
	if (gen != null) {
	    Component comp = gen.makeComponent(this);
	    int pos = getCaretPosition();
	    insertComponent(comp);
	    doc.setCharacterAttributes(pos, 1, doc.getStyle(style), false);
	    makeStyle(defaultStyle);
	    if (textDisabled)
		componentOnlyNormalize();
	}
    }

    ////////////////////////////////////////////
    // Special characters.

    // Process action, which is merging into special character.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("merge"))
	    combineCharacters();
	else if (e.getActionCommand().equals("show")) 
	    showSpecialMenu();
    }

    public void combineCharacters() {
        int pos = getCaretPosition();
        if (0 <= pos-2) {
            setCaretPosition(pos-2);
            moveCaretPosition(pos);
            String selected = getSelectedText();
            for (int i = 0; i < CharAux.specialMapping.length; i += 2)
                if (selected.equals(CharAux.specialMapping[i])) 
                    replaceSelection(CharAux.specialMapping[i+1]);
        }
    }

    // Show window with table of mapping.
    public void showSpecialMenu() {
	if (mappingWindow == null) {
	    URL url = FileAux.fromBase("data/help/util/specialchars.html");
	    mappingWindow = new HTMLWindow("Special characters", url);
	}
	mappingWindow.setVisible(true);
    }

    // If removed, remove extra window.
    public void removeNotify() {
        super.removeNotify();
        if (mappingWindow != null) {
            mappingWindow.dispose();
            mappingWindow = null;
        }
    }

    ////////////////////////////////////////
    // Undo/redo

    // Process undoable change to document.
    public void undoableEditHappened(UndoableEditEvent e) {
	if (respectUndoManager)
	    undoManager.addEdit(e.getEdit());
    }

    // Undo last change.
    public void undo() {
	try {
	    undoManager.undo();
	} catch (CannotUndoException e) {
	    // ignore
	}
    }

    // Redo.
    public void redo() {
	try {
	    undoManager.redo();
	} catch (CannotRedoException e) {
	    // ignore
	}
    }

    // Can we undo/redo?
    public boolean canUndo() {
	return undoManager.canUndo();
    }
    public boolean canRedo() {
	return undoManager.canRedo();
    }

    // The user can override this to be notified of
    // changed within components.
    public void stateChanged(ChangeEvent e) {
    }

    ////////////////////////////////////////
    // Extraction of content.

    // Get vector of paragraphs, each with vector
    // of substrings and styles for content.
    public Vector extractParagraphs() {
	normalize();
	Element root = doc.getDefaultRootElement();
	Vector pars = new Vector();
	Vector currentPar = new Vector();
	getParagraphs(root, pars, currentPar, false, false);
	startParagraph(pars, currentPar);
	return pars;
    }

    // Normalise by replacing newline by space if occurs in isolation.
    // Replace two spaces by one, unless either contains component.
    private void normalize() {
	String text = getText();
	int i = text.length() - 1;
	while (i > 0) {
	    i = text.lastIndexOf('\n', i);
	    if (i > 0) {
		if (text.charAt(i-1) != '\n') {
		    setCaretPosition(i);
		    moveCaretPosition(i+1);
		    replaceSelection(" ");
		} 
		while (i > 0 && text.charAt(i) == '\n')
		    i--;
	    }
	}
	i = text.length() - 1;
	while (i > 0) {
	    i = text.lastIndexOf("  ", i);
	    if (i > 0) {
		if (doc.getCharacterElement(i).getName().equals("component") ||
			doc.getCharacterElement(i+1).getName().equals("component"))
		    i = i-2;
		else {
		    setCaretPosition(i+1);
		    moveCaretPosition(i+2);
		    replaceSelection("");
		    i = i-1;
		}
	    }
	}
    }

    // Go through document to collect paragraphs.
    // Do not take pieces that are empty and that are at beginning or
    // end of paragraph.
    private void getParagraphs(Element node, Vector pars, Vector currentPar,
	    boolean atStart, boolean atEnd) {
	String tagName = node.getName();
	if (tagName.equals("section")) { // if is whole document
	    // do nothing; only go in recursion
	} else if (tagName.equals("paragraph")) {
	    startParagraph(pars, currentPar);
	} else if (tagName.equals("content")) {
	    int start = node.getStartOffset();
	    int end = node.getEndOffset();
	    int len = end-start;
	    try {
		String content = doc.getText(start, len);
		if (isItemStart(content))
		    startParagraph(pars, currentPar);
		String style = name(node);
		if (customComponents.get(style) != null)
		    style = "plain";
		if (style.equals("default"))
		    style = "plain";
		if (!textDisabled &&
			((!atStart && !atEnd) || !content.matches("\\s*"))) {
		    Object[] parElem = new Object[] {style, content};
		    currentPar.add(parElem);
		}
	    } catch (BadLocationException ex) {
		System.err.println("Location error in StyledTextPane");
	    }
	} else if (tagName.equals("component")) {
	    String style = name(node);
	    Component comp = comp(node);
	    EditorComponentGenerator gen = 
		(EditorComponentGenerator) customComponents.get(style);
	    if (gen != null) {
		Object info = gen.extract(comp);
		Object[] parElem = new Object[] {style, info};
		currentPar.add(parElem);
	    } 
	} else
	    System.err.println("Strange tagname in StyledTextPane " + tagName);
	// recur
	for (int i = 0; i < node.getElementCount(); i++) {
	    Element child = node.getElement(i);
	    getParagraphs(child, pars, currentPar, 
		    i == 0, i == node.getElementCount()-1);
	}
    }

    // Start new paragraph, if current one is non-empty.
    private void startParagraph(Vector pars, Vector currentPar) {
	if (!currentPar.isEmpty()) {
	    pars.add(currentPar.clone());
	    currentPar.clear();
	}
    }

    // Is at beginning of item if starts with bullet.
    private boolean isItemStart(String text) {
	return text.length() > 0 && text.substring(0, 1).equals(itemStart);
    }

    // Name attribute of element.
    private String name(Element node) {
	return (String) node.getAttributes().
	    getAttribute(StyleConstants.NameAttribute);
    }
    // Component attribute of element.
    private Component comp(Element node) {
	return (Component) node.getAttributes().
	    getAttribute(StyleConstants.ComponentAttribute);
    }

    ////////////////////////////////////////
    // At disable, disable also embedded components.

    public void setEnabled(boolean allow) {
	super.setEnabled(allow);
	for (int i = 0; i < getText().length(); i++) {
	    Element elem = doc.getCharacterElement(i);
	    if (elem.getName().equals("component")) 
		comp(elem).setEnabled(allow);
	}
    }

    ////////////////////////////////////////
    // When text is not allowed, normalize by inserting exactly
    // two spaces between components.

    // First symbol should not be component. Second symbol should be
    // component, third and fourth should be components, etc.
    // Afterwards, put cursor back, exactly between two components.
    public void componentOnlyNormalize() {
	respectUndoManager = false;
	int pos = getCaretPosition();
	int nComponents = 0;
	for (int i = 0; i < pos; i++) {
	    Element elem = doc.getCharacterElement(i);
	    if (elem.getName().equals("component"))
		nComponents++;
	}
	int i = 0;
	while (i < getText().length()) {
	    Element elem = doc.getCharacterElement(i);
	    if (elem.getName().equals("component")) {
		if (i % 3 != 1) {
		    setCaretPosition(i);
		    moveCaretPosition(i);
		    replaceSelection(" ");
		}
		i++;
	    } else {
		if (i % 3 == 1) {
		    setCaretPosition(i);
		    moveCaretPosition(i+1);
		    replaceSelection("");
		} else
		    i++;
	    }
	}
	if (getText().length() % 3 != 1) {
	    setCaretPosition(getText().length());
	    moveCaretPosition(getText().length());
	    replaceSelection(" ");
	}
	setCaretPosition(nComponents * 3);
	respectUndoManager = true;
    }

    ////////////////////////////////////////
    // Testing.

    // Testing.
    public static void main(String[] args) {
	Vector paragraphs = new Vector();
	Vector paragraph0 = new Vector();
	Vector paragraph1 = new Vector();
	Object[] stringStyle0 = new Object[] {"plain", "This  is "};
	Object[] stringStyle3 = new Object[] {"mycomponent", "labeltext"};
	Object[] stringStyle1 = new Object[] {"italic", " it italics "};
	Object[] stringStyle2 = new Object[] {"plain", "New paragraph "};
	Object[] stringStyle4 = new Object[] {"extra", "Extra material "};
	paragraph0.add(stringStyle0);
	paragraph0.add(stringStyle3);
	paragraph0.add(stringStyle1);
	paragraph1.add(stringStyle2);
	paragraph1.add(stringStyle4);
	paragraphs.add(paragraph0);
	paragraphs.add(paragraph1);

	StyledTextPane pane = new StyledTextPane("SansSerif", 18);
	pane.addStyle("extra", "Extra", Font.BOLD);
	pane.addFont("Extra", Font.BOLD, new Font("Serif", Font.BOLD, 20));
	pane.addComponent("mycomponent", new LabelGenerator());

	// pane.setParagraphs(paragraphs);
	pane.setParagraphs(new Vector());
	JFrame frame = new JFrame();
	frame.setContentPane(pane);
	frame.pack();
	frame.setVisible(true);
	Vector out = pane.extractParagraphs();
	printStructure(paragraphs);
	System.out.println("--------------");
	printStructure(out);
    }

    // Print input/output for testing.
    public static void printStructure(Vector paragraphs) {
	for (int i = 0; i < paragraphs.size(); i++) {
	    Vector par = (Vector) paragraphs.get(i);
	    for (int j = 0; j < par.size(); j++) {
		Object[] stringStyle = (Object[]) par.get(j);
		System.out.println(stringStyle[0] + "\n" + stringStyle[1] + "\n");
	    }
	}
    }

    // Testing EditorComponentGenerator.
    private static class LabelGenerator implements EditorComponentGenerator {
	public Component makeComponent(ChangeListener listener) { 
	    return new ExpBut("");
	}
	public Component makeComponent(Object o, ChangeListener listener) {
	    ExpBut l = new ExpBut((String) o);
	    l.setAlignmentY(0.75f);
	    return l;
	}
	public Object extract(Component comp) {
	    ExpBut label = (ExpBut) comp;
	    return label.getText();
	}
    }

    // Testing expanding component.
    private static class ExpBut extends JButton implements ActionListener {
	public ExpBut(String s) {
	    super(s);
	    setActionCommand("doit");
	    addActionListener(this);
	}
	public void actionPerformed(ActionEvent e) {
	    setText(getText() + "extra");
	    System.out.println(getText());
	}
    }

}
