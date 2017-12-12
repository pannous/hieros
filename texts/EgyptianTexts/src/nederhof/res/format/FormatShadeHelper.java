/***************************************************************************/
/*                                                                         */
/*  FormatShadeHelper.java                                                 */
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

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatShadeHelper {

    ////////////////////////////////////////////////////////////
    // Properties.

    // Chop up rectangle into smaller rectangles, according to shading
    // pattern.
    public static Rectangle chopRectangle(Rectangle rect, String pattern) {
        for (int i = 0; i < pattern.length(); i++)
            switch (pattern.charAt(i)) {
                case 't':
                    int height = rect.height / 2;
                    rect = new Rectangle(rect.x, rect.y, rect.width, height);
                    break;
                case 'b':
                    int topHeight = rect.height / 2;
                    int bottomHeight = rect.height - topHeight;
                    rect = new Rectangle(rect.x, rect.y + topHeight, rect.width, bottomHeight);
                    break;
                case 's':
                    int width = rect.width / 2;
                    rect = new Rectangle(rect.x, rect.y, width, rect.height);
                    break;
                case 'e':
                    int startWidth = rect.width / 2;
                    int endWidth = rect.width - startWidth;
                    rect = new Rectangle(rect.x + startWidth, rect.y, endWidth, rect.height);
                    break;
            }
        return rect;
    }

    // Chop up rectangle into two, either horizontally or vertically at x or
    // y. It can be that x < rect.x, y < rect.y, resp.
    public static Rectangle chopStartH(Rectangle rect, int x) {
	return new Rectangle(rect.x, rect.y, x - rect.x, rect.height);
    }
    public static Rectangle chopEndH(Rectangle rect, int x) {
        return new Rectangle(x, rect.y, rect.width - x + rect.x, rect.height);
    }
    public static Rectangle chopStartV(Rectangle rect, int y) {
        return new Rectangle(rect.x, rect.y, rect.width, y - rect.y);
    }
    public static Rectangle chopEndV(Rectangle rect, int y) {
        return new Rectangle(rect.x, y, rect.width, rect.height - y + rect.y);
    }

    // For box turned from horizontal to vertical,
    // translate the shading pattern accordingly.
    public static String turnPatternHV(String pattern) {
        String newPat = "";
        for (int i = 0; i < pattern.length(); i++)
            switch(pattern.charAt(i)) {
                case 't':
                    newPat += 'e';
                    break;
                case 'b':
                    newPat += 's';
                    break;
                case 's':
                    newPat += 't';
                    break;
                case 'e':
                    newPat += 'b';
                    break;
            }
        return newPat;
    }
    public static String turnPatternVH(String pattern) {
        String newPat = "";
        for (int i = 0; i < pattern.length(); i++)
            switch(pattern.charAt(i)) {
                case 't':
                    newPat += 's';
                    break;
                case 'b':
                    newPat += 'e';
                    break;
                case 's':
                    newPat += 'b';
                    break;
                case 'e':
                    newPat += 't';
                    break;
            }
        return newPat;
    }

    // Extract shading relevant to one side.
    // Return whether whole side is to be shaded.
    public static boolean zoomInStart(Vector patterns, Vector newPatterns) {
	for (int i = 0; i < patterns.size(); i++) {
	    String pattern = (String) patterns.get(i);
	    String newPattern = "";
	    for (int j = 0; j < pattern.length(); j++) {
		if (pattern.charAt(j) == 't' ||
			pattern.charAt(j) == 'b')
		    newPattern += pattern.charAt(j);
		else if (pattern.charAt(j) == 's') {
		    newPattern += pattern.substring(j+1);
		    break;
		} else {
		    newPattern = null;
		    break;
		}
	    }
	    if (newPattern != null) {
		if (newPattern.equals(""))
		    return true;
		else
		    newPatterns.add(newPattern);
	    }
	}
	return false;
    }
    public static boolean zoomInEnd(Vector patterns, Vector newPatterns) {
	for (int i = 0; i < patterns.size(); i++) {
	    String pattern = (String) patterns.get(i);
	    String newPattern = "";
	    for (int j = 0; j < pattern.length(); j++) {
		if (pattern.charAt(j) == 't' ||
			pattern.charAt(j) == 'b')
		    newPattern += pattern.charAt(j);
		else if (pattern.charAt(j) == 'e') {
		    newPattern += pattern.substring(j+1);
		    break;
		} else {
		    newPattern = null;
		    break;
		}
	    }
	    if (newPattern != null) {
		if (newPattern.equals(""))
		    return true;
		else
		    newPatterns.add(newPattern);
	    }
	}
	return false;
    }
    public static boolean zoomInTop(Vector patterns, Vector newPatterns) {
	for (int i = 0; i < patterns.size(); i++) {
	    String pattern = (String) patterns.get(i);
	    String newPattern = "";
	    for (int j = 0; j < pattern.length(); j++) {
		if (pattern.charAt(j) == 's' ||
			pattern.charAt(j) == 'e')
		    newPattern += pattern.charAt(j);
		else if (pattern.charAt(j) == 't') {
		    newPattern += pattern.substring(j+1);
		    break;
		} else {
		    newPattern = null;
		    break;
		}
	    }
	    if (newPattern != null) {
		if (newPattern.equals(""))
		    return true;
		else
		    newPatterns.add(newPattern);
	    }
	}
	return false;
    }
    public static boolean zoomInBottom(Vector patterns, Vector newPatterns) {
	for (int i = 0; i < patterns.size(); i++) {
	    String pattern = (String) patterns.get(i);
	    String newPattern = "";
	    for (int j = 0; j < pattern.length(); j++) {
		if (pattern.charAt(j) == 's' ||
			pattern.charAt(j) == 'e')
		    newPattern += pattern.charAt(j);
		else if (pattern.charAt(j) == 'b') {
		    newPattern += pattern.substring(j+1);
		    break;
		} else {
		    newPattern = null;
		    break;
		}
	    }
	    if (newPattern != null) {
		if (newPattern.equals(""))
		    return true;
		else
		    newPatterns.add(newPattern);
	    }
	}
	return false;
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Put shades specified at group.
    public static void shade(UniGraphics image, HieroRenderContext context,
            Rectangle shadeRect, boolean shade, Vector<String> shades) {
        if (shade)
            image.shade(shadeRect, context);
        else
            for (int i = 0; i < shades.size(); i++) {
                String s = shades.get(i);
                Rectangle part = chopRectangle(shadeRect, s);
                image.shade(part, context);
            }
    }

    ////////////////////////////////////////////////////////////
    // RESlite.

    // As above, but translate to RESlite.
    public static void shadeResLite(int x, int y, Vector code,
            HieroRenderContext context, Rectangle shadeRect,
            boolean shade, Vector shades) {
        if (shade)
            shadeResLite(x, y, code, context, shadeRect);
        else
            for (int i = 0; i < shades.size(); i++) {
                String s = (String) shades.get(i);
                Rectangle part = chopRectangle(shadeRect, s);
                shadeResLite(x, y, code, context, part);
            }
    }

    // Auxiliary method used by above, and elsewhere.
    public static void shadeResLite(int x, int y, Vector code,
            HieroRenderContext context, Rectangle rect) {
        if (rect.width <= 0 || rect.height <= 0)
            return;
        REScodeShades area = new REScodeShades();
        area.width = context.pixToMilEm(rect.width);
        area.height = context.pixToMilEm(rect.height);
        area.x = Math.round((1000.0f * (rect.x - x) + 500.0f * rect.width) /
                context.emSizePix());
        area.y = Math.round((1000.0f * (rect.y - y) + 500.0f * rect.height) /
                context.emSizePix());
        code.add(area);
    }

}
