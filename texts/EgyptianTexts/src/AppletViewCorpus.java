import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.corpus.frame.*;
import nederhof.corpus.egyptian.*;
import nederhof.util.*;

public class AppletViewCorpus extends JApplet 
	implements ActionListener {

    // The corpus file.
    private String corpus;

    public void init() {
	corpus = getParameter("corpus");
	JButton startButton = new JButton("view corpus");
	startButton.addActionListener(this);
	getContentPane().setLayout(new java.awt.GridLayout(1,0));
	getContentPane().add(startButton);
    }

    public void actionPerformed(ActionEvent e) {
	try {
	    URL url = FileAux.fromBase(corpus);
	    new EgyptianCorpusViewer(url.toString());
	} catch (Exception ex) {
	    String exName = ex.getClass().getName();
	    StackTraceElement[] stack = ex.getStackTrace();
	    String trace = "";
	    for (int i = 0; i < stack.length; i++) 
		trace += stack[i] + "\n";
	    HTMLWindow errorWindow = 
		new HTMLWindow(exName, trace, 900, 500);
	    errorWindow.setVisible(true);
	}
    }

    // For stand alone program as JAR.
    // Corpus can be in JAR or external.
    public static void main(String[] args) {
	if (args.length > 0) {
	    String corpus = args[0];
	    CorpusViewer frame = new EgyptianCorpusViewer(corpus);
	    frame.setStandAlone(true);
	} else {
	    String corpus = "corpus/corpus.xml";
	    URL url = FileAux.fromBase(corpus);
	    if (url == null) {
		System.err.println("Cannot find: " + corpus);
	    } else {
		CorpusViewer frame = new EgyptianCorpusViewer(url.toString());
		frame.setStandAlone(true);
	    }
	}
    }

}
