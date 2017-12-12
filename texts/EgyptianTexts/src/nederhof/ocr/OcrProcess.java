// Prmcess doing OCR in background.

package nederhof.ocr;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import nederhof.ocr.guessing.*;
import nederhof.ocr.images.*;
import nederhof.util.*;

public class OcrProcess extends PriorityBlockingQueue<OcrProcessTask> implements Runnable {

	// The OCR guesser.
	private OcrGuesser guesser;

	// The formatter of blobs.
	private BlobFormatter formatter;

	// The thread running this.
	private Thread thread;

	public OcrProcess(OcrGuesser guesser, BlobFormatter formatter) throws IOException {
		this.guesser = guesser;
		this.formatter = formatter;
		thread = new Thread(this);
		thread.start();
	}

	// Change to different guesser.
	public void setGuesser(OcrGuesser guesser) {
		this.guesser = guesser;
	}

	// Repeatedly take one blob from queue and do OCR.
	public void run() {
		while (true) {
			try {
				OcrProcessTask task = take();
				if (task instanceof Blob) {
					Blob glyph = (Blob) task;
					int unit = glyph.getUnitSize();
					float relativeHeight = 
						unit < 0 ? -1 : 1.0f * glyph.height() / unit;
					Vector<String> names = 
						guesser.findBestNames(glyph.im(), relativeHeight);
					glyph.setGuessed(names);
				} else if (task instanceof Line) {
					Line line = (Line) task;
					if (line.formatted.isEmpty()) 
						line.formatted = 
							formatter.toFormats(line.aliveGlyphs(), line.dir);
				} else if (task instanceof GlyphCombiner) {
					GlyphCombiner combiner = (GlyphCombiner) task;
					combiner.combineLine(guesser);
				}
			} catch (InterruptedException e) {
				break;
			} catch (IOException e) {
				System.err.println(e.getMessage());
				break;
			}
		}
	}

	// Mark glyph if entered in queue.
	public boolean offer(OcrProcessTask task) throws NullPointerException {
		if (task instanceof Blob) {
			Blob glyph = (Blob) task;
			glyph.setInQueue();
		}
		return super.offer(task);
	}

	// Stop this.
	public void quit() {
		try {
			thread.interrupt();
		} catch (SecurityException e) {
			// ignore
		}
	}

} 

