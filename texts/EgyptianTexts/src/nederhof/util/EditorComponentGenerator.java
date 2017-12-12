/***************************************************************************/
/*                                                                         */
/*  EditorComponentGenerator.java                                          */
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

// Auxiliary to StyledTextPane.
// Is a generator of typed components that may appear
// in edited text.

package nederhof.util;

import javax.swing.event.*;

import java.awt.*;

public interface EditorComponentGenerator {

    // Make fresh object.
    public Component makeComponent(ChangeListener listener);

    // Make object from existing object (e.g. string).
    public Component makeComponent(Object o, ChangeListener listener);

    // Extract information from component.
    public Object extract(Component comp);

}
