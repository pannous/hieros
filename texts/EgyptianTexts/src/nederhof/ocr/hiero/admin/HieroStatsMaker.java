package nederhof.ocr.hiero.admin;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.hiero.*;
import nederhof.ocr.images.*;

public class HieroStatsMaker extends StatsMaker {

	// Constructor.
	public HieroStatsMaker(File dir) throws IOException {
		super(dir);
	}

	protected LayoutAnalyzer createAnalyzer() {
		return new HieroLayoutAnalyzer();
	}

	protected GlyphStats createStats(File dir) throws IOException {
		return new GlyphStats(dir);
	}

	protected Project createProject(String file) throws IOException {
		return new HieroProject(new File(file));
	}

	// Overrides superclass.
	protected void gatherFromParts(BinaryImage im, String name, int unit) {
		Vector<Vector<Point>> comps = ImageComponents.find(im);
		if (comps.size() > 1) {
			for (Vector<Point> comp :  comps) {
				BinaryImage partIm = ImageComponents.constructImage(comp);
				gatherFrom(partIm, name + "[part]", unit);
			}
		}
	}

	public static void main(String[] args) {
		Vector<String> files = new Vector<String>();
		files.add("workinprogress/urk/urkIV-001");
		files.add("workinprogress/urk/urkIV-002");
		files.add("workinprogress/urk/urkIV-003");
		files.add("workinprogress/urk/urkIV-004");
		files.add("workinprogress/urk/urkIV-005");
		files.add("workinprogress/urk/urkIV-006");
		files.add("workinprogress/urk/urkIV-007");
		files.add("workinprogress/urk/urkIV-008");
		files.add("workinprogress/urk/urkIV-009");
		files.add("workinprogress/urk/urkIV-010");
		files.add("workinprogress/urk/urkIV-011");
		files.add("workinprogress/urk/urkIV-012");
		files.add("workinprogress/urk/urkIV-013");
		files.add("workinprogress/urk/urkIV-014");
		files.add("workinprogress/urk/urkIV-015");
		files.add("workinprogress/urk/urkIV-016");
		files.add("workinprogress/urk/urkIV-017");
		files.add("workinprogress/urk/urkIV-018");
		files.add("workinprogress/urk/urkIV-019");
		files.add("workinprogress/urk/urkIV-020");
		files.add("workinprogress/urk/urkIV-021");
		files.add("workinprogress/urk/urkIV-022");
		files.add("workinprogress/urk/urkIV-023");
		files.add("workinprogress/urk/urkIV-024");
		files.add("workinprogress/urk/urkIV-025");
		try {
			HieroStatsMaker maker = new HieroStatsMaker(new File("paleo/sethe"));
			maker.gatherFrom(files);
			maker.save();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}

