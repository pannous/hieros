/***************************************************************************/
/*                                                                         */
/*  TierPdfPart.java                                                       */
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

// TierPart that can be printed in Awt.

package nederhof.interlinear;

import java.util.*;

import com.itextpdf.text.pdf.*;

public interface TierPdfPart extends ITierPart {

    // Draw given positions.
    public abstract void draw(int i, int j, float x, float y, PdfContentByte surface);

}
