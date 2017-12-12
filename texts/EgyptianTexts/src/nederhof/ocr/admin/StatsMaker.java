package nederhof.ocr.admin;

import java.io.*;
import java.util.*;
import javax.swing.*;

import nederhof.ocr.*;
import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Extracts statistics on glyphs from OCR projects.
public abstract class StatsMaker {

	// Directory of prototypes.
	protected File dir;

	// The statistics.
	protected GlyphStats stats;

	// Analyzer of layout.
	protected LayoutAnalyzer analyzer;

	// Constructor takes index file.
	public StatsMaker(File dir) throws IOException {
		this.dir = dir;
		stats = createStats(dir);
		analyzer = createAnalyzer();
	}

	// Subclass to determine what statistics to create.
	protected abstract GlyphStats createStats(File dir) throws IOException;

	// Subclass to determine how to create project.
	protected abstract Project createProject(String file) throws IOException;

	// Maps name to list of sizes.
	protected HashMap<String,Vector<Double>> nameToSizes;
	// Maps name to list of width/height ratios.
	protected HashMap<String,Vector<Double>> nameToRatios;
	// Maps name to frequency.
	protected HashMap<String,Integer> nameToFreq;
	// Total number of glyphs.
	protected int n;

	protected abstract LayoutAnalyzer createAnalyzer();

	// Make statistics from projects.
	public void gatherFrom(Vector<String> files) {
		nameToSizes = new HashMap<String,Vector<Double>>();
		nameToRatios = new HashMap<String,Vector<Double>>();
		nameToFreq = new HashMap<String,Integer>();
		n = 0;
		for (String file : files)
			gatherFrom(file);
		processStatistics();
	}

	// Gather statistics from project.
	protected void gatherFrom(String fileName) {
		Project project;
		try {
			project = createProject(fileName);
			gatherFrom(project);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}
	protected void gatherFrom(Project project) {
		for (String pageName : project.images.keySet()) {
			Page page = project.pages.get(pageName);
			for (Line line : page.lines) {
				int unit = analyzer.predictUnitSize(line.glyphs);
				for (Blob blob : line.glyphs) {
					if (blob.isSaved()) {
						String name = blob.getName();
						if (name.equals("") &&
								blob.getGuessed() != null &&
								blob.getGuessed().size() > 0)
							name = blob.getGuessed().get(0);
						if (!name.equals("")) {
							BinaryImage im = blob.imSafe();
							gatherFrom(im, name, unit);
							gatherFromParts(im, name, unit);
						}
					}
				}
			}
		}
	}

	protected void gatherFrom(BinaryImage im, String name, int unit) {
		double size = im.height() * 1.0 / unit;
		double ratio = im.width() * 1.0 / im.height();
		if (nameToSizes.get(name) == null)
			nameToSizes.put(name, new Vector<Double>());
		if (nameToRatios.get(name) == null)
			nameToRatios.put(name, new Vector<Double>());
		if (nameToFreq.get(name) == null)
			nameToFreq.put(name, 0);
		nameToSizes.get(name).add(size);
		nameToRatios.get(name).add(ratio);
		nameToFreq.put(name, nameToFreq.get(name) + 1);
		n++;
	}

	// For blobs that consists of several blobs, the parts may be
	// analysed separately. Not done by default.
	protected void gatherFromParts(BinaryImage im, String name, int unit) {
		// subclass may override
	}

	// Turn gathered numbers into statistics.
	protected void processStatistics() {
		stats.clearNameToSize();
		stats.clearNameToRatio();
		stats.clearNameToRelFreq();
		double minVarianceSize = 0.01;
		double minVarianceRatio = 0.05;
		for (Map.Entry<String,Vector<Double>> nameSizes: nameToSizes.entrySet()) {
			String name = nameSizes.getKey();
			Vector<Double> sizes = nameSizes.getValue();
			LogNormalDistribution norm = new LogNormalDistribution(sizes);
			norm.setVariance(Math.max(norm.getVariance(), minVarianceSize));
			stats.storeSize(name, norm);
		}
		for (Map.Entry<String,Vector<Double>> nameRatios : nameToRatios.entrySet()) {
			String name = nameRatios.getKey();
			Vector<Double> ratios = nameRatios.getValue();
			LogNormalDistribution norm = new LogNormalDistribution(ratios);
			norm.setVariance(Math.max(norm.getVariance(), minVarianceRatio));
			stats.storeRatio(name, norm);
		}
		for (Map.Entry<String,Integer> nameFreq : nameToFreq.entrySet()) {
			String name = nameFreq.getKey();
			Integer freq = nameFreq.getValue();
			double p = freq * 1.0 / n;
			stats.storeFreq(name, p);
		}
	}

	// Save existing stats file.
	public void save() {
		try {
			stats.save();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}

