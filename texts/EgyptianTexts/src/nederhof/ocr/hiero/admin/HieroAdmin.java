package nederhof.ocr.hiero.admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.*;
import nederhof.ocr.images.*;
import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

// Frame in which collections of prototypes of hieroglyphs can be maintained.

public class HieroAdmin extends OcrAdmin {

	// Constructor without directory.
	public HieroAdmin() {
		super();
	}
	// Constructor with directory.
	public HieroAdmin(File dir) {
		super(dir);
	}
	// With directory and project from which to import.
	public HieroAdmin(File dir, Project proj) {
		super(dir, proj);
	}

	// Spawn new administrator.
	protected OcrAdmin makeAdmin(File dir) {
		return new HieroAdmin(dir);
	}

	// URL of manual page.
	protected String getHelpPage() {
		return "data/help/ocr/admin.html";
	}

	protected String getGlyphName(String fileName) {
		return fileName.replaceAll("-.*", "");
	}

	protected String getFileName(String glyphName, int index) {
		return glyphName + "-" + index;
	}

	protected String shortToLong(String s) {
		return NonHiero.modShortToLong(s);
	}

	protected Project getProject(File file) throws IOException {
		return new HieroProject(file);
	}

	//////////////////////////////////////////////////////
	// Glyph list.
	
    // Get ordered list of glyph names.
    // Do this only once.
    protected void compileNames() {
        if (names == null) {
            names = new Vector<String>();
            try {
                InputStream in = FileAux.addressToStream(HieroAdminSettings.infoFile);
                DocumentBuilder parser = SimpleXmlParser.construct(false, false);
                Document signInfo = parser.parse(in);
                NodeList signs = signInfo.getElementsByTagName("sign");
                for (int i = 0; i < signs.getLength(); i++) {
                    Element sign = (Element) signs.item(i);
                    String name = sign.getAttribute("name");
                    names.add(name);
                    for (String mod : NonHiero.modifiers)
                        names.add(name + mod);
                }
                Vector<String> extras = NonHiero.getExtras();
                for (int i = 0; i < extras.size(); i++) {
                    String extra = extras.get(i);
                    names.add(extra);
                    if (extra.equals("shade"))
                        names.add("shade[part]");
                }
                in.close();
            } catch (Exception e) {
                System.err.println("Could not read: " + HieroAdminSettings.infoFile);
                System.err.println(e.getMessage());
            }
        }
        protos = new HashMap<String, Vector<Prototype>>();
        for (String name : names)
            protos.put(name, new Vector<Prototype>());
    }

	///////////////////////////////////////////////////
	// Panel with prototypes.
	
	protected PrintGlyphButton getPrintButton(String name) {
		return new HieroPrintButton(name);
	}

	protected ScanGlyphButton getScanButton(String name, Prototype proto, int w, int h) {
		return new ConnectedScanButton(name, proto, w, h);
	}

	private class ConnectedScanButton extends HieroScanButton {
		public ConnectedScanButton(String name, Prototype proto, int w, int h) {
			super(name, proto, w, h);
		}
		public void remove() {
			super.remove();
			HieroAdmin.this.remove(name, proto);
		}
		public void move() {
			HieroAdmin.this.move(name, proto);
		}
	}

	///////////////////////////////////////////////////////
	// Import from OCR project.
	
	protected boolean importAllowed(String name) {
		return !name.equals("unk");
	}

    protected void importParts(File newDir, BufferedImage im, String name) {
        if (NonHiero.hasMod(name) ||
                (NonHiero.isExtra(name) && !name.equals("shade")))
            return;
        BinaryImage bin = new BinaryImage(im);
        Vector<Vector<Point>> comps = ImageComponents.find(bin);
        if (comps.size() < 2)
            return;
        for (Vector<Point> comp :  comps) {
            BinaryImage partBin = ImageComponents.constructImage(comp);
            BufferedImage partIm = partBin.toBufferedImage();
            File target = freeFile(newDir, name + "[part]");
            try {
                ImageIO.write(partIm, "png", target);
            } catch (IOException e) { /* ignore */ }
        }
    }

	///////////////////////////////////////////////////////
	// Main.

	public static void main(String[] args) {
		HieroAdmin admin = new HieroAdmin(new File("paleo/sethe"));
	}

}

