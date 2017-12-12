package nederhof.ocr.guessing;

import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.imageio.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.ocr.images.*;
import nederhof.ocr.images.distance.*;
import nederhof.ocr.admin.*;
import nederhof.util.*;
import nederhof.util.collections.*;
import nederhof.util.math.*;
import nederhof.util.xml.*;

// Guesser of glyphs.
public abstract class OcrGuesser {

	// Name of index file.
	protected final String INDEX = "index.xml";

	// Beam for FFT;
	protected int fftBeam = 5;
	// Beam for distortion model.
	protected int distBeam = 5;

	// The directory containing the prototypes.
	protected File dir;

	// Records of prototypes.
	protected class Record {
		public File file;
		public String fileName;
		public String glyphName;
		public Record(File file, String fileName, String glyphName) {
			this.file = file;
			this.fileName = fileName;
			this.glyphName = glyphName;
		}
	}

	// One record for each prototype.
	protected Record[] records;
	// Number of records added.
	protected int nImages = 0;

	// Mapping from glyph names to records.
	protected TreeMap<String,Vector<Record>> nameToRecords = new TreeMap<String,Vector<Record>>();

	// Make guesser for directory of prototypes.
	public OcrGuesser(String loc) throws IOException {
		this.dir = new File(loc);
		if (!dir.exists())
			throw new IOException("Cannot find: " + dir);
		initialize();
	}
	public OcrGuesser(File dir) throws IOException {
		this.dir = dir;
		if (!dir.exists())
			throw new IOException("Cannot find: " + dir.getAbsolutePath());
		initialize();
	}
	protected void initialize() throws IOException {
		readIndexFile();
		makeModels();
	}

	// Get current directory of prototypes.
	public File getPrototypeDir() {
		return this.dir;
	}

	// Internal beam.
	public abstract void setBeam(int b);
	public abstract int getBeam();
	// Beam for final selection.
	public abstract void setCandidates(int b);
	public abstract int getCandidates();

	////////////////////////////////////////////////////////
	// Index file.

	// Read index file (if exists) with information on handwriting.
	protected abstract void readIndexFile() throws IOException;

	// By e.g. index file, some glyphs may be excluded from 
	// competing in OCR.
	// Subclass may override.
	protected boolean isAllowed(String name) {
		return true;
	}

	// By e.g. index file, some names may be added to candidates,
	// after OCR.
	// Subclass may override.
	protected Vector<WeightedElem<String>> extend(Vector<WeightedElem<String>> names) {
		return names;
	}

	//////////////////////////////////////////////////////////
	// Creating models.

	protected void makeModels() throws IOException {
		File[] files = dir.listFiles();
		initModels(files.length);
		initRecords(files.length);
		for (File file : files) {
			if (file.isFile() && FileAux.hasExtension(file.getName(), "png")) {
				String fileName = FileAux.removeExtension(file.getName(), "png");
				String glyphName = getGlyphName(fileName);
				if (isAllowed(glyphName))
					try {
						addToModels(file, fileName, glyphName);
						addToRecords(file, fileName, glyphName);
						nImages++;
					} catch (NullPointerException e) {
						throw new IOException("Cannot access prototypes:" + e.getMessage());
					}
			}
		}
	}
	protected abstract void initModels(int nImagesMax);
	private void initRecords(int nImagesMax) {
		records = new Record[nImagesMax];
	}

	protected abstract void addToModels(File file, String fileName, String glyphName) 
			throws IOException;
	private void addToRecords(File file, String fileName, String glyphName) {
		records[nImages] = new Record(file, fileName, glyphName);
		if (!nameToRecords.containsKey(glyphName))
			nameToRecords.put(glyphName, new Vector<Record>());
		nameToRecords.get(glyphName).add(records[nImages]);
	}

	protected abstract String getGlyphName(String fileName);
	protected abstract String getNoGlyphName();

	////////////////////////////////////////////////////////
	// Finding best prototypes.

	// Find single best name.
	public String findBestName(BinaryImage im, float height) {
		Vector<String> bestNames = findNBestNames(im, height, 1);
		if (bestNames.size() > 0)
			return bestNames.get(0);
		else
			return getNoGlyphName();
	}
	// Find best names.
	public Vector<String> findBestNames(BinaryImage im, float height) {
		return findNBestNames(im, height, distBeam);
	}

	// Find N best names, according to chosen model(s).
	public abstract Vector<String> findNBestNames(BinaryImage im, float height, int distBeam);

	// Gather all prototypes, with zero weights.
	protected Vector<WeightedElem<Integer>> fullSearch() {
		Vector<WeightedElem<Integer>> candidates = new Vector<WeightedElem<Integer>>();
		for (int i = 0; i < nImages; i++) 
			candidates.add(new WeightedElem<Integer>(i, 0));
		return candidates;
	}

	// Rank names in given order.
	protected Vector<WeightedElem<String>> fullSearchNames(Vector<String> names) {
		Vector<WeightedElem<String>> weighted = new Vector<WeightedElem<String>>();
		float tiebreak = 0;
		for (String name : names) {
			weighted.add(new WeightedElem<String>(name, tiebreak));
			tiebreak += 0.00001;
		}
		return weighted;
	}

	// Best score to prototypes with name.
	public int score(BinaryImage im, String name) {
		float bestCost = Integer.MAX_VALUE;
		Vector<Record> records = nameToRecords.get(name);
		if (records != null)
			for (Record rec : records)
				try {
					BufferedImage proto = ImageIO.read(rec.file);
					float cost = cost(im, proto);
					bestCost = Math.min(bestCost, cost);
				} catch (IOException e) { /* ignore */ }
		return (int) bestCost;
	}

	// Distance between two images.
	protected abstract float cost(BinaryImage im, BufferedImage proto);

	// Translate weighted indices to weighted names. Omit duplicates. Keep first weight per name.
	protected Vector<WeightedElem<String>> weightedNamesOfWeightedCandidates(List<WeightedElem<Integer>> candidates) {
		Vector<WeightedElem<String>> names = new Vector<WeightedElem<String>>();
		TreeSet<String> seen = new TreeSet<String>();
        for (WeightedElem<Integer> weightedElem : candidates) {
			int elem = weightedElem.elem();
			float weight = weightedElem.weight();
            String name = records[elem].glyphName;
            if (!seen.contains(name)) {
                names.add(new WeightedElem<String>(name, weight));
                seen.add(name);
            }
        }
        return names;
	}

	// Remove weights from elements.
	protected Vector<Integer> unweightedCandidates(List<WeightedElem<Integer>> weighteds) {
		Vector<Integer> candidates = new Vector<Integer>();
		for (WeightedElem<Integer> weighted : weighteds) {
			int elem = weighted.elem();
			candidates.add(elem);
		}
		return candidates;
	}
	protected Vector<String> unweightedNames(List<WeightedElem<String>> weighteds) {
		Vector<String> names = new Vector<String>();
		for (WeightedElem<String> weighted : weighteds) {
			String name = weighted.elem();
			names.add(name);
		}
		return names;
	}

	// Translate indices to names. Omit duplicates.
	protected Vector<String> namesOfCandidates(List<Integer> elems) {
		Vector<String> names = new Vector<String>();
		TreeSet<String> seen = new TreeSet<String>();
        for (Integer elem : elems) {
            String name = records[elem].glyphName;
            if (!seen.contains(name)) {
                names.add(name);
                seen.add(name);
            }
        }
        return names;
	}

	// Get best few, with similar costs (not more than margin apart). Rerank them.
	protected Vector<String> tieBreaking(Vector<WeightedElem<String>> names, float margin,
			BinaryImage im, float height) {
		if (names.size() <= 1)
			return unweightedNames(names);
		float bestWeight = names.get(0).weight();
		int nTop = 1;
		while (nTop < names.size() && names.get(nTop).weight() / bestWeight < margin)
			nTop++;
		if (nTop <= 1)
			return unweightedNames(names);
		Vector<String> top = unweightedNames(names.subList(0, nTop));
		Vector<String> rest = unweightedNames(names.subList(nTop, names.size()));
		Vector<String> ranked = rerank(top, im, height);
		ranked.addAll(rest);
		return ranked;
	}

	// Rank according to additional model.
	protected abstract Vector<String> rerank(Vector<String> candidates, BinaryImage im, float height);

}
