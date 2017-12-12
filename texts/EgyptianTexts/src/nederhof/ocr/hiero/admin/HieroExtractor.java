package nederhof.ocr.hiero.admin;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

import nederhof.ocr.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.*;
import nederhof.ocr.images.*;
import nederhof.ocr.images.distance.*;

// Extracts prototypes from hand-corrected projects.
public class HieroExtractor {

	// The distortion model used.
	private DistortionModel distModel = new IDM();

	// Maps signs to averaged images.
	protected TreeMap<String,DelayedAveragedImage> averages = 
			new TreeMap<String,DelayedAveragedImage>();

	// Blob and distance to averaged image.
	private class DistanceRecord {
		public Blob blob;
		public float dist;
		public DistanceRecord(Blob blob, float dist) {
			this.blob = blob;
			this.dist = dist;
		}
	}

	// Maps signs to best blob (closest to averaged image).
	protected TreeMap<String,DistanceRecord> closestBlobs =
			new TreeMap<String,DistanceRecord>();

	// From paths of projects, extract averages to be prototypes. Put them in target.
	public void extractAveragedFrom(String[] projects, String target) {
		computeAveraged(projects);
		storeAveraged(target);
	}
	// As above, but with different kind of averaging.
	public void extractCenterAveragedFrom(String[] projects, String target) {
		computeAveraged(projects);
		storeCenterAveraged(target);
	}
	// From paths of projects, extract best prototypes. Put them in target.
	public void extractBestFrom(String[] projects, String target) {
		computeAveraged(projects);
		storeClosestToAveraged(projects, target);
	}

	private void computeAveraged(String[] projects) {
		for (int i = 0; i < projects.length; i++) {
			String projectName = projects[i];
			System.out.println("Exploring " + projectName);
			try {
				computeAveraged(projectName);
			} catch (IOException e) {
				System.err.println("Cannot open project " + projectName);
			}
		}
	}
    // For project.
    private void computeAveraged(String projectName) throws IOException {
        File projectDir = new File(projectName);
        Project project = new HieroProject(projectDir);
        for (Map.Entry<String,Page> entry : project.pages.entrySet()) {
            String pageName = entry.getKey();
            Page page = entry.getValue();
            System.out.println("  page " + pageName);
            computeAveraged(page);
        }
    }
    // For page.
    private void computeAveraged(Page page) throws IOException {
        for (Line line : page.lines)
            computeAveraged(line);
    }
    // For line.
    private void computeAveraged(Line line) throws IOException {
        for (Blob glyph : line.aliveGlyphs()) {
            String name = glyph.getName();
			BinaryImage im = glyph.im();
			if (averages.get(name) == null)
				averages.put(name, new DelayedAveragedImage(im));
			else
				averages.get(name).add(im);
        }
    }

	// Store averaged images in directory.
	private void storeAveraged(String dirName) {
		File dir = new File(dirName);
		removeImages(dir);
		for (Map.Entry<String,DelayedAveragedImage> entry : averages.entrySet()) {
			String name = entry.getKey();
			DelayedAveragedImage av = entry.getValue();
			try {
				File target = new File(dir, name + "-" + 0 + ".png");
				BufferedImage buffered = av.averaged().toBufferedImage();
				ImageIO.write(buffered, "png", target);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	// As above, but center-averaged.
	private void storeCenterAveraged(String dirName) {
		File dir = new File(dirName);
		removeImages(dir);
		for (Map.Entry<String,DelayedAveragedImage> entry : averages.entrySet()) {
			String name = entry.getKey();
			DelayedAveragedImage av = entry.getValue();
			try {
				File target = new File(dir, name + "-" + 0 + ".png");
				BufferedImage buffered = av.centerAveraged().toBufferedImage();
				ImageIO.write(buffered, "png", target);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private void storeClosestToAveraged(String[] projects, String dirName) {
		for (int i = 0; i < projects.length; i++) {
			String projectName = projects[i];
			System.out.println("Revisit " + projectName);
			try {
				findClosest(projectName);
			} catch (IOException e) {
				System.err.println("Cannot open project " + projectName);
			}
		}
		storeClosest(dirName);
	}
	private void findClosest(String projectName) throws IOException {
        File projectDir = new File(projectName);
        Project project = new HieroProject(projectDir);
        for (Map.Entry<String,Page> entry : project.pages.entrySet()) {
            String pageName = entry.getKey();
            Page page = entry.getValue();
            System.out.println("  page " + pageName);
            findClosest(page);
        }
    }
    // For page.
    private void findClosest(Page page) throws IOException {
        for (Line line : page.lines)
            findClosest(line);
    }
    // For line.
    private void findClosest(Line line) throws IOException {
        for (Blob glyph : line.aliveGlyphs()) {
            String name = glyph.getName();
            BinaryImage im = glyph.im();
			BinaryImage av = averages.get(name).averaged();
			im = im.scale(av.width(), av.height());
			float dist = distModel.distort(im, av);
            if (closestBlobs.get(name) == null) {
                closestBlobs.put(name, new DistanceRecord(glyph, dist));
			} else {
				DistanceRecord old = closestBlobs.get(name);
				if (old.dist > dist) {
					closestBlobs.put(name, new DistanceRecord(glyph, dist));
				}
			}
        }
    }

	// publioStore the prototype closest to the averaged.
	private void storeClosest(String dirName) {
		File dir = new File(dirName);
		removeImages(dir);
		for (Map.Entry<String,DistanceRecord> entry : closestBlobs.entrySet()) {
			String name = entry.getKey();
			DistanceRecord rec = entry.getValue();
			try {
				File target = new File(dir, name + "-" + 0 + ".png");
				BufferedImage buffered = rec.blob.im().toBufferedImage();
				ImageIO.write(buffered, "png", target);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	// Remove existing images.
	private void removeImages(File dir) {
		File[] files = dir.listFiles();
		for (File f : files)
			if (f.getName().matches(".*.png")) {
				f.delete();
			}
	}

	public static void main(String[] args) {
		String avDir = "paleo/av";
		String centerDir = "paleo/center";
		String bestDir = "paleo/best";
		HieroExtractor extr = new HieroExtractor();
		String[] projects = new String[]{
			/*
            "workinprogress/urk/urkIV-001",
            "workinprogress/urk/urkIV-002",
            "workinprogress/urk/urkIV-003",
            "workinprogress/urk/urkIV-004",
            "workinprogress/urk/urkIV-005",
			*/
            "workinprogress/urk/urkIV-006",
            "workinprogress/urk/urkIV-007",
            "workinprogress/urk/urkIV-008",
            "workinprogress/urk/urkIV-009",
            "workinprogress/urk/urkIV-010",
            "workinprogress/urk/urkIV-012",
            "workinprogress/urk/urkIV-013",
            "workinprogress/urk/urkIV-014",
            "workinprogress/urk/urkIV-015",
            "workinprogress/urk/urkIV-016",
            "workinprogress/urk/urkIV-017",
            "workinprogress/urk/urkIV-018",
            "workinprogress/urk/urkIV-019",
            "workinprogress/urk/urkIV-020",
            "workinprogress/urk/urkIV-021",
            "workinprogress/urk/urkIV-022",
            "workinprogress/urk/urkIV-023",
            "workinprogress/urk/urkIV-024",
            "workinprogress/urk/urkIV-025",
            "workinprogress/urk/urkIV-026",
            "workinprogress/urk/urkIV-027",
            "workinprogress/urk/urkIV-028"
            // "workinprogress/urk/urkIV-029"
		};
		extr.extractCenterAveragedFrom(projects, centerDir);
		extr.extractAveragedFrom(projects, avDir);
		// extr.extractBestFrom(projects, bestDir);
	}

}

