/***************************************************************************/
/*																		 */
/*  HieroRenderContext.java												*/
/*																		 */
/*  Copyright (c) 2009 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

// The context for rendering hieroglyphic.

package nederhof.res;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.text.*;

import com.itextpdf.awt.DefaultFontMapper;

import nederhof.fonts.*;

public class HieroRenderContext {

	//////////////////////////////////////////////////////////////////////
	// Properties of context.

	// The font names available, plus the file
	// where font information is available.
	private static Map fontFiles = new TreeMap();
	static {
		fontFiles.put("NewGardiner", "data/fonts/NewGardiner.txt");
		fontFiles.put("Aegyptus", "data/fonts/Aegyptus.txt");
	}
	// Default name.
	public static final String defaultFontName = "NewGardiner";
	// All names.
	public static Set fontNames() {
		return fontFiles.keySet();
	}

	// The current name of the font.
	private String fontName = defaultFontName;
	// File containing font information.
	// We avoid reading that file twice, by mapping name of fonts
	// file to record with font information.
	private String fontsFile = (String) fontFiles.get(fontName);
	private HieroFonts fontInfo;
	private static TreeMap fontsFileToInfo = new TreeMap();

	// Color allowed.
	private boolean colorAllowed = false;

	// Rendering to be made very precise? This circumvents
	// rounding-off error on pixel level, which may make glyphs
	// appear bigger than they are.
	public static final boolean pedantic = true;

	// Should errors and warning be suppressed?
	public boolean suppressErrors = false;

	// During scaling, there are iterations. Some might potentially lead to
	// infinite loops. Placing a bound on the number of iterations avoids
	// this.
	public static final int maxScalingIterations = 20;

	// Input font size in Pt in ResToImage.
	private int fontSizePt = 45;

	// We maintain two types of font sizes, both in pixels. 
	// (Some code here assumes 1 point equals 1 pixel.)
	// fontSize is only used in setting up the right font. 
	// pixelFontSize is used for everything else that requires the size of
	// 1 EM.
	// It seems the two differ slightly.
	private int fontSize;
	private int pixelFontSize;

	// How much does size of Latin characters need to be increased?
	private float pixelLatinFontSizeIncrease = 1;

	// Resolution, in pixels per point.
	private int resolution = 1;

	// Are colors represented by underline or overline?
	private int lineMode = ResValues.NO_LINE;
	// Appearance of line.
	private float lineDistEm = 0.13f;
	private float lineThicknessEm = 0.04f;
	// How dark (from 0=white to 255=black).
	private int lineGray = 200;
	// Color of line.
	private Color lineColor = Color.BLACK;

	// Shading properties.
	// How dark (from 0=white to 255=black).
	private int shadeGray = 100;
	// How many lines per EM?
	private int shadeFreq = 10;
	// How much of surface (1/shadeCover) covered with lines.
	private int shadeCover = 10;
	// The color of shading.
	private Color shadeColor = Color.BLACK;

	// For notes (footnote markers and such), we can determine size,
	// the minimal distance to glyphs,
	// and the default color (can be overridden by color in encoding).
	public int noteSizePt = 12;
	private int noteDistPt = 3;
	private Color16 noteColor = Color16.BLACK;

	// Forced text direction. Overrides direction in encoding.
	private int forcedDirection = ResValues.DIR_NONE;

	// Forced unit size.
	private float forcedSize = Float.NaN;

	// Specification of direction?
	private boolean directionSpec = false;
	// Distance between spec of direction and following
	// hieroglyphic.
	private float specDistEm = 0.20f;
	// Color of spec of direction.
	private Color16 specColor = Color16.BLACK;

	// If maximal length is set, and resulting text is smaller,
	// then it may be padded up, by a factor of the default glyph
	// separation.
	private float padding = 1.0f;

	// For the margins, we allow non-negative values. Default is 0.
	// Left and right refer to the physical left and right.
	private float userLeftMarginInch = 0f;
	private float userBottomMarginInch = 0f;
	private float userRightMarginInch = 0f;
	private float userTopMarginInch = 0f;

	//////////////////////////////////////////////////////////////////////

	// Make context with all defaults.
	// Fonts are to be arranged later.
	public HieroRenderContext() {
	}

	// Construct context for AELalign.
	public HieroRenderContext(String fontName,
			int fontSizePt, int noteSizePt, int resolution,
			boolean colorAllowed, boolean forcedDir) {
		this.fontName = fontName;
		fontsFile = (String) fontFiles.get(fontName);
		this.fontSizePt = fontSizePt;
		this.noteSizePt = noteSizePt;
		this.resolution = resolution;
		this.colorAllowed = colorAllowed;
		if (forcedDir)
			forcedDirection = ResValues.DIR_HLR;
		setFonts();
	}
	// Without font name.
	public HieroRenderContext(int fontSizePt, int noteSizePt, int resolution,
			boolean colorAllowed, boolean forcedDir) {
		this(defaultFontName, fontSizePt, noteSizePt, resolution,
				colorAllowed, forcedDir);
	}
	// Without noteSize.
	// With default noteSizePt.
	public HieroRenderContext(String fontName,
			int fontSizePt, int resolution,
			boolean colorAllowed, boolean forcedDir) {
		this(fontName, fontSizePt, 12, resolution, colorAllowed, forcedDir);
	}
	// Without fontName.
	public HieroRenderContext(int fontSizePt, int resolution,
			boolean colorAllowed, boolean forcedDir) {
		this(defaultFontName, fontSizePt, 12, resolution, colorAllowed, forcedDir);
	}

	// Same as above but assuming color is allowed, and resolution 1.
	public HieroRenderContext(String fontName,
			int fontSizePt, boolean forcedDir) {
		this(fontName, fontSizePt, 1, true, forcedDir);
	}
	// Without fontName.
	public HieroRenderContext(int fontSizePt, boolean forcedDir) {
		this(defaultFontName, fontSizePt, forcedDir);
	}

	// Same as above but assuming color is allowed, and resolution 1.
	public HieroRenderContext(String fontName,
			int fontSizePt, int noteSizePt, boolean forcedDir) {
		this(fontName, fontSizePt, noteSizePt, 1, true, forcedDir);
	}
	// Without fontName.
	public HieroRenderContext(int fontSizePt, int noteSizePt, boolean forcedDir) {
		this(defaultFontName, fontSizePt, noteSizePt, 1, true, forcedDir);
	}

	// Same as above without forced direction.
	public HieroRenderContext(int fontSizePt) {
		this(fontSizePt, 1, true, false);
	}

	// In the case of first constructor above, the font is to be supplied
	// later, after processOption below has been called for arguments.
	public synchronized void setFonts() {
		if (fontsFileToInfo.containsKey(fontsFile))
			fontInfo = (HieroFonts) fontsFileToInfo.get(fontsFile);
		else {
			fontInfo = new HieroFonts(fontsFile);
			fontsFileToInfo.put(fontsFile, fontInfo);
		}
		fontSize = fontSizePt * resolution;
		if (fontInfo.size() > 0) {
			Font unit = fontInfo.get(1).deriveFont((float) fontSize);
			StyleContext context = new StyleContext();
			FontMetrics metrics = context.getFontMetrics(unit);
			pixelFontSize = metrics.getAscent();
			getLatinFontCorrection();
		} else
			pixelFontSize = 1;
	}

	// We need to ensure that the height of [ in the Latin font equals one 
	// 1 EM, for the benefit of short_strings. 
	// As this is not true in general, we need to correct the size.
	private void getLatinFontCorrection() {
		final char norm = '[';
		Font latinFont = getLatinFont();
		if (latinFont != null && latinFont.canDisplay(norm)) {
			Glyphs gl = new Glyphs(norm, latinFont, false, 0,
					Color.BLACK, fontSize, this);
			Dimension dim = gl.dimension();
			pixelLatinFontSizeIncrease = pixelFontSize * 1.0f / dim.height;
		}
	}

	////////////////////////////////////////////////////////////////////
	// External access to rendering properties.

	// Get whether color allowed.
	public boolean colorAllowed() {
		return colorAllowed;
	}

	// Set whether output of errors should be suppressed.
	public void setSuppressErrors(boolean suppress) {
		suppressErrors = suppress;
	}

	// Should output of errors be suppressed.
	public boolean suppressErrors() {
		return suppressErrors;
	}

	// What color to take?
	public Color16 effectColor(Color16 color) {
		if (colorAllowed)
			return color;
		else if (color.isWhite())
			return Color16.WHITE;
		else
			return Color16.BLACK;
	}

	// Get image type.
	public int imageType() {
		if (colorAllowed)
			return BufferedImage.TYPE_INT_RGB;
		else
			return BufferedImage.TYPE_BYTE_GRAY;
	}

	// Size of font in pt.
	public int fontSizePt() {
		return fontSizePt;
	}

	// Size of font in pixels. To be used only for setting up the fonts.
	public int fontSize() {
		return fontSize;
	}

	// EM size in pixels.
	public int emSizePix() {
		return pixelFontSize;
	}

	// Convert from EM to pixels.
	public int emToPix(float d) {
		return Math.round(d * pixelFontSize);
	}

	// Convert from EM to points.
	public float emToPt(float d) {
		return d * pixelFontSize / resolution;
	}

	// Convert size in 1/1000 EM to number of pixels.
	public int milEmToPix(float d) {
		return Math.round(d * pixelFontSize / 1000.0f);
	}

	// Convert size in 1/1000 EM to number of points.
	public float milEmToPt(int d) {
		return d * pixelFontSize / 1000.0f / resolution;
	}

	// Convert pixel size to 1/1000 EM.
	public int pixToMilEm(int d) {
		return Math.round(1000.0f * d / pixelFontSize);
	}

	// How much do Latin characters be increased in size?
	public float pixelLatinFontSizeIncrease() {
		return pixelLatinFontSizeIncrease;
	}

	// Resolution.
	public int resolution() {
		return resolution;
	}

	// Convert from pixels to inches.
	public float pixToInch(int d) {
		return d / 72.0f / resolution;
	}

	// Convert from inches and points to pixels.
	// We take floor as we would rather make images that are
	// smaller than required as opposed to bigger.
	public int inchToPix(float d) {
		return (int) Math.floor(d * 72.0f * resolution);
	}
	public int ptToPix(float d) {
		return (int) Math.floor(d * resolution);
	}

	// EM size in points.
	public float emSizePt() {
		return pixelFontSize * 1.0f / resolution;
	}

	// Colors represented by underline/overline?
	public int lineMode() {
		return lineMode;
	}
	// Distance from glyphs.
	public int lineDistPix() {
		return emToPix(lineDistEm);
	}
	// Thickness of line.
	public int lineThicknessPix() {
		return emToPix(lineThicknessEm);
	}
	// Color of line.
	public Color lineColor() {
		return makeColor(lineColor, lineGray);
	}

	// How many lines per EM?
	public int shadeFreq() {
		return shadeFreq;
	}
	// How much of surface (1/shadeCover) covered with lines.
	public int shadeCover() {
		return shadeCover;
	}
	// Color of shading lines.
	public Color shadeColor() {
		return makeColor(shadeColor, shadeGray);
	}

	// Appearance of notes.
	// noteSize() to be used for setting up font.
	public int noteSize() {
		return noteSizePt * resolution;
	}
	public int noteDistPix() {
		return noteDistPt * resolution;
	}
	public Color16 noteColor() {
		return noteColor;
	}

	// We assume dir is full (3-letter) specification of direction.
	public static boolean isH(int dir) {
		return dir == ResValues.DIR_HLR || dir == ResValues.DIR_HRL;
	}
	public static boolean isLR(int dir) {
		return dir == ResValues.DIR_HLR || dir == ResValues.DIR_VLR;
	}
	public static boolean isRL(int dir) {
		return dir == ResValues.DIR_HRL || dir == ResValues.DIR_VRL;
	}

	// Is one direction horizontal and the other vertical?
	public static boolean dirCrosses(int dir1, int dir2) {
		return (isH(dir1) && !isH(dir2)) || (!isH(dir1) && isH(dir2));
	}

	// Is ideal direction horizontal, and effective direction vertical?
	public static boolean swapHV(int dir1, int dir2) {
		return isH(dir1) && !isH(dir2);
	}
	public static boolean swapVH(int dir1, int dir2) {
		return !isH(dir1) && isH(dir2);
	}

	// Return dir, when needed overridden by forcedDir.
	public static int effectDir(int forcedDir, int dir) {
		switch (forcedDir) {
			case ResValues.DIR_HLR:
			case ResValues.DIR_HRL:
			case ResValues.DIR_VLR:
			case ResValues.DIR_VRL:
				return forcedDir;
			case ResValues.DIR_H:
				if (dir == ResValues.DIR_VLR)
					return ResValues.DIR_HLR;
				else if (dir == ResValues.DIR_VRL)
					return ResValues.DIR_HRL;
				else 
					return dir;
			case ResValues.DIR_V:
				if (dir == ResValues.DIR_HLR)
					return ResValues.DIR_VLR;
				else if (dir == ResValues.DIR_HRL)
					return ResValues.DIR_VRL;
				else 
					return dir;
			case ResValues.DIR_LR:
				if (dir == ResValues.DIR_HRL)
					return ResValues.DIR_HLR;
				else if (dir == ResValues.DIR_VRL)
					return ResValues.DIR_VLR;
				else 
					return dir;
			case ResValues.DIR_RL:
				if (dir == ResValues.DIR_HLR)
					return ResValues.DIR_HRL;
				else if (dir == ResValues.DIR_VLR)
					return ResValues.DIR_VRL;
				else 
					return dir;
			default:
				return dir;
		}
	}

	// Effective direction, taking into account the forced direction.
	public int effectDir(int dir) {
		return effectDir(forcedDirection, dir);
	}

	// Same as above, but for REScode, we can only swap LR and RL,
	// and the H and L from the forced direction is to be ignored.
	public int effectDirREScode(int dir) {
		if (forcedDirection == ResValues.DIR_LR || isLR(forcedDirection))
			return effectDir(ResValues.DIR_LR, dir);
		else if (forcedDirection == ResValues.DIR_RL || isRL(forcedDirection))
			return effectDir(ResValues.DIR_RL, dir);
		else
			return dir;
	}

	// Is effectively horizontal?
	public boolean isEffectH(int dir) {
		return isH(effectDir(dir));
	}
	// Is effectively left-to-right?
	public boolean isEffectLR(int dir) {
		return isLR(effectDir(dir));
	}

	// Is effectively LR in REScode.
	public boolean isEffectLRREScode(int dir) {
		return isLR(effectDirREScode(dir));
	}

	// Is ideal direction horizontal and the forced direction vertical (or
	// vice versa)? (If so, certain RES options are to be ignored.)
	public boolean effectDirCrosses(int dir) {
		return dirCrosses(effectDir(dir), dir);
	}

	// Return size, when needed overridden by forcedSize.
	public static float effectSize(float forcedSize, float size) {
		if (Float.isNaN(forcedSize))
			return size;
		else
			return forcedSize;
	}

	// Unit size overriding what is in RES.
	public float effectSize(float size) {
		return effectSize(forcedSize, size);
	}

	// First, we consider forcedSize, then the other two.
	public float effectSize2(float size1, float size2) {
		if (Float.isNaN(forcedSize)) 
			return effectSize(size1, size2);
		else
			return forcedSize;
	}

	// Direction specified?
	public boolean directionSpec() {
		return directionSpec;
	}
	// Distance between spec and hieroglyphic.
	public int specDistPix() {
		return emToPix(specDistEm);
	}
	// Color of spec.
	public Color16 specColor() {
		return specColor;
	}

	// Allowable padding per separation.
	public float padding() {
		return padding;
	}

	// Get margins, in pixels
	public int leftMarginPix() {
		return inchToPix(userLeftMarginInch);
	}
	public int bottomMarginPix() {
		return inchToPix(userBottomMarginInch);
	}
	public int rightMarginPix() {
		return inchToPix(userRightMarginInch);
	}
	public int topMarginPix() {
		return inchToPix(userTopMarginInch);
	}

	// Combine grayness with color.
	private static Color makeColor(Color col, int gray) {
		int redInv = 255 - col.getRed();
		int greenInv = 255 - col.getGreen();
		int blueInv = 255 - col.getBlue();
		int redCombine = gray * redInv / 255;
		int greenCombine =  gray * greenInv / 255;
		int blueCombine =  gray * blueInv / 255;
		return new Color(
				255 - redCombine, 
				255 - greenCombine, 
				255 - blueCombine);
	}

	////////////////////////////////////////////////////////////////////////
	// Reading of options from command line.

	// Process option at argument position i. 
	// Return how many argument positions processed.
	public int processOption(String[] args, int i) {
		if (i >= args.length)
			return 0;
		else if (args[i].equals("-grayscale")) {
			colorAllowed = false;
			return 1;
		} else if (args[i].equals("-color")) {
			colorAllowed = true;
			return 1;
		} else if (args[i].equals("-noline")) {
			lineMode = ResValues.NO_LINE;
			return 1;
		} else if (args[i].equals("-underline")) {
			lineMode = ResValues.UNDERLINE;
			return 1;
		} else if (args[i].equals("-overline")) {
			lineMode = ResValues.OVERLINE;
			return 1;
		} else if (args[i].equals("-linedist") && i+1 < args.length) {
			try {
				lineDistEm = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			return 2;
		} else if (args[i].equals("-linesize") && i+1 < args.length) {
			try {
				lineThicknessEm = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (lineThicknessEm < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-linegray") && i+1 < args.length) {
			try {
				lineGray = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (lineGray < 0 || lineGray > 255)
				return 0;
			return 2;
		} else if (args[i].equals("-linecolor") && i+1 < args.length) {
			if (Color16.isColor(args[i+1]))
				lineColor = Color16.getColor(args[i+1]);
			else 
				return 0;
			return 2;
		} else if (args[i].equals("-dpp") && i+1 < args.length) {
			try {
				resolution = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (resolution <= 0)
				return 0;
			return 2;
		} else if (args[i].equals("-fontsize") && i+1 < args.length) {
			try {
				fontSizePt = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (fontSizePt <= 0)
				return 0;
			return 2;
		} else if (args[i].equals("-f") && i+1 < args.length) {
			fontsFile = args[i+1];
			return 2;
		} else if (args[i].equals("-freedir")) {
			forcedDirection = ResValues.DIR_NONE;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-hlr")) {
			forcedDirection = ResValues.DIR_HLR;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-hrl")) {
			forcedDirection = ResValues.DIR_HRL;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-vlr")) {
			forcedDirection = ResValues.DIR_VLR;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-vrl")) {
			forcedDirection = ResValues.DIR_VRL;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-h")) {
			forcedDirection = ResValues.DIR_H;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-v")) {
			forcedDirection = ResValues.DIR_V;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-lr")) {
			forcedDirection = ResValues.DIR_LR;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-rl")) {
			forcedDirection = ResValues.DIR_RL;
			directionSpec = false;
			return 1;
		} else if (args[i].equals("-hlrspec")) {
			forcedDirection = ResValues.DIR_HLR;
			directionSpec = true;
			return 1;
		} else if (args[i].equals("-size") && i+1 < args.length) {
			try {
				forcedSize = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (forcedSize == 0)
				forcedSize = Float.NaN;
			else if (forcedSize < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-specdist") && i+1 < args.length) {
			try {
				specDistEm = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (specDistEm < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-speccolor") && i+1 < args.length) {
			if (Color16.isColor(args[i+1]))
				specColor = new Color16(args[i+1]);
			else 
				return 0;
			return 2;
		} else if (args[i].equals("-padding") && i+1 < args.length) {
			try {
				padding = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (padding < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-lm") && i+1 < args.length) {
			try {
				userLeftMarginInch = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (userLeftMarginInch < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-bm") && i+1 < args.length) {
			try {
				userBottomMarginInch = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (userBottomMarginInch < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-rm") && i+1 < args.length) {
			try {
				userRightMarginInch = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (userRightMarginInch < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-tm") && i+1 < args.length) {
			try {
				userTopMarginInch = Float.parseFloat(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (userTopMarginInch < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-shadegray") && i+1 < args.length) {
			try {
				shadeGray = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (shadeGray < 0 || shadeGray > 255)
				return 0;
			return 2;
		} else if (args[i].equals("-shadefreq") && i+1 < args.length) {
			try {
				shadeFreq = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (shadeFreq < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-shadecover") && i+1 < args.length) {
			try {
				shadeCover = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (shadeCover <= 0)
				return 0;
			return 2;
		} else if (args[i].equals("-shadecolor") && i+1 < args.length) {
			if (Color16.isColor(args[i+1]))
				shadeColor = Color16.getColor(args[i+1]);
			else 
				return 0;
			return 2;
		} else if (args[i].equals("-notesize") && i+1 < args.length) {
			try {
				noteSizePt = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (noteSizePt <= 0)
				return 0;
			return 2;
		} else if (args[i].equals("-notedist") && i+1 < args.length) {
			try {
				noteDistPt = Integer.parseInt(args[i+1]);
			} catch (NumberFormatException e) {
				return 0;
			}
			if (noteDistPt < 0)
				return 0;
			return 2;
		} else if (args[i].equals("-notecolor") && i+1 < args.length) {
			if (Color16.isColor(args[i+1]))
				noteColor = new Color16(args[i+1]);
			else 
				return 0;
			return 2;
		} else
			return 0;
	}

	////////////////////////////////////////////////////////////////////////
	// Font access.

	// Get separation between glyphs as specified in font.
	public float fontSep() {
		return fontInfo.fontSep();
	}

	// Get separation between glyphs at box as specified in font.
	public float fontBoxSep() {
		return fontInfo.fontBoxSep();
	}

	// Retrieve glyph place.
	public GlyphPlace getGlyph(String name) {
		return fontInfo.getGlyph(name);
	}

	// Retrieve box places.
	public BoxPlaces getBox(String type) {
		return fontInfo.getBox(type);
	}

	// Retrieve set of names of box types in font.
	public Set getBoxTypes() {
		return fontInfo.getBoxTypes();
	}

	// Retrieve places for specifiers of direction.
	public GlyphPlace getSpec(int spec) {
		return fontInfo.getSpec(spec);
	}

	// Get Latin font for named glyphs.
	// Return null if no such font.
	public Font getLatinFont() {
		return fontInfo.getLatinFont();
	}

	// Get font for note.
	// Return null if no such font.
	public Font getNoteFont() {
		return fontInfo.getNoteFont();
	}
	// Get number thereof.
	public int getNoteFontNumber() {
		return fontInfo.getNoteFontNumber();
	}

	// Get numbered font.
	public Font getFont(int fileNumber) {
		return fontInfo.get(fileNumber);
	}

	// Character present in font?
	public boolean canDisplay(int fileNumber, long glyphIndex) {
		char c = (char) glyphIndex;
		if (fileNumber < 1 || fileNumber > fontInfo.size())
			return false;
		else 
			return fontInfo.get(fileNumber).canDisplay(c);
	}

	// Same, but character represented by place.
	public boolean canDisplay(GlyphPlace place) {
		return canDisplay(place.file, place.index);
	}

	// Characters in string are all present in font?
	public boolean canDisplay(int fileNumber, String str) {
		if (fileNumber < 1 || fileNumber > fontInfo.size())
			return false;
		else 
			return fontInfo.get(fileNumber).canDisplayUpTo(str) < 0;
	}

	// Mapper for PDF output.
	public DefaultFontMapper pdfMapper() {
		return fontInfo.pdfMapper();
	}

	// Get all glyphs in category. J is interpreted as Aa.
	public Vector getCategory(char cat) {
		return fontInfo.getCategory(cat);
	}

	// If name is mnemonic, map to Gardiner name.
	public String nameToGardiner(String name) {
		return fontInfo.nameToGardiner(name);
	}

	// If name is custom mnemonic, map to Gardiner name.
	public String customNameToGardiner(String name) {
		return fontInfo.customNameToGardiner(name);
	}

	// Does name exist?
	public boolean exists(String name) {
		GlyphPlace place = getGlyph(name);
		return canDisplay(place);
	}
}
