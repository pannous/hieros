/***************************************************************************/
/*																		 */
/*  ResToImage.java														*/
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

// Converting RES to image from command line.

package nederhof.res;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import javax.imageio.*;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;

import org.jibble.epsgraphics.*;

import nederhof.images.*;
import nederhof.res.format.*;

public class ResToImage {

	// Process options, and process input.
	public static void main(String[] args) {
		ToImageContext context = new ToImageContext(args);
		ParsingContext parsingContext =
			new ParsingContext(context, false);

		InputStreamReader inReader = null;
		if (context.encodingFile().equals(""))
			inReader = new InputStreamReader(System.in);
		else {
			try {
				inReader = new FileReader(context.encodingFile());
			} catch (FileNotFoundException e) {
				System.err.println("Cannot open encoding file " +
						context.encodingFile());
				System.exit(-1);
			}
		}
		BufferedReader in = new BufferedReader(inReader);

		PrintStream out = null;
		if (context.outputFile().equals(""))
			out = System.out;
		else {
			try {
				out = new PrintStream(
						new FileOutputStream(context.outputFile(), true));
			} catch (FileNotFoundException e) {
				System.err.println("Cannot open output file " + 
						context.outputFile());
				System.exit(-1);
			}
		}

		try {
			switch (context.processingMode()) {
				case ToImageContext.BASIC:
					processBasic(context, parsingContext, in, out);
					break;
				case ToImageContext.REPEAT:
					processRepeat(context, parsingContext, in, out);
					break;
				case ToImageContext.MULTI:
					processMulti(context, parsingContext, in, out);
					break;
				case ToImageContext.CHUNKS:
					processChunks(context, parsingContext, in, out);
					break;
				case ToImageContext.ECHO:
					processEcho(context, parsingContext, in, out);
					break;
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		try {
			if (!context.encodingFile().equals(""))
				in.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		if (!context.outputFile().equals(""))
			out.close();
	}

	// In basic mode, as much of the encoding is processed as
	// fits in the specified space, and the rest plus any global settings
	// other than the defaults are written to the second output line.
	// On the first output line, print the base_name of the file where the
	// picture is stored, followed by the sizes of the margins.
	// If nothing at all could be processed from the encoding,
	// the first line remains blank, and
	// the remainder of the encoding on the second line starts with a space.
	private static void processBasic(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out) throws IOException {
		String total = readLines(in);
		if (ResOrLite.isResLite(total)) {
			REScode res = new REScode(total);
			String trail = res.getRemainder();
			if (!trail.matches("(?s)\\s*")) {
				System.err.println("Trailing symbols:");
				System.err.println(res.getRemainder());
			}
			REScodeDivision div;
			if (Float.isNaN(context.maxLengthPt()))
				div = new REScodeDivision(res, context);
			else
				div = new REScodeDivision(res, context.maxLengthPt(), context, true);
			if (!res.isEmpty() && div.getInitialNumber() == 0) {
				System.err.println("Warning: nothing could be processed from:");
				System.err.println(res);
				out.println();
				out.print(" ");
			} else {
				// writeCommon(context, out, res, div, -1, -1);
				out.println();
			}
			REScode remain = div.getRemainder();
			System.out.println(remain);
		} else {
			ResFragment parsed = ResFragment.parse(total, parsingContext);
			FormatFragment formatted = new FormatFragment(parsed, context);
			int nGroups = parsed.nGroups();
			if (!Float.isNaN(context.maxLengthPt())) {
				int pixBound = context.ptToPix(context.maxLengthPt());
				nGroups = formatted.boundedNGroups(pixBound);
			}
			ResFragment prefix = parsed.prefixGroups(nGroups);
			ResFragment suffix = parsed.suffixGroups(nGroups);
			if (nGroups != parsed.nGroups())
				formatted = new FormatFragment(prefix, context);
			if (!Float.isNaN(context.maxLengthPt())) {
				int pixBound = context.ptToPix(context.maxLengthPt());
				int pad = formatted.effectIsH() ?
					pixBound - formatted.width() : pixBound - formatted.height();
				int normalSepPix = formatted.nPaddable() *
					context.emToPix(context.fontSep());
				if (pad <= normalSepPix * context.padding())
					formatted = new FormatFragment(prefix, context, pad);
			}
			if (!parsed.isEmpty() && prefix.isEmpty()) {
				System.err.println("Warning: nothing could be processed from:");
				System.err.println(parsed);
				out.println();
				out.print(" ");
			} else {
				writeCommon(context, out, formatted, -1, -1);
				out.println();
			}
			System.out.println(suffix);
		}
	}

	// Repeat mode.
	// The -1 is a dummy line number, indicating that no line number
	// is to be put in file names of the graphical representations.
	private static void processRepeat(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out) throws IOException {
		String total = readLines(in);
		processRepeatedly(context, parsingContext, in, out, total, -1);
	}

	// In multi-input mode, the input consists of several lines, and each
	// is processed in several passes as in the case of REPEAT.
	private static void processMulti(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out) throws IOException {
		int lineNum = 1;
		String line = in.readLine();
		while (line != null) {
			processRepeatedly(context, parsingContext, in, out, line, lineNum++);
			line = in.readLine();
		}
	}

	// In chunk mode, the input is divided into the largest possible
	// number of groups. (In particular, where there is 'fix' two groups 
	// cannot be split up.)
	// The first output line contains the number of these atomic groups.
	// Then, for each atomic group, there is a line containing the
	// name of the file where the image is stored, followed by the sizes
	// of the margins, followed by the distance between the preceding group
	// and the current group. 
	// The last line contains the remainder.
	private static void processChunks(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out) throws IOException {
		String total = readLines(in);
		int num = 1;
		if (ResOrLite.isResLite(total)) {
			REScode res = new REScode(total);
			String trail = res.getRemainder();
			if (!trail.matches("(?s)\\s*")) {
				System.err.println("Trailing symbols:");
				System.err.println(res.getRemainder());
			}
			while (!res.isEmpty()) {
				// REScodeDivision div = new REScodeDivision(res, 1, context);
				// writeCommon(context, out, res, div, -1, num);
				num++;
				// res = div.getRemainder();
			}
			out.println();
			out.println(res);
		} else {
			ResFragment parsed = ResFragment.parse(total, parsingContext);
			out.println(parsed.nChunks());
			while (!parsed.isEmpty()) {
				int chunkLen = parsed.firstChunkLength();
				ResFragment first = parsed.prefixGroups(chunkLen);
				ResFragment rest = parsed.suffixGroups(chunkLen);
				FormatFragment firstFormat = new FormatFragment(first, context);
				writeCommon(context, out, firstFormat, -1, num);
				int secondChunkLen = rest.firstChunkLength();
				if (secondChunkLen > 0) {
					ResFragment second = rest.prefixGroups(secondChunkLen);
					FormatFragment secondFormat = new FormatFragment(second, context);
					ResFragment firstSecond = 
						parsed.prefixGroups(chunkLen + secondChunkLen);
					FormatFragment firstSecondFormat = 
						new FormatFragment(firstSecond, context);
					int len = firstFormat.effectIsH() ?
						firstSecondFormat.width() -
							firstFormat.width() - secondFormat.width() :
						firstSecondFormat.height() -
							firstFormat.height() - secondFormat.height();
					printDistance(context, out, len);
				} else 
					printDistance(context, out, 0);
				parsed = rest;
				out.println();
				num++;
			}
			out.println(parsed);
		}
	}

	// Auxiliary function for the above processRepeat and processMulti.
	// Parse the encoding and repeatedly make images from consecutive pieces.
	// In multi-pass modes, what does not fit within the specified space
	// is reprocessed in a next pass.
	// The names of the files are written on one line of the output file (or
	// stdout), each followed by the sizes of the margins.
	// The next line contains and switches, or one space if
	// not everything could be processed.
	private static void processRepeatedly(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out,
			String line, int lineNum) throws IOException {
		int num = 1;
		if (ResOrLite.isResLite(line)) {
			REScode res = new REScode(line);
			String trail = res.getRemainder();
			if (!trail.matches("(?s)\\s*")) {
				System.err.println("Trailing symbols:");
				System.err.println(res.getRemainder());
			}
			while (!res.isEmpty()) {
				/*
				REScodeDivision div;
				if (Float.isNaN(context.maxLengthPt()))
					div = new REScodeDivision(res, context);
				else
					div = new REScodeDivision(res, context.maxLengthPt(), context, true);
				if (!res.isEmpty() && div.getInitialNumber() == 0) {
					System.err.println("Warning: nothing could be processed from:");
					System.err.println(res);
					break;
				} else {
					writeCommon(context, out, res, div, lineNum, num);
					num++;
					res = div.getRemainder();
				}
				*/
			}
			out.println();
			if (!res.isEmpty())
				out.print(" ");
			out.println(res);
		} else {
			ResFragment parsed = ResFragment.parse(line, parsingContext);
			while (!parsed.isEmpty()) {
				FormatFragment formatted = new FormatFragment(parsed, context);
				int nGroups = parsed.nGroups();
				if (!Float.isNaN(context.maxLengthPt())) {
					int pixBound = context.ptToPix(context.maxLengthPt());
					nGroups = formatted.boundedNGroups(pixBound);
				}
				ResFragment prefix = parsed.prefixGroups(nGroups);
				ResFragment suffix = parsed.suffixGroups(nGroups);
				if (nGroups != parsed.nGroups())
					formatted = new FormatFragment(prefix, context);
				if (!Float.isNaN(context.maxLengthPt())) {
					int pixBound = context.ptToPix(context.maxLengthPt());
					int pad = formatted.effectIsH() ?
						pixBound - formatted.width() : pixBound - formatted.height();
					int normalSepPix = formatted.nPaddable() *
						context.emToPix(context.fontSep());
					if (pad <= normalSepPix * context.padding())
						formatted = new FormatFragment(prefix, context, pad);
				}
				if (!parsed.isEmpty() && prefix.isEmpty()) {
					System.err.println("Warning: nothing could be processed from:");
					System.err.println(parsed);
					break;
				} else {
					writeCommon(context, out, formatted, lineNum, num);
					num++;
					parsed = suffix;
				}
			}
			out.println();
			if (!parsed.isEmpty())
				out.print(" ");
			out.println(parsed);
		}
	}

	// In echo mode, the input is parsed and echoed back.
	private static void processEcho(ToImageContext context,
			ParsingContext parsingContext,
			BufferedReader in, PrintStream out) throws IOException {
		String line = in.readLine();
		while (line != null) {
			if (ResOrLite.isResLite(line)) {
				REScode res = new REScode(line);
				String trail = res.getRemainder();
				if (!trail.matches("(?s)\\s*")) {
					System.err.println("Trailing symbols:");
					System.err.println(res.getRemainder());
				}
				out.println(res);
			} else {
				ResFragment parsed = ResFragment.parse(line, parsingContext);
				out.println(parsed);
			}
			line = in.readLine();
		}
	}

	// Read all lines from stream.
	private static String readLines(BufferedReader in) throws IOException {
		String total = "";
		String line = in.readLine();
		while (line != null) {
			total += line + " ";
			line = in.readLine();
		}
		return total;
	}

	// A common part in above methods. An image is written to a file,
	// or alternatively the REScode.
	// Filename is determined on the basis of num1 and num2.
	/*
	private static void writeCommon(ToImageContext context,
			PrintStream out, REScode res, REScodeDivision div, int num1, int num2) {
		if (context.imageKind().equals("code")) {
			int n = div.getInitialNumber();
			out.print(new REScode(res, n));
		} else {
			String file = makeExtFileName(context.baseName(), num1, num2);
			out.print(file + " ");
			Insets margins = writeImage(context, file, div);
			printMargins(context, out, margins);
		}
	}
	*/

	// As above, but now with RES.
	/* OBSOLOTE
	private static void writeCommon(ToImageContext context,
			PrintStream out, ResDivision div, int num1, int num2) {
		if (context.imageKind().equals("code")) 
			out.print(div.toREScode());
		else {
			String file = makeExtFileName(context.baseName(), num1, num2);
			out.print(file + " ");
			Insets margins = writeImage(context, file, div);
			printMargins(context, out, margins);
		}
	}
	*/

	// As above, but now with FormatFragment.
	private static void writeCommon(ToImageContext context,
			PrintStream out, FormatFragment res, int num1, int num2) {
		if (context.imageKind().equals("code")) 
			out.print(res.toResLite());
		else {
			String file = makeExtFileName(context.baseName(), num1, num2);
			out.print(file + " ");
			Insets margins = writeImage(context, file, res);
			printMargins(context, out, margins);
		}
	}



	// Combine file name with up to two numbers, provided these are
	// non-negative.
	private static String makeExtFileName(String base, int num1, int num2) {
		if (num1 < 0 && num2 < 0)
			return base;
		else if (num1 < 0)
			return base + num2;
		else
			return base + num1 + "-" + num2;
	}

	// Write image, to some kind of image format.
	// We make a distinction between eps (no pixelization) and 
	// the rest (pixels).
	/*
	private static Insets writeImage(ToImageContext context, String file,
			RESorREScodeDivision div) {
		String fileExt = file + "." + context.imageKind();
		if (context.imageKind().equals("eps")) {
			Graphics2D g = new EpsGraphics2D(file);
			div.write(g, 0, 0);
			try {
				if (context.pipe().equals("")) {
					PrintWriter out =
						new PrintWriter(new FileOutputStream(fileExt));
					out.println(g);
					out.close();
				} else {
					Process proc = pipeProcess(context, file);
					PrintWriter out = new PrintWriter(proc.getOutputStream());
					out.println(g);
					out.close();
					proc.destroy();
				}
			} catch (FileNotFoundException e) {
				System.err.println("Could not open: " + fileExt);
				System.exit(-1);
			}
			return div.margins();
		} else {
			MovedBuffer buf = div.toImage();
			BufferedImage im = buf.getImage();
			try {
				if (context.pipe().equals("")) {
					File out = new File(fileExt);
					boolean succeed = ExtendedImageIO.write(im, 
							context.imageKind(), out, context.resolution());
					if (!succeed) {
						System.err.println("Erroneous image format: " + context.imageKind());
						System.exit(-1);
					}
				} else {
					Process proc = pipeProcess(context, file);
					OutputStream out = proc.getOutputStream();
					boolean succeed = ExtendedImageIO.write(im, 
							context.imageKind(), out, context.resolution());
					if (!succeed) {
						System.err.println("Erroneous image format: " + context.imageKind());
						System.exit(-1);
					}
					out.close();
					proc.destroy();
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			return buf.margins();
		}
	}
	*/

	// As above, but with FormatFragment.
	private static Insets writeImage(ToImageContext context, String file,
			FormatFragment res) {
		String fileExt = file + "." + context.imageKind();
		if (context.imageKind().equals("eps")) {
			Graphics2D g = new EpsGraphics2D(file);
			g.setColor(Color.white);
			g.fillRect(0, 0, res.width(), res.height());
			res.write(g, 0, 0);
			try {
				if (context.pipe().equals("")) {
					PrintWriter out =
						new PrintWriter(new FileOutputStream(fileExt));
					out.println(g);
					out.close();
				} else {
					Process proc = pipeProcess(context, file);
					PrintWriter out = new PrintWriter(proc.getOutputStream());
					out.println(g);
					out.close();
					proc.destroy();
				}
			} catch (FileNotFoundException e) {
				System.err.println("Could not open: " + fileExt);
				System.exit(-1);
			}
			return res.margins();
		} else if (context.imageKind().equals("pdf")) {
			Document doc = new Document(new Rectangle(res.width(), res.height()));
			PdfWriter writer = null;
			Process proc = null;
			try {
				if (context.pipe().equals("")) {
					writer = PdfWriter.getInstance(doc, new FileOutputStream(fileExt));
				} else { 
					proc = pipeProcess(context, file);
					writer = PdfWriter.getInstance(doc, proc.getOutputStream());
				}
			} catch (DocumentException e) {
				System.err.println("Could not open: " + fileExt);
				System.exit(-1);
			} catch (FileNotFoundException e) {
				System.err.println("Could not open: " + fileExt);
				System.exit(-1);
			}
			doc.open();
			PdfContentByte canvas = writer.getDirectContent();
			Graphics2D g = new PdfGraphics2D(canvas, res.width(), res.height(), context.pdfMapper());
			g.setColor(Color.white);
			g.fillRect(0, 0, res.width(), res.height());
			res.write(g, 0, 0);
			g.dispose();
			doc.close();
			if (!context.pipe().equals(""))
				proc.destroy();
			return res.margins();
		} else {
			MovedBuffer buf = res.toImage();
			BufferedImage im = buf.getImage();
			try {
				if (context.pipe().equals("")) {
					File out = new File(fileExt);
					boolean succeed = ExtendedImageIO.write(im, 
							context.imageKind(), out, context.resolution());
					if (!succeed) {
						System.err.println("Erroneous image format: " + context.imageKind());
						System.exit(-1);
					}
				} else {
					Process proc = pipeProcess(context, file);
					OutputStream out = proc.getOutputStream();
					boolean succeed = ExtendedImageIO.write(im, 
							context.imageKind(), out, context.resolution());
					if (!succeed) {
						System.err.println("Erroneous image format: " + context.imageKind());
						System.exit(-1);
					}
					out.close();
					proc.destroy();
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			return buf.margins();
		}
	}

	// Get process that represents pipe.
	private static Process pipeProcess(ToImageContext context, String file) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		try {
			String pipe = expandedPipe(context, file);
			proc = runtime.exec(pipe);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		return proc;
	}

	// Expand pipe: replace %s by filename (without extension) and %% by %.
	private static String expandedPipe(ToImageContext context, String file) {
		String pipe = context.pipe();
		int i = 0;
		while (i < pipe.length() - 1) {
			if (pipe.charAt(i) == '%' && pipe.charAt(i+1) == 's') {
				pipe = pipe.substring(0, i) + file + pipe.substring(i+2);
				i += file.length();
			} else if (pipe.charAt(i) == '%' && pipe.charAt(i+1) == '%') {
				pipe = pipe.substring(0, i) + pipe.substring(i+1);
				i++;
			} else
				i++;
		}
		return pipe;
	}

	// Format for small floats.
	private static DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(Locale.UK);
	private static final NumberFormat nf = new DecimalFormat("0.0000", decimalSymbols);

	// Print margins.
	private static void printMargins(ToImageContext context, PrintStream out,
			Insets margins) {
		out.print(
				nf.format(context.pixToInch(margins.left)) + " " + 
				nf.format(context.pixToInch(margins.bottom)) + " " +
				nf.format(context.pixToInch(margins.right)) + " " + 
				nf.format(context.pixToInch(margins.top)) + " ");
	}

	// Print distance.
	private static void printDistance(ToImageContext context, PrintStream out,
			int dist) {
		out.print(nf.format(context.pixToInch(dist)) + " ");
	}

}
