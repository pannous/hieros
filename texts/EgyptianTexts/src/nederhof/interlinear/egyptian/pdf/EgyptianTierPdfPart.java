/***************************************************************************/
/*                                                                         */
/*  EgyptianTierPdfPart.java                                               */
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

// TierPart for PDF.

package nederhof.interlinear.egyptian.pdf;

import java.awt.Color;
import com.itextpdf.text.BaseColor;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;

public abstract class EgyptianTierPdfPart extends EgyptianTierPart
	implements TierPdfPart {

    // The parameters.
    protected EgyptianPdfRenderParameters renderParams;

    public void setParams(RenderParameters renderParams) {
	this.renderParams = (EgyptianPdfRenderParameters) renderParams;
    }

    // Colors need to be converted to BaseColor for PDF.
    public BaseColor toBase(Color color) {
	return new BaseColor(color.getRed(), 
		color.getGreen(), color.getBlue(), color.getAlpha());
    }

}
