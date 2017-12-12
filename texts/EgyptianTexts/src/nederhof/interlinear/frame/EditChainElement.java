/***************************************************************************/
/*                                                                         */
/*  EditChainElement.java                                                  */
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

// Element in chain of windows allowing editing.
// If editing passes in next window, the current
// window should stop editing.

package nederhof.interlinear.frame;

public interface EditChainElement {

    // Is editing allowed in current window?
    public void allowEditing(boolean b);

}
