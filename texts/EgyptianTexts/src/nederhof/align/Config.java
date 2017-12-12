/***************************************************************************/
/*                                                                         */
/*  Config.java                                                            */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// A configuration in formatting.
// Contains paragraph, which consists of parts of streams, which
// are to be aligned beneath each other. 
// It also contains descriptions of the remainders of the streams.

package nederhof.align;

import java.util.*;

final class Config implements Cloneable {

    // Number of streams.
    private int size;
    // Streams as arrays.
    private Elem[][] streams;
    // Files, versions, schemes for different streams,
    // and names of creator of file.
    private int[] files;
    private String[] versions;
    private String[] schemes;
    private String[] names;
    // What remains of streams. As position.
    private int[] restStreams;
    // What is left of stream elements.
    private Elem[] bufferStreams;
    // What belongs to stream in current paragraph.
    private Line[] lines;
    // How much of paragraphs filled horizontally.
    private float[] filled;
    // Order in which streams are to be added to paragraphs.
    private int[] order;

    // How many open brackets of positions seen in previous paragraphs.
    private IntHashMap posOpenFreqTotal;
    // How many open brackets of positions seen in current paragraph.
    private IntHashMap posOpenFreqPar;
    // Rightmost coordinate of open bracket in current paragraph.
    private FloatHashMap posOpenMaxPar;
    // Which positions processed in previous paragraphs.
    private HashSet posTotal;
    // Which positions processed in current paragraph.
    private HashSet posPar;
    // Rightmost coordinate of position in current paragraph.
    private FloatHashMap posMaxPar;

    // List of Footnotes.
    private LinkedList notes;
    // Number of last note added to paragraph.
    private int lastNoteNum;

    // Private constructor only used for cloning.
    private Config(int size, Elem[][] streams, int[] files, 
	    String[] versions, String[] schemes, String[] names,
	    int[] restStreams, Elem[] bufferStreams, Line[] lines, float[] filled, int[] order, 
	    IntHashMap posOpenFreqTotal, IntHashMap posOpenFreqPar, FloatHashMap posOpenMaxPar,
	    HashSet posTotal, HashSet posPar, FloatHashMap posMaxPar, 
	    LinkedList notes, int lastNoteNum) {
	this.size = size;
	this.streams = streams;
	this.files = files;
	this.versions = versions;
	this.schemes = schemes;
	this.names = names;
	this.restStreams = (int[]) restStreams.clone();
	this.bufferStreams = (Elem[]) bufferStreams.clone();
	this.lines = new Line[lines.length];
	for (int i = 0; i < lines.length; i++)
	    this.lines[i] = (Line) lines[i].clone();
	this.filled = (float[]) filled.clone();
	this.order = order;
	this.posOpenFreqTotal = posOpenFreqTotal;
	this.posOpenFreqPar = (IntHashMap) posOpenFreqPar.clone();
	this.posOpenMaxPar = (FloatHashMap) posOpenMaxPar.clone();
	this.posTotal = posTotal;
	this.posPar = (HashSet) posPar.clone();
	this.posMaxPar = (FloatHashMap) posMaxPar.clone();
	this.notes = (LinkedList) notes.clone();
	this.lastNoteNum = lastNoteNum;
    }

    // Public constructor.
    public Config(RenderContext context, Vector fileNames, 
	    Vector streamToList, Vector streamToId,
	    boolean[] streamSelection) {
	float offset = context.textOffset();
	size = streamToList.size();
	streams = new Elem[size][];
	files = new int[size];
	versions = new String[size];
	schemes = new String[size];
	names = new String[size];
	restStreams = new int[size];
	bufferStreams = new Elem[size];
	lines = new Line[size];
	filled = new float[size];
	order = new int[size];
	posOpenFreqTotal = new IntHashMap();
	posOpenFreqPar = new IntHashMap();
	posOpenMaxPar = new FloatHashMap();
	posTotal = new HashSet();
	posPar = new HashSet();
	posMaxPar = new FloatHashMap();
	notes = new LinkedList();
	lastNoteNum = 0;
	for (int str = 0; str < size; str++) {
	    LinkedList streamList = (LinkedList) streamToList.elementAt(str);
	    ListIterator streamIter = streamList.listIterator();
	    streams[str] = new Elem[streamList.size()];
	    for (int i = 0; streamIter.hasNext(); i++) {
		Elem elem = (Elem) streamIter.next();
		streams[str][i] = elem.allowVisible(streamSelection[str]);
	    }
	    StreamId id = (StreamId) streamToId.elementAt(str);
	    files[str] = id.getFile();
	    versions[str] = id.getVersion();
	    schemes[str] = id.getScheme();
	    names[str] = (String) fileNames.get(files[str]);
	    restStreams[str] = 0;
	    bufferStreams[str] = null;
	    lines[str] = new Line(files[str], names[str], versions[str], schemes[str], 
		    context);
	    filled[str] = offset;
	    order[str] = str;
	}
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Clone.
    public Object clone() {
	return new Config(size, streams, files, versions, schemes, names, 
		restStreams, bufferStreams, lines, filled, order, 
		posOpenFreqTotal, posOpenFreqPar, posOpenMaxPar, posTotal,
		posPar, posMaxPar, notes, lastNoteNum);
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Are there nonempty streams left?
    public boolean empty() {
	for (int str = 0; str < size; str++) 
	    if (!empty(str))
		return false;
	return true;
    }

    // Is stream nonempty?
    private boolean empty(int str) {
	Elem[] stream = streams[str];
	int rest = restStreams[str];
	Elem buffer = bufferStreams[str];
	if (rest < stream.length || buffer != null)
	    return false;
	else
	    return true;
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Take configuration to state where score is at least as good.
    // This means that textual elements can be freely added, but only one
    // collection of chunks is allowed involving positions and beside that nothing else.
    // Elements are added from left to right, so leftmost are done first;
    // for this purpose there is a priority queue.
    // posMapPar maps position to first stream where it occurs.
    // Return whether some chunks were added.
    public boolean next(RenderContext context, Unification uniClasses) {
	TreeSet chunkRecords = new TreeSet(new ExtendComparator());
	HashMap posMapPar = new HashMap();
	integrateChunks(context, uniClasses, chunkRecords, posMapPar);
	boolean added = false;
	while (!chunkRecords.isEmpty()) {
	    Object leftMost = chunkRecords.first();
	    chunkRecords.remove(leftMost);
	    if (leftMost instanceof EqClass) {
		if (added)
		    break;
		EqClass cl = (EqClass) leftMost;
		if (cl.isObsolete())
		    continue;
		if (!cl.allowable) 
		    break;
		if (cl.end() <= context.rightBound() || 
			cl.start() == context.textOffset())
		    addClass(context, uniClasses, cl, chunkRecords, posMapPar);
		else 
		    break;
	    } else {
		ChunkRecord record = (ChunkRecord) leftMost;
		if (record.end <= context.rightBound() || 
			record.start == context.textOffset()) {
		    addChunk(context, record.stream, 0, uniClasses);
		    integrateChunk(context, uniClasses, record.stream,
			    chunkRecords, posMapPar);
		} else
		    break;
	    }
	    added = true;
	}
	return added;
    }

    // For each stream in class, add chunks. Update priority queue of chunks.
    private void addClass(RenderContext context, Unification uniClasses,
	    EqClass cl, TreeSet chunkRecords, HashMap posMapPar) {
	ListIterator iter = cl.records.listIterator();
	while (iter.hasNext()) {
	    ChunkRecord record = (ChunkRecord) iter.next();
	    float skip = cl.pointStart - record.pointStart;
	    addChunk(context, record.stream, skip, uniClasses);
	    integrateChunk(context, uniClasses, record.stream, 
		    chunkRecords, posMapPar);
	}
    }

    // Investigate chunks that can be added to paragraph, and 
    // equivalence classes thereof.
    private void integrateChunks(RenderContext context, Unification uniClasses,
	    TreeSet chunkRecords, HashMap posMapPar) {
	for (int str = 0; str < size; str++) 
	    integrateChunk(context, uniClasses, str, chunkRecords, posMapPar);
    }

    // Get next chunk, and when it contains positions, unify it with other
    // chunks.
    private void integrateChunk(RenderContext context, Unification uniClasses,
	    int str, TreeSet chunkRecords, HashMap posMapPar) {
	ChunkRecord record = nextChunk(context, str);
	if (record.hasPoints()) {
	    EqClass cl = new EqClass(uniClasses, record);
	    unifyOnPos(uniClasses, cl, record.points, posMapPar);
	    chunkRecords.add(cl);
	} else if (record.hasElems())
	    chunkRecords.add(record);
    }

    // Unify classes where necessary based on points.
    private void unifyOnPos(Unification uniClasses, EqClass cl,
	    LinkedList points, HashMap posMapPar) {
	ListIterator iter = points.listIterator();
	while (iter.hasNext()) {
	    Point point = (Point) iter.next();
	    Pos pos = point.getPos();
	    pos = uniClasses.deref(pos);
	    if (posMapPar.containsKey(pos)) {
		EqClass other = (EqClass) posMapPar.get(pos);
		cl.unify(uniClasses, other);
	    } else
		posMapPar.put(pos, cl);
	}
    }

    // Find next chunk, i.e. series of elements or parts thereof in stream str, 
    // until next possible break. Determine provisional starting and ending
    // x-coordinates, which may change if chunks in different streams are to be
    // aligned.
    private ChunkRecord nextChunk(RenderContext context, int str) {
	ChunkRecord record = new ChunkRecord(str);
	nextChunk(context, filled[str], lines[str].getLast(), bufferStreams[str], 
		streams[str], restStreams[str], record);
	return record;
    }

    // As above, but with properties of stream spelled out.
    private void nextChunk(RenderContext context, float fill, Elem last, Elem elem, 
	    Elem[] stream, int rest, ChunkRecord record) {
	if (last != null && last.isPrefix()) {
	    int nextBreak = last.nextBreak();
	    float width = last.getWidth(context, nextBreak) - last.getAdvance(context);
	    record.recordBounds(fill, fill + width);
	    if (nextBreak < 0) {
		float advance = last.getAdvance(context, nextBreak) - last.getAdvance(context);
		nextChunk(context, fill + advance, null, null, stream, rest, record);
	    }
	    return;
	}
	if (elem != null)
	    ;
	else if (rest < stream.length)
	    elem = stream[rest++];
	else
	    return;
	if (elem instanceof EmptyPoint) {
	    record.points.addLast(elem);
	    record.recordPointStart(fill);
	} else if (elem instanceof OpenPoint) {
	    if (((OpenPoint) elem).hasShortContent()) {
		record.points.addLast(elem);
		Elem content = stream[rest++];
		float width = content.getWidth(context);
		record.recordPointStart(fill + width / 2);
		record.recordBounds(fill, fill + width);
		fill += content.getAdvance(context);
		elem = stream[rest++];
	    } else {
		record.pointsOpen.addLast(elem);
		record.recordPointStart(fill);
	    }
	} else if (elem instanceof ClosePoint) {
	    record.pointsClose.addLast(elem);
	    record.recordPointStart(fill);
	}
	int firstBreak = elem.firstBreak();
	float width = elem.getWidth(context, firstBreak);
	record.recordBounds(fill, fill + width);
	if (firstBreak < 0) {
	    float advance = elem.getAdvance(context);
	    nextChunk(context, fill + advance, null, null, stream, rest, record);
	} 
    }

    // Extend stream with elements until first breakable. Skip certain space.
    // Record occurrences of positions.
    private void addChunk(RenderContext context, int str, float skip, 
	    Unification uniClasses) {
	Elem last = lines[str].getLast();
	if (last != null && last.isPrefix()) {
	    if (skip > 0) {
		Elem suf = last.suffix();
		float width = last.getWidth(context, -1) - last.getAdvance(context);
		float sep = width - suf.getWidth(context);
		bufferStreams[str] = suf;
		filled[str] += sep;
	    } else {
		int nextBreak = last.nextBreak();
		float advance = last.getAdvance(context, nextBreak) - last.getAdvance(context);
		filled[str] += advance;
		last.setPrefix(nextBreak);
		if (nextBreak < 0) 
		    addChunk(context, str, 0, uniClasses);
		return;
	    }
	}
	filled[str] += skip;
	Elem[] stream = streams[str];
	Elem elem = bufferStreams[str];
	bufferStreams[str] = null;
	if (elem != null)
	    ;
	else if (restStreams[str] < stream.length)
	    elem = (Elem) stream[restStreams[str]++].clone();
	else
	    return;
	if (elem instanceof EmptyPoint) {
	    EmptyPoint point = (EmptyPoint) elem;
	    Pos pos = point.getPos();
	    pos = uniClasses.deref(pos);
	    posPar.add(pos);
	    posMaxPar.max(pos, filled[str]);
	} else if (elem instanceof OpenPoint) {
	    OpenPoint point = (OpenPoint) elem;
	    Pos pos = point.getPos();
	    pos = uniClasses.deref(pos);
	    if (point.hasShortContent()) {
		posPar.add(pos);
		posMaxPar.max(pos, filled[str]);
	    } else {
		posOpenFreqPar.incr(pos);
		posOpenMaxPar.max(pos, filled[str]);
	    }
	} else if (elem instanceof Note) {
	    Note note = (Note) elem;
	    notes.add(note);
	    note.setMarker(Integer.toString(++lastNoteNum));
	    elem = note;
	}
	int firstBreak = elem.firstBreak();
	if (firstBreak < 0) {
	    elem.setX(filled[str]);
	    lines[str].addElem(elem);
	    filled[str] += elem.getAdvance(context);
	    addChunk(context, str, 0, uniClasses);
	} else {
	    Elem pref = elem.prefix(firstBreak);
	    pref.setX(filled[str]);
	    lines[str].addElem(pref);
	    filled[str] += pref.getAdvance(context);
	}
    }

    // Chunk objects have unique numbers, to allow comparison of
    // objects with same rank.
    private int lastChunkId = 0;

    // Unifies ChunkRecord and EqClass below. Is something that is 
    // added in one iteration of formatting.
    // All such objects get unique id.
    private class ChunkObject {
	public int id = ++lastChunkId;
    }

    // Record for chunk, i.e. part of text without breaks.
    // Included are number of stream, locations where chunk starts,
    // where first point starts, and there chunk ends.
    // (Negative if none.)
    // Also the three kinds of points within the chunk are included.
    private class ChunkRecord extends ChunkObject {
	public int stream;
	public float start;
	public float pointStart;
	public float end;
	public LinkedList points;
	public LinkedList pointsOpen;
	public LinkedList pointsClose;

	public ChunkRecord(int stream) {
	    this.stream = stream;
	    start = -1;
	    pointStart = -1;
	    end = -1;
	    points = new LinkedList();
	    pointsOpen = new LinkedList();
	    pointsClose = new LinkedList();
	}

	public void recordBounds(float start, float end) {
	    if (this.start < 0)
		this.start = start;
	    this.end = Math.max(this.end, end);
	}

	public void recordPointStart(float pointStart) {
	    if (this.pointStart < 0)
		this.pointStart = pointStart;
	}

	public boolean hasElems() {
	    return start >= 0;
	}

	public boolean hasPoints() {
	    return pointStart >= 0;
	}
    }

    // Equivalence class of streams, where they are to be aligned
    // beneath each other because they have positions in common.
    private class EqClass extends ChunkObject {
	// Indirection link to other class.
	private EqClass link;
	// The records of streams that take part in this equivalence class.
	public LinkedList records;
	// The sets/bags of dereferenced positions that are involved.
	public HashSet poss;
	public IntHashMap possOpen;
	public HashSet possClose;
	// Total number of occurrences of involved positions.
	public int totalOccur;
	// X-coordinate of start of point(s).
	public float pointStart;
	// Is class allowable?
	public boolean allowable;

	// Constructor.
	public EqClass(Unification uniClasses, ChunkRecord record) {
	    link = null;
	    records = new LinkedList();
	    records.addFirst(record);
	    poss = new HashSet();
	    possOpen = new IntHashMap();
	    possClose = new HashSet();
	    totalOccur = 0;
	    pointStart = record.pointStart;
	    allowable = false;
	    ListIterator iter = record.points.listIterator();
	    while (iter.hasNext()) {
		Point point = (Point) iter.next();
		Pos pos = uniClasses.deref(point.getPos());
		if (!posTotal.contains(pos) && !posPar.contains(pos)) {
		    poss.add(pos);
		    totalOccur++;
		}
	    }
	    iter = record.pointsOpen.listIterator();
	    while (iter.hasNext()) {
		Point point = (Point) iter.next();
		Pos pos = uniClasses.deref(point.getPos());
		possOpen.incr(pos);
	    }
	    iter = record.pointsClose.listIterator();
	    while (iter.hasNext()) {
		Point point = (Point) iter.next();
		Pos pos = uniClasses.deref(point.getPos());
		possClose.add(pos);
	    }
	    determineAllowable(uniClasses);
	}

	// Unify.
	public void unify(Unification uniClasses, EqClass other) {
	    other = other.deref();
	    if (other != this) {
		other.link = this;
		records.addAll(other.records);
		poss.addAll(other.poss);
		possOpen.add(other.possOpen);
		possClose.addAll(other.possClose);
		totalOccur += other.totalOccur;
		pointStart = Math.max(pointStart, other.pointStart);
		determineAllowable(uniClasses);
	    }
	}

	// Is not representative of equivalence class.
	public boolean isObsolete() {
	    return link != null;
	}

	// Dereference.
	public EqClass deref() {
	    if (link != null)
		return link.deref();
	    else
		return this;
	}

	// Total number of occurrences of positions.
	public int totalRequired(Unification uniClasses) {
	    int num = 0;
	    Iterator iter = poss.iterator();
	    while (iter.hasNext()) {
		Pos pos = (Pos) iter.next();
		num += uniClasses.freq(pos);
	    }
	    return num;
	}

	// A stream is allowable if:
	// 1) For normal positions in class, number of occurrences equals
	// number of occurrences in input.
	// 2) For each of those positions, all open positions have been seen,
	// either in previous paragraphs, in current one or in class.
	// 3) For each close occurrence in class, normal occurrences (if there are
	// any) have been processed or are also in class.
	// Updated is minimum x-coordinate where chunks should be printed.
	private void determineAllowable(Unification uniClasses) {
	    if (totalOccur != totalRequired(uniClasses))
		return;
	    Iterator iter = poss.iterator();
	    while (iter.hasNext()) {
		Pos pos = (Pos) iter.next();
		if (posOpenFreqTotal.get(pos) +
			posOpenFreqPar.get(pos) + 
			possOpen.get(pos) < uniClasses.freqOpen(pos))
		    return;
		else
		    pointStart = Math.max(pointStart, posOpenMaxPar.get(pos));
	    }
	    iter = possClose.iterator();
	    while (iter.hasNext()) {
		Pos pos = (Pos) iter.next();
		if (uniClasses.freq(pos) == 0)
		    continue;
		if (!posTotal.contains(pos) && !posPar.contains(pos) && 
			!poss.contains(pos))
		    return;
		else
		    pointStart = Math.max(pointStart, posMaxPar.get(pos));
	    }
	    allowable = true;
	}

	// Minimum left location for all streams in class.
	public float start() {
	    float start = Float.MAX_VALUE;
	    Iterator iter = records.iterator();
	    while (iter.hasNext()) {
		ChunkRecord record = (ChunkRecord) iter.next();
		float thisStart = pointStart - record.pointStart + record.start;
		start = Math.min(start, thisStart);
	    }
	    return start;
	}

	// Maximum right location for all streams in class.
	public float end() {
	    float end = 0.0f;
	    Iterator iter = records.iterator();
	    while (iter.hasNext()) {
		ChunkRecord record = (ChunkRecord) iter.next();
		float thisEnd = pointStart - record.pointStart + record.end;
		end = Math.max(end, thisEnd);
	    }
	    return end;
	}
    }

    // Compares chunks or equivalence classes thereof,
    // so ChunkRecord or EqClass.
    // Comparison based on x coordinate of start.
    // If those are same, then look at id.
    private class ExtendComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    ChunkObject c1 = (ChunkObject) o1;
	    ChunkObject c2 = (ChunkObject) o2;
	    float start1 = chunkRank(o1);
	    float start2 = chunkRank(o2);
	    if (start1 < start2)
		return -1;
	    else if (start1 > start2)
		return 1;
	    else if (c1.id < c2.id)
		return -1;
	    else if (c1.id > c2.id)
		return 1;
	    else
		return 0;
	}
    }

    // The goodness of ChunkRecord or EqClass.
    // Is where x coordinate of start is.
    private static float chunkRank(Object o) {
	if (o instanceof ChunkRecord)
	    return ((ChunkRecord) o).start;
	else {
	    EqClass cl1 = (EqClass) o;
	    return cl1.allowable ? cl1.pointStart : Float.MAX_VALUE;
	}
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Goodness of configuration.
    // We count how many remainders start with position, and of what kind.
    // We also count how much of lines is used.
    // The first criterion is more important and is multiplied by 10^6.
    // It is ignored however if there is only one stream containing
    // content elements.
    public int score(RenderContext context) {
	float score = 0;
	int nContent = numberContent();
	for (int str = 0; str < size; str++) {
	    if (nContent > 1)
		score += pointScore(context, str) * 1000000;
	    score += filled[str];
	}
	return Math.round(score);
    }

    // Count how many lines contain content information.
    private int numberContent() {
	int n = 0;
	for (int str = 0; str < size; str++) {
	    if (lines[str].isContent())
		n++;
	}
	return n;
    }

    // Investigate positions in following chunk, and score.
    // End of phrase is best (5), including end of input, 
    // then other empty point (4) and open point (4). 
    private float pointScore(RenderContext context, int str) {
	ChunkRecord record = nextChunk(context, str);
	if (!record.points.isEmpty()) {
	    ListIterator iter = record.points.listIterator();
	    while (iter.hasNext()) {
		Point point = (Point) iter.next();
		if (point.getPos().isPhrasal())
		    return 5;
	    }
	    return 4;
	} else if (!record.pointsOpen.isEmpty()) 
	    return 4;
	else if (!record.hasElems()) 
	    return 5;
	else
	    return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Return the streams in current paragraph, and remove from configuration.
    // Ignore empty lines. Also add lines for footnotes.
    // Return null if no real lines in paragraph.
    public Vector paragraph(RenderContext context) {
	investigateOrder();
	Vector allLines = new Vector();
	for (int str = 0; str < size; str++) {
	    int strOrder = order[str];
	    Line line = lines[strOrder];
	    if (line.isContent())
		allLines.addElement(line);
	}
	if (!context.collectNotes()) {
	    ListIterator iter = notes.listIterator();
	    while (iter.hasNext()) {
		Note note = (Note) iter.next();
		String marker = note.getMarker();
		layoutFootnote(context, note.getStream(), marker, allLines);
	    }
	    notes = new LinkedList();
	    lastNoteNum = 0;
	}
	float offset = context.textOffset();
	for (int str = 0; str < size; str++) {
	    Elem last = lines[str].getLast();
	    if (last != null && last.isPrefix())
		bufferStreams[str] = last.suffix();
	    else
		bufferStreams[str] = null;
	    lines[str] = new Line(files[str], names[str],
		    versions[str], schemes[str], context);
	    filled[str] = offset;
	}
	posOpenFreqTotal.add(posOpenFreqPar);
	posOpenFreqPar = new IntHashMap();
	posOpenMaxPar = new FloatHashMap();
	posTotal.addAll(posPar);
	posPar = new HashSet();
	posMaxPar = new FloatHashMap();
	if (allLines.size() > 0)
	    return allLines;
	else
	    return null;
    }

    // Get uncollected footnotes.
    public Vector footnotes(RenderContext context) {
	Vector allLines = new Vector();
	ListIterator iter = notes.listIterator();
	while (iter.hasNext()) {
	    Note note = (Note) iter.next();
	    String marker = note.getMarker();
	    layoutFootnote(context, note.getStream(), marker, allLines);
	}
	notes = new LinkedList();
	lastNoteNum = 0;
	if (allLines.size() > 0)
	    return allLines;
	else
	    return null;
    }

    // We investigate necessary reorderings of the streams, based on
    // phrasal positions, the first one in each stream. If phrasal position
    // comes first textually, the stream is to be rendered first.
    private void investigateOrder() {
	int[] phrasalRank = new int[size];
	for (int str = 0; str < size; str++) 
	    phrasalRank[str] = firstPhrasal(str);
	bubblesortOrder(phrasalRank);
    }	

    // Return textual position of first phrasal position.
    // Return -1 is no phrasal position.
    private int firstPhrasal(int str) {
	Line line = lines[str];
	LinkedList elems = line.getElems();
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem instanceof Point) {
		Point point = (Point) elem;
		if (point.getPos().isPhrasal()) 
		    return point.getPos().getNum();
	    }
	}
	return -1;
    }

    // Bubblesort to ensure that streams occur in an order consistent with
    // ranking. Negative values are ignored.
    private void bubblesortOrder(int[] phrasalRank) {
	boolean changed = false;
	int prev = 0;
	for (int str = 1; str < size; str++) {
	    if (phrasalRank[order[str]] < 0)
		continue;
	    if (phrasalRank[order[prev]] > phrasalRank[order[str]]) {
		int target = order[prev];
		order[prev] = order[str];
		order[str] = target;
		prev = str;
		changed = true;
	    }
	    prev = str;
	}
	if (changed)
	    bubblesortOrder(phrasalRank);
    }

    // We determine layout of footnote, and add it to configuration. 
    // First marker is added, then words added until line full, then next
    // line, etc.
    private void layoutFootnote(RenderContext context, 
	    LinkedList stream, String markStr, Vector lines) {
	float x = context.leftBound();
	Line line = new Line(true);
	Note marker = new Note();
	marker.setMarker(markStr);
	marker.setX(x);
	line.addElem(marker);
	x += marker.getAdvance(context);
	ListIterator iter = stream.listIterator();
	layoutFootnoteLines(context, iter, line, x, lines, false);
    }

    // Further fill in line, then next line. Use backtracking.
    // Return whether successful.
    // Failure can occur when there is no breakable
    // element so that no line-break can be achieved before the
    // end of the line.
    private boolean layoutFootnoteLines(RenderContext context,
	    ListIterator stream, Line line, float x, Vector lines, boolean added) {
	if (stream.hasNext()) {
	    Elem elem = (Elem) ((Elem) stream.next()).clone();
	    boolean done = layoutFootnoteLines(context, elem, stream, line, x, lines, added);
	    if (!done)
		stream.previous();
	    return done;
	} else {
	    lines.addElement(line);
	    return true;
	}
    }

    // As above, but with element taken already from the stream.
    // We try to add entire element. If that doesn't work, break it.
    // If it is not breakable, then fail.
    // If no chunks added yet, allow going past right edge.
    private boolean layoutFootnoteLines(RenderContext context, Elem elem,
	    ListIterator stream, Line line, float x, Vector lines, boolean added) {
	if (elem.getWidth(context) <= context.rightBound() - x ||
		!added && !elem.breakable()) {
	    elem.setX(x);
	    line.addElem(elem);
	    boolean newAdded = added || elem.breakable();
	    boolean done = layoutFootnoteLines(context, stream, line, 
		    x + elem.getAdvance(context), lines, newAdded);
	    if (done)
		return done;
	    else 
		line.removeLast();
	}
	int index = elem.lastBreak(context, context.rightBound() - x);
	if (index < 0 && !added) 
	    index = elem.firstBreak();
	if (index < 0)
	    return false; 
	else {
	    Elem pref = elem.prefix(index);
	    pref.setX(x);
	    line.addElem(pref);
	    lines.addElement(line);
	    x = context.leftBound();
	    if (pref.isPrefix()) {
		line = new Line(true);
		Elem suf = pref.suffix();
		layoutFootnoteLines(context, suf, stream, line, x, lines, false);
	    } else if (stream.hasNext()) {
		line = new Line(true);
		layoutFootnoteLines(context, stream, line, x, lines, false);
	    } 
	    return true;
	}
    }

    ////////////////////////////////////////////////////////////////////////////////

    // Return list of positions with which streams start.
    public TreeSet firstPoints() {
	TreeSet s = new TreeSet();
	for (int str = 0; str < size; str++) 
	    s.addAll(firstPoints(
			files[str] + 1, versions[str], schemes[str],
			streams[str], bufferStreams[str], restStreams[str]));
	return s;
    }

    // As above, for one stream.
    private TreeSet firstPoints(
	    int file, String version, String scheme,
	    Elem[] stream, Elem elem, int rest) {
	TreeSet s = new TreeSet();
	if (elem != null)
	    ;
	else if (rest < stream.length)
	    elem = stream[rest++];
	else 
	    return s;
	if (elem instanceof Point) {
	    Point point = (Point) elem;
	    String name = "File " + file + ":";
	    name += VersionLabel.getVersionLabel(version, scheme) + " ";
	    if (point.getPos().isPhrasal())
		name += "phrasal boundary";
	    else {
		if (point.isAlign()) {
		    String posVersion = point.getPos().getVersion();
		    String posScheme = point.getPos().getScheme();
		    name += "[align]";
		    name += VersionLabel.getVersionLabel(posVersion, posScheme) + " ";
		} else
		    name += "[coord] ";
		name += point.getPos().getTag();
		if (point instanceof OpenPoint)
		    name += " (open)";
		else if (point instanceof ClosePoint)
		    name += " (close)";
	    }
	    s.add(name);
	}
	if (!elem.breakable()) {
	    s.addAll(firstPoints(file, version, scheme, stream, null, rest));
	}
	return s;
    }
}
