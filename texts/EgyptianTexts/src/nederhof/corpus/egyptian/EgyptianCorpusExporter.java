// Export corpus of Ancient Egyptian to PDF.

package nederhof.corpus.egyptian;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.corpus.*;
import nederhof.interlinear.frame.pdf.*;

public class EgyptianCorpusExporter extends EgyptianCorpusViewer {

    // Open invisible viewer, export and leave.
    public EgyptianCorpusExporter(String location, String pdfPath) {
	super(location, false);
	File pdfDir = new File(pdfPath);
	writePdfTo(pdfDir);
	dispose();
    }
    // Without explicit directory.
    public EgyptianCorpusExporter(String location) {
	super(location, false);
	File corpusLoc = new File(corpus.getLocation());
	File pdfDir = new File(corpusLoc.getParentFile(), Settings.defaultPdfDir);
	writePdfTo(pdfDir);
	dispose();
    }

    // Export corpus.
    public static void main(String args[]) {
	if (args.length == 1) 
	    new EgyptianCorpusExporter(args[0]);
	else if (args.length == 2)
	    new EgyptianCorpusExporter(args[0], args[1]);
    }

}
