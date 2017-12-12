/***************************************************************************/
/*                                                                         */
/*  ITextFontMapper.java                                                   */
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

// Implemented by 
// PdfExport.ExportVariables

// Map font code to IText font.

package nederhof.align;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.awt.DefaultFontMapper;

import nederhof.res.*;

interface ITextFontMapper {

    // Get basefont belonging to given font code.
    public BaseFont getBaseFont(int f);

    // Get font size belonging to given font code.
    public float getFontSize(int f);

    // Get mapping from ASCII to transliteration font.
    public TrMap getEgyptMap();

    // Get mapper from fonts to basefonts.
    public DefaultFontMapper getHieroFontMapper();
}
