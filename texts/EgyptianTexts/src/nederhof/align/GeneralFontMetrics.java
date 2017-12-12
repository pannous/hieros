/***************************************************************************/
/*                                                                         */
/*  GeneralFontMetrics.java                                                */
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

// Font metrics that can be used for different kinds of fonts.
// Also, we allow for more precise distances than whole pixels,
// e.g. points.
// 
// Implemented by AWTFontMetrics and ITextFontMetrics.

package nederhof.align;

interface GeneralFontMetrics {

    public float getAscent();

    public float getAscent(String str);

    public float getDescent();

    public float getDescent(String str);

    public float getHeight();

    public float getLeading();

    public float stringWidth(String str);

}
