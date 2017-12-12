/***************************************************************************/
/*                                                                         */
/*  StreamSystem.java                                                      */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// System of streams, with information on XML files.

package nederhof.align; 

import java.awt.*;
import java.util.*;
import org.w3c.dom.*;

public final class StreamSystem {

    // For each XML file, information on: the file name, 
    // the created info, the name, etc.
    private Vector fileNames;
    private Vector createds;
    private Vector names;
    private Vector encodings;
    private Vector headers;
    private Vector bibls;
    private Vector streams;

    // Number of read files.
    public int nFiles() {
	// Invariant: fileNames.size() == createds.size() == 
	// names.size() == ..
	return fileNames.size();
    }

    // Each stream represented by linked list.
    private Vector streamToList;

    // For each stream, the ID:
    private Vector streamToId;

    // Number of stored streams.
    public int nStreams() {
	// Invariant: streamToList.size() == streamToId.size()
	return streamToList.size();
    }

    // Hash mapping stream id to number.
    private IntHashMap idToStream;

    // Record of unified versions and positions.
    private Unification uniClasses;

    // Make empty system of streams and XML files.
    // Because addFile below is called after a file is entirely processed,
    // we keep `streams' one larger, to keep ahead.
    public StreamSystem() {
	final int initNFiles = 5;
	final int initNStreams = 30;
	fileNames = new Vector(initNFiles);
	createds = new Vector(initNFiles);
	names = new Vector(initNFiles);
	encodings = new Vector(initNFiles);
	headers = new Vector(initNFiles);
	bibls = new Vector(initNFiles);
	streams = new Vector(initNFiles);
	streams.addElement(new LinkedList());
	streamToList = new Vector(initNStreams);
	streamToId = new Vector(initNStreams);
	idToStream = new IntHashMap();
	uniClasses = new Unification();
    }

    // Add XML file information, except body.
    public void addFile(String fileName, Element created, String name, 
	    String encoding, Element header, Element bibl) {
	fileNames.addElement(fileName);
	createds.addElement(created);
	names.addElement(name);
	encodings.addElement(encoding);
	headers.addElement(header);
	bibls.addElement(bibl);
	streams.addElement(new LinkedList());
    }

    // Get file name for filenumber.
    public String getFileName(int f) {
	return (String) fileNames.elementAt(f);
    }

    // Get first file name, or noname if none available
    public String getFileName() {
	if (nFiles() > 0)
	    return (String) getFileName(0);
	else
	    return "noname";
    }

    // Get created text for filenumber.
    public Element getCreated(int f) {
	return (Element) createds.elementAt(f);
    }
    public String getCreatedString(int f) {
	String created = XMLfiles.getString(getCreated(f));
	created = created.replaceFirst("^\\s*","");
	created = created.replaceFirst("\\s*$","");
	return created;
    }

    // Get name for filenumber.
    public String getName(int f) {
	return (String) names.elementAt(f);
    }

    // Get information on encoding for filenumber.
    public String getEncoding(int f) {
	return (String) encodings.elementAt(f);
    }

    // Get header text for filenumber.
    public Element getHeader(int f) {
	return (Element) headers.elementAt(f);
    }
    public String getHeaderString(int f) {
	return XMLfiles.getString(getHeader(f));
    }

    // Get bibliography for filenumber.
    public Element getBibl(int f) {
	return (Element) bibls.elementAt(f);
    }
    public String getBiblString(int f) {
	return XMLfiles.getString(getBibl(f));
    }

    // Get list of streams connected to filenumber.
    public LinkedList getFileStreams(int f) {
	return (LinkedList) streams.elementAt(f);
    }

    // Get ID belonging to stream.
    public StreamId getStreamID(int str) {
	return (StreamId) streamToId.elementAt(str);
    }

    // Add equality of versions. Find representatives, and then unify.
    public void addEqualVersion(String n1, String s1, String n2, String s2) {
	uniClasses.addEqualVersion(n1, s1, n2, s2);
    }

    // Add equality of positions to list. This list is processed later.
    public void addEqualPos(int f, String n1, String s1, String t1,
	    String n2, String s2, String t2) {
	uniClasses.addEqualPos(f, n1, s1, t1, n2, s2, t2);
    }

    // Get stream, determined by file number, version, scheme, text type.
    public LinkedList getStream(int f, String v, String s, int type) {
	StreamId id = new StreamId(f, v, s, type);
	if (!idToStream.containsKey(id)) {
	    idToStream.put(id, nStreams());
	    LinkedList newStream = new LinkedList();
	    streamToList.addElement(newStream);
	    streamToId.addElement(id);
	    LinkedList streamList = (LinkedList) streams.elementAt(f);
	    streamList.addLast(new Integer(nStreams() - 1));
	}
	return (LinkedList) streamToList.elementAt(idToStream.get(id));
    }

    // Given number of stream, yield id.
    public StreamId getStreamId(int stream) {
	return (StreamId) streamToId.elementAt(stream);
    }

    //////////////////////////////////////////////////////////////////////
    
    // After all streams are input, there is more processing.
    public void reprocess() {
	uniClasses.finish();
	normalizeSpaces();
	countStreamPos();
	makeAllSelected();
    }

    // Normalize whitespace in the streams:
    // 1) Space added around lexical entry and hieroglyphic.
    // 2) Whitespace jumps over nonprintables.
    // 3) Leading spaces at beginning of phrase are removed.
    // 4) Space before notes removed.
    // 5) Last element in phrase is made to end on one space. 
    private void normalizeSpaces() {
	for (int str = 0; str < nStreams(); str++) {
	    LinkedList stream = (LinkedList) streamToList.elementAt(str);
	    ListIterator iter = stream.listIterator();
	    Elem lastElem = null;
	    Elem prevElem = null;
	    while (iter.hasNext()) {
		Elem elem = (Elem) iter.next();
		if (elem instanceof TextElem) {
		    TextElem elemText = (TextElem) elem;
		    if (elemText.hasLeadingSpace()) {
			elemText.removeLeadingSpace();
			endOnSpace(prevElem);
		    }
		} else if (elem instanceof HieroElem || elem instanceof Lx) {
		    if (elem.isPrintable()) {
			endOnSpace(prevElem);
			elem.setTrailingSpace(true);
		    }
		} else if (elem instanceof Note) {
		    Note elemNote = (Note) elem;
		    normalizeNote(elemNote);
		    removeLastSpace(prevElem);
		} else if (elem instanceof Point) {
		    Point point = (Point) elem;
		    if (point.getPos().isPhrasal()) {
			endOnSpace(lastElem);
			prevElem = null;
		    } 
		}
		if (elem.isPrintable())
		    prevElem = elem;
		lastElem = elem;
	    }
	    endOnSpace(lastElem);
	}
    }

    // Normalize spaces in stream inside note:
    // 1) Whitespace at beginning of textual element moved to end of previous
    // such element if any.
    // 2) Last element is made to end on one space. 
    private static void normalizeNote(Note note) {
	LinkedList stream = note.getStream();
	ListIterator iter = stream.listIterator();
	Elem lastElem = null;
	Elem prevElem = null;
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem instanceof TextElem) {
		TextElem elemText = (TextElem) elem;
		if (elemText.hasLeadingSpace()) {
		    elemText.removeLeadingSpace();
		    endOnSpace(prevElem);
		}
	    } 
	    if (elem.isPrintable())
		prevElem = elem;
	    lastElem = elem;
	}
	endOnSpace(lastElem);
    }

    // Like above, but called externally for paragraph from preamble.
    public static void normalizeList(LinkedList stream) {
	ListIterator iter = stream.listIterator();
	Elem lastElem = null;
	Elem prevElem = null;
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem instanceof TextElem) {
		TextElem elemText = (TextElem) elem;
		if (elemText.hasLeadingSpace()) {
		    elemText.removeLeadingSpace();
		    endOnSpace(prevElem);
		}
	    }
	    if (elem.isPrintable())
		prevElem = elem;
	    lastElem = elem;
	}
	endOnSpace(lastElem);
    }

    // Put space at end of textual element if not already there.
    private static void endOnSpace(Elem elem) {
	if (elem != null) 
	    elem.setTrailingSpace(true);
    }

    // Remove last space from textual element if present.
    private static void removeLastSpace(Elem elem) {
	if (elem != null) 
	    elem.setTrailingSpace(false);
    }

    // Go through all streams and count frequencies of positions.
    // First convert to array to allow lookahead.
    // Do not count close tags; mark open tags as special if
    // following content is unbreakable.
    private void countStreamPos() {
	for (int str = 0; str < nStreams(); str++) {
	    LinkedList stream = (LinkedList) streamToList.elementAt(str);
	    ListIterator iter = stream.listIterator();
	    Elem[] streamArray = new Elem[stream.size()];
	    for (int i = 0; i < streamArray.length; i++) 
		streamArray[i] = (Elem) iter.next();
	    for (int i = 0; i < streamArray.length; i++) {
		Elem elem = streamArray[i];
		if (elem instanceof EmptyPoint) {
		    EmptyPoint point = (EmptyPoint) elem;
		    uniClasses.countPos(point);
		} else if (elem instanceof OpenPoint) {
		    OpenPoint point = (OpenPoint) elem;
		    if (point.breakable() || streamArray[i+1].breakable())
			uniClasses.countPosOpen(point);
		    else {
			point.setShortContent();
			ClosePoint closePoint = (ClosePoint) streamArray[i+2];
			closePoint.setShortContent();
			uniClasses.countPos(point);
		    }
		}
	    }
	}
    }

    //////////////////////////////////////////////////////////////////////
    // Certain streams may be selected or unselected by application

    // Boolean indicating for each stream whether to be used.
    private boolean[] streamSelection;

    // Number of selected streams.
    private int nSelected;
    // Number of files from which streams were selected.
    private int nSelectedFiles;

    // Make all streams selected.
    private void makeAllSelected() {
	streamSelection = new boolean[nStreams()];
	for (int str = 0; str < nStreams(); str++)
	    streamSelection[str] = true;
	computeNSelected();
    }

    // Make certain stream selected/unselected.
    public void setSelected(int str, boolean select) {
	streamSelection[str] = select;
	computeNSelected();
    }

    // Get whether stream selected.
    public boolean getSelected(int str) {
	return streamSelection[str];
    }

    // Compute number of selected streams,
    // and number of files from which streams were selected.
    private void computeNSelected() {
	int num = 0;
	TreeSet files = new TreeSet();
	for (int str = 0; str < nStreams(); str++)
	    if (streamSelection[str]) {
		num++;
		StreamId id = (StreamId) streamToId.elementAt(str);
		int file = id.getFile();
		files.add(new Integer(file));
	    }
	nSelected = num;
	nSelectedFiles = files.size();
    }

    // Get number of selected streams.
    public int nSelected() {
	return nSelected;
    }

    // Get number of files among selected streams.
    public int nSelectedFiles() {
	return nSelectedFiles;
    }

    //////////////////////////////////////////////////////////////////////
    // We arrange the streams into paragraphs, each containing text
    // that is to be aligned beneath one another. We prefer to 
    // make the paragraph boundaries at phrasal positions.
    // By backtracking we build up the paragraphs by taking parts from
    // the streams, making sure that matching positions are aligned.
    // The highest-ranked paragraph is taken.

    // Format into list of paragraphs.
    public LinkedList format(RenderContext context) {
	LinkedList paragraphs = new LinkedList();
	Config config = new Config(context, names, 
		streamToList, streamToId, streamSelection);
	while (config != null && !config.empty()) 
	    config = splitOffPar(context, config, paragraphs);
	if (config != null) {
	    Vector notes = config.footnotes(context);
	    if (notes != null)
		paragraphs.addLast(notes);
	}
	return paragraphs;
    }

    // Split off one paragraph from configuration. 
    // We maintain best configuration.
    // Return new configuration, or null if we cannot split off a paragraph.
    private Config splitOffPar(RenderContext context,
	    Config config, LinkedList pars) {
	if (!config.next(context, uniClasses)) {
	    TreeSet deadlockPoints = config.firstPoints();
	    deadlockReportingPar(deadlockPoints, pars);
	    return null;
	}
	int bestScore = 0;
	Config bestConfig = null;
	do {
	    int score = config.score(context);
	    if (score >= bestScore) {
		bestScore = score;
		bestConfig = config;
		config = (Config) config.clone();
	    }
	} while (config.next(context, uniClasses));
	Vector par = bestConfig.paragraph(context);
	if (par != null) 
	    pars.addLast(par);
	return bestConfig;
    }

    // Make dummy paragraph containing text explaining deadlock.
    private void deadlockReportingPar(TreeSet points, LinkedList pars) {
	Vector par = new Vector();
	Line errorLine = new Line(true);
	Elem text = new TextElem(RenderContext.LATIN_FONT, " Deadlock involving:");
	text.setX(0);
	errorLine.addElem(text);
	par.addElement(errorLine);
	Iterator iter = points.iterator();
	while (iter.hasNext()) {
	    String point = (String) iter.next();
	    errorLine = new Line(true);
	    text = new TextElem(RenderContext.LATIN_FONT, "   " + point);
	    text.setX(0);
	    errorLine.addElem(text);
	    par.addElement(errorLine);
	}
	pars.addLast(par);
    }

    ///////////////////////////////////////////////////////////////////////////
    // For linked list of Elems. 

    // Split off line from stream.
    public static Line splitOffLine(RenderContext context,
	    LinkedList stream, float x) {
	Line line = new Line(false);
	splitOffLine(context, stream, line, x, false);
	return line;
    }

    // We try to add entire element. If that doesn't work, break it.
    // If it is not breakable, then fail.
    // If no chunks added yet, allow going past right edge.
    private static boolean splitOffLine(RenderContext context,
	    LinkedList stream, Line line, float x, boolean added) {
	if (stream.isEmpty())
	    return true;
	Elem elem = (Elem) stream.getFirst();
	stream.removeFirst();
	if (elem.getWidth(context) <= context.rightBound() - x ||
		!added && !elem.breakable()) {
	    Elem elemCopy = (Elem) elem.clone();
	    elemCopy.setX(x);
	    line.addElem(elemCopy);
	    boolean newAdded = added || elem.breakable();
	    boolean done = splitOffLine(context, stream, line,
		    x + elem.getAdvance(context), newAdded);
	    if (done)
		return done;
	    else
		line.removeLast();
	}
	int index = elem.lastBreak(context, context.rightBound() - x);
	if (index < 0 && !added)
	    index = elem.firstBreak();
	if (index < 0) {
	    stream.addFirst(elem);
	    return false;
	} else {
	    Elem pref = elem.prefix(index);
	    pref.setX(x);
	    line.addElem(pref);
	    if (pref.isPrefix()) {
		Elem suf = pref.suffix();
		stream.addFirst(suf);
	    }
	    return true;
	}
    }
}
