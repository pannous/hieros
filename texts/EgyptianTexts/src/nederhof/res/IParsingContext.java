/***************************************************************************/
/*                                                                         */
/*  IParsingContext.java                                                   */
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

// The context for parsing, allows gathering
// of errors and checking of availability of
// glyphs in font.

package nederhof.res;

import java.util.*;

public interface IParsingContext {

    public void setInput(String string);

    public int nErrors();

    public String error(int i);

    public int errorPos(int i);

    public void addError(String message);

    public void reportError(String message, int charPos, int lineNo);

    public void reportWarning(String message, int charPos, int lineNo);

    public boolean suppressReporting();

    public void setSuppressReporting(boolean suppress);

    public void setIgnoreWarnings(boolean suppress);

    public BoxPlaces getBox(String type);

    public GlyphPlace getGlyph(String name);

    public String nameToGardiner(String name);

}
