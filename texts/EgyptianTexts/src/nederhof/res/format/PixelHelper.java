/***************************************************************************/
/*                                                                         */
/*  PixelHelper.java                                                       */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res.format;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import nederhof.res.*;

// Operations on the pixel level.
class PixelHelper {

    // Everything not equal to this is regarded non-white pixel.
    private static int BACKGROUND = Color.WHITE.getRGB();

    // Should pixel be regarded as blank?
    public static boolean isBlank(BufferedImage im, int x, int y) {
	return im.getRGB(x, y) == BACKGROUND;
    }

    // Distance from last non-white pixel to end at y.
    private static int lastNonWhiteHor(BufferedImage im, int y) {
        int d = 0;
        for (int x = im.getWidth() - 1; x >= 0 && isBlank(im, x, y); x--)
            d++;
        return d;
    }
    // Distance from beginning to first non-white pixel at y.
    private static int firstNonWhiteHor(BufferedImage im, int y) {
        int x = 0;
        while (x < im.getWidth() && isBlank(im, x, y))
            x++;
        return x;
    }
    // As above.
    private static int lastNonWhiteVert(BufferedImage im, int x) {
        int d = 0;
        for (int y = im.getHeight() - 1; y >= 0 && isBlank(im, x, y); y--)
            d++;
        return d;
    }
    private static int firstNonWhiteVert(BufferedImage im, int x) {
        int y = 0;
        while (y < im.getHeight() && isBlank(im, x, y))
            y++;
        return y;
    }

    /////////////////////////////////////////////////////////////
    // Auras.

    // Extend image to left by sep, and make circles with radius sep
    // around left-most pixels.
    private static BufferedImage makeAuraHor(HieroRenderContext context,
            BufferedImage im, int sep) {
        if (sep == 0)
            return im;
        BufferedImage aura =
            MovedBuffer.whiteImage(context, im.getWidth() + sep, im.getHeight());
        Graphics2D g = aura.createGraphics();
        g.setColor(Color.BLACK);
        for (int y = 0; y < im.getHeight(); y++) {
            int x = firstNonWhiteHor(im, y);
            g.drawOval(x, y - sep, 2 * sep, 2 * sep);
        }
        g.dispose();
        return aura;
    }
    // As above.
    private static BufferedImage makeAuraVert(HieroRenderContext context,
            BufferedImage im, int sep) {
        if (sep == 0)
            return im;
        BufferedImage aura =
            MovedBuffer.whiteImage(context, im.getWidth(), im.getHeight() + sep);
        Graphics2D g = aura.createGraphics();
        g.setColor(Color.BLACK);
        for (int x = 0; x < im.getWidth(); x++) {
            int y = firstNonWhiteVert(im, x);
            g.drawOval(x - sep, y, 2 * sep, 2 * sep);
        }
        g.dispose();
        return aura;
    }

    /////////////////////////////////////////////////////////////
    // Fitting.

    // Fitting of two images horizontally, of same height.
    // Returned is the distance that the second image is to be moved left
    // i.e. a negative value. If it is moved right, a positive value is
    // returned. Moving left can never be more than max.
    // To simplify measuring the shortest distance between pixels, we first
    // make an "aura" around the left-most pixels of the second image.
    public static int fitHor(HieroRenderContext context,
            BufferedImage im1, BufferedImage im2, int sep, int max) {
        BufferedImage aura = makeAuraHor(context, im2, sep);
        int fit = max;
        int height = Math.min(im1.getHeight(), aura.getHeight());
        for (int y = 0; y < height; y++) {
            int fit1 = lastNonWhiteHor(im1, y);
            int fit2 = firstNonWhiteHor(im2, y);
            fit = Math.min(fit, fit1 + fit2 - sep);
        }
        return -fit;
    }
    // Fitting of two images vertically, of same width.
    public static int fitVert(HieroRenderContext context,
            BufferedImage im1, BufferedImage im2, int sep, int max) {
        BufferedImage aura = makeAuraVert(context, im2, sep);
        int fit = max;
        int width = Math.min(im1.getWidth(), aura.getWidth());
        for (int x = 0; x < width; x++) {
            int fit1 = lastNonWhiteVert(im1, x);
            int fit2 = firstNonWhiteVert(im2, x);
            fit = Math.min(fit, fit1 + fit2 - sep);
        }
        return -fit;
    }

}
