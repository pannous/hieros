package nederhof.ocr.images;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// Utilities for manipulating BufferedImages.
public class ImageUtil {

    // Turn to grayscale.
    public static BufferedImage convertToGrayscale(BufferedImage im) {
	// Replaced, because seemed to give null-pointer exception
	// on some platforms.
        // BufferedImageOp op = new ColorConvertOp(
		// ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        // return op.filter(im, null);

	BufferedImage gray = new BufferedImage(im.getWidth(), im.getHeight(),  
		      BufferedImage.TYPE_BYTE_GRAY);  
	Graphics g = gray.getGraphics();  
	g.drawImage(im, 0, 0, null);  
	g.dispose();  
	return gray;
    }

    // Scaling to given width, assuming binary.
    public static BufferedImage scaleToWidth(BufferedImage buffer,
            int width) {
        double scale = width * 1.0 / buffer.getWidth();
        int height = (int) Math.round(buffer.getHeight() * scale);
	return scale(buffer, width, height);
    }

    // Scaling to given height, assuming binary.
    public static BufferedImage scaleToHeight(BufferedImage buffer,
            int height) {
        double scale = height * 1.0 / buffer.getHeight();
        int width = (int) Math.round(buffer.getWidth() * scale);
	return scale(buffer, width, height);
    }

    // Scaling to given width and height, assuming binary.
    public static BufferedImage scale(BufferedImage buffer,
	    int width, int height) {
        BufferedImage scaled = new BufferedImage(
                width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(buffer, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    // Scaling by factor, assuming binary.
    public static BufferedImage scale(BufferedImage buffer, double scale) {
	int width = (int) Math.round(buffer.getWidth() * scale);
	int height = (int) Math.round(buffer.getHeight() * scale);
	width = Math.max(1, width);
	height = Math.max(1, height);
	return scale(buffer, width, height);
    }

}
