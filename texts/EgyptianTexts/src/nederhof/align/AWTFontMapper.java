/***************************************************************************/
/*                                                                         */
/*  AWTFontMapper.java                                                     */
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

// Map font code to AWT font.

package nederhof.align;

import java.awt.*;

interface AWTFontMapper {

    // Get font belonging to given font code.
    public Font getFont(int f);

}
