/***************************************************************************/
/*                                                                         */
/*  EgyptianTierAwtPart.java                                               */
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

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;

public abstract class EgyptianTierAwtPart extends EgyptianTierPart 
	implements TierAwtPart {

    // The parameters.
    protected EgyptianRenderParameters renderParams;

    public void setParams(RenderParameters renderParams) {
	this.renderParams = (EgyptianRenderParameters) renderParams;
    }

    // Positions to be highlighted.
    protected TreeSet highlights = new TreeSet();

    // Positions after which highlighting should be put.
    protected TreeSet highlightsAfter = new TreeSet();

    // How wide is bar after highlighted positions.
    protected int highlightBarWidth = 3;

    // Highlight position.
    public void highlight(int i) {
	highlights.add(new Integer(i));
    }
    // Undo highlighting.
    public void unhighlight(int i) {
	highlights.remove(new Integer(i));
    }

    // Highlight after position.
    public void highlightAfter(int i) {
	highlightsAfter.add(new Integer(i));
    }
    // Undo highlighting after position.
    public void unhighlightAfter(int i) {
	highlightsAfter.remove(new Integer(i));
    }

    // Default (overridden where appropriate, is to have
    // rectangle comprising entire object.
    public Rectangle getRectangle(int i, int j) {
	return new Rectangle(0,
		- Math.round(ascent()),
		Math.round(width(0, nSymbols())),
		Math.round(ascent() + descent()));
    }

}
