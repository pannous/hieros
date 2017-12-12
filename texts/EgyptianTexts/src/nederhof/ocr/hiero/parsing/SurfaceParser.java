package nederhof.ocr.hiero.parsing;

import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.res.*;
import nederhof.res.editor.*;

// Parses a line of blobs (in 2D).
public class SurfaceParser {

	// Unit size.
	private int unitSize;

	// Common separation between glyhphs.
	private int sep;

	// One of four directions:
	// hlr, hrl, vlr, vrl.
	private String direction;

	// Row or column? Computed from direction.
	private boolean horizontal;

	// The line is broken up into chunks of unit size.
	// This is to avoid the running time to increase too much
	// for increasing line length.
	// The positions are x values or y values depending on text direction.
	private int minPos = Integer.MAX_VALUE;
	private int maxPos = Integer.MIN_VALUE;
	private int nChunks = 0;

	// Blobs put in array. Henceforth referred to using indices.
	// Makes it easier to match sets of blobs.
	private Vector<Blob> blobs = new Vector<Blob>();

	// Maps chunk number to (numbers of) included and overlapping blobs.
	private TreeMap<Integer,Vector<Integer>> chunkToBlobs =
		new TreeMap<Integer,Vector<Integer>>();

	// Maps chunk number to collections of items that overlap.
	private TreeMap<Integer,Vector<Item>> chunkToItems = 
		new TreeMap<Integer,Vector<Item>>();

	// Maps key of item to (best) item.
	private TreeMap<String,Item> keyToItem = new TreeMap<String,Item>();

	// Item covering all blobs.
	private Item rootItem;

	// Agenda. Arranged by number of covered blobs.
	private TreeMap<Integer,Vector<Item>> agenda = new TreeMap<Integer,Vector<Item>>();

	// Auxiliary class, for correcting groups.
	// Created at most once.
	private static GroupCorrection correction = null;

	// Constructor.
	public SurfaceParser(Vector<Blob> blobs, int unitSize, String direction) {
		this.unitSize = unitSize;
		this.sep = unitSize / 5;
		this.direction = direction;
		this.horizontal = direction.equals("hlr") || direction.equals("hrl");
		if (correction == null)
			correction = new GroupCorrection();
		initialize(blobs);
		chunkBlobs();
		parse();
	}

	// Initialize agenda.
	// For boxes, do the process recursively.
	private void initialize(Vector<Blob> allBlobs) {
		blobs = (Vector<Blob>) allBlobs.clone();
		if (blobs.size() == 0)
			return;
		else 
			for (Blob blob : blobs) {
				if (horizontal) {
					minPos = Math.min(minPos, blob.x());
					maxPos = Math.max(maxPos, blob.x()+blob.width());
				} else {
					minPos = Math.min(minPos, blob.y());
					maxPos = Math.max(maxPos, blob.y()+blob.height());
				}
			}
		if (horizontal)
			Collections.sort(blobs, new XComparator());
		else
			Collections.sort(blobs, new YComparator());
		Vector<Blob> boxes = new Vector<Blob>();
		Vector<ResBox> boxEncodings = new Vector<ResBox>();
		removeBoxes(boxes, boxEncodings);
		Vector<Item> basicItems = new Vector<Item>();
		basicItems.addAll(fillNamedAgenda());
		basicItems.addAll(fillBoxAgenda(boxes, boxEncodings));
		makeRootItem();
		for (Item basic : basicItems) 
			store(basic);
	}

	// Isolate and remove boxes, 
	// and fill arguments with the results of recursive calls.
	private void removeBoxes(Vector<Blob> boxes, Vector<ResBox> boxEncodings) {
		while (true) {
			int boxNum = firstBox(blobs);
			if (boxNum < 0)
				break;
			else {
				Blob box = blobs.remove(boxNum);
				Vector<Blob> contents = new Vector<Blob>();
				for (Blob blob : blobs)
					if (includedIn(blob, box))
						contents.add(blob);
				blobs.removeAll(contents);
				SurfaceParser boxParser = new SurfaceParser(contents, unitSize, direction);
				ResFragment encoding = boxParser.encoding();
				ResBox boxEncoding = new ResBox();
				boxEncoding.type = getBoxName(box);
				if (encoding != null) {
					boxEncoding.direction = encoding.direction;
					boxEncoding.hiero = encoding.hiero;
				}
				/* Notes of boxes not included at this time (not compatible
				 * with philolog format)
				if (!box.getNote().equals("")) {
					String note = "\"" + box.getNote().replaceAll("[\\\"\\\\]", "") + "\"";
					boxEncoding.notes.add(new ResNote(note));
				}
				*/
				boxes.add(box);
				boxEncodings.add(boxEncoding);
			}
		}
	}

	// Return index of first box, if any. Otherwise negative.
	private int firstBox(Vector<Blob> blobs) {
		for (int i = 0; i < blobs.size(); i++)
			if (NonHiero.isBox(getName(blobs.get(i))))
				return i;
		return -1;
	}

	// Make initial agenda, each (non-box) blob in one item.
	private Vector<Item> fillNamedAgenda() {
		Vector<Item> basicItems = new Vector<Item>();
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = blobs.get(i);
			String name = getName(blob);
			ResBasicgroup encoding = null;
			String note = "";
			if (!blob.getNote().equals("")) 
				note = "\"" + blob.getNote().replaceAll("[\\\"\\\\]", "") + "\"";
			if (name.equals("unk")) {
				ResNamedglyph named = new ResNamedglyph("\"?\"");
				if (!note.equals("")) 
					named.notes.add(new ResNote(note));
				encoding = named;
			} else if (name.equals("shade")) {
				ResEmptyglyph empty = new ResEmptyglyph();
				empty.shade = true;
				if (!note.equals(""))
					empty.note = new ResNote(note);
				encoding = empty;
			} else if (!NonHiero.isExtra(name)) {
				ResNamedglyph named = new ResNamedglyph(name);
				if (!note.equals(""))
					named.notes.add(new ResNote(note));
				encoding = named;
			}
			TreeSet<Integer> blobNums = new TreeSet<Integer>();
			blobNums.add(i);
			Item item = new Item("basic", blobNums, encoding, 0);
			basicItems.add(item);
		}
		return basicItems;
	}

	// Get name from blob.
	private String getName(Blob blob) {
		String name = blob.getName();
		if (name.equals("") && 
				blob.getGuessed() != null && blob.getGuessed().size() > 0)
			name = blob.getGuessed().get(0);
		if (name.equals(""))
			name = "\"?\"";
		return name;
	}

	// Get box name from blob (turn hwt into Hwt).
	private String getBoxName(Blob blob) {
		String name = blob.getName();
		if (name.equals("") && 
				blob.getGuessed() != null && blob.getGuessed().size() > 0)
			name = blob.getGuessed().get(0);
		if (name.equals(""))
			name = "cartouche";
		return name.replaceAll("^hwt", "Hwt");
	}

	// Add boxes separately to agenda first. Individual blobs follow.
	private Vector<Item> fillBoxAgenda(Vector<Blob> boxes, Vector<ResBox> boxEncodings) {
		Vector<Item> basicItems = new Vector<Item>();
		for (int i = 0; i < boxes.size(); i++) {
			Blob box = boxes.get(i);
			ResBox boxEncoding = boxEncodings.get(i);
			TreeSet<Integer> blobNums = new TreeSet<Integer>();
			int index = blobs.size();
			blobNums.add(index);
			blobs.add(box);
			Item item = new Item("basic", blobNums, boxEncoding, 0);
			basicItems.add(item);
		}
		return basicItems;
	}

	// Create key of item comprising all.
	private void makeRootItem() {
		TreeSet<Integer> rootNums = new TreeSet<Integer>();
		for (int i = 0; i < blobs.size(); i++) 
			rootNums.add(i);
		rootItem = new Item("fragment", rootNums, null, Integer.MAX_VALUE);
		keyToItem.put(rootItem.key(), rootItem);
	}

	// Place blobs in chunks.
	private void chunkBlobs() {
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = blobs.get(i);
			for (int chunk = minChunk(blob); chunk <= maxChunk(blob); chunk++) {
				if (chunkToBlobs.get(chunk) == null) 
					chunkToBlobs.put(chunk, new Vector<Integer>());
				chunkToBlobs.get(chunk).add(i);
				nChunks = Math.max(nChunks, chunk+1);
			}
		}
	}

	// Combine item with other items, until agenda is empty.
	// Do items covering few blobs first.
	private void parse() {
		for (int rank = 0; rank < blobs.size(); rank++) {
			if (agenda.get(rank) != null)
				for (Item item : agenda.get(rank)) {
					for (Item newItem : combineBinary(item)) {
						newItem = correct(newItem);
						store(newItem);
					}
					for (int chunk = item.minChunk; chunk <= item.maxChunk; chunk++) {
						if (chunkToItems.get(chunk) == null) 
							chunkToItems.put(chunk, new Vector<Item>());
						chunkToItems.get(chunk).add(item);
					}
				}
		}
	}

	// Main element of encoding.
	// If all fails, give linear sequence.
	public ResFragment encoding() {
		ResHieroglyphic hiero = rootItem != null ?
			(ResHieroglyphic) rootItem.encoding : null;
		ResFragment encoding = new ResFragment(hiero);
		if (encoding.nGlyphs() == 0 && blobs.size() != 0) {
			Vector<String> names = new Vector<String>();
			for (Blob blob : blobs) {
				String name = getName(blob);
				if (name.equals("unk"))
					names.add("\"?\"");
				else if (name.equals("shade"))
					names.add("empty[shade]");
				else if (!NonHiero.isExtra(name))
					names.add(name);
			}
			ResComposer composer = new ResComposer();
			encoding = composer.composeNames(names);
		} 
		if (direction.equals("hlr"))
			encoding.direction = ResValues.DIR_HLR;
		else if (direction.equals("hrl"))
			encoding.direction = ResValues.DIR_HRL;
		else if (direction.equals("vlr"))
			encoding.direction = ResValues.DIR_VLR;
		else if (direction.equals("vrl"))
			encoding.direction = ResValues.DIR_VRL;
		return encoding;
	}

	// Lookup corrections for item.
	private Item correct(Item item) {
		if (!item.type.equals("basic") && 
				!item.type.equals("hor") &&
				!item.type.equals("vert"))
			return item;
		GroupCorrection.Correction corr = 
			correction.correct((ResTopgroup) item.encoding);
		if (corr != null) {
			ResComposer composer = new ResComposer();
			ResTopgroup group = composer.makeTopgroup(corr.outGroup);
			if (group != null) {
				String type = group instanceof ResBasicgroup ? "basic" :
					(group instanceof ResHorgroup ? "hor" : "vert");
				return new Item(type, item.blobNums, group, item.cost + corr.penalty);
			}
		}
		return item;
	}

	/////////////////////////////////////////////////////
	// Parse table.

	// Parse item includes covered blobs.
	private class Item {
		public String type; // basic, hor, vert, fragment
		public TreeSet<Integer> blobNums;
		public Object encoding; // some RES element
		public int cost;
		// The minimum and maximum coordinates.
		public int minX = Integer.MAX_VALUE;
		public int maxX = Integer.MIN_VALUE;
		public int minY = Integer.MAX_VALUE;
		public int maxY = Integer.MIN_VALUE;
		// The minimum and maximum chunks covered.
		public int minChunk = Integer.MAX_VALUE;
		public int maxChunk = Integer.MIN_VALUE;
		// Other blobs form gaps needing to be filled.
		public boolean requiresInserts;
		// Can be segment prefix (not requiring inserts before end).
		public boolean isValidPrefix;

		// Constructor.
		public Item(String type, TreeSet<Integer> blobNums, Object encoding, int cost) {
			this.type = type;
			this.blobNums = blobNums;
			this.encoding = encoding;
			this.cost = cost;
			for (int i : blobNums)  {
				Blob blob = blobs.get(i);
				minX = Math.min(minX, blob.x());
				maxX = Math.max(maxX, blob.x() + blob.width());
				minY = Math.min(minY, blob.y());
				maxY = Math.max(maxY, blob.y() + blob.height());
				minChunk = Math.min(minChunk, SurfaceParser.this.minChunk(blob));
				maxChunk = Math.max(maxChunk, SurfaceParser.this.maxChunk(blob));
			}
			requiresInserts = requiresInserts();
			isValidPrefix = isValidPrefix();
		}

		// Number of included blobs minus one.
		public int rank() {
			return blobNums.size() - 1;
		}

		// The key of the item.
		public String key() {
			String k = type + ":";
			for (int i : blobNums) 
				k += "" + i + ".";
			return k;
		}

		private boolean requiresInserts() {
			TreeSet<Integer> bs = blobsIn(minChunk, maxChunk);
			for (int b : bs) 
				if (!blobNums.contains(b) &&
						includedIn(blobs.get(b), minX, maxX, minY, maxY)) 
					return true;
			return false;
		}
		private boolean isValidPrefix() {
			TreeSet<Integer> bs = blobsIn(0, maxChunk);
			for (int b : bs) 
				if (!blobNums.contains(b)) {
					Blob blob = blobs.get(b);
					int xMin = Math.min(minX, blob.x());
					int xMax = Math.max(maxX, blob.x() + blob.width());
					int yMin = Math.min(minY, blob.y());
					int yMax = Math.max(maxY, blob.y() + blob.height());
					if (horizontal && 
							includedIn(blob, 0, maxX, yMin, yMax))
						return false;
					else if (!horizontal &&
							includedIn(blob, xMin, xMax, 0, maxY)) 
						return false;
				}
			return true;
		}

		// For debugging.
		public String toString() {
			return key() + " " + cost + " " + encoding;
		}
	}

	// Store in agenda, if new item.
	private void store(Item item) {
		String key = item.key();
		Item oldItem = keyToItem.get(key);
		if (oldItem == null) {
			keyToItem.put(key, item);
			if (agenda.get(item.rank()) == null)
				agenda.put(item.rank(), new Vector<Item>());
			agenda.get(item.rank()).add(item);
		} else if (oldItem.cost >= item.cost) {
			oldItem.cost = item.cost;
			oldItem.encoding = item.encoding;
		} else
			return;
		for (Item newItem : closeUnary(item))
			store(newItem);
	}

	///////////////////////////////////////////////////////
	// Operations to combine item with other items.

	// Derive other item from this single item.
	private Vector<Item> closeUnary(Item item) {
		Vector<Item> items = new Vector<Item>();
		items.addAll(closeToFragment(item));
		return items;
	}

	// Some type of initial group is turned into a fragment.
	private Vector<Item> closeToFragment(Item item) {
		Vector<Item> items = new Vector<Item>();
		if (!item.type.equals("basic") && 
				!item.type.equals("hor") &&
				!item.type.equals("vert"))
			return items;
		int width = item.maxX - item.minX;
		int height = item.maxY - item.minY;
		int sizeCost = 0;
		if (horizontal) {
			sizeCost += Math.max(0, (unitSize / 2 - width) * 50 / unitSize);
			sizeCost += Math.max(0, (width - unitSize * 3 / 2) * 100 / unitSize);
		} else {
			sizeCost += Math.max(0, (unitSize / 2 - height) * 50 / unitSize);
			sizeCost += Math.max(0, (height - unitSize * 3 / 2) * 100 / unitSize);
		}
		int cost = item.cost + sizeCost;
		if (item.isValidPrefix) {
			ResTopgroup topgroup = (ResTopgroup) item.encoding;
			ResHieroglyphic encoding = new ResHieroglyphic(topgroup);
			items.add(new Item("fragment", item.blobNums, encoding, cost));
		}
		return items;
	}

	// Combine this item with existing items.
	private Vector<Item> combineBinary(Item item) {
		Vector<Item> items = new Vector<Item>();
		items.addAll(combineFragmentAndGroup(item));
		items.addAll(combineHorizontal(item));
		items.addAll(combineVertical(item));
		items.addAll(combineInsert(item));
		return items;
	}

	// Combine A1-B1:C1 and D1-E1 to A1-B1:C1 - D1-E1.
	private Vector<Item> combineFragmentAndGroup(Item item) {
		Vector<Item> items = new Vector<Item>();
		if (item.type.equals("fragment"))
			for (Item future : futureItems(item)) 
				items.addAll(combineFragmentAndGroup(item, future));
		else if (!item.requiresInserts)
			for (Item past : pastFragmentItems(item)) 
				items.addAll(combineFragmentAndGroup(past, item));
		return items;
	}
	private Vector<Item> combineFragmentAndGroup(Item item1, Item item2) {
		Vector<Item> items = new Vector<Item>();
		if (!item2.type.equals("basic") && 
				!item2.type.equals("hor") &&
				!item2.type.equals("vert"))
			return items;
		int width2 = item2.maxX - item2.minX;
		int height2 = item2.maxY - item2.minY;
		int penalty = horizontal ? 
			horPositioned(item1, item2) : 
			vertPositioned(item1, item2);
		if (penalty == Integer.MAX_VALUE) 
			return items;
		TreeSet<Integer> union = new TreeSet<Integer>();
		union.addAll(item1.blobNums);
		union.addAll(item2.blobNums);
		ResHieroglyphic encoding = (ResHieroglyphic) item1.encoding;
		encoding = (ResHieroglyphic) encoding.clone();
		ResTopgroup encoding2 = (ResTopgroup) item2.encoding;
		ResOp op = new ResOp();
		encoding.addGroup(op, encoding2);
		if (horizontal) {
		   if (horPosSep(item1, item2) < - 0.5)
			   op.fit = true;
		   else if (horPosSep(item1, item2) < 0.5)
			   op.sep = 0.5f;
		} else {
		   if (vertPosSep(item1, item2) < - 0.5)
			   op.fit = true;
		   else if (vertPosSep(item1, item2) < 0.5)
			   op.sep = 0.5f;
		}
		int sizeCost = 0;
		if (horizontal) {
			sizeCost += Math.max(0, (unitSize / 2 - width2) * 50 / unitSize);
			sizeCost += Math.max(0, (width2 - unitSize * 3 / 2) * 100 / unitSize);
		} else {
			sizeCost += Math.max(0, (unitSize / 2 - height2) * 50 / unitSize);
			sizeCost += Math.max(0, (height2 - unitSize * 3 / 2) * 100 / unitSize);
		}
		int cost = item1.cost + item2.cost + penalty + sizeCost;
		Item combined = new Item("fragment", union, encoding, cost);
		if (combined.isValidPrefix) {
			items.add(combined);
		}
		return items;
	}

	// Combine A1 and B1:C1 to A1*(B1:C1).
	private Vector<Item> combineHorizontal(Item item) {
		Vector<Item> items = new Vector<Item>();
		if (!item.requiresInserts)
			for (Item near : nearItems(item)) {
				items.addAll(combineHorizontal(item, near));
				items.addAll(combineHorizontal(near, item));
			}
		return items;
	}
	private Vector<Item> combineHorizontal(Item item1, Item item2) {
		Vector<Item> items = new Vector<Item>();
		if (!item1.type.equals("basic") && 
				!item1.type.equals("hor") &&
				!item1.type.equals("vert"))
			return items;
		if (!item2.type.equals("basic") && 
				!item2.type.equals("vert"))
			return items;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		if (item1.blobNums.size() > 1 &&
				(width1 > unitSize * 2 || height1 > unitSize * 2) ||
				item2.blobNums.size() > 1 &&
				(width2 > unitSize * 2 || height2 > unitSize * 2))
			return items;
		int penalty = horPositioned(item1, item2);
		if (penalty == Integer.MAX_VALUE) 
			return items;
		TreeSet<Integer> union = new TreeSet<Integer>();
		union.addAll(item1.blobNums);
		union.addAll(item2.blobNums);
		ResOp op = new ResOp();
		ResHorgroup encoding = null;
		ResHorsubgroupPart group2 = (ResHorsubgroupPart) item2.encoding;
		if (moveDown(item1, item2)) {
			ResVertsubgroupPart group2Vert = (ResVertsubgroupPart) group2;
			group2 = new ResVertgroup(new ResEmptyglyph(0, 0), new ResOp(), group2Vert);
		} else if (moveUp(item1, item2)) {
			ResVertsubgroupPart group2Vert = (ResVertsubgroupPart) group2;
			group2 = new ResVertgroup(group2Vert, new ResOp(), new ResEmptyglyph(0, 0));
		} 
		if (item1.type.equals("basic")) {
			ResBasicgroup group1 = (ResBasicgroup) item1.encoding;
			if (moveDown(item2, item1)) {
				ResVertgroup moved = 
					new ResVertgroup(new ResEmptyglyph(0, 0), new ResOp(), group1);
				encoding = new ResHorgroup(moved, op, group2);
			} else if (moveUp(item2, item1)) {
				ResVertgroup moved = 
					new ResVertgroup(group1, new ResOp(), new ResEmptyglyph(0, 0));
				encoding = new ResHorgroup(moved, op, group2);
			} else
				encoding = new ResHorgroup(group1, op, group2);
		} else if (item1.type.equals("vert")) {
			ResHorsubgroupPart group1 = (ResHorsubgroupPart) item1.encoding;
			encoding = new ResHorgroup(group1, op, group2);
		} else {
			encoding = (ResHorgroup) item1.encoding;
			encoding = (ResHorgroup) encoding.clone();
			encoding.addGroup(op, group2);
		}
		if (horPosSep(item1, item2) < - 0.5)
			op.fit = true;
		else if (horPosSep(item1, item2) < 0.5)
			op.sep = 0.5f;
		int cost = item1.cost + item2.cost + penalty;
		Item combined = new Item("hor", union, encoding, cost);
		items.add(combined);
		return items;
	}

	// Combine A1 and B1*C1 to A1-(B1*C1).
	private Vector<Item> combineVertical(Item item) {
		Vector<Item> items = new Vector<Item>();
		if (!item.requiresInserts)
			for (Item near : nearItems(item)) {
				items.addAll(combineVertical(item, near));
				items.addAll(combineVertical(near, item));
			}
		return items;
	}
	private Vector<Item> combineVertical(Item item1, Item item2) {
		Vector<Item> items = new Vector<Item>();
		if (!item1.type.equals("basic") && 
				!item1.type.equals("hor") &&
				!item1.type.equals("vert"))
			return items;
		if (!item2.type.equals("basic") && 
				!item2.type.equals("hor"))
			return items;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		if (item1.blobNums.size() > 1 &&
				(width1 > unitSize * 2 || height1 > unitSize * 2) ||
				item2.blobNums.size() > 1 &&
				(width2 > unitSize * 2 || height2 > unitSize * 2))
			return items;
		int penalty = vertPositioned(item1, item2);
		if (penalty == Integer.MAX_VALUE) 
			return items;
		TreeSet<Integer> union = new TreeSet<Integer>();
		union.addAll(item1.blobNums);
		union.addAll(item2.blobNums);
		ResOp op = new ResOp();
		ResVertgroup encoding = null;
		ResVertsubgroupPart group2 = (ResVertsubgroupPart) item2.encoding;
		if (moveRight(item1, item2)) {
			ResHorsubgroupPart group2Hor = (ResHorsubgroupPart) group2;
			group2 = new ResHorgroup(new ResEmptyglyph(0, 0), new ResOp(), group2Hor);
		} else if (moveLeft(item1, item2)) {
			ResHorsubgroupPart group2Hor = (ResHorsubgroupPart) group2;
			group2 = new ResHorgroup(group2Hor, new ResOp(), new ResEmptyglyph(0, 0));
		}
		if (item1.type.equals("basic")) { 
			ResBasicgroup group1 = (ResBasicgroup) item1.encoding;
			if (moveRight(item2, item1)) {
				ResHorgroup moved = 
					new ResHorgroup(new ResEmptyglyph(0, 0), new ResOp(), group1);
				encoding = new ResVertgroup(moved, op, group2);
			} else if (moveLeft(item2, item1)) {
				ResHorgroup moved = 
					new ResHorgroup(group1, new ResOp(), new ResEmptyglyph(0, 0));
				encoding = new ResVertgroup(moved, op, group2);
			} else
				encoding = new ResVertgroup(group1, op, group2);
		} else if (item1.type.equals("hor")) {
			ResVertsubgroupPart group1 = (ResVertsubgroupPart) item1.encoding;
			encoding = new ResVertgroup(group1, op, group2);
		} else {
			encoding = (ResVertgroup) item1.encoding;
			encoding = (ResVertgroup) encoding.clone();
			encoding.addGroup(op, group2);
		}
		if (vertPosSep(item1, item2) < - 0.5)
			op.fit = true;
		else if (vertPosSep(item1, item2) < 0.5)
			op.sep = 0.5f;
		int cost = item1.cost + item2.cost + penalty;
		Item combined = new Item("vert", union, encoding, cost);
		items.add(combined);
		return items;
	}

	// Combine A1 and B1:C1 to insert(A1,B1:C1).
	private Vector<Item> combineInsert(Item item) {
		Vector<Item> items = new Vector<Item>();
		for (Item near : nearItems(item)) 
			if (!near.requiresInserts)
				items.addAll(combineInsert(item, near));
		if (!item.requiresInserts)
			for (Item near : nearItems(item)) 
				items.addAll(combineInsert(near, item));
		return items;
	}

	// Combine A1 and B1*C1 to insert(A1, B1*B2).
	private Vector<Item> combineInsert(Item item1, Item item2) {
		Vector<Item> items = new Vector<Item>();
		if (!item1.type.equals("basic") && 
				!item1.type.equals("hor") &&
				!item1.type.equals("vert"))
			return items;
		if (!item2.type.equals("basic") && 
				!item2.type.equals("hor") &&
				!item2.type.equals("vert"))
			return items;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		if (item1.blobNums.size() > 1 &&
				(width1 > unitSize * 3 / 2 || height1 > unitSize * 3 / 2) ||
				item2.blobNums.size() > 1 &&
				(width2 > unitSize * 3 / 2 || height2 > unitSize * 3 / 2))
			return items;
		int penalty = insertPositioned(item1, item2);
		if (penalty == Integer.MAX_VALUE) 
			return items;
		TreeSet<Integer> union = new TreeSet<Integer>();
		union.addAll(item1.blobNums);
		union.addAll(item2.blobNums);
		ResTopgroup group1 = (ResTopgroup) item1.encoding;
		ResTopgroup group2 = (ResTopgroup) item2.encoding;
		ResBasicgroup encoding = null;
		String arg = insertLocation(item1, item2);
		if (arg.equals("stack"))
			encoding = new ResStack(group1, group2);
		else {
			ResInsert insert = new ResInsert(group1, group2);
			insert.place = arg;
			encoding = insert;
		}
		int cost = item1.cost + item2.cost + penalty;
		Item combined = new Item("basic", union, encoding, cost);
		items.add(combined);
		return items;
	}

	///////////////////////////////////////////////////////
	// Chunks.

	// The minimum and maximum chunks covered.
	private int minChunk(Blob blob) {
		if (horizontal)
			return (blob.x() - minPos) / unitSize;
		else
			return (blob.y() - minPos) / unitSize;
	}
	private int maxChunk(Blob blob) {
		if (horizontal)
			return (blob.x() + blob.width() - minPos) / unitSize;
		else
			return (blob.y() + blob.height() - minPos) / unitSize;
	}

	// Take union of all blobs in range of chunks.
	private TreeSet<Integer> blobsIn(int minChunk, int maxChunk) {
		TreeSet<Integer> all = new TreeSet<Integer>();
		for (int chunk = minChunk; chunk <= maxChunk; chunk++) 
			if (chunkToBlobs.get(chunk) != null)
				all.addAll(chunkToBlobs.get(chunk));
		return all;
	}

	// Find items near other item, with disjoint blobs.
	private Vector<Item> nearItems(Item item) {
		int minChunk = Math.max(item.minChunk - 1, 0);
		int maxChunk = Math.min(item.maxChunk + 1, nChunks - 1);
		HashSet<Item> items = new HashSet<Item>();
		for (int chunk = minChunk; chunk <= maxChunk; chunk++) 
			if (chunkToItems.get(chunk) != null)
				items.addAll(chunkToItems.get(chunk));
		Vector<Item> nearItems = new Vector<Item>();
		for (Item other : items) 
			if (Collections.disjoint(item.blobNums, other.blobNums) &&
					near(item, other))
				nearItems.add(other);
		return nearItems;
	}

	// Find item that is possible next group after fragment. 
	// We need to bridge gap between groups with nothing in between.
	private Vector<Item> futureItems(Item item) {
		int minChunk = Math.max(item.maxChunk - 1, 0);
		int maxChunk = Math.min(item.maxChunk + 1, nChunks - 1);
		while (maxChunk < nChunks-1 && 
				(chunkToBlobs.get(maxChunk) == null ||
				 chunkToBlobs.get(maxChunk).size() == 0))
			maxChunk++;
		HashSet<Item> items = new HashSet<Item>();
		for (int chunk = minChunk; chunk <= maxChunk; chunk++) 
			if (chunkToItems.get(chunk) != null)
				items.addAll(chunkToItems.get(chunk));
		Vector<Item> futureItems = new Vector<Item>();
		for (Item other : items) 
			if (Collections.disjoint(item.blobNums, other.blobNums) &&
					!other.type.equals("fragment"))
				futureItems.add(other);
		return futureItems;
	}

	// Find fragments preceding present item.
	private Vector<Item> pastFragmentItems(Item item) {
		int minChunk = Math.max(item.minChunk - 1, 0);
		int maxChunk = Math.min(item.minChunk + 1, nChunks - 1);
		while (minChunk > 0 &&
				(chunkToBlobs.get(minChunk) == null ||
				 chunkToBlobs.get(minChunk).size() == 0))
			minChunk--;
		HashSet<Item> items = new HashSet<Item>();
		for (int chunk = minChunk; chunk <= maxChunk; chunk++) 
			if (chunkToItems.get(chunk) != null)
				items.addAll(chunkToItems.get(chunk));
		Vector<Item> pastItems = new Vector<Item>();
		for (Item other : items) 
			if (Collections.disjoint(item.blobNums, other.blobNums) &&
					other.type.equals("fragment"))
				pastItems.add(other);
		return pastItems;
	}

	///////////////////////////////////////////////////////
	// Geometric operations and tests.

	// Comparing glyphs with x position.
	private class XComparator implements Comparator<Blob> {
		public int compare(Blob b1, Blob b2) {
			if (b1.x() < b2.x())
				return -1;
			else if (b1.x() > b2.x())
				return 1;
			else if (b1.y() < b2.y())
				return -1;
			else if (b1.y() > b2.y())
				return 1;
			else
				return 0;
		}
	}
	// Comparing glyphs with y position.
	private class YComparator implements Comparator<Blob> {
		public int compare(Blob b1, Blob b2) {
			if (b1.y() < b2.y())
				return -1;
			else if (b1.y() > b2.y())
				return 1;
			else if (b1.x() < b2.x())
				return -1;
			else if (b1.x() > b2.x())
				return 1;
			else
				return 0;
		}
	}

	// Is blob (mostly) included in other blob?
	private boolean includedIn(Blob inner, Blob outer) {
		return includedIn(inner, outer.x(), outer.x() + outer.width(),
				outer.y(), outer.y() + outer.height());
	}

	// Is blob (mostly) included in surface?
	private boolean includedIn(Blob inner, int xMin, int xMax, int yMin, int yMax) {
		double includeRatio = 0.7;
		int xStart = Math.max(inner.x(), xMin);
		int xEnd = Math.min(inner.x() + inner.width(), xMax);
		int yStart = Math.max(inner.y(), yMin);
		int yEnd = Math.min(inner.y() + inner.height(), yMax);
		return xStart < xEnd &&
			yStart < yEnd &&
			(xEnd-xStart) * 1.0 / inner.width() > includeRatio &&
			(yEnd-yStart) * 1.0 / inner.height() > includeRatio;
	}

	// Are two items not further than unit size apart?
	private boolean near(Item item1, Item item2) {
		return 
			item2.minX < item1.maxX + unitSize &&
			item1.minX < item2.maxX + unitSize &&
			item2.minY < item1.maxY + unitSize &&
			item1.minY < item2.maxY + unitSize;
	}

	// Penalty of two items being horizontally arranged.
	// There is penalty for difference in centre points
	// normalized to average height, up to 100.
	// There is penalty for overlap, normalized to width of
	// first group, up to 100.
	// There is also penalty for distance being too big.
	// Maximum penalty for second before first, or more than
	// unit size before.
	private int horPositioned(Item item1, Item item2) {
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int height = (height1 + height2) / 2;
		if (item2.maxX < item1.maxX || 
				item2.minX < Math.max(item1.minX, item1.maxX - unitSize))
			return Integer.MAX_VALUE;
		int xPenalty = 0;
		int yPenalty = 0;
		if (item2.minX < item1.maxX && width1 > 0)
			xPenalty += (item1.maxX - item2.minX) * 100 / width1;
		if (item2.minX > item1.maxX + unitSize / 2 && width1 > 0)
			xPenalty += (item2.minX - item1.maxX - unitSize / 2) * 200 / width1;
		int center1 = item1.minY + height1 / 2;
		int center2 = item2.minY + height2 / 2;
		if (height > 0)
			yPenalty += Math.abs(center1 - center2) * 100 / 
				Math.max(height, unitSize / 2);
		if (item1.minY > item2.maxY)
			yPenalty += (item1.minY - item2.maxY) * 300 / unitSize;
		if (item2.minY > item1.maxY)
			yPenalty += (item2.minY - item1.maxY) * 300 / unitSize;
		return xPenalty + yPenalty;
	}
	// Same for vertical arrangement.
	private int vertPositioned(Item item1, Item item2) {
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int width = (width1 + width2) / 2;
		if (item2.maxY < item1.maxY || 
				item2.minY < Math.max(item1.minY, item1.maxY - unitSize))
			return Integer.MAX_VALUE;
		int xPenalty = 0;
		int yPenalty = 0;
		if (item2.minY < item1.maxY && height1 > 0)
			yPenalty += (item1.maxY - item2.minY) * 100 / height1;
		if (item2.minY > item1.maxY + unitSize / 2 && height1 > 0)
			yPenalty += (item2.minY - item1.maxY - unitSize / 2) * 200 / height1;
		int center1 = item1.minX + width1 / 2;
		int center2 = item2.minX + width2 / 2;
		if (width > 0)
			xPenalty += Math.abs(center1 - center2) * 100 / 
				Math.max(width, unitSize / 2);
		if (item1.minX > item2.maxX)
			xPenalty += (item1.minX - item2.maxX) * 300 / unitSize;
		if (item2.minX > item1.maxX)
			xPenalty += (item2.minX - item1.maxX) * 300 / unitSize;
		return xPenalty + yPenalty;
	}

	// For horizontal positioning, the second item may be small and low,
	// below half-way point of first group.
	// This can be solved by having A1*(.:B2) instead of A1*B2.
	private boolean moveDown(Item item1, Item item2) {
		if (item2.blobNums.size() > 1)
			return false;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int mid1 = item1.minY + height1 / 2;
		int down = item1.minY + height1 * 4 / 5;
		return height2 < height1 / 2 && item2.minY > mid1 && item2.maxY > down;
	}
	private boolean moveUp(Item item1, Item item2) {
		if (item2.blobNums.size() > 1)
			return false;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int up = item1.minY + height1 / 5;
		int mid1 = item1.minY + height1 / 2;
		return height2 < height1 / 2 && item2.minY < up && item2.maxY < mid1;
	}
	private boolean moveRight(Item item1, Item item2) {
		if (item2.blobNums.size() > 1)
			return false;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int mid1 = item1.minX + width1 / 2;
		int right = item1.minX + width1 * 4 / 5;
		return width2 < width1 / 2 && item2.minX > mid1 && item2.maxX > right;
	}
	private boolean moveLeft(Item item1, Item item2) {
		if (item2.blobNums.size() > 1)
			return false;
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int left = item1.minX + width1 / 5;
		int mid1 = item1.minX + width1 / 2;
		return width2 < width1 / 2 && item2.minX < left && item2.maxX < mid1;
	}

	// If in horizontal arrangement, what is separation, as factor of sep?
	private double horPosSep(Item item1, Item item2) {
		return 1.0 * (item2.minX - item1.maxX) / sep;
	}
	// And for vertical.
	private double vertPosSep(Item item1, Item item2) {
		return 1.0 * (item2.minY - item1.maxY) / sep;
	}

	// Penalty for inserting group into other group.
	// Ideally, the first group is a single hieroglyph;
	// above that penalties are added.
	// Ideally, the second is contained in the first,
	// the distances outside are another penalty.
	private int insertPositioned(Item item1, Item item2) {
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int width = Math.min(width1, width2);
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int height = Math.min(height1, height2);
		int insertPenalty = 10;
		int numPenalty = Math.max(0, (item1.blobNums.size() - 1) * 50);
		int outsidePenalty = 0;
		int xBefore = Math.max(0, item1.minX - item2.minX);
		int xAfter = Math.max(0, item2.minX + width2 - item1.minX - width1);
		int yBefore = Math.max(0, item1.minY - item2.minY);
		int yAfter = Math.max(0, item2.minY + height2 - item1.minY - height1);
		if (1.0 * xBefore / width > 0.3 ||
				1.0 * xAfter / width > 0.3 ||
				1.0 * (xBefore + xAfter) / width > 0.4 ||
				1.0 * yBefore / height > 0.3 ||
				1.0 * yAfter / height > 0.3 ||
				1.0 * (yBefore + yAfter) / height > 0.4)
			return Integer.MAX_VALUE;
		float xOutside = Math.max(0, 1.0f * (xBefore + xAfter) / width2 - 0.2f);
		float yOutside = Math.max(0, 1.0f * (yBefore + yAfter) / height2 - 0.2f);
		outsidePenalty += Math.round(xOutside * 100 + yOutside * 100);
		return insertPenalty + numPenalty + outsidePenalty;
	}

	// Assuming an insert, where is item2 in item1?
	private String insertLocation(Item item1, Item item2) {
		int width1 = item1.maxX - item1.minX;
		int width2 = item2.maxX - item2.minX;
		int height1 = item1.maxY - item1.minY;
		int height2 = item2.maxY - item2.minY;
		int xMinDist = item2.minX - item1.minX;
		int xMaxDist = item1.minX + width1 - item2.minX - width2;
		int yMinDist = item2.minY - item1.minY;
		int yMaxDist = item1.minY + height1 - item2.minY - height2;
		double xMinRatio = 1.0 * xMinDist / width1;
		double xMaxRatio = 1.0 * xMaxDist / width1;
		double yMinRatio = 1.0 * yMinDist / height1;
		double yMaxRatio = 1.0 * yMaxDist / height1;
		double distLimitBig = 0.2;
		double distLimitSmall = 0.1;
		if (xMinRatio < distLimitSmall && xMaxRatio >= distLimitBig) {
			if (yMinRatio < distLimitSmall && yMaxRatio >= distLimitBig) 
				return "ts";
			else if (yMinRatio >= distLimitBig && yMaxRatio < distLimitSmall) 
				return "bs";
			else
				return "s";
		} else if (xMinRatio >= distLimitBig && xMaxRatio < distLimitSmall) {
			if (yMinRatio < distLimitSmall && yMaxRatio >= distLimitBig) 
				return "te";
			else if (yMinRatio >= distLimitBig && yMaxRatio < distLimitSmall) 
				return "be";
			else
				return "e";
		} else {
			if (yMinRatio < distLimitSmall && yMaxRatio >= distLimitBig) 
				return "t";
			else if (yMinRatio >= distLimitBig && yMaxRatio < distLimitSmall) 
				return "b";
		} 
		if (xMinRatio > 0 && xMaxRatio > 0 && yMinRatio <= 0 && yMaxRatio <= 0 ||
				xMinRatio <= 0 && xMaxRatio <= 0 && yMinRatio > 0 && yMaxRatio > 0)
			return "stack";
		else
			return "";
	}

}
