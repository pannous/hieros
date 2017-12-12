/***************************************************************************/
/*                                                                         */
/*  TreeVertsubgroup.java                                                  */
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

import nederhof.res.*;

public class TreeVertsubgroup extends ResVertsubgroup {

    // Constructor when tree-group already constructed.
    public TreeVertsubgroup(TreeVertsubgroupPart group) {
        super(group);
    }

    // Constructor.
    public TreeVertsubgroup(ResVertsubgroup vert) {
        super(vert.switchs1,
                TreeVertsubgroupPartHelper.makeGroup(vert.group),
                vert.switchs2);
    }

    // Make formatted groups.
    public static Vector<ResVertsubgroup> makeGroups(Vector<ResVertsubgroup> groups) {
        Vector<ResVertsubgroup> formatGroups = new Vector<ResVertsubgroup>(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ResVertsubgroup group = groups.get(i);
            formatGroups.add(new TreeVertsubgroup(group));
        }
        return formatGroups;
    }

}
