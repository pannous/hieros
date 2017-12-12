package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.alignment.generic.*;
import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.ocr.images.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;

public abstract class HieroOcrLineFocus extends OcrLineFocus {

	public HieroOcrLineFocus(BinaryImage im, Line line, double scale) {
		super(im, line, scale);
	}

	///////////////////////////////////////////////////////////////
	// The choices after OCR.
	
	protected ChoiceBox createChoiceBox(Blob blob) {
		return new ConnectedComboBox(blob);
	}

	private class ConnectedComboBox extends HieroComboBox {
		public ConnectedComboBox(Blob blob) {
			super(blob);
		}
		public void setFocus(Blob blob) {
			HieroOcrLineFocus.this.selectBlob(blob);
			subPanel.repaint();
		}
		public void openMenu() {
			findGlyph();
		}
		public void openExtraMenu() {
			findExtra();
		}
		public void openNoteEditor() {
			editHieroNote();
		}
	}

    // Edit note attached to glyph.
    protected void editHieroNote() {
        if (selected == null)
            return;
        allowEdits(false);
        String oldNote = selected.getNote();
        String newNote = (String) JOptionPane.showInputDialog(
                this,
                "Edit note:",
                "Hieroglyph note",
                JOptionPane.QUESTION_MESSAGE,
                null, null, oldNote);
        if (newNote != null) {
            selected.setNote(newNote);
            revalidate();
            repaint();
        }
        allowEdits(true);
    }

	/////////////////////////////////////////////////////
	// Formatted text after Ocr.
	
	protected void addFormat() {
        formatPanel.removeAll();
        formatPanel.add(new AddFormatButton(0));
        for (int i = 0; i < line.formatted.size(); i++) {
            LineFormat format = line.formatted.get(i);
            if (format instanceof ResFormat) {
                ResFormat resFormat = (ResFormat) format;
                ResFragment frag = ResFragment.parse(resFormat.getVal(), getParsingContext());
                FormatFragment form = new FormatFragment(frag, getContext());
                formatPanel.add(new HieroPreview(resFormat, form) {
					public void selected(ResFormat preview) {
						editHieroPreview(preview);
					}
					public void rightSelected(ResFormat preview) {
						editHieroNotes(preview);
					}
                });
            } else if (format instanceof NumFormat) {
                formatPanel.add(new NumPreview(format) {
					public void selected(LineFormat preview) {
						editNumPreview(preview);
					}
                });
            }
            formatPanel.add(new AddFormatButton(i+1));
        }
        formatPanel.add(Box.createHorizontalGlue());
	}

    // Edit hiero preview.
    private void editHieroPreview(final ResFormat preview) {
        allowEdits(false);
        String res = preview.getVal();
        new FragmentEditor(res, true, false,
                HieroSettings.editPreviewHieroFontSize,
                HieroSettings.editTreeHieroFontSize) {
            protected void receive(String out) {
                TreeMap<Integer,String> newNotes =
                    adjustNotes(preview.getVal(), out, preview.getNotes());
                preview.setVal(out);
                preview.setNotes(newNotes);
                addFormat();
                HieroOcrLineFocus.this.revalidate();
                HieroOcrLineFocus.this.repaint();
                allowEdits(true);
            }
            protected void cancel() {
                allowEdits(true);
            }
            protected void error(int pos) {
                allowEdits(true);
            }
        };
    }
    // Edit notes attached to hiero preview.
    private void editHieroNotes(final ResFormat preview) {
        allowEdits(false);
        String res = preview.getVal();
        TreeMap<Integer,String> notes = preview.getNotes();
        ResFragment frag = ResFragment.parse(res, getParsingContext());
        new HieroNoteEditor(frag, getContext(), notes) {
            protected void receive(TreeMap<Integer,String> notes) {
                preview.setNotes(notes);
                allowEdits(true);
            }
            protected void cancel() {
                allowEdits(true);
            }
        };
    }

    // Edit line number.
    private void editNumPreview(final LineFormat preview) {
        allowEdits(false);
        String num = preview.getVal();
        String out = (String) JOptionPane.showInputDialog(
                this,
                "Enter new line number:",
                "Line number",
                JOptionPane.QUESTION_MESSAGE,
                null, null, num);
        if (out != null) {
            preview.setVal(out);
            addFormat();
            revalidate();
            repaint();
        }
        allowEdits(true);
    }

    // If editing from old to new hieroglyphic, where to move the
    // notes?
    private TreeMap<Integer,String> adjustNotes(String oldHi, String newHi,
            TreeMap<Integer,String> oldNotes) {
        TreeMap<Integer,String> newNotes = new TreeMap<Integer,String>();
        ResFragment res1 = ResFragment.parse(oldHi, getParsingContext());
        ResFragment res2 = ResFragment.parse(newHi, getParsingContext());
        Vector<String> names1 = res1.glyphNames();
        Vector<String> names2 = res2.glyphNames();
        MinimumEditUpdater updater = new MinimumEditUpdater(names1, names2);
        for (Map.Entry<Integer,String> pair : oldNotes.entrySet())
            newNotes.put(updater.map(pair.getKey()), pair.getValue());
        return newNotes;
    }

    // Button for adding new format element.
    private class AddFormatButton extends JPanel implements ActionListener {
        // Place to insert.
        private int i;
        public AddFormatButton(int i) {
            this.i = i;
            setLayout(new BorderLayout());
            JButton hiBut = new JButton("+res");
            JButton numBut = new JButton("+num");
            hiBut.setMargin(new Insets(0, 0, 0, 0));
            numBut.setMargin(new Insets(0, 0, 0, 0));
            add(hiBut, BorderLayout.NORTH);
            add(numBut, BorderLayout.SOUTH);
            hiBut.setActionCommand("res");
            numBut.setActionCommand("num");
            hiBut.addActionListener(this);
            numBut.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("res")) {
                addHiero(i);
            } else if (e.getActionCommand().equals("num")) {
                addNum(i);
            }
        }
        public Dimension getMaximumSize() {
            return super.getPreferredSize();
        }
    }

    private void addHiero(int i) {
        if (i <= line.formatted.size()) {
            line.formatted.add(i, new ResFormat(""));
            addFormat();
            revalidate();
            repaint();
        }
    }
    private void addNum(int i) {
        if (i <= line.formatted.size()) {
            line.formatted.add(i, new NumFormat(""));
            addFormat();
            revalidate();
            repaint();
        }
    }

	/////////////////////////////////////////////////////
	// Communication to caller.

	protected abstract void findGlyph();
	protected abstract void findExtra();

	/////////////////////////////////////////////////////
	// Actions.

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
        if (command.equals("menu")) 
            findGlyph();
        else if (command.equals("extra")) 
            findExtra();
		else if (command.equals("none")) 
			setModifier("");
		else if (NonHiero.isMod(command))
			setModifier(command);
		else
			super.actionPerformed(e);
	}

	/////////////////////////////////////////////////////
	// Focus.

    // Put modified name in glyph.
    protected void setModifier(String mod) {
        if (selected != null) {
            ChoiceBox box = blobToBox.get(selected);
            if (box != null) {
                String oldName = (String) box.getSelectedItem();
                if (!NonHiero.isExtra(oldName)) {
                    String bare = oldName.replaceAll("\\[.*\\]", "");
                    box.receive(bare + mod);
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////
    // Contexts created only once.

    private static HieroRenderContext hieroContext = null;
    private static HieroRenderContext getContext() {
        if (hieroContext == null)
            hieroContext = new HieroRenderContext(HieroSettings.previewHieroFontSize,
                    HieroSettings.previewNoteFontSize, false);
        return hieroContext;
    }

    private static ParsingContext parsingContext = null;
    private static ParsingContext getParsingContext() {
        if (parsingContext == null)
            parsingContext = new ParsingContext(getContext(), true);
        return parsingContext;
    }

}
