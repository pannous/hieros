/***************************************************************************/
/*                                                                         */
/*  TreeNodeNote.java                                                      */
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

package nederhof.res.editor;

// Internal node in tree representation of RES fragment 
// that can carry a Note. 
// Implemented by:
// TreeBox
// TreeEmptyglyph
// TreeNamedglyph
public interface TreeNodeNote extends TreeNode {

    // Remove note.
    public void removeNote(TreeNote note);

    // Append note behind this note.
    public void appendNote(TreeNote note);

}
