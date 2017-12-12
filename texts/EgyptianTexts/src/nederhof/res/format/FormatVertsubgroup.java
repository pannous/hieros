/***************************************************************************/
/*                                                                         */
/*  FormatVertsubgroup.java                                                */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatVertsubgroup extends ResVertsubgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatVertsubgroup(ResVertsubgroup vert, HieroRenderContext context) {
        super(vert.switchs1,
                FormatVertsubgroupPartHelper.makeGroup(vert.group, context),
                vert.switchs2);
        this.context = context;
    }

    // Make formatted groups.
    public static Vector makeGroups(Vector groups, HieroRenderContext context) {
        Vector formatGroups = new Vector(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ResVertsubgroup group = (ResVertsubgroup) groups.get(i);
            formatGroups.add(new FormatVertsubgroup(group, context));
        }
        return formatGroups;
    }

}
