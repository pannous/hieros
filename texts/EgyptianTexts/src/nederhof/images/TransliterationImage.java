/***************************************************************************/
/*                                                                         */
/*  TransliterationImage.java                                              */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.images;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import nederhof.align.TrMap;
import nederhof.fonts.*;
import nederhof.interlinear.egyptian.*;
import nederhof.util.*;

import org.jibble.epsgraphics.*;

class TransliterationImage {

    // It seems images placed in HTML are generally too low,
    // unless this is corrected by adding extra whitespace
    // at the bottom.
    private static int HEIGHT_CORRECTION = 4;

    // Print Egyptological transliteration as EPS to file.
    // Style can be one of: ib, i, sb, s.
    public static void printEgyptological(String text, String style, float size, 
	    String filename) throws IOException {
	int styleNum = Font.PLAIN;
	if (style.equals("ib"))
	    styleNum = Font.BOLD + Font.ITALIC;
	else if (style.equals("i"))
	    styleNum = Font.ITALIC;
	else if (style.equals("sb"))
	    styleNum = Font.BOLD;
	Font lowerFont = TransHelper.translitLower(styleNum, size);
	Font upperFont = TransHelper.translitUpper(styleNum, size);
	Vector parts = TransHelper.lowerUpperParts(text);

	if (FileAux.hasExtension(filename, "eps")) 
	    printEgyptologicalEps(parts, lowerFont, upperFont, filename);
	else 
	    printEgyptologicalPixel(parts, lowerFont, upperFont, filename);
    }

    // Special treatment of EPS.
    public static void printEgyptologicalEps(Vector parts,
	    Font lowerFont, Font upperFont, String filename) throws IOException {
	EpsGraphics2D dummy = new EpsGraphics2D("dummy");
	FontMetrics lowerMetrics = dummy.getFontMetrics(lowerFont);
	FontMetrics upperMetrics = dummy.getFontMetrics(upperFont);
	int width = textWidth(dummy, parts, lowerFont, upperFont);
	int height = 
	    Math.max(lowerMetrics.getHeight(), upperMetrics.getHeight());
	dummy.close();

	OutputStream out = new FileOutputStream(filename);
	EpsGraphics2D g = new EpsGraphics2D("transliteration", 
		out, 0, 0, width, height);
	printText(g, parts, lowerFont, upperFont);
	g.close();
	out.close();
    }

    // Treatment of pixelized images.
    public static void printEgyptologicalPixel(Vector parts,
	    Font lowerFont, Font upperFont, String filename) throws IOException {
	BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	Graphics2D dummy = (Graphics2D) dummyImage.getGraphics();
	FontMetrics lowerMetrics = dummy.getFontMetrics(lowerFont);
	FontMetrics upperMetrics = dummy.getFontMetrics(upperFont);
	int width = textWidth(dummy, parts, lowerFont, upperFont);
	int height = 
	    Math.max(lowerMetrics.getHeight(), upperMetrics.getHeight() + 
		    HEIGHT_CORRECTION);
	dummy.dispose();

	BufferedImage im = new BufferedImage(width, height, 
		BufferedImage.TYPE_INT_RGB);
	Graphics2D g = (Graphics2D) im.getGraphics();
	g.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
	g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	g.setColor(Color.WHITE);
	g.fillRect(0, 0, width, height);
	g.setColor(Color.BLACK);
	printText(g, parts, lowerFont, upperFont);
	g.dispose();
	File out = new File(filename);
	boolean succeed = ExtendedImageIO.write(im,
		FileAux.getExtension(filename), out, 1);
	if (!succeed) 
	    System.err.println("Erroneous image format: " + filename);
    }

    // Determine total width.
    private static int textWidth(Graphics2D g, Vector parts, 
	    Font lowerFont, Font upperFont) {
	FontRenderContext context = g.getFontRenderContext();
	int width = 0;
	for (int i = 0; i < parts.size(); i++) {
	    Object[] pair = (Object[]) parts.get(i);
	    String kind = (String) pair[0];
	    String text = (String) pair[1];
	    Font font = kind.equals("translower") ? lowerFont : upperFont;
	    if (i == parts.size() - 1) {
		TextLayout layout = new TextLayout(text, font, context);
		Rectangle2D bounds = layout.getBounds();
		width += (int) Math.ceil(bounds.getWidth());
	    } else {
		FontMetrics metrics = g.getFontMetrics(font);
		width += metrics.stringWidth(text);
	    }
	}
	return width;
    }

    // Draw parts.
    private static void printText(Graphics2D g, Vector parts,
	    Font lowerFont, Font upperFont) throws IOException {
	FontRenderContext context = g.getFontRenderContext();
	FontMetrics upperMetrics = g.getFontMetrics(upperFont);
	int width = 0;
	for (int i = 0; i < parts.size(); i++) {
	    Object[] pair = (Object[]) parts.get(i);
	    String kind = (String) pair[0];
	    String text = (String) pair[1];
	    Font font = kind.equals("translower") ? lowerFont : upperFont;
	    FontMetrics metrics = g.getFontMetrics(font);
	    TextLayout layout = new TextLayout(text, font, context);
	    Rectangle2D bounds = layout.getBounds();
	    g.setFont(font);
	    g.drawString(text, width - (int) bounds.getX(), 
		    upperMetrics.getAscent());
	    width += metrics.stringWidth(text);
	}
    }

    public static void main(String[] args) {
	if (args.length < 2)
	    return;
	try {
	    String text = args[0];
	    String filename = args[1];

	    float fontSize = 12f;
	    if (args.length > 2)
		fontSize = Float.parseFloat(args[2]);

	    String style = "s";
	    if (args.length > 3 && 
		    (args[3].equals("s") ||  
		     args[3].equals("sb") ||
		     args[3].equals("i") ||
		     args[3].equals("ib")))
		style = args[3];

	    printEgyptological(text, style, fontSize, filename);
	} catch (Exception e) {
	    System.out.println(e);
	}
    }
}
