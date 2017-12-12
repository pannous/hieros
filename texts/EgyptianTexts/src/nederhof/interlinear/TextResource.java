/***************************************************************************/
/*                                                                         */
/*  TextResource.java                                                      */
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

// A resource consists of properties and a series of phrases.
// This is an abstract class, and subclasses should determine
// allowable properties and phrases.

package nederhof.interlinear;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public abstract class TextResource {

    // Versions are numbered while resource is being edited.
    private int version = 0;

    public int getVersion() {
	return version;
    }

    ////////////////////////////
    // Storage.

    // The file where resource is found. This is URI.
    protected String location;

    public void setLocation(String location) {
	this.location = location;
    }

    public String getLocation() {
        return location;
    }

    // Is location editable?
    protected boolean editable = false;

    protected void detectEditable() {
        editable = false;
        try {
            if (!location.startsWith("jar")) {
                File file = new File(location);
                editable = file.canWrite();
            }
        } catch (SecurityException e) {
            // ignore
        }
    }

    // Get editable.
    public boolean isEditable() {
	return editable;
    }

    // Write to file.
    // First write to temporary, before overwriting original file.
    public void save() throws IOException {
        File temp = new File(location + "~");
        write(temp);
        write(new File(location));
        temp.delete();
    }

    // Write to file.
    protected void write(File outFile) throws IOException {
        PrintWriter out = new Utf8FileWriter(outFile);
        write(out);
        out.close();
    }

    // Write to file.
    protected abstract void write(PrintWriter out) throws IOException;

    // Write to XML file.
    protected abstract void writeXml(PrintWriter out) throws IOException;

    // Write as XML to file above, but without upload information, if any. 
    protected void writeXmlExclusive(PrintWriter out) throws IOException {
	writeXml(out);
    }

    // Get file as string, exclusive of upload information.
    protected String exclusiveString() throws IOException {
	StringWriter swriter = new StringWriter();
	PrintWriter out = new PrintWriter(swriter);
	writeXmlExclusive(out);
	String str = swriter.toString();
	out.close();
	return str;
    }

    // Move resource to different location.
    public void moveTo(File newLoc) throws IOException {
        File oldLoc = new File(location);
        if (newLoc.equals(oldLoc))
            ; // ignore
        else if (newLoc.exists())
            throw new IOException("Target file exists: " + newLoc.getPath());
        else if (!allowableName(newLoc.getName()))
            throw new IOException("Text file name has wrong extension");
        else
            try {
                location = newLoc.getPath();
		write(newLoc);
                oldLoc.delete();
            } catch (FileNotFoundException e) {
                throw new IOException(e.getMessage());
            }
    }

    // Is filename allowed? May be overridden in subclass.
    protected boolean allowableName(String name) {
	return FileAux.hasExtension(name, "xml");
    }

    ////////////////////////////
    // Properties.

    // Enumeration of properties. 
    // Usually there will be properties "name", "labelname",
    // "created" and "modified".
    protected String[] propertyNames;

    // Number of properties.
    public int nProperties() {
	return propertyNames.length;
    }

    // Get name of property.
    public String propertyName(int i) {
	return propertyNames[i];
    }

    // Properties, mapping from Strings to other objects,
    // usually String, or having a String representation.
    protected TreeMap propertyValues = new TreeMap();

    // Set property initially. Do not modify date.
    public void initProperty(String prop, Object value) {
	propertyValues.put(prop, value);
    }

    // Set property. Implicitly update modified date.
    public void setProperty(String prop, Object value) {
	propertyValues.put(prop, value);
	makeModified();
    }

    // Adjust modified date.
    public void makeModified() {
	propertyValues.put("modified", getDate());
	version++;
    }

    // Properties.
    public Object getProperty(String prop) {
	return propertyValues.get(prop);
    }

    // Get property value as string.
    public String getStringProperty(String prop) {
	String v = (String) getProperty(prop);
	return v == null ? "" : v;
    }

    // Get property value as string, with XML escapes.
    public String getEscapedProperty(String prop) {
	return XmlAux.escape(getStringProperty(prop));
    }

    ////////////////////////////
    // Derived properties.

    // There may be separate property "name",
    // or a subclass may redefine it to be combination
    // of other properties.
    public String getName() {
	String propName = (String) getProperty("name");
	if (propName != null && !propName.equals(""))
	    return propName;
	else
	    return "???";
    }
    // There may be separate property "labelname",
    // or a subclass may redefine it to be combination
    // of other properties.
    public String getLabelname() {
	String propName = (String) getProperty("labelname");
	if (propName != null && !propName.equals(""))
	    return propName;
	else
	    return "???";
    }

    // Preamble as panel, derived from properties.
    public abstract Component preamble();

    ////////////////////////////
    // PDF.

    // Preamble of resource in vector of PDF parts.
    // By default nothing.
    public Vector pdfPreamble(PdfRenderParameters params) {
	return new Vector();
    }

    ////////////////////////////
    // Editing of properties.

    // The editor of properties.
    private PropertiesEditor editor;

    // Get editor of properties. Make it only once.
    public PropertiesEditor editor(EditChainElement parent) {
	if (editor == null)
	    editor = makeEditor(parent);
	return editor;
    }

    // Make editor of properties. To be set by subclass.
    protected abstract PropertiesEditor makeEditor(EditChainElement parent);

    ////////////////////////////
    // Tiers.

    // Enumeration of possible tiers.
    private String[] tierNames;

    // Set tier names, To be called by subclass.
    protected void setTierNames(String[] names) {
	tierNames = names;
	prepareTiers();
    }

    // Get number of tiers.
    public int nTiers() {
	return tierNames == null ? 0 : tierNames.length;
    }

    // Get name of tier.
    public String tierName(int i) {
	return tierNames[i];
    }

    ////////////////////////////
    // Phrases.

    // Produce new empty phrase.
    public TextPhrase phrase() {
	return new TextPhrase(this);
    }

    // Produce empty phrase with initial text.
    public TextPhrase phrase(Vector<ResourcePart>[] tiers) {
	return new TextPhrase(this, tiers);
    }

    // Phrases.
    protected Vector<TextPhrase> phrases = new Vector<TextPhrase>();

    // Get number of phrases.
    public int nPhrases() {
	return phrases.size();
    }

    // Get phrase.
    public TextPhrase getPhrase(int i) {
	return phrases.get(i);
    }

    // Replace phrase.
    public void setPhrase(TextPhrase phrase, int i) {
	phrases.set(i, phrase);
    }

    // Remove phrase from index.
    public void removePhrase(int i) {
	phrases.remove(i);
    }

    // Remove all phrases.
    public void clearPhrases() {
	phrases.clear();
    }

    // Insert phrase before index.
    public void insertPhrase(TextPhrase phrase, int i) {
	phrases.insertElementAt(phrase, i);
    }

    // Add phrase at end.
    public void addPhrase(TextPhrase phrase) {
	phrases.add(phrase);
    }

    /////////////////////////////////////////////
    // Precedence between named positions.

    protected PosPrecedence precedence = new PosPrecedence();

    // Add or remove.
    public void addPrecedence(String id1, String type1, 
	    String id2, String type2) {
	precedence.add(id1, type1, id2, type2);
    }
    public void removePrecedence(String id1, String id2) {
	precedence.remove(id1, id2);
    }

    // Name of label for position in phrase of tier.
    // Return null if failed. If labelled position does not exist, create one.
    public String positionId(int phraseNum, int tierNum, int pos, boolean mayCreate) {
	TextPhrase phrase = getPhrase(phraseNum);
	Vector<ResourcePart> tier = phrase.getTier(tierNum);
	return positionId(tier, pos, mayCreate);
    }

    // Name of label for position in phrase of tier.
    // By default null, unless implementation is provided by subclass.
    public String positionId(Vector<ResourcePart> tier, int pos, boolean mayCreate) {
	return null;
    }

    // Name of label for position in phrase of tier, plus an offset.
    // If labelled position does not exist, create one if possible.
    // The wrapped boolean indicates whether a label was added.
    public TreeSet<LabelOffset> positionIdOffset(int phraseNum, int tierNum, int pos,
	    boolean mayCreate, boolean all, WrappedBool changed) {
	TextPhrase phrase = getPhrase(phraseNum);
	Vector<ResourcePart> tier = phrase.getTier(tierNum);
	TreeSet posIds = positionIdOffset(tier, pos, mayCreate, changed);

	int phraseNumBelow = phraseNum;
	int posBelow = pos;
	int nPhrases = 1;
	while ((posIds.isEmpty() || all) && phraseNumBelow > 0) {
	    phraseNumBelow--;
	    phrase = getPhrase(phraseNumBelow);
	    tier = phrase.getTier(tierNum);
	    posBelow += nSymbols(tier) + 1;
	    nPhrases++;
	    posIds.addAll(positionIdOffset(tier, posBelow, mayCreate, changed));
	}
	if (posIds.isEmpty() || all) 
	    posIds.add(new LabelOffset("", posBelow + 1));
	    // This is needed to account for PhraseSeparators during editing:
	    // -nPhrases.
	    // However, there is no solution yet for non-editing mode. Needs to be
	    // corrected.

	int phraseNumAbove = phraseNum;
	int posAbove = pos;
	while ((posIds.isEmpty() || all) && phraseNumAbove < nPhrases() - 1) {
	    posAbove -= nSymbols(tier) + 1;
	    phraseNumAbove++;
	    phrase = getPhrase(phraseNumAbove);
	    tier = phrase.getTier(tierNum);
	    posIds.addAll(positionIdOffset(tier, posAbove, mayCreate, changed));
	}

	return posIds;
    }

    // Name of label for position in phrase of tier, plus an offset.
    // If this method is used, the implementation should be
    // provided by subclass.
    public TreeSet positionIdOffset(Vector<ResourcePart> tier, int pos, 
	    boolean mayCreate, WrappedBool changed) {
	return null;
    }

    // Number of symbols in vector of parts of tiers.
    private static int nSymbols(Vector<ResourcePart> tier) {
	int n = 0;
	for (int i = 0; i < tier.size(); i++) {
	    Object o = tier.get(i);
	    if (o instanceof ITierPart) {
		ITierPart part = (ITierPart) o;
		n += part.nSymbols();
	    }
	}
	return n;
    }

    ////////////////////////////
    // Use of tiers.

    // Tier can be in following modes:
    // shown (normally viewed)
    // ignored (entirely ignored)
    // omitted (taken into account for alignment of other tiers, 
    // but not shown)
    // erased (text not shown, for exercises)
    public static final String SHOWN = "shown";
    public static final String IGNORED = "ignored";
    public static final String OMITTED = "omitted";
    public static final String ERASED = "erased";
    // All valid modes.
    protected final String[] validModes = 
	{ SHOWN, IGNORED, OMITTED, ERASED };

    // For each tier, whether it is used and if so how.
    protected String[] modes;

    // Make mode for viewing.
    public void setMode(int i, String mode) {
	modes[i] = mode;
    }

    // Get mode of viewing.
    public String getMode(int i) {
	return modes[i];
    }

    // Is mode of viewing the given one?
    public boolean hasMode(int i, String mode) {
	return modes[i].equals(mode);
    }

    // Is valid mode?
    public boolean isValidMode(String mode) {
	for (int i = 0; i < validModes.length; i++) 
	    if (validModes[i].equals(mode))
		return true;
	return false;
    }

    // Modes other than ignore.
    public Vector<String> nonIgnoreModes() {
	Vector<String> nonIgnoreModes = new Vector<String>();
	for (int i = 0; i < validModes.length; i++)
	    if (!validModes[i].equals(IGNORED))
		nonIgnoreModes.add(validModes[i]);
	return nonIgnoreModes;
    }

    // Prepare modes.
    private void prepareTiers() {
	modes = new String[nTiers()];
	for (int i = 0; i < nTiers(); i++) {
	    modes[i] = SHOWN;
	}
    }

    // Is the tier empty?
    public boolean isEmptyTier(int i) {
	for (int k = 0; k < nPhrases(); k++) {
	    TextPhrase phrase = (TextPhrase) phrases.get(k);
	    if (!phrase.isEmptyTier(i))
		return false;
	}
	return true;
    }

    // Translate phrases into a number of tiers and add them.
    // Default is do nothing.
    public void addTiers(Vector<Tier> tiers, Vector<Integer> tierNums, 
	    Vector<String> labels, Vector<String> versions,
	    Vector<Vector<Integer>> phraseStarts,
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPositions, 
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPrePositions, 
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPostPositions, 
	    TreeMap<ResourceId,int[]> resourceIdToPositions,
	    TreeMap<VersionSchemeLabel,VersionSchemeLabel> schemeMappings,
	    RenderParameters params, 
	    boolean pdf, boolean edit) {
	// nothing
    }

    // Get editors for tiers within phrase.
    // If no editors should be made, return null.
    public Vector getEditors(TextPhrase phrase) {
	return null;
    }

    // Get editor for entire resource.
    // If none, then return null.
    // Either getEditors or getEditor should return null.
    public ResourceEditor getEditor(int currentPhrase) {
	return null;
    }

    // Get empty phrase. For most resources, this will consist
    // of empty tiers. But some resources may require special
    // structure of empty tier.
    public TextPhrase emptyPhrase() {
	Vector[] tiers = new Vector[nTiers()];
	for (int i = 0; i < nTiers(); i++)
	    tiers[i] = new Vector();
	TextPhrase empty = new TextPhrase(this, tiers);
	return empty;
    }

    // Join two phrases. Return null if not possible.
    // Subclass to define appropriately.
    public TextPhrase joinPhrases(TextPhrase phrase1, TextPhrase phrase2) {
	return null;
    }

    // Cut phrase in i-th tier into two parts at position.
    // Subclass to define appropriately.
    public void cutPhrase(int i, 
	    Vector original, int pos, Vector left, Vector right) {
    }

    ///////////////////////////////////////////////////////
    // For some text resources, there can be individual viewer.

    // For most there is none.
    public boolean hasViewer() {
	return false;
    }
    // Create viewer.
    public ResourceViewer getViewer() {
	return null;
    }

    ///////////////////////////////////////////////////////
    // Uploading.

    // Upload. The default is do nothing.
    public String upload() throws IOException {
	return "nothing uploaded";
    }

    // Can be uploaded? The default is no.
    public boolean uploadable() {
	return false;
    }

    ///////////////////////////////////////////////////////
    // Auxiliaries.

    // Get current date.
    public static String getDate() {
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(cal.getTime());
    }

}
