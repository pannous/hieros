/***************************************************************************/
/*                                                                         */
/*  ResourceGenerator.java                                                 */
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

// A resource generator produces an empty resource.

package nederhof.interlinear;

import java.io.*;

public abstract class ResourceGenerator {

    public abstract TextResource generate(File file) throws IOException;

    public abstract TextResource interpret(String fileName, Object in);

    public abstract String getName();

    public abstract String getDescription();

}
