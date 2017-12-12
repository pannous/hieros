// Exporting text to PDF.

package nederhof.corpus.egyptian;

import nederhof.corpus.*;

import nederhof.interlinear.frame.pdf.*;

public class EgyptianTextExporter {

    // Create exporter for text with path.
    public EgyptianTextExporter(String textPath) {
	export(new Text(textPath));
    }

    // Export text to PDF.
    public static void export(Text text) {
	Exporter.export(text, 
		EgyptianTextViewer.resourceGenerators(), 
		EgyptianTextViewer.aligner(), 
		EgyptianTextViewer.pdfRenderParameters(text));
    }

    // Print all texts.
    public static void main(String args[]) {
	for (int i = 0; i < args.length; i++)
	    new EgyptianTextExporter(args[i]);
    }

}

