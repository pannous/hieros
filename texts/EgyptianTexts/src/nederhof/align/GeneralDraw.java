/***************************************************************************/
/*                                                                         */
/*  GeneralDraw.java                                                       */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Drawing text and lines, in AWT or iText.
// 
// Implemented by AWTDraw and ITextDraw.

package nederhof.align;

import java.awt.*;

import nederhof.res.*;
import nederhof.res.format.*;

interface GeneralDraw {

    public void drawString(int font, Color color, String s, float x, float y);

    public void drawHiero(FormatFragment hiero, float x, float y);

    public void fillRect(Color color, float x, float y, float width, float height);

    public void fillOval(Color color, float x, float y, float width, float height);

}
