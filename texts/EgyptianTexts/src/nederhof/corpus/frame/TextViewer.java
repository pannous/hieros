/***************************************************************************/
/*                                                                         */
/*  TextViewer.java                                                        */
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

// Viewer of text, manipulating resources in text.

package nederhof.corpus.frame;

import nederhof.corpus.*;

public interface TextViewer {

    // The text being viewed.
    public Text getText();

    // Make window visible.
    public void setVisible(boolean b);

    // Try to save and quit. Return whether successful.
    public boolean trySaveQuit();

    // Dispose of window.
    public void dispose();

}
