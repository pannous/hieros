/***************************************************************************/
/*																		 */
/*  HieroFonts.java														*/
/*																		 */
/*  Copyright (c) 2009 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

// The records of a fonts file and the fonts.

package nederhof.res;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

import com.itextpdf.awt.DefaultFontMapper;

import nederhof.fonts.*;
import nederhof.util.*;

public class HieroFonts {

	// See spec of fonts file at site of RES.
	private Vector fontFiles = new Vector();
	private float fontSep = 0.0f;
	private float fontBoxSep = 0.0f;
	private int namedGlyphLatin = 0;
	private int noteLatin = 0;

	// Places of special glyphs.
	private GlyphPlace open = new GlyphPlace();
	private GlyphPlace close = new GlyphPlace();
	private GlyphPlace hlrspec = new GlyphPlace();
	private GlyphPlace hrlspec = new GlyphPlace();
	private GlyphPlace vlrspec = new GlyphPlace();
	private GlyphPlace vrlspec = new GlyphPlace();

	// For PDF.
	private DefaultFontMapper pdfMapper = new DefaultFontMapper();

	// Places of Gardiner glyphs, arranged by category. (Aa at place of J.)
	// NL and NU are appended extra at the end.
	// Per category there is Vector. 
	private static int nCategories = 'Z'+1-'A' + 2;
	private static int NLcategory = nCategories - 2;
	private static int NUcategory = nCategories - 1;
	private Vector[] categories = new Vector[nCategories];

	// Places for box types. Box type is mapped to 3 places.
	private TreeMap boxes = new TreeMap();

	// Read fonts file.
	public HieroFonts(String fontsFile) {
		for (int i = 0; i < nCategories; i++)
			categories[i] = new Vector();
		try {
			processFontsFile(fontsFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Cannot interpret: " + fontsFile,
					"Fonts error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Read fonts file, and store its information.
	// Assume fonts file is relative to directory with fonts.
	// Other than res2image in C, the Java code only accepts unicode fonts.
	private void processFontsFile(String fontsFile) throws IOException {
		URL url = null;
		InputStream in = null;
		try {
			url = FileAux.fromBase(fontsFile);
			if (url == null)
				throw new MalformedURLException();
			in = url.openStream();
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null,
					"File not found: " + fontsFile,
					"Fonts error", JOptionPane.ERROR_MESSAGE);
			return;
		} 
		BufferedReader reader = 
			new BufferedReader(new InputStreamReader(in));
		// First line.
		String line = reader.readLine();
		if (line == null) {
			System.err.println("Missing first line of font index file " + fontsFile);
			return;
		}
		String[] args = line.split("\\s+");
		if (args.length != 4 || 
				!args[0].equals("%") || !args[1].equals("encoding_unicode")) {
			System.err.println("Incorrect first line of font index file " + fontsFile);
			return;
		}
		try {
			fontSep = Float.parseFloat(args[2]);
			fontBoxSep = Float.parseFloat(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("Incorrect first line of font index file " + fontsFile);
			fontSep = 0.0f;
			fontBoxSep = 0.0f;
			return;
		}
		// Font files.
		line = reader.readLine();
		while (line != null && !line.startsWith("%")) {
			if (line.matches("\\s*(\\S+)\\s*")) {
				line = line.replaceAll("\\s+", "");
				URL fontUrl = new URL(url, line);
				Font f = FontUtil.getFontFrom(fontUrl, pdfMapper);
				if (f != null) 
					fontFiles.add(f);
			} else {
				System.err.println("Incorrect line in font index file:");
				System.err.println(line);
				return;
			}
			line = reader.readLine();
		}
		// Line with Latin fonts.
		if (line == null) {
			System.err.println("Premature end of font index file " + fontsFile);
			return;
		}
		args = line.split("\\s+");
		if (args.length != 3 || !args[0].equals("%")) {
			System.err.println("Incorrect line (Latin fonts) in font index file:");
			System.err.println(line);
			return;
		}
		try {
			namedGlyphLatin = Integer.parseInt(args[1]);
			noteLatin = Integer.parseInt(args[2]);
			if (namedGlyphLatin < 0 || namedGlyphLatin > fontFiles.size() ||
					noteLatin < 0 || noteLatin > fontFiles.size())
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			namedGlyphLatin = 0;
			noteLatin = 0;
			System.err.println("Incorrect line (Latin fonts) in font index file:");
			System.err.println(line);
			return;
		}
		// Glyphs.
		line = reader.readLine();
		while (line != null) {
			if (!line.matches("\\s*")) {
				args = line.split("\\s+");
				int fileNum = 1;
				int glyphNum = 0;
				if (args.length == 2)
					try {
						glyphNum = Integer.decode(args[1]).intValue();
						if (glyphNum < 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						System.err.println("Incorrect line (glyph) in font index file:");
						System.err.println(line);
						return;
					}
				else if (args.length == 3)
					try {
						fileNum = Integer.parseInt(args[1]);
						glyphNum = Integer.decode(args[2]).intValue();
						if (fileNum < 0 || fileNum > fontFiles.size() || glyphNum < 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						System.err.println("Incorrect line (glyph) in font index file:");
						System.err.println(line);
						return;
					}
				else {
					System.err.println("Incorrect line (glyph) in font index file:");
					System.err.println(line);
					return;
				}
				storeGlyph(args[0], new GlyphPlace(fileNum, glyphNum));
			}
			line = reader.readLine();
		}
		reader.close();
		in.close();
	}

	// Pattern of Gardiner code.
	public static final Pattern gardinerPat =
		Pattern.compile("^([A-I]|[K-Z]|Aa|NL|NU)([0-9]+)([a-z]?)");

	// Depending on nature of name, glyph is stored.
	private void storeGlyph(String name, GlyphPlace place) {
		Matcher m = gardinerPat.matcher(name);
		if (m.find()) 
			try {
				int num = Integer.parseInt(m.group(2));
				if (num <= 0 || num > 999)
					throw new NumberFormatException();
				storeGardinerGlyph(m.group(1), num, m.group(3), place);
			} catch (NumberFormatException e) {
				System.err.println("Incorrect glyph in font index file " + name);
			}
		else if (name.equals("open")) {
			if (open.isKnown())
				System.err.println("Warning: open glyph doubly defined");
			open = place;
		} else if (name.equals("close")) {
			if (close.isKnown())
				System.err.println("Warning: close glyph doubly defined");
			close = place;
		} else if (name.matches(".+open"))
			storeBoxOpen(name.replaceFirst("open$", ""), place);
		else if (name.matches(".+segment"))
			storeBoxSegment(name.replaceFirst("segment$", ""), place);
		else if (name.matches(".+close"))
			storeBoxClose(name.replaceFirst("close$", ""), place);
		else if (name.equals("hlr")) {
			if (hlrspec.isKnown())
				System.err.println("Warning: hlr doubly defined");
			hlrspec = place;
		} else if (name.equals("hrl")) {
			if (hrlspec.isKnown())
				System.err.println("Warning: hrl doubly defined");
			hrlspec = place;
		} else if (name.equals("vlr")) {
			if (vlrspec.isKnown())
				System.err.println("Warning: vlr doubly defined");
			vlrspec = place;
		} else if (name.equals("vrl")) {
			if (vrlspec.isKnown())
				System.err.println("Warning: vrl doubly defined");
			vrlspec = place;
		} else 
			System.err.println("Incorrect glyph in font index file " + name);
	}

	// Store place of glyph.
	private void storeGardinerGlyph(String cat, int num, String affix, GlyphPlace place) {
		Vector catNums = categories[catIndex(cat)];
		if (num > catNums.size()) 
			catNums.setSize(num);
		if (catNums.get(num-1) == null)
			catNums.set(num-1, new TreeMap());
		TreeMap catSet = (TreeMap) catNums.get(num-1);
		if (catSet.containsKey(cat + num + affix))
			System.err.println("Warning: glyph doubly defined " + cat + num + affix);
		catSet.put(cat + num + affix, place);
	}

	// Index of category.
	// Aa is stored as J. NL and NU are at the end.
	private int catIndex(String cat) {
		if (cat.length() == 1)
			return cat.charAt(0) - 'A';
		else if (cat.equals("Aa"))
			return 'J' - 'A';
		else if (cat.equals("NL"))
			return NLcategory;
		else
			return NUcategory;
	}

	// Store place of part of box.
	private void storeBoxOpen(String type, GlyphPlace place) {
		if (!boxes.containsKey(type))
			boxes.put(type, new BoxPlaces());
		BoxPlaces places = (BoxPlaces) boxes.get(type);
		if (places.open.isKnown())
			System.err.println("Warning: open glyph of box doubly defined " + type);
		places.open = place;
	}
	private void storeBoxSegment(String type, GlyphPlace place) {
		if (!boxes.containsKey(type))
			boxes.put(type, new BoxPlaces());
		BoxPlaces places = (BoxPlaces) boxes.get(type);
		if (places.segment.isKnown())
			System.err.println("Warning: segment glyph of box doubly defined " + type);
		places.segment = place;
	}
	private void storeBoxClose(String type, GlyphPlace place) {
		if (!boxes.containsKey(type))
			boxes.put(type, new BoxPlaces());
		BoxPlaces places = (BoxPlaces) boxes.get(type);
		if (places.close.isKnown())
			System.err.println("Warning: close glyph of box doubly defined " + type);
		places.close = place;
	}

	///////////////////////////////////////////////////////////////////////////
	// Retrieving glyphs and fonts.

	// Get separation between glyphs as specified in font.
	public float fontSep() {
		return fontSep;
	}

	// Get separation between glyphs at box as specified in font.
	public float fontBoxSep() {
		return fontBoxSep;
	}

	// Number of font files.
	public int size() {
		return fontFiles.size();
	}

	// Get font.
	public Font get(int fileNumber) {
		return (Font) fontFiles.get(fileNumber - 1);
	}

	// Get font from place.
	public Font getFont(GlyphPlace place) {
		return get(place.file);
	}

	// Get Latin font for named glyphs.
	// Return null if no such font.
	public Font getLatinFont() {
		if (namedGlyphLatin > 0)
			return (Font) fontFiles.get(namedGlyphLatin - 1);
		else
			return null;
	}

	// Get font for note.
	// Return null if no such font.
	public Font getNoteFont() {
		if (noteLatin > 0)
			return (Font) fontFiles.get(noteLatin - 1);
		else
			return null;
	}
	// Get number thereof. Is positive if it exists.
	public int getNoteFontNumber() {
		return noteLatin;
	}

	// Name can be Gardiner code, mnemonic, open, close, or short
	// string. Return non-existent place if no such name exists.
	public GlyphPlace getGlyph(String name) {
		Matcher m = gardinerPat.matcher(name);
		if (m.find())
			return getGardinerGlyph(m.group(1), m.group(2), m.group(3));
		else if (mnemonics.containsKey(name))
			return getGlyph((String) mnemonics.get(name));
		else if (name.equals("open"))
			return open;
		else if (name.equals("close"))
			return close;
		else if (namedGlyphLatin > 0) {
			if (name.matches("\".\""))
				return new GlyphPlace(namedGlyphLatin, name.charAt(1));
			else if (name.matches("\"\\\\\""))
				return new GlyphPlace(namedGlyphLatin, '\"');
			else if (name.matches("\"\\\\\\\\\""))
				return new GlyphPlace(namedGlyphLatin, '\\');
			else
				return new GlyphPlace();
		} else
			return new GlyphPlace();
	}

	// Get place for Gardiner code. If no such code, return non-existent
	// place.
	private GlyphPlace getGardinerGlyph(String cat, String numString, String affix) {
		int num = Integer.parseInt(numString);
		Vector catNums = categories[catIndex(cat)];
		if (num > catNums.size())
			return new GlyphPlace();
		TreeMap catSet = (TreeMap) catNums.get(num-1);
		if (catSet == null)
			return new GlyphPlace();
		if (catSet.containsKey(cat + num + affix))
			return (GlyphPlace) catSet.get(cat + num + affix);
		else
			return new GlyphPlace();
	}

	// Get sign for specifying direction.
	public GlyphPlace getSpec(int spec) {
		switch (spec) {
			case ResValues.DIR_HRL:
				return hrlspec;
			case ResValues.DIR_VLR:
				return vlrspec;
			case ResValues.DIR_VRL:
				return vrlspec;
			default:
				return hlrspec;
		}
	}

	// Retrieve box places.
	// If no such type, return non-existence places.
	public BoxPlaces getBox(String type) {
		if (boxes.containsKey(type))
			return (BoxPlaces) boxes.get(type);
		else
			return new BoxPlaces();
	}

	// Retrieve box types.
	public Set getBoxTypes() {
		return boxes.keySet();
	}

	// Get mapper for PDF output.
	public DefaultFontMapper pdfMapper() {
		return pdfMapper;
	}

	// Get all glyphs in category. J is interpreted as Aa,
	// l is interpreted as NL and u as NU.
	public Vector getCategory(char cat) {
		Vector catNums;
		if (cat == 'l')
			catNums = categories[NLcategory];
		else if (cat == 'u')
			catNums = categories[NUcategory];
		else
			catNums = categories[cat - 'A'];
		Vector glyphs = new Vector();
		for (int i = 0; i < catNums.size(); i++) {
			Map catSet = (Map) catNums.get(i);
			if (catSet != null)
				glyphs.addAll(catSet.keySet());
		}
		return glyphs;
	}

	//////////////////////////////////////////////////////////////////////////
	// Mnemonics.

	// File containing mnemonics and customized mnemonics.
	private static final String mnemonicsFile = "data/ortho/mnemonics.txt";
	private static final String customMnemonicsFile = "data/ortho/custom_mnemonics.txt";

	// Map from mnemonic to Gardiner code.
	private static HashMap mnemonics;
	private static HashMap customMnemonics;

	// Read file containing mnemonics.
	static {
		mnemonics = new HashMap();
		customMnemonics = new HashMap();
		try {
			processMnemonicsFile(mnemonicsFile, mnemonics);
		} catch (IOException e) {
			System.err.println("Cannot interpret " + mnemonicsFile);
		}
		try {
			processMnemonicsFile(customMnemonicsFile, customMnemonics);
		} catch (IOException e) {
			System.err.println("Cannot interpret " + customMnemonicsFile);
		}
	}

	// Read file containing mnemonics.
	// Assume path is relative to directory of fonts.
	private static void processMnemonicsFile(String mnemonicsFile, HashMap mnemonics) 
				throws IOException {
		InputStream in = null;
		try {
			URL url = FileAux.fromBase(mnemonicsFile);
			if (url == null)
				throw new MalformedURLException();
			in = url.openStream();
		} catch (MalformedURLException e) {
			System.err.println("File not found " + mnemonicsFile);
			return;
		}
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(in));
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.matches("\\s*"))
				continue;
			String[] args = line.split("\\s+");
			if (args.length != 2) {
				System.err.println("Incorrect line mnemonics file:");
				System.err.println(line);
				continue;
			}
			String mnem = args[0];
			String gard = args[1];
			if (!mnem.matches("[a-zA-Z]+") && !mnem.matches("[0-9]+")) {
				System.err.println("Incorrect mnemonic in mnemonics file:" + mnem);
				continue;
			}
			if (!HieroFonts.gardinerPat.matcher(gard).find()) {
				System.err.println("Incorrect Gardiner code in mnemonics file:" + gard);
				continue;
			}
			if (mnemonics.containsKey(mnem))
				System.err.println("Warning: mnemonic doubly defined " + mnem);
			mnemonics.put(mnem, gard);
		}
		reader.close();
		in.close();
	}

	// If name is mnemonic, map to Gardiner name.
	public String nameToGardiner(String name) {
		if (mnemonics.get(name) != null)
			return (String) mnemonics.get(name);
		else
			return name;
	}

	// If name is custom mnemonic, map to Gardiner name.
	public String customNameToGardiner(String name) {
		if (customMnemonics.get(name) != null)
			return (String) customMnemonics.get(name);
		else
			return name;
	}

}
