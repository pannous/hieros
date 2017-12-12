/***************************************************************************/
/*                                                                         */
/*  FormatHorsubgroup.java                                                 */
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

public class FormatHorsubgroup extends ResHorsubgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatHorsubgroup(ResHorsubgroup hor, HieroRenderContext context) {
	super(hor.switchs1,
		FormatHorsubgroupPartHelper.makeGroup(hor.group, context),
		hor.switchs2);
        this.context = context;
    }

    // Make formatted groups.
    public static Vector makeGroups(Vector groups, HieroRenderContext context) {
        Vector formatGroups = new Vector(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ResHorsubgroup group = (ResHorsubgroup) groups.get(i);
	    formatGroups.add(new FormatHorsubgroup(group, context));
        }
        return formatGroups;
    }

}
