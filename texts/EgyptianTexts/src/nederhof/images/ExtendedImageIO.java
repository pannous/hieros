/***************************************************************************/
/*                                                                         */
/*  ExtendedImageIO.java                                                   */
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

// Handles TIFF and PNM in addition to formats already handled by Java 1.4.

package nederhof.images;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class ExtendedImageIO {

    // Write image as TIFF or PNM, or as some format standardly available.
    public static boolean write(BufferedImage im, String formatName, File output,
	    int resolution) 
    throws IOException {
	if (formatName.equals("tif") || formatName.equals("pnm")) {
	    FileOutputStream stream = new FileOutputStream(output);
	    boolean succeed = write(im, formatName, stream, resolution);
	    stream.close();
	    return succeed;
	} else 
	    return ImageIO.write(im, formatName, output);
    }

    // Same as above, but output directed to stream.
    public static boolean write(BufferedImage im, String formatName, OutputStream output,
	    int resolution) 
    throws IOException {
	if (formatName.equals("tif")) {
	    return Tiff.write(im, output, resolution);
	} else if (formatName.equals("pnm")) {
	    return Pnm.write(im, output);
	} else
	    return ImageIO.write(im, formatName, output);
    }
}
