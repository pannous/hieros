/***************************************************************************/
/*                                                                         */
/*  ResTopgroup.java                                                       */
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

// Subclasses:
// ResVertsubgroupPart
// ResHorsubgroupPart

package nederhof.res;

import java.util.*;

public interface ResTopgroup extends Cloneable {

    public Object clone();

    // Normalise positions of switches.
    public ResSwitch propagateBack(ResSwitch sw);
    public ResSwitch propagateBack();

    // Propagate global values.
    public GlobalValues propagate(GlobalValues globals);

    // Glyphs occurring in the group.
    public Vector<ResNamedglyph> glyphs();
}
