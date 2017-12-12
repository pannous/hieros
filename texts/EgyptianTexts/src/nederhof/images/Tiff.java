/***************************************************************************/
/*                                                                         */
/*  Tiff.java                                                              */
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

// Writing image in TIFF.
//
// Following the TIFF 6.0 Specification, found at:
// http://partners.adobe.com/asn/developer/graphics/graphics.html.

package nederhof.images;

import java.awt.image.*;
import java.io.*;

class Tiff {

    // Constants in TIFF encoding.
    private static int SHORT = 3;
    private static int LONG = 4;
    private static int RATIONAL = 5;

    public static boolean write(BufferedImage im, OutputStream output, 
	    int resolution) throws IOException {
	if (im.getType() == BufferedImage.TYPE_BYTE_GRAY) {
	    writeGray(im, new DataOutputStream(output), resolution);
	    return true;
	} else if (im.getType() == BufferedImage.TYPE_INT_RGB) {
	    writeColor(im, new DataOutputStream(output), resolution);
	    return true;
	} else
	    return false;
    }

    // Write gray image.
    private static void writeGray(BufferedImage im, DataOutputStream output,
	    int resolution) throws IOException {
	writeGrayBegin(im, output, resolution);
	Raster r = im.getWritableTile(0, 0);
	byte[] bytes = (byte[])
	    (r.getDataElements(0, 0, im.getWidth(), im.getHeight(), null));
	int i = 0;
	for (int y = 0; y < im.getHeight(); y++)
	    for (int x = 0; x < im.getWidth(); x++) 
		output.write(bytes[i++]);
    }

    // Write body of gray image.
    private static void writeGrayBegin(BufferedImage im, DataOutputStream output,
	    int resolution) throws IOException {
	openTiff(output);
	int width = im.getWidth();
	int height = im.getHeight();
	checkOffset(output, 8);

	output.writeShort(11); /* nr of entries in following */
	output.writeShort(256); /* ENTRY 1 ImageWidth */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(width); /* nr of columns */
	output.writeShort(257); /* ENTRY 2 ImageLength */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(height); /* nr of rows */
	output.writeShort(258); /* ENTRY 3 BitsPerSample */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(8); /* bits per component */
	output.writeShort(0); /* dummy */
	output.writeShort(259); /* ENTRY 4 Compression */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(1); /* no compression */
	output.writeShort(0); /* dummy */
	output.writeShort(262); /* ENTRY 5 Photometric */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(1); /* BlackIsZero */
	output.writeShort(0); /* dummy */
	output.writeShort(273); /* ENTRY 6 StripOffsets */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(164); /* offset start of strip */
	output.writeShort(278); /* ENTRY 7 RowsPerStrip */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(height); /* nr of rows */
	output.writeShort(279); /* ENTRY 8 StripByteCounts */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(width*height); /* size of picture */
	output.writeShort(282); /* ENTRY 9 XResolution */
	output.writeShort(RATIONAL);
	output.writeInt(1);
	output.writeInt(148); /* offset of XResol */
	output.writeShort(283); /* ENTRY 10 YResolution */
	output.writeShort(RATIONAL);
	output.writeInt(1);
	output.writeInt(156); /* offset of YResol */
	output.writeShort(296); /* ENTRY 11 ResolutionUnit */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(2); /* inch */
	output.writeShort(0); /* dummy */
	checkOffset(output, 8+2+12*11); /* 8=start IDF, 11 entries of length 12 */
	output.writeInt(0); /* end IFD */
	jumpOffset(output, 148);
	output.writeInt(72 * resolution); /* Xresolution, numerator */
	output.writeInt(1); /* Xresolution, denomenator */
	checkOffset(output, 156);
	output.writeInt(72 * resolution); /* Yresolution, numerator */
	output.writeInt(1); /* Yresolution, denomenator */
	checkOffset(output, 164); /* hereafter strip starts */
    }

    // Write color image.
    private static void writeColor(BufferedImage im, DataOutputStream output,
	    int resolution) throws IOException {
	writeColorBegin(im, output, resolution);
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

    // Write body of color image.
    private static void writeColorBegin(BufferedImage im, DataOutputStream output,
	    int resolution) throws IOException {
	openTiff(output);
	int width = im.getWidth();
	int height = im.getHeight();
	checkOffset(output, 8);

	output.writeShort(12); /* nr of entries in following */
	output.writeShort(256); /* ENTRY 1 ImageWidth */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(width); /* nr of columns */
	output.writeShort(257); /* ENTRY 2 ImageLength */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(height); /* nr of rows */
	output.writeShort(258); /* ENTRY 3 BitsPerSample */
	output.writeShort(SHORT);
	output.writeInt(3);
	output.writeInt(160); /* offset of (3 times) bits per sample */
	output.writeShort(259); /* ENTRY 4 Compression */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(1); /* no compression */
	output.writeShort(0); /* dummy */
	output.writeShort(262); /* ENTRY 5 Photometric */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(2); /* RGB */
	output.writeShort(0); /* dummy */
	output.writeShort(273); /* ENTRY 6 StripOffsets */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(182); /* offset start of strip */
	output.writeShort(277); /* ENTRY 7 SamplesPerPixel */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(3); /* 3 samples per pixel */
	output.writeShort(0); /* dummy */
	output.writeShort(278); /* ENTRY 8 RowsPerStrip */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(height); /* nr of rows */
	output.writeShort(279); /* ENTRY 9 StripByteCounts */
	output.writeShort(LONG);
	output.writeInt(1);
	output.writeInt(width*height*3); /* size of picture */
	output.writeShort(282); /* ENTRY 10 XResolution */
	output.writeShort(RATIONAL);
	output.writeInt(1);
	output.writeInt(166); /* offset of XResol */
	output.writeShort(283); /* ENTRY 11 YResolution */
	output.writeShort(RATIONAL);
	output.writeInt(1);
	output.writeInt(174); /* offset of YResol */
	output.writeShort(296); /* ENTRY 12 ResolutionUnit */
	output.writeShort(SHORT);
	output.writeInt(1);
	output.writeShort(2); /* inch */
	output.writeShort(0); /* dummy */
	checkOffset(output, 8+2+12*12); /* 8=start IDF, 12 entries of length 12 */
	output.writeInt(0); /* end IFD */

	jumpOffset(output, 160);
	output.writeShort(8); /* bits per component */
	output.writeShort(8); /* bits per component */
	output.writeShort(8); /* bits per component */
	checkOffset(output, 166);
	output.writeInt(72 * resolution); /* Xresolution, numerator */
	output.writeInt(1); /* Xresolution, denomenator */
	checkOffset(output, 174);
	output.writeInt(72 * resolution); /* Yresolution, numerator */
	output.writeInt(1); /* Yresolution, denomenator */
	checkOffset(output, 182); /* hereafter strip starts */
    }

    // For making sure there are no programming errors,
    // we check if the estimated offset is reached.
    private static void checkOffset(DataOutputStream output, int i) 
    throws IOException {
	if (output.size() != i) 
	    throw new IOException("Programming error; wrong offset in TIFF output");
    }

    // Make offset required value, by padding.
    private static void jumpOffset(DataOutputStream output, int i) 
    throws IOException {
	if (i < output.size())
	    throw new IOException("Programming error; wrong offset in TIFF output");
	else
	    while (output.size() < i)
		output.writeShort(0);
    }

    // Write top of TIFF file.
    private static void openTiff(DataOutputStream output) 
    throws IOException {
	output.writeShort(0x4D4D); /* big-endian */
	output.writeShort(42); /* this is TIFF file */
	output.writeInt(8); /* where first IFD starts */
    }
}
