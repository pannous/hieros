/***************************************************************************/
/*                                                                         */
/*  ExternalFont.java                                                      */
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

// External font for export to PDF.

package nederhof.fonts;

public class ExternalFont {

    // Name of font.
    private String name;

    // File names.
    private String plainFile;
    private String italicFile;
    private String boldFile;
    private String boldItalicFile;

    // Construct font.
    public ExternalFont(String name, String plainFile, String italicFile,
	    String boldFile, String boldItalicFile) {
	this.name = name;
	this.plainFile = plainFile;
	this.italicFile = italicFile;
	this.boldFile = boldFile;
	this.boldItalicFile = boldItalicFile;
    }

    // Get name of font.
    public String getName() {
	return name;
    }

    // Get file names of font.
    public String getPlain() {
	return plainFile;
    }
    public String getItalic() {
	return italicFile;
    }
    public String getBold() {
	return boldFile;
    }
    public String getBoldItalic() {
	return boldItalicFile;
    }

}
