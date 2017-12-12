/***************************************************************************/
/*                                                                         */
/*  FormatTopgroupHelper.java                                              */
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

public class FormatTopgroupHelper {

    // Make formatted group.
    public static FormatTopgroup makeGroup(ResTopgroup group,
            HieroRenderContext context) {
        if (group instanceof ResVertgroup) {
            ResVertgroup g = (ResVertgroup) group;
            return new FormatVertgroup(g, context);
        } else if (group instanceof ResHorgroup) {
            ResHorgroup g = (ResHorgroup) group;
            return new FormatHorgroup(g, context);
        } else if (group instanceof ResBasicgroup) {
            ResBasicgroup g = (ResBasicgroup) group;
            return FormatBasicgroupHelper.makeGroup(g, context);
        } else {
            System.err.println("Missing subclass in FormatTopgroupHelper");
            return null;
        }
    }

    // Make formatted groups.
    public static Vector makeGroups(Vector groups, HieroRenderContext context) {
        Vector formatGroups = new Vector(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ResTopgroup group = (ResTopgroup) groups.get(i);
	    formatGroups.add(makeGroup(group, context));
        }
        return formatGroups;
    }

}
