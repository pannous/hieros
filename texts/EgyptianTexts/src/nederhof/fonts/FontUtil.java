/***************************************************************************/
/*                                                                         */
/*  FontUtil.java                                                          */
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

// Utilities for fonts.

package nederhof.fonts;

import java.awt.*;
import java.io.*;
import java.net.*;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.awt.DefaultFontMapper;

import nederhof.util.*;

public class FontUtil {

    // Get fonts from files.
    public static Font[] getFontsFrom(String[] files) {
	Font[] fonts = new Font[files.length];
	for (int i = 0; i < files.length; i++) 
	    fonts[i] = getFontFrom(files[i]);
	return fonts;
    }

    // As above, but also create mapper for PDF output, which maps
    // fonts to basefonts.
    public static Font[] getFontsFrom(String[] files, DefaultFontMapper mapper) {
	Font[] fonts = new Font[files.length];
	for (int i = 0; i < files.length; i++) {
	    fonts[i] = getFontFrom(files[i]);
	    URL url = expandToURL(files[i]);
	    DefaultFontMapper.BaseFontParameters params = 
		new DefaultFontMapper.BaseFontParameters(url.toString());
	    params.encoding =  BaseFont.IDENTITY_H;
	    params.embedded = true;
	    mapper.putName(fonts[i].getFontName(), params);
	}
	return fonts;
    }

    // Get font from file.
    public static Font getFontFrom(URL url) {
	InputStream fontFile = null;
	Font font = null;
	try {
	    fontFile = url.openStream();
	} catch (IOException e) {
	    System.err.println("Cannot interpret " + url);
	    return null;
	}
	try {
	    font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
	    fontFile.close();
	    return font;
	} catch (FontFormatException e) {
	    System.err.println("Cannot interpret " + url);
	    return null;
	} catch (IOException e) {
	    System.err.println("Cannot interpret " + url);
	    return null;
	}
    }

    // As above, but take url from file name.
    public static Font getFontFrom(String file) {
	URL url = expandToURL(file);
	if (url == null) {
	    System.err.println("File not found " + file);
	    return null;
	} else
	    return getFontFrom(url);
    }

    // Get base font from data file.
    public static Font font(String file) {
	URL url = FileAux.fromBase(file);
	if (url == null) {
	    System.err.println("File not found " + file);
	    return null;
	} else
	    return getFontFrom(url);
    }

    // As above, but also create mapper for PDF output, which maps
    // fonts to basefonts.
    public static Font getFontFrom(URL url, DefaultFontMapper mapper) {
	Font font = getFontFrom(url);
	DefaultFontMapper.BaseFontParameters params = 
	    new DefaultFontMapper.BaseFontParameters(url.toString());
	params.encoding =  BaseFont.IDENTITY_H;
	params.embedded = true;
	mapper.putName(font.getFontName(), params);
	return font;
    }

    // As above, but get file relative to current directory.
    public static Font getFontFrom(String file, DefaultFontMapper mapper) {
	URL url = expandToURL(file);
	return getFontFrom(url, mapper);
    }

    // Get base font from file.
    public static BaseFont getBaseFontFrom(String file) {
	try {
	    URL url = expandToURL(file);
	    if (url == null)
		throw new DocumentException();
	    file = url.toString();
	    return BaseFont.createFont(file,
		    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	} catch (DocumentException e) {
	    System.err.println("Cannot interpret " + file);
	    return null;
	} catch (IOException e) {
	    System.err.println("Cannot interpret " + file);
	    return null;
	}
    }

    // Get base font from data file.
    public static BaseFont baseFont(String file) {
	try {
	    URL url = FileAux.fromBase(file);
	    if (url == null)
		throw new DocumentException();
	    file = url.toString();
	    return BaseFont.createFont(file,
		    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	} catch (DocumentException e) {
	    System.err.println("Cannot interpret " + file);
	    return null;
	} catch (IOException e) {
	    System.err.println("Cannot interpret " + file);
	    return null;
	}
    }

    // Get URL of present class and put in front of file name.
    public static URL expandToURL(String file) {
	return FontUtil.class.getResource(file);
    }

}
