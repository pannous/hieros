/***************************************************************************/
/*                                                                         */
/*  EgyptianTierPart.java                                                  */
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

import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;

public abstract class EgyptianTierPart extends TierPart implements ResourcePart {

    // Add parameters to part.
    public abstract void setParams(RenderParameters renderParams);

    // What follows on this, relevant to whitespace after this.
    protected EgyptianTierPart next;

    public void setNext(EgyptianTierPart next) {
	this.next = next;
    }
    public EgyptianTierPart getNext() {
	return next;
    }

    // Starts with space? Default is no.
    public boolean hasLeadSpace() {
	return false;
    }

    // What is advance up to next non-space character. Default is 0;
    public float leadSpaceAdvance() {
	return 0f;
    }

    // Ascent without coordinate markers.
    public float exclusiveAscent() {
	return ascent();
    }

    // Is in footnote? (Only possible for a small number of tierparts.
    protected boolean footnote = false;

    public void setFootnote(boolean footnote) {
	this.footnote = footnote;
    }

    public boolean isFootnote() {
	return footnote;
    }

    // Is in edit mode?
    protected boolean edit = false;

    public void setEdit(boolean edit) {
	this.edit = edit;
    }

    public boolean isEdit() {
	return edit;
    }

}
