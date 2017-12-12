/***************************************************************************/
/*																		 */
/*  ToImageContext.java													*/
/*																		 */
/*  Copyright (c) 2008 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

// HieroRenderContext, extended to handle more arguments, for
// conversion from RES to image.

package nederhof.res;

import java.io.*;

import nederhof.align.Align;

class ToImageContext extends HieroRenderContext {

	// Possible processing modes.
	public static final int NOTHING = 0;
	public static final int BASIC = 1;
	public static final int REPEAT = 2;
	public static final int MULTI = 3;
	public static final int CHUNKS = 4;
	public static final int ECHO = 5;

	// Processing mode.
	private int processingMode = BASIC;

	// File from which encoding is to be read.
	// Empty string indicates standard input.
	private String encodingFile = "";

	// File to which names are written of files containing images, without extension.
	// Also remaining encoding (for BASIC mode) and headers and
	// switches are written to this file.
	// For ECHO mode, this is the file to which normalized output is written.
	// Empty string indicates the above is written to standard output
	// instead of to a file.
	private String outputFile = "";

	// File name without extension for file(s) to which graphics output is
	// written. In the cases of REPEAT and MULTIPLE modes,
	// one or two numbers are appended.
	private String baseName = "noname";

	// Image type.
	private String imageKind = "tif"; 

	// For piping graphics output to string. If string is empty, output is
	// written to a file.
	private String pipe = "";

	// The maximal length of a segment in graphical form.
	private float maxLengthInch = Float.NaN;

	//////////////////////////////////////////////////////////////////////////////
	// Accessors.

	public int processingMode() {
		return processingMode;
	}

	public String encodingFile() {
		return encodingFile;
	}

	public String outputFile() {
		return outputFile;
	}

	public String baseName() {
		return baseName;
	}

	public String imageKind() {
		return imageKind;
	}

	public String pipe() {
		return pipe;
	}

	public float maxLengthInch() {
		return maxLengthInch;
	}

	public float maxLengthPt() {
		if (Float.isNaN(maxLengthInch))
			return Float.NaN;
		else
			return maxLengthInch * 72;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Creation of context from options from command line.

	// Take one or two arguments at a time. If incorrect arguments seen,
	// stop, and prevent further processing.
	protected ToImageContext(String[] args) {
		int i = 0;
		while (i < args.length) {
			int j = processOption(args, i);
			if (j <= 0) {
				writeHelp();
				processingMode = NOTHING;
				return;
			} else
				i += j;
		}
		setFonts();
	}

	// Process option at argument position i.
	// Return how many argument positions processed.
	// Zero means no (valid) argument was processed.
	public int processOption(String[] args, int i) {
		if (i >= args.length)
			return 0;
		int n = super.processOption(args, i);
		if (n > 0)
			return n;
		else if (args[i].equals("-help")) {
			writeHelp();
			processingMode = NOTHING;
			return args.length - i;
		} else if (args[i].equals("-version")) {
			writeVersion();
			processingMode = NOTHING;
			return args.length - i;
		} else if (args[i].equals("-basic")) {
			processingMode = BASIC;
			return 1;
		} else if (args[i].equals("-repeat")) {
			processingMode = REPEAT;
			return 1;
		} else if (args[i].equals("-multi")) {
			processingMode = MULTI;
			return 1;
		} else if (args[i].equals("-chunks")) {
			processingMode = CHUNKS;
			return 1;
		} else if (args[i].equals("-echo")) {
			processingMode = ECHO;
			return 1;
		} else if (args[i].equals("-tif")) {
			imageKind = "tif";
			return 1;
		} else if (args[i].equals("-pnm")) {
			imageKind = "pnm";
			return 1;
		} else if (args[i].equals("-jpg")) {
			imageKind = "jpg";
			return 1;
		} else if (args[i].equals("-png")) {
			imageKind = "png";
			return 1;
		} else if (args[i].equals("-eps")) {
			imageKind = "eps";
			return 1;
		} else if (args[i].equals("-pdf")) {
			imageKind = "pdf";
			return 1;
		} else if (args[i].equals("-code")) {
			imageKind = "code";
			return 1;
		} else if (args[i].equals("-e") && i+1 < args.length) {
			encodingFile = args[i+1];
			return 2;
		} else if (args[i].equals("-o") && i+1 < args.length) {
			outputFile = args[i+1];
			return 2;
		} else if (args[i].equals("-b") && i+1 < args.length) {
			baseName = args[i+1];
			return 2;
		} else if (args[i].equals("-pipe") && i+1 < args.length) {
			pipe = args[i+1];
			return 2;
		} else if (args[i].equals("-length") && i+1 < args.length) {
			try {
				maxLengthInch = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (maxLengthInch < 0)
				maxLengthInch = Float.NaN;
			return 2;
		} else
			return 0;
	}

	// Write possible options.
	private static void writeHelp() {
		System.err.println("Usage:");
		System.err.println("	ResToImage\t[-help] [-version]");
		System.err.println("\t\t[-basic] [-repeat] [-multi] [-chunks] [-echo]");
		System.err.println("\t\t[-grayscale] [-color]");
		System.err.println("\t\t[-tif] [-pnm] [-jpg] [-png] [-eps] [-pdf] [-code]");
		System.err.println("\t\t[-noline] [-underline] [-overline]");
		System.err.println("\t\t[-linedist line_distance] [-linesize line_thickness]");
		System.err.println("\t\t[-linegray line_grayness] [-linecolor line_color]");
		System.err.println("\t\t[-dpp device_resolution] [-fontsize font_size]");
		System.err.println("\t\t[-f fonts_file] [-e encoding_file] [-o output_file]");
		System.err.println("\t\t[-b base_name] [-pipe command]");
		System.err.println("\t\t[-freedir] [-hlr] [-hrl] [-vlr] [-vrl]");
		System.err.println("\t\t[-h] [-v] [-lr] [-rl] [-hlrspec] [-size size]");
		System.err.println("\t\t[-specdist spec_distance] [-speccolor spec_color]");
		System.err.println("\t\t[-length length] [-padding padding_factor]");
		System.err.println("\t\t[-lm margin] [-bm margin] [-rm margin] [-tm margin]");
		System.err.println("\t\t[-shadegray shading_grayness] [-shadefreq shading_frequency]");
		System.err.println("\t\t[-shadecover shading_cover] [-shadecolor shading_color]");
		System.err.println("\t\t[-notesize note_font_size] [-notedist note_distance]");
		System.err.println("\t\t[-notecolor note_color]");
	}

	// Write version.
	private static void writeVersion() {
		System.err.println(Align.programName);
	}
}
