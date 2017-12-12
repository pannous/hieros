package nederhof.ocr.hiero;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.ocr.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

// Turns project into hieroglyphic image resource.
public class ProjectImageExporter {

	// Always for this kind of resource.
	private static final int nTiers = 1;

	// The project.
	private Project project;

	// Name of image resource.
	private final String IMAGE_RESOURCE = "image_resource.xml";

	// Temporary structures.
	private Vector<String> images = new Vector<String>();
	private Vector<ImageSign> signs = new Vector<ImageSign>();

	public ProjectImageExporter(Project project) {
		this.project = project;
		getPages();
		writeContent();
	}

	private void getPages() {
		int i = 0;
		for (Map.Entry<String,Page> entry : project.pages.entrySet()) {
			String image = entry.getKey();
			images.add(image + ".png");
			Page page = entry.getValue();
			getLines(page, i);
			i++;
		}
	}

	private void getLines(Page page, int pageNum) {
		for (Line line : page.lines) {
			getSignPlaces(line.aliveGlyphs(), pageNum);
		}
	}

	// Pattern of Gardiner name.
	private String gardinerPat = "^([A-I]|[K-Z]|Aa|NL|NU)[0-9]+[a-z]?$";

	private void getSignPlaces(Vector<Blob> glyphs, int pageNum) {
		for (Blob glyph : glyphs) {
			String name = glyph.getName();
			if (name.matches(gardinerPat)) {
				ImagePlace place = new ImagePlace(pageNum, 
						glyph.x(), glyph.y(), glyph.width(), glyph.height());
				Vector<ImagePlace> places = new Vector<ImagePlace>();
				places.add(place);
				ImageSign sign = new ImageSign(name, places);
				signs.add(sign);
			}
		}
	}

	// Write to resource.
	private void writeContent() {
		EgyptianImage resource;
		try {
			resource = makeResource();
			if (resource == null)
				return;
			for (int i = 0; i < resource.nTiers(); i++) 
				if (resource.tierName(i).equals("signplaces"))
					resource.setMode(i, TextResource.SHOWN);
				else
					resource.setMode(i, TextResource.IGNORED);
			resource.setProperty("images", images);
			writePlaces(resource);
			resource.save();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Cannot export: " + e.getMessage(),
					"Writing error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Put places in resource.
	private void writePlaces(EgyptianImage resource) {
		int id = 0;
		for (ImageSign sign : signs) {
			Vector tier = new Vector();
			ImagePlacePart part = new ImagePlacePart(sign, "" + (id++));
			tier.add(part);
			Vector[] tiers = new Vector[nTiers];
			tiers[nTiers - 1] = tier;
			TextPhrase phrase = new TextPhrase(resource, tiers);
			resource.addPhrase(phrase);
		}
	}

	// Create resource in project directory.
	private EgyptianImage makeResource() throws IOException {
		File resourceFile = new File(project.dir(), IMAGE_RESOURCE);
		if (resourceFile.exists()) {
			if (userConfirmsLoss("Overwrite existing file" +
						resourceFile.getAbsolutePath() + "?"))
				resourceFile.delete();
			else
				return null;
		}
		return EgyptianImage.make(resourceFile);
	}

	// Ask user whether loss of data is intended.
	private boolean userConfirmsLoss(String message) {
		Object[] options = {"proceed", "cancel"};
		int answer = JOptionPane.showOptionDialog(null, message,
				"warning: impending loss of data",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[1]);
		return answer == 0;
	}

}
