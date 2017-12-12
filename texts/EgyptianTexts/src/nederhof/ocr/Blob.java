package nederhof.ocr;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.images.*;
import nederhof.util.*;

// Records about blob in OCR. Could be glyphs, or noise.

public class Blob extends OcrProcessTask {

	// Page name where it is found.
	private String page = "";

	// File where it is found as image, and (x,y) on page of corner.
	private File file = null;
	private int x;
	private int y;

	// Cached. If not cached then dummy value.
	private final int NO_INT = Integer.MAX_VALUE;
	private int width = NO_INT;
	private int height = NO_INT;

	// Cached image.
	private BinaryImage im = null;

	// Name, or empty if no name is confirmed.
	private String name = "";

	// Guessed names (by OCR). In decreasing order of probability.
	private Vector<String> guessed = null;

	// Any note about glyph.
	private String note = "";

	// Is in queue for OCR.
	private boolean inQueue = false;

	// Is obsolete?
	// This is a clean way to let the OCR thread remove a sign
	// from a line without risking ConcurrentModificationException
	// in main thread.
	private boolean obsolete = false;

	// The hypothesized unit size of the context,
	// if available, otherwise negative.
	private int unitSize = -1;

	// Constructor when already stored in file.
	public Blob(File file, int x, int y) {
		if (!file.equals(""))
			this.file = file;
		this.x = x;
		this.y = y;
	}

	// To be used for pixels.
	public Blob(Vector<Point> points) {
		if (points.size() == 0) {
			// to be safe
			im = new BinaryImage(1, 1);
			x = 0;
			y = 0;
			width = 1;
			height = 1;
			return;
		}
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
		for (Point p : points) {
			xMin = Math.min(xMin, p.x);
			xMax = Math.max(xMax, p.x);
			yMin = Math.min(yMin, p.y);
			yMax = Math.max(yMax, p.y);
		}
		x = xMin;
		y = yMin;
		width = xMax - xMin + 1;
		height = yMax - yMin + 1;
		im = new BinaryImage(width, height);
		for (Point p : points) 
			im.set(p.x - x, p.y - y, true);
	}

	// Empty blob of indicated size and location.
	public Blob(int x, int y, int width, int height) {
		im = new BinaryImage(width, height);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	// Copy of pixels.
	public Blob pixelCopy() {
		Blob copy = new Blob(x(), y(), width(), height());
		BinaryImage.superimpose(copy.imSafe(), imSafe(), 0, 0);
		copy.unitSize = unitSize;
		return copy;
	}

	// Copy properties from other blob.
	public void copyProperties(Blob other) {
		remove();
		this.x = other.x;
		this.y = other.y;
		this.width = other.width;
		this.height = other.height;
		this.im = other.im;
		this.guessed = other.guessed;
		this.name = other.name;
		this.obsolete = other.obsolete;
		this.unitSize = other.unitSize;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setGuessed(Vector<String> guessed) {
		this.guessed = guessed;
	}
	public Vector<String> getGuessed() {
		return guessed;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getNote() {
		return note;
	}
	public void setInQueue() {
		this.inQueue = true;
	}
	public boolean inQueue() {
		return inQueue;
	}
	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete() {
		obsolete = true;
	}

	public void setUnitSize(int unit) {
		unitSize = unit;
	}

	public int getUnitSize() {
		return unitSize;
	}

	public void setPage(String page) {
		this.page = page;
	}
	public String page() {
		return page;
	}
	public int x() {
		return x;
	}
	public int y() {
		return y;
	}
	public int width() {
		if (width == NO_INT) 
			try {
				width = im().width();
			} catch (IOException e) {
				width = 1;
			}
		return width;
	}
	public int height() {
		if (height == NO_INT) 
			try {
				height = im().height();
			} catch (IOException e) {
				height = 1;
			}
		return height;
	}

	// Is one blob properly included in another?
	public boolean includes(Blob other) {
		return x() <= other.x() && other.x() + other.width() <= x() + width() &&
			y() <= other.y() && other.y() + other.height() <= y() + height();
	}

	// Get value at pixel. Relative to corner.
	public boolean get(int px, int py) {
		return imSafe().getSafe(px - x, py - y);
	}

	// Set value at pixel, relative to corner. Adapt image size if needed.
	public void set(int px, int py, boolean val) {
		if (val) {
			if (px < x() || py < y() || 
					px >= x() + width() || py >= y() + height()) {
				int xMin = Math.min(x(), px);
				int xMax = Math.max(x() + width(), px + 1);
				int yMin = Math.min(y(), py);
				int yMax = Math.max(y() + height(), py + 1);
				int newWidth = xMax-xMin;
				int newHeight = yMax-yMin;
				BinaryImage newIm = new BinaryImage(newWidth, newHeight);
				BinaryImage.superimpose(newIm, imSafe(), x - xMin, y - yMin);
				im = newIm;
				x = xMin;
				y = yMin;
				width = newWidth;
				height = newHeight;
			} 
			im.set(px-x, py-y, val);
		} else {
			imSafe().setSafe(px-x(), py-y(), val);
			int left = emptyLeft();
			int right = emptyRight();
			int top = emptyTop();
			int bottom = emptyBottom();
			if (left + right >= width()) {
				im = new BinaryImage(1, 1);
				width = 1;
				height = 1;
			} else if (left != 0 || right != 0 || top != 0 || bottom != 0) {
				int newWidth = width() - left - right;
				int newHeight = height() - top - bottom;
				BinaryImage newIm = new BinaryImage(newWidth, newHeight);
				BinaryImage.superimpose(newIm, imSafe(), -left, -top);
				im = newIm;
				x = x + left;
				y = y + top;
				width = newWidth;
				height = newHeight;
			}
		}
	}
	// Number of columns/rows that are empty.
	private int emptyLeft() {
		int n = 0;
		for (int x = 0; x < width(); x++)
			if (!columnEmpty(x))
				return n;
			else 
				n++;
		return n;
	}
	private int emptyRight() {
		int n = 0;
		for (int x = width()-1; x >= 0; x--)
			if (!columnEmpty(x))
				return n;
			else 
				n++;
		return n;
	}
	private int emptyTop() {
		int n = 0;
		for (int y = 0; y < height(); y++)
			if (!rowEmpty(y))
				return n;
			else 
				n++;
		return n;
	}
	private int emptyBottom() {
		int n = 0;
		for (int y = height()-1; y >= 0; y--)
			if (!rowEmpty(y))
				return n;
			else 
				n++;
		return n;
	}
	private boolean columnEmpty(int x) {
		for (int y = 0; y < height(); y++)
			if (imSafe().get(x, y))
				return false;
		return true;
	}
	private boolean rowEmpty(int y) {
		for (int x = 0; x < width(); x++)
			if (imSafe().get(x, y))
				return false;
		return true;
	}

	// The black pixels as points.
	public Vector<Point> points() {
		Vector<Point> points = new Vector<Point>();
		for (int px = 0; px < width(); px++)
			for (int py = 0; py < height(); py++)
				if (imSafe().get(px, py))
					points.add(new Point(px + x, py + y));
		return points;
	}

	// Empty if consisting of single white dot.
	public boolean isEmpty() {
		return width() == 1 && height() == 1 && !imSafe().get(0, 0);
	}

	// Return (cached) image.
	public BinaryImage im() throws IOException {
		if (im == null)
			im = new BinaryImage(file);
		return im;
	}

	// Get image without worrying about file.
	public BinaryImage imSafe() {
		try {
			return im();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return new BinaryImage(1, 1);
		}
	}

	// File name without extension.
	public String getBaseName() {
		if (file == null) 
			return "";
		else
			return FileAux.removeExtension(file.getName());
	}

	// Is already saved?
	public boolean isSaved() {
		return file != null;
	}

	// Store.
	public void save(File file) throws IOException {
		this.file = file;
		im().write(file);
	}

	// File (or null if not saved).
	public File file() {
		return file;
	}

	// Remove from file. Return whether succeeded.
	public boolean remove() {
		if (file != null)
			try {
				if (file.delete()) {
					file = null;
					return true;
				}
			} catch (SecurityException e) { }
		return false;
	}

	// Is same blob?
	public boolean equalTo(Blob blob) {
		try {
			return x() == blob.x() && y() == blob.y() &&
				width() == blob.width() && height() == blob.height() &&
				im().equalTo(blob.im());
		} catch (IOException e) { }
		return false;
	}

}
