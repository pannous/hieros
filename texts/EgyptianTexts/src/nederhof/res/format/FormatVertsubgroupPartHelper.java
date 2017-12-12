/***************************************************************************/
/*                                                                         */
/*  FormatVertsubgroupPartHelper.java                                      */
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

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatVertsubgroupPartHelper {

    // Make formatted group.
    public static FormatVertsubgroupPart makeGroup(ResVertsubgroupPart group,
            HieroRenderContext context) {
        if (group instanceof ResHorgroup) {
            ResHorgroup g = (ResHorgroup) group;
            return new FormatHorgroup(g, context);
        } else if (group instanceof ResBasicgroup) {
            ResBasicgroup g = (ResBasicgroup) group;
            return FormatBasicgroupHelper.makeGroup(g, context);
        } else {
            System.err.println("Missing subclass in FormatVertsubgroupPartHelper: "
		    + group.getClass());
            return null;
        }
    }

}
