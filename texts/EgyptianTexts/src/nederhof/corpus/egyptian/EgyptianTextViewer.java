// Viewer for single text.

package nederhof.corpus.egyptian;

import java.io.*;
import java.util.*;

import nederhof.alignment.*;
import nederhof.alignment.egyptian.*;
import nederhof.corpus.*;
import nederhof.corpus.frame.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.util.*;

public class EgyptianTextViewer extends InterlinearViewer {

    // Create viewer for Egyptian text with path.
    public EgyptianTextViewer(String textPath) {
	this(new Text(textPath));
    }

    // Create viewer for text itself.
    public EgyptianTextViewer(Text text) {
	super(null, text, resourceGenerators(), aligner(),
	    renderParameters(), pdfRenderParameters(text));
    }

    public static Vector resourceGenerators() {
        Vector generators = new Vector();
        generators.add(new EgyptianResourceGenerator());
        generators.add(new EgyptianLexicoGenerator());
        generators.add(new EgyptianOrthoGenerator());
        generators.add(new EgyptianImageGenerator());
        return generators;
    }

    public static Autoaligner aligner() {
	return new EgyptianAutoaligner();
    }

    private static RenderParameters renderParameters() {
	return new EgyptianRenderParameters();
    }

    public static PdfRenderParameters pdfRenderParameters(Text text) {
	File pdfDir = new File(".");
	String baseName = baseName(text);
	String name = text.getName();
	return new EgyptianPdfRenderParameters("" + pdfDir, baseName, name);
    }

    // Get base name of text.
    private static String baseName(Text text) {
        File file = new File(text.getLocation());
        String name = file.getName();
        return FileAux.removeExtension(name);
    }

    // As called from e.g. applet.
    public static void main(String[] args) {
	String textPath = args.length > 0 ? args[0] : "project/text.php";
	new EgyptianTextViewer(textPath);
    }

}
