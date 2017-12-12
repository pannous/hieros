/***************************************************************************/
/*                                                                         */
/*  Color16.java                                                           */
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

// Choice of 16 colors, for RES and REScode.
// Is immutable.

package nederhof.res;

import java.awt.*;

public class Color16 {
    // Main identifier of color is numerical code.
    private int colorCode;

    private static final int WHITE_CODE = 0;
    private static final int BLUE_CODE = 11;
    private static final int BLACK_CODE = 15;
    private static final int NONE = -1;

    // Number of colors.
    public static final int SIZE = 16;

    // In REScode, 0 is white.
    public static final Color16 WHITE = new Color16(WHITE_CODE);
    public static final Color16 BLACK = new Color16(BLACK_CODE);
    public static final Color16 BLUE = new Color16(BLUE_CODE);
    public static final Color16 NO_COLOR = new Color16(NONE);

    // Construct color from REScode number.
    public Color16(int code) {
	colorCode = code;
    }

    // Construct color from RES name.
    // If not valid name, return black.
    public Color16(String name) {
	this(toNumber(name));
    }

    // Get numerical code for color.
    public int code() {
	return colorCode;
    }

    // Equality of colors.
    public static boolean equal(Color16 color1, Color16 color2) {
	return color1.code() == color2.code();
    }

    // Make Color from RES name.
    public static Color getColor(String name) {
	Color16 c = new Color16(name);
	return c.getColor();
    }

    // Is white?
    public boolean isWhite() {
	return colorCode == WHITE_CODE;
    }

    // Is black?
    public boolean isBlack() {
	return colorCode == BLACK_CODE;
    }

    // Is colored? (I.e. not white and not black.)
    public boolean isColored() {
	return !isWhite() && !isBlack();
    }

    // Convert name to REScode number.
    // If not valid name, return black.
    private static int toNumber(String name) {
	if (name.equals("white"))
	    return 0;
	else if (name.equals("silver"))
	    return 1;
	else if (name.equals("gray"))
	    return 2;
	else if (name.equals("yellow"))
	    return 3;
	else if (name.equals("fuchsia"))
	    return 4;
	else if (name.equals("aqua"))
	    return 5;
	else if (name.equals("olive"))
	    return 6;
	else if (name.equals("purple"))
	    return 7;
	else if (name.equals("teal"))
	    return 8;
	else if (name.equals("red"))
	    return 9;
	else if (name.equals("lime"))
	    return 10;
	else if (name.equals("blue"))
	    return 11;
	else if (name.equals("maroon"))
	    return 12;
	else if (name.equals("green"))
	    return 13;
	else if (name.equals("navy"))
	    return 14;
	else /* black or erroneous name */
	    return 15;
    }

    // Is name of RES color?
    public static boolean isColor(String name) {
	return name.equals("black") || name.equals("red") || name.equals("green") ||
	    name.equals("blue") || name.equals("white") || name.equals("aqua") ||
	    name.equals("fuchsia") || name.equals("gray") || name.equals("lime") ||
	    name.equals("maroon") || name.equals("navy") || name.equals("olive") ||
	    name.equals("purple") || name.equals("silver") || name.equals("teal") ||
	    name.equals("yellow");
    }

    // Is valid color?
    public boolean isColor() {
	return colorCode != NONE;
    }

    // Translate REScode color number to Color.
    public Color getColor() {
	int red, green, blue;
	switch (colorCode) {
	    case 0: // white
		red = 0xFF; green = 0xFF; blue = 0xFF;
		break;
	    case 1: // silver
		red = 0xC0; green = 0xC0; blue = 0xC0;
		break;
	    case 2: // gray
		red = 0x80; green = 0x80; blue = 0x80;
		break;
	    case 3: // yellow
		red = 0xFF; green = 0xFF; blue = 0x00;
		break;
	    case 4: // fuchsia
		red = 0xFF; green = 0x00; blue = 0xFF;
		break;
	    case 5: // aqua
		red = 0x00; green = 0xFF; blue = 0xFF;
		break;
	    case 6: // olive
		red = 0x80; green = 0x80; blue = 0x00;
		break;
	    case 7: // purple
		red = 0x80; green = 0x00; blue = 0x80;
		break;
	    case 8: // teal
		red = 0x00; green = 0x80; blue = 0x80;
		break;
	    case 9: // red
		red = 0xFF; green = 0x00; blue = 0x00;
		break;
	    case 10: // lime
		red = 0x00; green = 0xFF; blue = 0x00;
		break;
	    case 11: // blue
		red = 0x00; green = 0x00; blue = 0xFF;
		break;
	    case 12: // maroon
		red = 0x80; green = 0x00; blue = 0x00;
		break;
	    case 13: // green
		red = 0x00; green = 0x80; blue = 0x00;
		break;
	    case 14: // navy
		red = 0x00; green = 0x00; blue = 0x80;
		break;
	    case 15: // black
		red = 0x00; green = 0x00; blue = 0x00;
		break;
	    default: // in case of error, return: black
		System.err.println("Strange REScode color number: " + colorCode);
		red = 0x00; green = 0x00; blue = 0x00;
		break;
	}
	return new Color(red, green, blue);
    }

    public String toString() {
	switch (colorCode) {
	    case 0:
		return "white";
	    case 1:
		return "silver";
	    case 2: 
		return "gray";
	    case 3: 
		return "yellow";
	    case 4: 
		return "fuchsia";
	    case 5: 
		return "aqua";
	    case 6: 
		return "olive";
	    case 7: 
		return "purple";
	    case 8: 
		return "teal";
	    case 9: 
		return "red";
	    case 10: 
		return "lime";
	    case 11: 
		return "blue";
	    case 12: 
		return "maroon";
	    case 13: 
		return "green";
	    case 14: 
		return "navy";
	    case 15: 
		return "black";
	    default: 
		int c = 5/0;
		System.err.println("Strange REScode color number: " + colorCode);
		return "";
	}
    }
}
