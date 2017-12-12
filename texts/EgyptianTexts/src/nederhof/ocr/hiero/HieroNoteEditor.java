// Editor of notes in hieroglyphic.
package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;

public abstract class HieroNoteEditor extends JFrame implements ActionListener, EditChainElement {

	// Hieroglyphic.
	private ResFragment res;

	// Notes.
	private TreeMap<Integer,String> notes;

	// Panel containing hieroglyphic.
	private GlyphSelectPanel hieroPanel;

	// Panel containing notes.
	private JPanel notePanels = new JPanel();

	// The listener to closing.
	private CloseListener closeListener = new CloseListener();

	// Panel elements.
	protected Vector<JComponent> panelElements = new Vector<JComponent>();

	// Text elements.
	protected Vector<StyledTextPane> textElements = new Vector<StyledTextPane>();

	// Position of focus.
	private int focus = -1;

	public HieroNoteEditor(ResFragment res, HieroRenderContext context,
			TreeMap<Integer,String> notes) {
		this.res = res;
		this.notes = (TreeMap<Integer,String>) notes.clone();
		setTitle("Note editor");
		setJMenuBar(getMenu());
		setSize(HieroSettings.hieroNoteEditorWidth, HieroSettings.hieroNoteEditorHeight);
		Container content = getContentPane();
		hieroPanel = new GlyphSelectPanel(new ResFragment(), context) {
			protected void notifyFocus(int pos) {
				changeFocus(pos);
			}
		};
		if (!ResValues.isV(res.dir())) {
			content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
			content.add(new JScrollPane(hieroPanel,
						JScrollPane.VERTICAL_SCROLLBAR_NEVER,
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
				public Dimension getMaximumSize() {
					return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
				}
				public Dimension getMinimumSize() {
					return new Dimension(super.getMinimumSize().width, getPreferredSize().height);
				}
			});
		} else {
			content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
			content.add(new JScrollPane(hieroPanel,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
				public Dimension getMaximumSize() {
					return new Dimension(getPreferredSize().width, super.getMaximumSize().height);
				}
				public Dimension getMinimumSize() {
					return new Dimension(getPreferredSize().width, super.getMinimumSize().height);
				}
			});
		}

		notePanels.setLayout(new BoxLayout(notePanels, BoxLayout.Y_AXIS));
		notePanels.setBackground(Color.WHITE);
		content.add(new ScrollConservative(notePanels));
		listNotes(-1);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(closeListener);
		setVisible(true);
	}

	// List notes, with highlight for focus.
	// If there is no focus, then negative.
	private void listNotes(int focus) {
		this.focus = focus;
		if (focus >= 0)
			retrieveNotes();
		notePanels.removeAll();
		textElements.clear();
		if (focus >= 0 && notes.get(focus) == null)
			notes.put(focus, "");
		hieroPanel.setHiero(notedHiero());
		int nMarks = 0;
		for (Map.Entry<Integer,String> pair : notes.entrySet()) {
			int symb = pair.getKey();
			String text = pair.getValue();
			notePanels.add(new NotePanel(text, symb == focus, symb, ++nMarks));
		}
		notePanels.add(Box.createVerticalGlue());
		notePanels.revalidate();
	}

	// Styled text editor within pane.
	private class NotePanel extends JPanel {
		// The pane.
		private StyledTextPane styledPane;
		// The symbol.
		private int symbol;

		// Constructor.
		public NotePanel(String text, boolean highlight, int symbol, int mark) {
			this.symbol = symbol;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.WHITE);
			String titleString = mark >= 0 ? "" + mark : "new";
			Color borderCol = highlight ? Color.BLUE : Color.GRAY;
			TitledBorder title =
				BorderFactory.createTitledBorder(
						new LineBorder(borderCol, 2), titleString);
			setBorder(
					BorderFactory.createCompoundBorder(title,
						BorderFactory.createEmptyBorder(0,5,5,5)));

			styledPane = 
				new StyledTextPane(HieroSettings.inputTextFontName,
						HieroSettings.inputTextFontSize) {
					public void stateChanged(ChangeEvent e) {
						; // ignore
					}
				};

			Vector<Object[]> parts = new Vector<Object[]>();
			parts.add(new Object[] {"plain", text});
			Vector<Vector<Object[]>> pars = new Vector<Vector<Object[]>>();
			pars.add(parts);
			styledPane.setParagraphs(pars);
			// styledPane.getDocument().addDocumentListener(HieroNoteEditor.this);
			add(styledPane);
			panelElements.add(this);
			textElements.add(styledPane);
		}

		// Retrieve content.
		// These are paragraphs. The parts have to be merged.
		public String getText() {
			String text = "";
			Vector pars = styledPane.extractParagraphs();
			Vector parts = new Vector();
			for (int i = 0; i < pars.size(); i++) {
				Vector par = (Vector) pars.get(i);
				for (int j = 0; j < par.size(); j++) {
					Object[] pair = (Object[]) par.get(j);
					text += pair[1];
				}
			}
			return text;
		}

		// Retrieve symbol.
		public int getSymbol() {
			return symbol;
		}
		// Change symbol.
		public void setSymbol(int symbol) {
			this.symbol = symbol;
		}

		// Is caret in text?
		public boolean hasCaret() {
			return styledPane.isFocusOwner();
		}
	}

	// Get panel carrying focus. Return null if none.
	private NotePanel focusCarrier() {
		Component[] children = notePanels.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof NotePanel) {
				NotePanel panel = (NotePanel) children[i];
				if (panel.getSymbol() == focus)
					return panel;
			}
		}
		return null;
	}

	// Move note carrying caret backward.
	private void moveBackward() {
		NotePanel panel = focusCarrier();
		if (panel != null &&
				0 <= panel.getSymbol()-1 && panel.getSymbol()-1 < res.nGlyphs()) {
			panel.setSymbol(panel.getSymbol()-1);
			hieroPanel.goLeft();
		}
	}

	// Move note carrying caret forward.
	private void moveForward() {
		NotePanel panel = focusCarrier();
		if (panel != null &&
				0 <= panel.getSymbol()+1 && panel.getSymbol()+1 < res.nGlyphs()) {
			panel.setSymbol(panel.getSymbol()+1);
			hieroPanel.goRight();
		}
	}

	// Get notes from panels.
	private void retrieveNotes() {
		notes.clear();
		Component[] children = notePanels.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof NotePanel) {
				NotePanel panel = (NotePanel) children[i];
				int sym = panel.getSymbol();
				String text = panel.getText();
				if (!text.matches("\\s*")) 
					notes.put(sym, text);
				else
					notes.remove(sym);
			}
		}
	}

	/////////////////////////////////////////////
	// Menu.

	// Items in menu that may be disabled/enabled.
	private final JMenu fileMenu = new EnabledMenu(
			"<u>F</u>ile", KeyEvent.VK_F);
	private final JMenuItem leftItem = new EnabledMenuItem(this,
			"left", "left", KeyEvent.VK_COMMA);
	private final JMenuItem rightItem = new EnabledMenuItem(this,
			"right", "right", KeyEvent.VK_PERIOD);
	private final JMenuItem backwardItem = new EnabledMenuItem(this,
			"b<u>A</u>ckward", "backward", KeyEvent.VK_A);
	private final JMenuItem forwardItem = new EnabledMenuItem(this,
			"f<u>O</u>rward", "forward", KeyEvent.VK_O);
	private final JMenuItem saveItem = new EnabledMenuItem(this,
			"clo<u>S</u>e", "save", KeyEvent.VK_S);

	// Menu.
	private JMenuBar getMenu() {
		final int STRUT_SIZE = 10;
		JMenuBar box = new JMenuBar();
		box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
		box.setBackground(Color.LIGHT_GRAY);
		box.add(Box.createHorizontalStrut(STRUT_SIZE));
		box.add(fileMenu);
		fileMenu.add(leftItem);
		fileMenu.add(rightItem);
		fileMenu.add(backwardItem);
		fileMenu.add(forwardItem);
		fileMenu.add(saveItem);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("left")) {
			hieroPanel.goLeft();
		} else if (e.getActionCommand().equals("right")) {
			hieroPanel.goRight();
		} else if (e.getActionCommand().equals("backward")) {
			moveBackward();
		} else if (e.getActionCommand().equals("forward")) {
			moveForward();
		} else if (e.getActionCommand().equals("save")) {
			retrieveNotes();
			receive(notes);
			dispose();
		} 
	}

	// Listen if window to be closed or iconified.
	// Let legend disappear with iconification of main window.
	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			if (userConfirmsLoss("Do you want to proceed and discard any edits?")) {
				cancel();
				dispose();
			}
		}
	}

	// Ask user whether loss of data is intended.
	private boolean userConfirmsLoss(String message) {
		Object[] options = {"proceed", "cancel"};
		int answer = JOptionPane.showOptionDialog(this, message,
				"warning: impending loss of data",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[1]);
		return answer == 0;
	}

	/////////////////////////////////////////////
	// Values.

	// User has clicked on position.
	private void changeFocus(int pos) {
		listNotes(pos);
	}

	// Connection with caller.
	protected abstract void receive(TreeMap<Integer,String> changedNotes);
	protected abstract void cancel();

	/////////////////////////////////////////////
	// Appearance.

	// Make border title gray if not editing.
	public void allowEditing(boolean allow) {
		fileMenu.setEnabled(allow);
		leftItem.setEnabled(allow);
		rightItem.setEnabled(allow);
		backwardItem.setEnabled(allow);
		forwardItem.setEnabled(allow);
		saveItem.setEnabled(allow);
		hieroPanel.setEnabled(allow);
		for (int i = 0; i < panelElements.size(); i++) {
			JComponent comp = (JComponent) panelElements.get(i);
			comp.setBackground(backColor(allow));
		}
		for (int i = 0; i < textElements.size(); i++) {
			JComponent comp = (JComponent) textElements.get(i);
			comp.setEnabled(allow);
		}
	}

	// Backgroup color.
	private Color backColor(boolean allow) {
		return Color.WHITE;
	}

	//////////////////////////////////////////
	// Auxiliary.

	// Extend hieroglyphic with markers where there are footnotes.
	private ResFragment notedHiero() {
		ResFragment noted = (ResFragment) res.clone();
		TreeSet glyphs = new TreeSet();
		int nMarks = 0;
		for (Map.Entry<Integer,String> pair : notes.entrySet()) {
			int symb = pair.getKey();
			String text = pair.getValue();
			noted.addNote(symb, "" + (++nMarks), Color16.BLUE);
		}
		return noted;
	}

}
