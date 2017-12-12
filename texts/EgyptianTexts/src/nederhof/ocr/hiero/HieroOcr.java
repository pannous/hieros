package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.ocr.images.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.util.*;

// Frame in which OCR project of hieroglyphic is displayed.
public class HieroOcr extends Ocr {

	// Auxiliary windows for choosing glyphs.
	private GlyphChooser chooser = null;
	private ClosedSetChooser extraChooser = null;

	// Make window. With directory if present.
	public HieroOcr(String dir) {
		super(dir);
	}

	// Without given directory.
	public HieroOcr() {
		super();
	}

	// Title in frame.
	protected String getOcrTitle() {
		return "Ancient Egyptian OCR";
	}

	// Menu in frame.
	protected JMenuBar getMenu() {
		return new HieroMenu(this);
	}

	// Default directory of prototypes.
	protected String getProtoDir() {
		return HieroSettings.hieroProtoDir;
	}
	
	// Default beam size.
	protected int getDefaultBeam() {
		return HieroSettings.beam;
	}
	// Default number of candidates returned.
	protected int getDefaultNCandidates() {
		return HieroSettings.nCandidates;
	}

    // Segment detection starts when line is selected.
    protected boolean getDefaultAutoSegment() {
        return HieroSettings.autoSegment;
    }
    // Ocr starts when line is selected.
    protected boolean getDefaultAutoOcr() {
        return HieroSettings.autoOcr;
    }
    // Formatting starts after OCR.
    protected boolean getDefaultAutoFormat() {
        return HieroSettings.autoFormat;
    }

	// Creates guesser.
	protected OcrGuesser createGuesser(String protoDir) throws IOException {
		return new HieroGuesser(protoDir);
	}
	// Creates analyzer of layout.
	protected LayoutAnalyzer createAnalyzer() {
		return new HieroLayoutAnalyzer();
	}
	// Creates process to combine glyphs consisting of several components.
	protected GlyphCombiner createCombiner(Line line) {
		return new HieroGlyphCombiner(line);
	}
	// Creates formatter.
	protected BlobFormatter createFormatter() {
		return new HieroBlobFormatter();
	}

    // URL of manual page.
    protected String getHelpPage() {
		return "data/help/ocr/hiero_ocr.html";
	}

	//////////////////////////////////////////////////////////////////////////////
	// Menu at top of window.

	// Menu containing quit and edit buttons.
	protected class HieroMenu extends Menu {
		public HieroMenu(ActionListener lis) {
			super(lis);
		}

		protected void addCustomItems(ActionListener lis) {
			addModifyItems(lis);
		}

		protected void addModifyItems(ActionListener lis) {
			JMenu modifyMenu = new EnabledMenu(
					"modifi<u>E</u>rs", KeyEvent.VK_E);
			JMenuItem noneItem = new EnabledMenuItem(lis,
					"none", "none", KeyEvent.VK_0);
			JMenuItem rotate90Item = new EnabledMenuItem(lis,
					"[rotate=90]", "[rotate=90]", KeyEvent.VK_1);
			JMenuItem rotate180Item = new EnabledMenuItem(lis,
					"[rotate=180]", "[rotate=180]", KeyEvent.VK_2);
			JMenuItem rotate270Item = new EnabledMenuItem(lis,
					"[rotate=270]", "[rotate=270]", KeyEvent.VK_3);
			JMenuItem mirrorItem = new EnabledMenuItem(lis,
					"mirror", "[mirror]", KeyEvent.VK_4);
			JMenuItem partItem = new EnabledMenuItem(lis,
					"part", "[part]", KeyEvent.VK_5);
			JMenuItem hlrItem = new EnabledMenuItem(lis,
					"hlr", "hlr", KeyEvent.VK_7);
			JMenuItem hrlItem = new EnabledMenuItem(lis,
					"hrl", "hrl", KeyEvent.VK_8);
			JMenuItem vlrItem = new EnabledMenuItem(lis,
					"vlr", "vlr", KeyEvent.VK_9);
			JMenuItem vrlItem = new EnabledMenuItem(lis,
					"vrl", "vrl", KeyEvent.VK_0);
			add(modifyMenu);
			menuItems.add(modifyMenu);
			modifyMenu.add(noneItem);
			menuItems.add(noneItem);
			modifyMenu.add(rotate90Item);
			menuItems.add(rotate90Item);
			modifyMenu.add(rotate180Item);
			menuItems.add(rotate180Item);
			modifyMenu.add(rotate270Item);
			menuItems.add(rotate270Item);
			modifyMenu.add(mirrorItem);
			menuItems.add(mirrorItem);
			modifyMenu.add(partItem);
			menuItems.add(partItem);
			modifyMenu.add(hlrItem);
			menuItems.add(hlrItem);
			modifyMenu.add(hrlItem);
			menuItems.add(hrlItem);
			modifyMenu.add(vlrItem);
			menuItems.add(vlrItem);
			modifyMenu.add(vrlItem);
			menuItems.add(vrlItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));
		}
	}

	//////////////////////////////////////////////////////
	// Prototype admin.
	
	protected OcrAdmin createOcrAdmin(File dir, Project project) {
		if (project == null)
			return new ConnectedAdmin(dir);
		else
			return new ConnectedAdmin(dir, project);
	}

	private class ConnectedAdmin extends HieroAdmin {
		public ConnectedAdmin(File dir) {
			super(dir);
		}
		public ConnectedAdmin(File dir, Project project) {
			super(dir, project);
		}
		public void refreshApplication() {
			try {
				reloadGuesser();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Cannot open prototypes: " + e.getMessage(),
						"Reading error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	//////////////////////////////////////////////////////
	// Fill project.
	
	protected Project createProject(File dir) throws IOException {
		return new HieroProject(dir);
	}

	protected OcrPage createPage(BinaryImage im, Page page) {
		return new ConnectedPage(im, page);
	}

    // OcrPage connected to this.
	private class ConnectedPage extends HieroOcrPage {
		public ConnectedPage(BinaryImage im, Page page) {
			super(im, page);
		}
		protected OcrProcess getProcess() {
			return process;
		}
		protected LayoutAnalyzer getAnalyzer() {
			return analyzer;
		}
		protected BlobFormatter getFormatter() {
			return formatter;
		}
		protected GlyphCombiner createCombiner(Line line) {
			return HieroOcr.this.createCombiner(line);
		}
		protected boolean getAutoSegment() {
			return autoSegment;
		}
		protected boolean getAutoOcr() {
			return autoOcr;
		}
		protected boolean getAutoFormat() {
			return autoFormat;
		}
		protected void findGlyph() {
			HieroOcr.this.findGlyph();
		}
		protected void findExtra() {
			HieroOcr.this.findExtra();
		}
		protected void allowEdits(boolean allow) {
			HieroOcr.this.allowEdits(allow);
		}
		protected void setWait(boolean wait) {
			HieroOcr.this.setWait(wait);
		}
	}

    ///////////////////////////////////////////////////////
    // Glyph choosing menu.

    // Activate glyph chooser.
    protected void findGlyph() {
        if (chooser == null)
            chooser = new GlyphChooser() {
                protected void receive(String name) {
                    sendNameToPage(name);
                }
                protected void receiveNothing() {
                    sendNameToPage(null);
                }
            };
        chooser.setVisible(true);
    }
    // Activate non-glyph chooser.
    protected void findExtra() {
        if (extraChooser == null)
            extraChooser = new NonHieroChooser() {
                protected void receive(String name) {
                    sendNameToPage(name);
                }
            };
        extraChooser.setVisible(true);
    }

	// Before dispose of superclass, also dispose of auxiliary window.
	public void dispose() {
		if (chooser != null)
			chooser.dispose();
		if (extraChooser != null)
			extraChooser.dispose();
		super.dispose();
	}

	///////////////////////////////////////////////////////
	// Exporting to hieroglyphic resource.

	protected void export() {
		if (project != null) {
			setWait(true);
			new ProjectHieroExporter(project);
			new ProjectImageExporter(project);
			setWait(false);
		}
	}

	///////////////////////////////////////////////////////
	// Main.

	public static void main(String[] args) {
		if (args.length == 1 && !args[0].equals("")) {
			HieroOcr ocr = new HieroOcr(args[0]);
			ocr.setStandAlone(true);
		} else {
			HieroOcr ocr = new HieroOcr();
			ocr.setStandAlone(true);
		}
	}

}
