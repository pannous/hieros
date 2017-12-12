package nederhof.ocr.images;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

// Wrapper for AveragedImage, which only creates the image once enough
// examples have been gathered.
public class DelayedAveragedImage {

	// From when create AveragedImage?
	private final int maxBuffered = 3;

	// Averaged image, if it has been created.
	protected AveragedImage averaged = null;

	// Images not yet incorporated into averaged image.
	protected Vector<BinaryImage> buffered = new Vector<BinaryImage>();

	// Construct with first image.
	public DelayedAveragedImage(BinaryImage image) {
		add(image);
	}

	// Add image.
	public void add(BinaryImage image) {
		if (averaged != null)
			averaged.add(image.toBufferedImage());
		else {
			buffered.add(image);
			if (buffered.size() >= maxBuffered)
				createAveraged();
		}
	}

	// Create averaged from buffered, taking averages of dimensions.
	public void createAveraged() {
		int widthSum = 0;
		double ratioSum = 0;
		for (int i = 0; i < buffered.size(); i++) {
			BinaryImage image = buffered.get(i);
			widthSum += image.width();
			ratioSum += 1.0 * image.height() / image.width();
		}
		int width = (int) Math.round(widthSum / buffered.size());
		double ratio = ratioSum / buffered.size();
		int height = (int) Math.round(width * ratio);
		averaged = new AveragedImage(width, height);
		for (int i = 0; i < buffered.size(); i++)
			averaged.add(buffered.get(i).toBufferedImage());
		buffered.clear();
	}

	// Return averaged image.
	public BinaryImage averaged() {
		if (averaged == null)
			createAveraged();
		return averaged.averaged();
	}
	public BinaryImage centerAveraged() {
		if (averaged == null)
			createAveraged();
		return averaged.centerAveraged();
	}

}
