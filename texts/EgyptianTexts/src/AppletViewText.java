import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.corpus.frame.*;
import nederhof.corpus.egyptian.*;
import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;
import nederhof.util.*;

public class AppletViewText extends JApplet 
	implements ActionListener {

    // The text name.
    private String text;

    public void init() {
	text = getParameter("text");
	JButton startButton = new JButton("view text");
	startButton.addActionListener(this);
	getContentPane().setLayout(new java.awt.GridLayout(1,0));
	getContentPane().add(startButton);
    }

    public void actionPerformed(ActionEvent e) {
	new EgyptianTextViewer(FileAux.fromBase(text).toString());
    }

    //////////////////////////////////////////////
    // Participating in joint translation project.
    // For stand alone program as JAR file.

    // File names.
    private static String textTarget = "text.xml";
    private static String resourceTarget1 = "PtahhotepTrP.txt";
    private static String resourceTarget2 = "PtahhotepDevaudP.txt";
    private static String resourceTarget3 = "PtahhotepHiP.xml";
    private static String resourceTarget4 = "Ptahhotep.xml";
    // Originals of files.
    private static String textSource = "project/client/" + textTarget;
    private static String resourceSource1 = "project/client/" + resourceTarget1;
    private static String resourceSource2 = "project/resources/" + resourceTarget2;
    private static String resourceSource3 = "project/resources/" + resourceTarget3;
    private static String resourceSource4 = "project/align/" + resourceTarget4;

    // If files do not exist, create them by copying.
    // Then start viewer.
    public static void main(String[] args) {
	File called = FileAux.calledPath();
	File parentDir = called.getParentFile();
	File fileTarget0 = new File(parentDir, textTarget);
	File fileTarget1 = new File(parentDir, resourceTarget1);
	File fileTarget2 = new File(parentDir, resourceTarget2);
	File fileTarget3 = new File(parentDir, resourceTarget3);
	File fileTarget4 = new File(parentDir, resourceTarget4);

	if (!fileTarget1.exists()) {
	    try {
		URL url0 = FileAux.fromBase(textSource);
		URL url1 = FileAux.fromBase(resourceSource1);
		URL url2 = FileAux.fromBase(resourceSource2);
		URL url3 = FileAux.fromBase(resourceSource3);
		URL url4 = FileAux.fromBase(resourceSource4);

		// Get parameters.
		String fullName =
		    JOptionPane.showInputDialog(null, 
			    "your name as you are known on the AEL list:",
			    "Resource parameters", JOptionPane.PLAIN_MESSAGE);
		if (fullName == null || fullName.matches("\\s+"))
		    throw new IOException("long name is needed");
		String shortName =
		    JOptionPane.showInputDialog(null, 
			    "your short name used as label " +
			    "(e.g. given name, family name if short, or abbreviated family name):",
			    "Resource parameters", JOptionPane.PLAIN_MESSAGE);
		if (shortName == null || shortName.matches("\\s+"))
		    throw new IOException("short name is needed");
		String email =
		    JOptionPane.showInputDialog(null, 
			    "email (with which you are subscribed to AEL list):",
			    "Resource parameters", JOptionPane.PLAIN_MESSAGE);
		if (email == null || email.matches("\\s+"))
		    throw new IOException("email is needed");
		String password =
		    JOptionPane.showInputDialog(null, 
			    "password (as provided through the AEL list):",
			    "Resource parameters", JOptionPane.PLAIN_MESSAGE);
		if (password == null || password.matches("\\s+"))
		    throw new IOException("password is needed");

		// Copy.
		FileAux.copyFile(url0.toString(), fileTarget0);
		FileAux.copyFile(url1.toString(), fileTarget1);
		FileAux.copyFile(url2.toString(), fileTarget2);
		FileAux.copyFile(url3.toString(), fileTarget3);
		FileAux.copyFile(url4.toString(), fileTarget4);

		// Fill in parameters.
		TextResource resource = IndexPane.toResource(fileTarget1.getPath(), 
		    EgyptianTextViewer.resourceGenerators());
		resource.setProperty("creator", fullName);
		resource.setProperty("name", fullName);
		resource.setProperty("labelname", shortName);
		resource.setProperty("email", email);
		resource.setProperty("password", password);
		resource.save();
	    } catch (Exception e) {
		LogAux.reportError("Cannot create resource:\n" + e.getMessage());
		return;
	    }
	} 
	// String textUri = FileAux.getUriString(fileTarget0);
	InterlinearViewer frame = new EgyptianTextViewer(fileTarget0.getPath());
	frame.setStandAlone(true);
    }

}
