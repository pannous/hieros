/***************************************************************************/
/*                                                                         */
/*  Pnm.java                                                               */
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

// Writing image in PNM.
//
// Following specifications found at:
// http://www.acme.com/software/pbmplus/

package nederhof.images;

import java.awt.image.*;
import java.io.*;

class Pnm {

    public static boolean write(BufferedImage im, OutputStream output) 
    throws IOException {
	if (im.getType() == BufferedImage.TYPE_BYTE_GRAY) {
	    writeGray(im, output);
	    return true;
	} else if (im.getType() == BufferedImage.TYPE_INT_RGB) {
	    writeRGB(im, output);
	    return true;
	} else
	    return false;
    }

    private static void writeGray(BufferedImage im, OutputStream output) 
    throws IOException {
	output.write("P5\n".getBytes());
	output.write((im.getWidth() + " " + im.getHeight() + "\n").getBytes());
	output.write("255\n".getBytes());
	Raster r = im.getWritableTile(0, 0);
	byte[] bytes = (byte[]) 
	    (r.getDataElements(0, 0, im.getWidth(), im.getHeight(), null));
	int i = 0;
	for (int y = 0; y < im.getHeight(); y++)
	    for (int x = 0; x < im.getWidth(); x++) 
		output.write(bytes[i++]);
    }

    private static void writeRGB(BufferedImage im, OutputStream output) 
    throws IOException {
	output.write("P6\n".getBytes());
	output.write((im.getWidth() + " " + im.getHeight() + "\n").getBytes());
	output.write("255\n".getBytes());
	for (int y = 0; y < im.getHeight(); y++)
	    for (int x = 0; x < im.getWidth(); x++) {
		int rgb = im.getRGB(x, y);
		int r = (rgb & 0xff0000) >>> 16;
		int g = (rgb & 0x00ff00) >>> 8;
		int b = (rgb & 0x0000ff);
		output.write(r);
		output.write(g);
		output.write(b);
	    }
    }
}
