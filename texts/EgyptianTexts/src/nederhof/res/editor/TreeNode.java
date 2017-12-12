/***************************************************************************/
/*                                                                         */
/*  TreeNode.java                                                          */
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

import java.util.*;

// Node in tree representation of RES fragment.
// Implemented by:
// TreeOp
// TreeNote
// TreeFragment
// TreeHorgroup
// TreeInsert
// TreeModify
// TreeStack
// TreeVertgroup
// Extended by:
// TreeNodeNote
public interface TreeNode {

    // Return panel.
    public NodePanel panel();

    // Parent.
    public TreeNode parent();

    // All children,
    public Vector allChildren();

    // Children,
    public Vector children();

    // Root of tree.
    public TreeFragment root();

    // Has focus?
    public boolean hasFocus();

    // Claim focus.
    public void claimFocus();

    // Label in panel.
    public String label();

    // RES to be printed in panel.
    public String resString();

    // Should RES be printed in legend?
    public boolean legendPreview();

    // Make buttons for parameters.
    public LegendParams makeParams();

    // Make buttons for structure.
    public LegendStructure makeStructureButtons();

}
