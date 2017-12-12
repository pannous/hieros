package nederhof.ocr.admin;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.util.math.*;
import nederhof.util.xml.*;

// Gathers statistics on glyphs.
public class GlyphStats {

	// Directory of prototypes.
	protected File dir;

	// Name of index file in prototype directory.
	protected final String INDEX = "index.xml";
	// Name of temporary index file while index file is being overwritten.
	protected final String TEMP_INDEX = INDEX + "~";

	// Maps name to (normal distribution of) size (= height relative to unit).
	protected HashMap<String,LogNormalDistribution> nameToSize;
	// Maps name to (normal distribution of) width/height ratio.
	protected HashMap<String,LogNormalDistribution> nameToRatio;
	// Maps name to relative frequency.
	protected HashMap<String,Double> nameToRelFreq;
	// Relates names, the first being preferred to the second. This is used where two
	// signs are not (easily) distinguishable based on appearance. OCR will
	// only look for the first glyph, and present the second as alternative.
	protected Vector<NamePair> preferNames;

    // Read index file (if exists) with information on glyphs.
	// Ignore if doesn't exist.
	public GlyphStats(File dir) throws IOException {
		this.dir = dir;
		clearNameToSize();
		clearNameToRatio();
		clearNameToRelFreq();
		clearPreferNames();
		readFile();
	}

	public void clearNameToSize() {
		nameToSize = new HashMap<String,LogNormalDistribution>();
	}
	public void clearNameToRatio() {
		nameToRatio = new HashMap<String,LogNormalDistribution>();
	}
	public void clearNameToRelFreq() {
		nameToRelFreq = new HashMap<String,Double>();
	}
	public void clearPreferNames() {
		preferNames = new Vector<NamePair>();
	}

	public HashMap<String,LogNormalDistribution> getNameToSize() {
		return nameToSize;
	}
	public HashMap<String,LogNormalDistribution> getNameToRatio() {
		return nameToRatio;
	}
	public HashMap<String,Double> getNameToRelFreq() {
		return nameToRelFreq;
	}
	public Vector<NamePair> getPreferNames() {
		return preferNames;
	}

	public LogNormalDistribution getSize(String name) {
		if (getNameToSize().containsKey(name))
			return getNameToSize().get(name);
		else
			return new LogNormalDistribution();
	}
	public LogNormalDistribution getRatio(String name) {
		if (getNameToRatio().containsKey(name))
			return getNameToRatio().get(name);
		else
			return new LogNormalDistribution();
	}
	public double getRelFreq(String name) {
		if (getNameToRelFreq().containsKey(name))
			return getNameToRelFreq().get(name);
		else
			return 0.00001;
	}

	///////////////////////////////////////
	// Reading.

	protected void readFile() throws IOException {
        File index = new File(dir, INDEX);
        if (!index.exists())
            ;
        else if (!index.canRead())
            throw new IOException("Cannot read paleo index: " + index);
        else {
            DocumentBuilder parser = SimpleXmlParser.construct(false, false);
            InputStream in = new FileInputStream(index);
            try {
                Document doc = parser.parse(in);
                processIndex(doc);
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
            in.close();
        }
	}

    // Process index.
    protected void processIndex(Document doc) throws IOException {
        processSize(doc);
        processRatio(doc);
        processFreq(doc);
        processPrefer(doc);
    }

    protected void processSize(Document doc) throws IOException {
        NodeList sizes = doc.getElementsByTagName("size");
        for (int i = 0; i < sizes.getLength(); i++) {
            Element size = (Element) sizes.item(i);
            String name = size.getAttribute("name");
			LogNormalDistribution normal = processNormal(size);
			storeSize(name, normal);
        }
    }
	public void storeSize(String name, LogNormalDistribution normal) {
		nameToSize.put(name, normal);
	}

    protected void processRatio(Document doc) throws IOException {
        NodeList ratios = doc.getElementsByTagName("ratio");
        for (int i = 0; i < ratios.getLength(); i++) {
            Element ratio = (Element) ratios.item(i);
            String name = ratio.getAttribute("name");
			LogNormalDistribution normal = processNormal(ratio);
			storeRatio(name, normal);
        }
    }
	public void storeRatio(String name, LogNormalDistribution normal) {
		nameToRatio.put(name, normal);
	}

    protected void processFreq(Document doc) throws IOException {
        NodeList freqs = doc.getElementsByTagName("freq");
        for (int i = 0; i < freqs.getLength(); i++) {
            Element freq = (Element) freqs.item(i);
            String name = freq.getAttribute("name");
            String pStr = freq.getAttribute("p");
			try {
				double p = Double.parseDouble(pStr);
				storeFreq(name, p);
			} catch (NumberFormatException e) {
				throw new IOException(e.getMessage());
			}
        }
    }
	public void storeFreq(String name, Double p) {
		nameToRelFreq.put(name, p);
	}

    protected void processPrefer(Document doc) throws IOException {
        NodeList prefers = doc.getElementsByTagName("prefer");
        for (int i = 0; i < prefers.getLength(); i++) {
            Element prefer = (Element) prefers.item(i);
            String more = prefer.getAttribute("more");
            String less = prefer.getAttribute("less");
			storePrefer(more, less);
        }
    }
	protected void storePrefer(String more, String less) {
		preferNames.add(new NamePair(more, less));
	}

	protected LogNormalDistribution processNormal(Element elem) throws IOException {
		String meanStr = elem.getAttribute("mean");
		String varianceStr = elem.getAttribute("variance");
		try {
			double mean = Double.parseDouble(meanStr);
			double variance = Double.parseDouble(varianceStr);
			double fallbackVariance = 0.5;
			return new LogNormalDistribution(mean, variance, fallbackVariance);
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////////////////////////
	// Writing.

	// First write to temporary, before overwriting original file.
	public void save() throws IOException {
		File temp = new File(dir, TEMP_INDEX);
		save(temp);
		save(new File(dir, INDEX));
		temp.delete();
	}

	// Write to file.
    protected void save(File f) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element paleoEl = doc.createElement("paleo");
            getSizes(doc, paleoEl);
            getRatios(doc, paleoEl);
            getFreqs(doc, paleoEl);
            getPrefers(doc, paleoEl);
            doc.appendChild(paleoEl);
            XmlPretty.print(doc, f);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    protected void getSizes(Document doc, Element paleoEl) throws IOException {
        for (Map.Entry<String,LogNormalDistribution> entry : nameToSize.entrySet()) {
            String name = entry.getKey();
            LogNormalDistribution normal = entry.getValue();
            Element sizeEl = doc.createElement("size");
            sizeEl.setAttribute("name", name);
            sizeEl.setAttribute("mean", doubleStr(normal.getMean()));
            sizeEl.setAttribute("variance", doubleStr(normal.getVariance()));
            paleoEl.appendChild(sizeEl);
        }
    }

    protected void getRatios(Document doc, Element paleoEl) throws IOException {
        for (Map.Entry<String,LogNormalDistribution> entry : nameToRatio.entrySet()) {
            String name = entry.getKey();
            LogNormalDistribution normal = entry.getValue();
            Element ratioEl = doc.createElement("ratio");
            ratioEl.setAttribute("name", name);
            ratioEl.setAttribute("mean", doubleStr(normal.getMean()));
            ratioEl.setAttribute("variance", doubleStr(normal.getVariance()));
            paleoEl.appendChild(ratioEl);
        }
	}

    protected void getFreqs(Document doc, Element paleoEl) throws IOException {
        for (Map.Entry<String,Double> entry : nameToRelFreq.entrySet()) {
            String name = entry.getKey();
            Double p = entry.getValue();
            Element freqEl = doc.createElement("freq");
            freqEl.setAttribute("name", name);
            freqEl.setAttribute("p", doubleStr(p));
            paleoEl.appendChild(freqEl);
        }
	}

    protected void getPrefers(Document doc, Element paleoEl) throws IOException {
        for (NamePair pair : preferNames) {
			String more = pair.first;
			String less = pair.second;
			Element preferEl = doc.createElement("prefer");
			preferEl.setAttribute("more", more);
			preferEl.setAttribute("less", less);
			paleoEl.appendChild(preferEl);
		}
	}

	protected String doubleStr(double d) {
		return String.format("%4f", d);
	}

}
