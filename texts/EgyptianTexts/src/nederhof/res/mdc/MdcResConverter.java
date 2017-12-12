/***************************************************************************/
/*                                                                         */
/*  MdcResConverter.java                                                   */
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

package nederhof.res.mdc;

import java.io.*;

import nederhof.res.*;
import nederhof.util.*;

class MdcResConverter {

    // Only used for converting mnemonics to Gardiner codes.
    private static HieroRenderContext hieroContext =
	new HieroRenderContext(10); // 10 is arbitrary

    public static void convert(File inFile, File outFile) {
	try {
	    FileInputStream stream = new FileInputStream(inFile);
	    int len = stream.available();
	    byte b[] = new byte[len];
	    stream.read(b);
	    String content = new String(b);
	    stream.close();

	    MdcValidation validation = new MdcValidation(content, outFile);
	    TextValidationWindow.iterateCorrections(validation);
	} catch (FileNotFoundException e) {
	    System.err.println(e);
	} catch (IOException e) {
	    System.err.println(e);
	}
    }

    public static void main(String[] args) {
	if (args.length == 2)
	    convert(new File(args[0]), new File(args[1]));
	else
	    System.err.println("Warning: MdcResConverter expects 2 arguments");
    }
}
