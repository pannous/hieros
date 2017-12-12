package nederhof.ocr.images.distance;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;

import nederhof.ocr.images.*;
import nederhof.util.*;

// Image distortion model, giving distance between images.
public abstract class DistortionModel {

	// Get preprocessed form of one image.
	public abstract BinaryImage binImage(BufferedImage im);

	// Get distance between two images.
	public float distort(BufferedImage im1, BufferedImage im2) {
		return distort(binImage(im1), im2);
	}
	// Get distance between two images.
	public float distort(BinaryImage im1, BufferedImage im2) {
		return distort(im1, binImage(im2));
	}
	// Get distance between two images.
	public abstract float distort(BinaryImage im1, BinaryImage im2);

}
