package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Editor/viewer of Egyptian lexicon.
public class LexiconEditor extends JPanel implements ActionListener {

    // For hieroglyphic.
    private HieroRenderContext hieroContext =
	new HieroRenderContext(Settings.textHieroFontSize, true);
    private ParsingContext parsingContext =
	new ParsingContext(hieroContext, true);

    // For search.
    private JPanel searchPanel = new JPanel();
    private JTextField hiField = new JTextField("");
    private JTextField alField = new JTextField("");
    private JTextField trField = new JTextField("");
    private JTextField foField = new JTextField("");
    private JButton clearButton = new JButton("clear");
    private JButton searchButton = new JButton("search");
    // For lexicon.
    private JTabbedPane lexTabs = new JTabbedPane();
    // Lexica in pane.
    private Vector<LexTab> lexs = new Vector<LexTab>();

    // Constructor.
    public LexiconEditor(Vector<File> lexicons) throws IOException {
	setLayout(new BorderLayout());
	add(searchPanel, BorderLayout.NORTH);
	add(lexTabs, BorderLayout.CENTER);
	makeSearchPanel();
	openLexicons(lexicons);
    }
    // Constructor using default lexicons.
    public LexiconEditor() throws IOException {
	this(defaultLexicons());
    }
    // Default lexicons.
    private static Vector<File> defaultLexicons() {
	Vector<File> lexicons = new Vector<File>();
	lexicons.add(new File("data/dict/basic.xml"));
	lexicons.add(new File("data/dict/gods.xml"));
	lexicons.add(new File("data/dict/kings.xml"));
	lexicons.add(new File("data/dict/names.xml"));
	lexicons.add(new File("data/dict/places.xml"));
	return lexicons;
    }

    ///////////////////////////////////////////////
    // Search.

    private void makeSearchPanel() {
	hiField.setColumns(35);
	alField.setColumns(20);
	trField.setColumns(20);
	foField.setColumns(10);
	hiField.setActionCommand("search");
	alField.setActionCommand("search");
	trField.setActionCommand("search");
	foField.setActionCommand("search");
	hiField.addActionListener(this);
	alField.addActionListener(this);
	trField.addActionListener(this);
	foField.addActionListener(this);
	searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
	searchPanel.add(Box.createHorizontalStrut(10));
	searchPanel.add(new JLabel("hi:"));
	searchPanel.add(hiField);
	searchPanel.add(Box.createHorizontalStrut(10));
	searchPanel.add(new JLabel("al:"));
	searchPanel.add(alField);
	searchPanel.add(Box.createHorizontalStrut(10));
	searchPanel.add(new JLabel("tr:"));
	searchPanel.add(trField);
	searchPanel.add(Box.createHorizontalStrut(10));
	searchPanel.add(new JLabel("fo:"));
	searchPanel.add(foField);
	searchPanel.add(Box.createHorizontalStrut(10));
	clearButton.setForeground(Color.RED);
	searchPanel.add(clearButton);
	searchPanel.add(searchButton);
	searchPanel.add(Box.createHorizontalStrut(10));
	clearButton.addActionListener(this);
	searchButton.addActionListener(this);
    }

    //////////////////////////////////////////////////
    // Lexicon.

    // Make one tab for each lexicon.
    private void openLexicons(Vector<File> lexicons) throws IOException {
	for (File file : lexicons) {
	    LexTab lexPanel = new LexTab(file);
	    lexTabs.addTab(lexPanel.getDescr(), 
		    new SimpleScroller(lexPanel, true, true));
	    lexs.add(lexPanel);
	}
    }

    // Tab page with one lexicon.
    private class LexTab extends JPanel {
	public File file;
	public EgyptianLexicon lex;

	public LexTab(File file) throws IOException {
	    this.file = file;
	    lex = new EgyptianLexicon(file);
	    setBackground(Color.WHITE);
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    showNoEntries();
	}

	public String getDescr() {
	    return lex.getDescr();
	}
	public void showEntries(String hi, String al, String tr, String fo) {
	    Vector<String> names = splitHi(hi);
	    if (al.matches("\\s*"))
		al = ".*";
	    else
		al = "(" + al + ").*";
	    if (tr.matches("\\s*"))
		tr = ".*";
	    else
		tr = "(" + tr + ").*";
	    if (fo.matches("\\s*"))
		fo = ".*";
	    else
		fo = "(" + fo + ").*";
	    removeAll();
	    try {
		for (DictLemma lemma : lex.getLemmas()) {
		    if (matchHi(lemma.keyhi, names) &&
			    lemma.keyal.matches(al) && 
			    lemma.keytr.matches(tr) &&
			    lemma.keyfo.matches(fo)) {
			add(new ConnectedPanelLemma(lemma, lex));
			add(new FixedSeparator());
		    }
		}
	    } catch (PatternSyntaxException e) {
		JOptionPane.showMessageDialog(null,
			                    "Wrong search pattern:\n" + e.getMessage(), 
					    "Regular expression error",
					                        JOptionPane.ERROR_MESSAGE);
	    }
	    add(Box.createVerticalGlue());
	    revalidate();
	    repaint();
	}
	public void showNoEntries() {
	    removeAll();
	    add(Box.createVerticalGlue());
	    revalidate();
	    repaint();
	}
    }

    public void searchTransInLexicon(String al) {
	alField.setText(al);
	searchInLexicon();
    }

    private void searchInLexicon() {
	int tab = lexTabs.getSelectedIndex();
	if (tab >= 0 && tab < lexs.size())
	    lexs.get(tab).showEntries(hiField.getText(), alField.getText(), 
		    trField.getText(), foField.getText());
	for (int i = 0; i < lexs.size(); i++)
	    if (i != tab)
		lexs.get(i).showNoEntries();
    }

    // Hide all entries from all lexica.
    private void showNoEntries() {
	for (int i = 0; i < lexs.size(); i++)
	    lexs.get(i).showNoEntries();
    }

    private class ConnectedPanelLemma extends PanelLemma {
	private EgyptianLexicon lex;
	private DictLemma dictLemma;
	public ConnectedPanelLemma(DictLemma dictLemma, EgyptianLexicon lex) {
	    super(dictLemma);
	    this.lex = lex;
	    this.dictLemma = dictLemma;
	}
	public void askFocus(PanelLemma pLem) {
	    LexiconEditor.this.askFocus(pLem);
	}
	public void reportSelection(LexRecord rec) {
	    rec.cite = lex.getDescr();
	    LexiconEditor.this.reportSelection(rec);
	}
	public void edit() {
	    LexiconEditor.this.editLemma(lex, dictLemma);
	}
    }

    // Separator that doesn't stretch.
    private class FixedSeparator extends JSeparator {
	public FixedSeparator() {
	    super(SwingConstants.HORIZONTAL);
	}
	public Dimension getMaximumSize() {
	    Dimension pref = super.getPreferredSize();
	    Dimension max = super.getMaximumSize();
	    return new Dimension(max.width, pref.height);
	}
    }

    //////////////////////////////////////////////////
    // Events.

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("search")) {
	    searchInLexicon();
	} else if (e.getActionCommand().equals("clear")) {
	    hiField.setText("");
	    alField.setText("");
	    trField.setText("");
	    foField.setText("");
	    showNoEntries();
	} 
    }

    // Current focus, or null if none.
    private PanelLemma focusLemma = null;

    // The lemma requests focus.
    private void askFocus(PanelLemma pLem) {
	if (focusLemma != null) {
	    if (pLem == null) {
		focusLemma.clearFocus();
		focusLemma = null;
	    } else if (pLem != focusLemma) {
		focusLemma.clearFocus();
		pLem.setFocus(true);
		focusLemma = pLem;
	    }
	} else if (pLem != null) {
	    pLem.setFocus(true);
	    focusLemma = pLem;
	}
    }

    // Report lexical record to outside. Caller to override.
    public void reportSelection(LexRecord rec) {
    }

    //////////////////////////////////////////////////
    // Editing of lexicon.

    // Only one lemma editor is created.
    private LemmaEditor lemmaEditor = null;

    // Edit lemma.
    private void editLemma(EgyptianLexicon lex, DictLemma dictLemma) {
	if (lemmaEditor == null)
	    lemmaEditor = new ConnectedLemmaEditor();
	boolean done = lemmaEditor.set(lex, dictLemma);
	if (!done)
	    JOptionPane.showMessageDialog(null,
		    "Lemma already being edited", "Procedural error",
		    JOptionPane.ERROR_MESSAGE);
    }

    // Edit new lemma.
    private void editLemma(EgyptianLexicon lex) {
	if (lemmaEditor == null)
	    lemmaEditor = new ConnectedLemmaEditor();
	boolean done = lemmaEditor.set(lex);
	if (!done)
	    JOptionPane.showMessageDialog(null,
		    "Lemma already being edited", "Procedural error",
		    JOptionPane.ERROR_MESSAGE);
    }

    // Create new lemma and edit, for current lexicon.
    public void createLemma() {
	int tab = lexTabs.getSelectedIndex();
	if (tab >= 0 && tab < lexs.size())
	editLemma(lexs.get(tab).lex);
    }

    private class ConnectedLemmaEditor extends LemmaEditor {
	// Search again.
	public void refresh() {
	    searchInLexicon();
	}
    }

    // Kill all auxiliary windows.
    public void dispose() {
	if (lemmaEditor != null)
	    lemmaEditor.dispose();
    }

    //////////////////////////////////////////////////
    // Auxiliary.

    // Split up string into non-empty glyph names.
    private Vector<String> splitHi(String hi) {
	Vector<String> names = new Vector<String>();
	String[] ss = hi.split(" ");
	for (int i = 0; i < ss.length; i++) 
	    if (!ss[i].equals(""))
		names.add(ss[i]);
	return names;
    }

    // Do names in query match hieroglyphic of key?
    // First check whether names are present as substrings.
    private boolean matchHi(String key, Vector<String> namesQuery) {
	if (namesQuery.isEmpty())
	    return true;
	for (int i = 0; i < namesQuery.size(); i++)
	    if (key.indexOf(namesQuery.get(i)) < 0)
		return false;
	ResFragment parsedKey = ResFragment.parse(key, parsingContext);
	Vector<String> namesKey = parsedKey.glyphNames();
	return isSubsequence(namesQuery, namesKey);
    }

    // Is first subsequence of second?
    private boolean isSubsequence(Vector<String> list1, Vector<String> list2) {
	int i = 0;
	int j = 0;
	while (i < list1.size()) {
	    String s1 = list1.get(i);
	    while (j < list2.size() && !list2.get(j).equals(s1))
		j++;
	    if (j >= list2.size())
		return false;
	    i++;
	}
	return true;
    }

    //////////////////////////////////////////////////
    // Testing.

    // For testing.
    public static void main(String[] args) {
	try {
	    LexiconEditor editor = new LexiconEditor();
	    JFrame frame = new JFrame();
	    frame.getContentPane().add(editor);
	    frame.setSize(500, 700);
	    frame.setVisible(true);
	 } catch (Exception e) {
	     System.err.println(e.getMessage());
	 }
    }

}

