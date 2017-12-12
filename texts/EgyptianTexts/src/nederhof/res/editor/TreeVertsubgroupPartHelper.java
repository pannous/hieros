/***************************************************************************/
/*                                                                         */
/*  TreeVertsubgroupPartHelper.java                                        */
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

public class TreeVertsubgroupPartHelper {

    // Make formatted group.
    public static TreeVertsubgroupPart makeGroup(ResVertsubgroupPart group) {
        if (group instanceof ResHorgroup) {
            ResHorgroup g = (ResHorgroup) group;
            return new TreeHorgroup(g);
        } else if (group instanceof ResBasicgroup) {
            ResBasicgroup g = (ResBasicgroup) group;
            return TreeBasicgroupHelper.makeGroup(g);
        } else {
            System.err.println("Missing subclass in TreeVertsubgroupPartHelper");
            return null;
        }
    }

}

