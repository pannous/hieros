/***************************************************************************/
/*                                                                         */
/*  TreeHorsubgroup.java                                                   */
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

public class TreeHorsubgroup extends ResHorsubgroup {

    // Constructor when tree-group already constructed.
    public TreeHorsubgroup(TreeHorsubgroupPart group) {
        super(group);
    }

    // Constructor.
    public TreeHorsubgroup(ResHorsubgroup hor) {
        super(hor.switchs1,
                TreeHorsubgroupPartHelper.makeGroup(hor.group),
                hor.switchs2);
    }

    // Make formatted groups.
    public static Vector<ResHorsubgroup> makeGroups(Vector<ResHorsubgroup> groups) {
        Vector<ResHorsubgroup> formatGroups = new Vector<ResHorsubgroup>(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ResHorsubgroup group = groups.get(i);
            formatGroups.add(new TreeHorsubgroup(group));
        }
        return formatGroups;
    }

}
