/***************************************************************************/
/*                                                                         */
/*  MovedBuffer.java                                                       */
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

// Like BufferedImage, but the (0,0) coordinate need not be in upper
// left corner, and conceptual image is subimage.

package nederhof.res;

import java.awt.*;
import java.awt.image.*;

public class MovedBuffer {

    // Actual image.
    private BufferedImage image;

    // Rectangle inside ImageBuffer that is conceptual image.
    private Rectangle rect;

    // We keep information whether image mirrored, in order to
    // determine correct location of margins.
    private boolean mirror;

    // Copy image into MovedBuffer.
    protected MovedBuffer(BufferedImage image,
	    int x, int y, int width, int height,
	    boolean mirror) { 
	this.image = image;
	rect = new Rectangle(x, y, width, height);
	this.mirror = mirror;
    }

    // Image.
    public BufferedImage getImage() {
	return image;
    }

    // Real width.
    public int getWidth() {
	return image.getWidth();
    }

    // Real height.
    public int getHeight() {
	return image.getHeight();
    }

    // Get margins around image, in pixels.
    public Insets margins() {
	int left = (mirror ? getWidth() - rect.width - rect.x : rect.x);
	int right = (mirror ? rect.x : getWidth() - rect.width - rect.x);
	int top = rect.y;
	int bottom = getHeight() - rect.height - rect.y;
	return new Insets(top, left, bottom, right);
    }

    //////////////////////////////////////////////////////////////////////////
    // Fitting. Two images are "fitted", which means that the shortest distance between
    // pixels is determined.

    // Everything not equal to this is regarded non-white pixel.
    private static int BACKGROUND = Color.WHITE.getRGB();

    ///////////////////////////////////////////////////////////////////////////
    // General purpose.

    // White image of dimensions.
    public static BufferedImage whiteImage(HieroRenderContext context,
	    int width, int height) {
	width = Math.max(width, 1);
	height = Math.max(height, 1);
	BufferedImage image = new BufferedImage(width, height, context.imageType());
	Graphics2D g = image.createGraphics();
	g.setColor(Color.WHITE);
	g.fillRect(0, 0, width, height);
	g.dispose();
	return image;
    }

}
