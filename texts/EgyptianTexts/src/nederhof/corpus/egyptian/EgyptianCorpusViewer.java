/***************************************************************************/
/*                                                                         */
/*  EgyptianCorpusViewer.java                                              */
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

// Corpus of Ancient Egyptian.

package nederhof.corpus.egyptian;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.alignment.*;
import nederhof.alignment.egyptian.*;
import nederhof.corpus.*;
import nederhof.corpus.frame.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

public class EgyptianCorpusViewer extends CorpusViewer {

	// The type.
	protected String type() {
		return "Ancient Egyptian";
	}

	// Default directory for corpus.
	protected String getDefaultCorpusDir() {
		return Settings.defaultCorpusDir;
	}
	// Default directory for texts.
	protected String getDefaultTextDir() {
		return Settings.defaultTextDir;
	}

	// Make window.
	public EgyptianCorpusViewer(String location, boolean visible) {
		super(location, visible);
		renderParameters = new EgyptianRenderParameters(this);
	}
	// By default visible.
	public EgyptianCorpusViewer(String location) {
		this(location, true);
	}

	// Make text viewer.
	protected TextViewer getTextViewer(Text text) {
		if (renderParameters == null)
			return null; // if user clicked before constructor done
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String baseName = baseName(text);
		String name = text.getName();
		Autoaligner aligner = new EgyptianAutoaligner();
		File corpusDir = corpus == null ? 
			new File(".") : 
			(new File(corpus.getLocation())).getParentFile();
		File pdfDir = corpus == null || corpus.getLocation().startsWith("jar:") ?
			new File(".") :
			new File(corpusDir, Settings.defaultPdfDir);
		PdfRenderParameters pdfRenderParameters = 
			new EgyptianPdfRenderParameters("" + pdfDir, baseName, name);
		TextViewer viewer = new InterlinearViewer(corpusDir.getPath(),
				text, resourceGenerators(), aligner,
				renderParameters, pdfRenderParameters) {
			protected void exit() {
				textViewers.remove(this);
			}
			protected String makeDescription(Vector resources) {
				return composeDescription(resources);
			}
			protected void refreshIndices() {
				corpus.refreshTrees();
				makeIndices();
				changed = true;
			}
		};
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		return viewer;
	}

	protected Autoaligner autoaligner() {
		return new EgyptianAutoaligner();
	}

	protected PdfRenderParameters pdfRenderParameters() {
		return new EgyptianPdfRenderParameters();
	}

	public Vector<ResourceGenerator> resourceGenerators() {
		Vector<ResourceGenerator> generators = new Vector<ResourceGenerator>();
		generators.add(new EgyptianResourceGenerator());
		generators.add(new EgyptianLexicoGenerator());
		generators.add(new EgyptianOrthoGenerator());
		generators.add(new EgyptianImageGenerator());
		generators.add(new SchemeMapGenerator());
		return generators;
	}

	// Get base name of text.
	private static String baseName(Text text) {
		File file = new File(text.getLocation());
		String name = file.getName();
		return FileAux.removeExtension(name, "xml");
	}

	// Make description of available resources.
	private static String composeDescription(Vector resources) {
		boolean hiero = false;
		TreeSet languages = new TreeSet();
		boolean translit = false;
		boolean lex = false;
		boolean ortho = false;
		boolean image = false;
		for (int i = 0; i < resources.size(); i++) {
			TextResource resource = (TextResource) resources.get(i);
			if (resource instanceof EgyptianResource) {
				EgyptianResource egResource = (EgyptianResource) resource;
				for (int j = 0; j < egResource.nTiers(); j++) {
					if (!egResource.isEmptyTier(j)) {
						String name = egResource.tierName(j);
						if (name.equals("hieroglyphic"))
							hiero = true;
						else if (name.equals("translation")) {
							String lang = egResource.getStringProperty("language");
							if (lang != null && !lang.matches("\\s*"))
								languages.add(lang);
						} else if (name.equals("transliteration")) 
							translit = true;
						else if (name.equals("lexical")) 
							lex = true;
					}
				}
			}
			if (resource instanceof EgyptianLexico) {
				EgyptianLexico egResource = (EgyptianLexico) resource;
				for (int j = 0; j < egResource.nTiers(); j++) 
					if (!egResource.isEmptyTier(j)) 
						lex = true;
			}
			if (resource instanceof EgyptianOrtho) {
				EgyptianOrtho egResource = (EgyptianOrtho) resource;
				for (int j = 0; j < egResource.nTiers(); j++) 
					if (!egResource.isEmptyTier(j)) 
						ortho = true;
			}
			if (resource instanceof EgyptianImage) {
				EgyptianImage egResource = (EgyptianImage) resource;
				for (int j = 0; j < egResource.nTiers(); j++) 
					if (!egResource.isEmptyTier(j)) 
						image = true;
			}
		}
		StringBuffer buf = new StringBuffer();
		boolean prefix = false;
		if (hiero) {
			buf.append("hiero");
			prefix = true;
		}
		if (!languages.isEmpty()) {
			if (prefix)
				buf.append("; ");
			boolean before = false;
			for (Iterator it = languages.iterator(); it.hasNext(); ) {
				String lang = (String) it.next();
				if (before)
					buf.append("/");
				buf.append(lang);
				before = true;
			}
			prefix = true;
		}
		if (translit) {
			if (prefix)
				buf.append("; ");
			buf.append("translit");
			prefix = true;
		}
		if (lex) {
			if (prefix)
				buf.append("; ");
			buf.append("lexical");
			prefix = true;
		}
		if (ortho) {
			if (prefix)
				buf.append("; ");
			buf.append("ortho");
			prefix = true;
		}
		if (image) {
			if (prefix)
				buf.append("; ");
			buf.append("image");
			prefix = true;
		}
		return buf.toString();
	}

	// Make corpus viewer.
	public static void main(String[] args) {
		// circumvents bug in Java
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		CorpusViewer frame;
		if (args.length > 0)
			frame = new EgyptianCorpusViewer(args[0]);
		else
			frame = new EgyptianCorpusViewer(null);
		frame.setStandAlone(true);
	}

}
