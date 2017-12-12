/***************************************************************************/
/*                                                                         */
/*  TreeHorsubgroupPartHelper.java                                         */
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

public class TreeHorsubgroupPartHelper {

    // Make formatted group.
    public static TreeHorsubgroupPart makeGroup(ResHorsubgroupPart group) {
        if (group instanceof ResVertgroup) {
            ResVertgroup g = (ResVertgroup) group;
            return new TreeVertgroup(g);
        } else if (group instanceof ResBasicgroup) {
            ResBasicgroup g = (ResBasicgroup) group;
            return TreeBasicgroupHelper.makeGroup(g);
        } else {
            System.err.println("Missing subclass in TreeHorsubgroupPartHelper");
            return null;
        }
    }

}

