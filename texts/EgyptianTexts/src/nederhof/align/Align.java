/***************************************************************************/
/*                                                                         */
/*  Align.java                                                             */
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

// Main class in implementation of AELalignViewer.

package nederhof.align;

import java.applet.*;
import java.io.*;

public class Align {

    // Version number of software.
    public static final String versionNumber = "0.3.2";

    // Name of program and version.
    public static final String programName = "AELalignViewer " +
	Align.versionNumber;

    // Store list of XML files in system of streams.
    public static void main(String[] args) {
	XMLfiles files = new XMLfiles(args);
	StreamSystem system = files.alignSystem();
	File fullPath = new File(system.getFileName());
	String fileName = fullPath.getName();
	Display window = 
	    new Display(system, fileName, null);
	window.setStandAlone(true);
    }

    // Same but for call (usually) from applet. If from application, context is null.
    public static void openText(String[] args, String dataBase,
	    String textName, AppletContext context) {
	String[] fullArgs = new String[args.length];
	for (int i = 0; i < args.length; i++)
	    fullArgs[i] = dataBase + args[i];
	XMLfiles files = new XMLfiles(fullArgs);
	StreamSystem system = files.alignSystem();
	Display window = new Display(system, textName, context);
    }

    // Same but for the sole purpose of writing a PDF file.
    public static void writePdf(String[] args, String target, String header) {
	XMLfiles files = new XMLfiles(args);
	StreamSystem system = files.alignSystem();
	PdfExport.printPdf(system, target, header);
    }
}
