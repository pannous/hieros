package nederhof.ocr.hiero.admin;

import java.io.*;
import java.net.*;
import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.*;

// Do experiments with accuracy of OCR of hieroglyphic.
public class Experiments {

	private Statistics stats = new Statistics();
	private ExclusiveStatistics exStats = new ExclusiveStatistics();

	public Experiments() {
	}

	// Two confused signs.
	private static class Confusion implements Comparable<Confusion> {
		public String correct;
		public String guessed;
		public Confusion(String correct, String guessed) {
			this.correct = correct;
			this.guessed = guessed;
		}
		public int compareTo(Confusion other) {
			if (correct.compareTo(other.correct) != 0)
				return correct.compareTo(other.correct);
			else
				return guessed.compareTo(other.guessed);
		}
		public boolean equals(Object obj) {
			if (obj instanceof Confusion) {
				Confusion other = (Confusion) obj;
				return correct.equals(other.correct) && guessed.equals(other.guessed);
			} else
				return false;
		}
	}

	// Record statistics related to accuracy.
	private static class Statistics {
		// How many signs considered.
		public int nSigns = 0;
		// How many signs guessed correctly.
		public int nRecognized = 0;
		// For how many signs is correct among top two.
		public int nSecondGuessed = 0;
		// For sign pair, how often wrong.
		public TreeMap<Confusion, Integer> confusion = new TreeMap<Confusion,Integer>();

		public void addRecognition(String correct, String guessed, String second) {
			nSigns++;
			if (guessed.equals(correct)) {
				nRecognized++;
				nSecondGuessed++;
			} else {
				if (second.equals(correct))
					nSecondGuessed++;
				Confusion conf = new Confusion(correct, guessed);
				if (confusion.get(conf) == null)
					confusion.put(conf, 0);
				confusion.put(conf, confusion.get(conf) + 1);
			}
		}
		public double getAccuracy() {
			if (nSigns > 0)
				return 1.0 * nRecognized / nSigns;
			else
				return 0;
		}
		public double getSecondAccuracy() {
			if (nSigns > 0)
				return 1.0 * nSecondGuessed / nSigns;
			else
				return 0;
		}
		public int getNWrong() {
			return nSigns - nRecognized;
		}

		public List<Confusion> topConfusions(int n) {
			List<Confusion> confusionList = new LinkedList<Confusion>(confusion.keySet());
			Collections.sort(confusionList, new Comparator<Confusion>() {
					public int compare(Confusion c1, Confusion c2) {
						return confusion.get(c2) - confusion.get(c1);
					}
			});
			return confusionList.subList(0, Math.min(n, confusionList.size()));
		}
	}

	// As above, but ignoring Z1 and Z15.
	private static class ExclusiveStatistics extends Statistics {
		public void addRecognition(String correct, String guessed, String second) {
			if (!correct.equals("Z1") && !correct.equals("Z15"))
				super.addRecognition(correct, guessed, second);
		}
	}

	// Measure accuracy for texts with prototypes.
	private void measureRecognitionAccuracy(String protoDir, 
			String[] testFiles) {
		HieroGuesser guesser = null;
		try {
			guesser = new HieroGuesser(protoDir);
			guesser.setBeam(20);
			guesser.setCandidates(5);
		} catch (IOException e) {
			System.err.println("Cannot construct HieroGuesser");
			System.exit(0);
		}
		stats = new Statistics();
		exStats = new ExclusiveStatistics();
		for (int i = 0; i < testFiles.length; i++) {
			String testFile = testFiles[i];
			System.out.println("Testing " + testFile);
			try {
				measureRecognitionAccuracy(guesser, testFile);
			} catch (IOException e) {
				System.err.println("Cannot open project " + testFile);
			}
		}
		System.out.println("Prototypes from " + protoDir);
		System.out.println("Out of " + stats.nSigns + " correct " + 
				stats.nRecognized + " or " + percent(stats.getAccuracy()));
		System.out.println("Among best two is " + percent(stats.getSecondAccuracy()));
		   System.out.println("Most often confused:");
		for (Confusion confusion : stats.topConfusions(20)) {
			int nConfused = stats.confusion.get(confusion);
			double percConfused = 1.0 * nConfused / stats.getNWrong();
			System.out.println("" + confusion.correct + " interpreted as " +
			confusion.guessed + " (" + nConfused + " times or " + percent(percConfused));
		}
		System.out.println("Without Z1/Z15, out of " + exStats.nSigns + " correct " + 
				exStats.nRecognized + " or " + percent(exStats.getAccuracy()));
		System.out.println("Among best two is " + percent(exStats.getSecondAccuracy()));
	}

	// Measure accuracy for text.
	private void measureRecognitionAccuracy(HieroGuesser guesser, String testFile) throws IOException {
		File testDir = new File(testFile);
		Project project = new HieroProject(testDir);
		for (Map.Entry<String,Page> entry : project.pages.entrySet()) {
			String pageName = entry.getKey();
			Page page = entry.getValue();
			System.out.println("  page " + pageName);
			measureRecognitionAccuracy(guesser, page);
		}
	}

	// Measure accuracy for page.
	private void measureRecognitionAccuracy(HieroGuesser guesser, Page page) throws IOException {
		for (Line line : page.lines) 
			measureRecognitionAccuracy(guesser, line);
	}

	// Measure accuracy for line.
	private void measureRecognitionAccuracy(HieroGuesser guesser, Line line) throws IOException {
		LayoutAnalyzer analyzer = new HieroLayoutAnalyzer();
		int unit = analyzer.predictUnitSize(line.aliveGlyphs());
		for (Blob glyph : line.aliveGlyphs()) {
			float relativeHeight = unit < 0 ? -1 : 1.0f * glyph.height() / unit;
			Vector<String> names =
				guesser.findBestNames(glyph.im(), relativeHeight);
			names = nonPartNames(names);
			String name = glyph.getName();
			String guessed = names.size() > 0 ? names.get(0) : "none";
			String secondGuess = names.size() > 1 ? names.get(1) : "none";
			// System.out.print(name);
			// System.out.println(" " + guessed);
			stats.addRecognition(name, guessed, secondGuess);
			exStats.addRecognition(name, guessed, secondGuess);
		}
	}

	////////////////////////////////////////
	// Auxiliary.

	// Remove part-of names.
	private Vector<String> nonPartNames(Vector<String> names) {
		Vector<String> nonParts = new Vector<String> ();
		for (String name : names) 
			if (!name.matches(".*\\[part\\]"))
				nonParts.add(name);
		return nonParts;
	}

	// Turn double to percent string.
	private String percent(double d) {
		return String.format("%.2f", d * 100) + " %";
	}

	// Main.
	public static void main(String[] args) {
		Experiments experiments = new Experiments();
		String protoDir = "paleo/sethe";
		String avDir = "paleo/av";
		String centerDir = "paleo/center";
		String bestDir = "paleo/best";
		String[] testFiles = new String[]{
			// "workinprogress/urk/urkIV-001",
			// "workinprogress/urk/urkIV-002",
			// "workinprogress/urk/urkIV-003",
			// "workinprogress/urk/urkIV-004"
			"workinprogress/urk/urkIV-026",
			"workinprogress/urk/urkIV-027",
			"workinprogress/urk/urkIV-028",
			"workinprogress/urk/urkIV-029",
			"workinprogress/urk/urkIV-030",
			"workinprogress/urk/urkIV-031",
			"workinprogress/urk/urkIV-032"
			};
		experiments.measureRecognitionAccuracy(protoDir, testFiles);
		// experiments.measureRecognitionAccuracy(avDir, testFiles);
		// experiments.measureRecognitionAccuracy(bestDir, testFiles);
		// experiments.measureRecognitionAccuracy(centerDir, testFiles);
	}
}
