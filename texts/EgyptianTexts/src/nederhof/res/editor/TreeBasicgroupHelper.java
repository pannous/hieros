/***************************************************************************/
/*                                                                         */
/*  TreeBasicgroupHelper.java                                              */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
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

import nederhof.res.*;

public class TreeBasicgroupHelper {

    // Make group as tree.
    public static TreeBasicgroup makeGroup(ResBasicgroup group) {
        if (group instanceof ResNamedglyph) {
            ResNamedglyph g = (ResNamedglyph) group;
            return new TreeNamedglyph(g);
        } else if (group instanceof ResEmptyglyph) {
            ResEmptyglyph g = (ResEmptyglyph) group;
            return new TreeEmptyglyph(g);
        } else if (group instanceof ResBox) {
            ResBox g = (ResBox) group;
            return new TreeBox(g);
        } else if (group instanceof ResStack) {
            ResStack g = (ResStack) group;
            return new TreeStack(g);
        } else if (group instanceof ResInsert) {
            ResInsert g = (ResInsert) group;
            return new TreeInsert(g);
        } else if (group instanceof ResModify) {
            ResModify g = (ResModify) group;
            return new TreeModify(g);
        } else {
            System.err.println("Missing subclass in TreeBasicgroupHelper");
            return null;
        }
    }

}
