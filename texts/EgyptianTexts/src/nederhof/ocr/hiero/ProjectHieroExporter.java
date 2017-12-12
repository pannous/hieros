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
import nederhof.ocr.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

// Turns project into hieroglyphic resource.
public class ProjectHieroExporter {

	// The project.
	private Project project;

	// Name of hieroglyphic resource.
	private final String HI_RESOURCE = "hiero_resource.xml";

	// Parsing context.
	private ParsingContext context = new ParsingContext();

	// All content elements.
	private Vector<LineFormat> content = new Vector<LineFormat>();

	public ProjectHieroExporter(Project project) {
		this.project = project;
		getPages();
		writeContent();
	}

	private void getPages() {
		for (Map.Entry<String,Page> entry : project.pages.entrySet()) {
			Page page = entry.getValue();
			getLines(page);
		}
	}

	private void getLines(Page page) {
		for (Line line : page.lines) 
			getFormat(line.formatted);
	}

	private void getFormat(Vector<LineFormat> formatted) {
		for (LineFormat form : formatted) {
			if (!content.isEmpty() && 
					content.lastElement() instanceof ResFormat &&
					form instanceof ResFormat) {
				ResFormat concat = resConcat((ResFormat) content.lastElement(), (ResFormat) form);
				content.set(content.size()-1, concat);
			} else 
				content.add(form);
		}
	}

	// Safe concatenation of two RES elements.
	private ResFormat resConcat(ResFormat format1, ResFormat format2) {
		String res1 = format1.getVal();
		String res2 = format2.getVal();
		ResComposer comp = new ResComposer();
		ResFragment frag1 = ResFragment.parse(res1, context);
		ResFragment frag2 = ResFragment.parse(res2, context);
		ResFragment frag3 = comp.append(frag1, frag2);
		int size1 = frag1.nGlyphs();
		ResFormat concat = new ResFormat(frag3.toString());
		TreeMap<Integer,String> notes1 = 
			(TreeMap<Integer,String>) format1.getNotes().clone();
		TreeMap<Integer,String> notes2 = format2.getNotes();
		for (Map.Entry<Integer,String> pair : notes2.entrySet())
			notes1.put(pair.getKey() + size1, pair.getValue());
		concat.setNotes(notes1);
		return concat;
	}

	// Write to resource.
	private void writeContent() {
		EgyptianResource resource;
		try {
			resource = makeResource();
			if (resource == null)
				return;
			for (int i = 0; i < resource.nTiers(); i++) 
				if (resource.tierName(i).equals("hieroglyphic"))
					resource.setMode(i, TextResource.SHOWN);
				else
					resource.setMode(i, TextResource.IGNORED);
			writeContent(resource);
			resource.save();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Cannot export: " + e.getMessage(),
					"Writing error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Ideally, we want to have a phrase consisting of a coordinate followed 
	// by hieroglyphic.
	private void writeContent(EgyptianResource resource) {
		Vector<EgyptianTierAwtPart> elems = new Vector<EgyptianTierAwtPart>();
		for (LineFormat form : content) {
			if (form.getVal().equals(""))
				continue;
			if (form instanceof ResFormat) {
				ResFormat resForm = (ResFormat) form;
				EgyptianTierAwtPart elem = new HiPart(resForm.getVal(), false);
				if (!elems.isEmpty() && elems.lastElement() instanceof HiPart) {
					resource.addPhrase(combineElems(resource, elems));
					elems.clear();
				}
				for (Map.Entry<Integer,String> pair : resForm.getNotes().entrySet()) 
					if (pair.getValue() != null && !pair.getValue().matches("\\s*")) {
						Vector<EgyptianTierAwtPart> text = new Vector<EgyptianTierAwtPart>();
						text.addAll(parsedNote(pair.getValue()));
						elems.add(new NotePart(text, pair.getKey()));
					}
				elems.add(elem);
			} else if (form instanceof NumFormat) {
				EgyptianTierAwtPart elem = new CoordPart(form.getVal());
				if (!elems.isEmpty()) {
					resource.addPhrase(combineElems(resource, elems));
					elems.clear();
				}
				elems.add(elem);
			}
		}
		if (!elems.isEmpty()) 
			resource.addPhrase(combineElems(resource, elems));
	}

	// Pattern of Gardiner name and stuff before and after.
	private Pattern gardinerPat =
					Pattern.compile("^(.*)(([A-I]|[K-Z]|Aa|NL|NU)[0-9]+[a-z]?)(.*)$");

	// In string, turn substrings that are Gardiner names into hieroglyphic.
	private Vector<EgyptianTierAwtPart> parsedNote(String raw) {
		Vector<EgyptianTierAwtPart> parts = new Vector<EgyptianTierAwtPart>();
		Matcher m = gardinerPat.matcher(raw);
		while (m.find()) {
			parts.add(new NoPart(m.group(1)));
			parts.add(new HiPart(m.group(2), true));
			raw = m.group(4);
			m = gardinerPat.matcher(raw);
		}
		parts.add(new NoPart(raw));
		return parts;
	}

	// Turn vector of elements into phrase.
	private TextPhrase combineElems(EgyptianResource resource, Vector<EgyptianTierAwtPart> elems) {
		Vector[] tiers = new Vector[resource.nTiers()];
		for (int i = 0; i < resource.nTiers(); i++) {
			tiers[i] = new Vector();
			if (resource.tierName(i).equals("hieroglyphic"))
				tiers[i].addAll(elems);
		}
		return new TextPhrase(resource, tiers);
	}

	// Create resource in project directory.
	private EgyptianResource makeResource() throws IOException {
		File resourceFile = new File(project.dir(), HI_RESOURCE);
		if (resourceFile.exists()) {
			if (userConfirmsLoss("Overwrite existing file" +
					resourceFile.getAbsolutePath() + "?"))
				resourceFile.delete();
			else
				return null;
		}
		return EgyptianResource.make(resourceFile);
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
