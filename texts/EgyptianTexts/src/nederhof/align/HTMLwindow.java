/***************************************************************************/
/*                                                                         */
/*  HTMLwindow.java                                                        */
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

// Window for AELalignViewer, showing HTML text.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.util.*;

public class HTMLwindow extends JFrame implements ActionListener {

    // Initial dimensions.
    private static final int width = Settings.htmlWidthInit;
    private static final int height = Settings.htmlHeightInit;

    // Context of browser. may be null.
    AppletContext context;

    // Auxiliary constructor.
    private HTMLwindow(String title) {
	setTitle(title);
	setJMenuBar(new QuitMenu(this));
	setSize(width, height);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new Listener());
    }

    // Constructors inside package.
    HTMLwindow(String title, String address, AppletContext context) {
	this(title);
	this.context = context;
	Container content = getContentPane();
	content.add(getAddressContents(address));
    }

    HTMLwindow(String name, String created, String header, String bibl,
	    AppletContext context) {
	this(name);
	this.context = context;
	Container content = getContentPane();
	String biblSection = (bibl.equals("") ? "" :
		"<h2> Bibliography </h2>\n" + "<ul>\n" + bibl + "</ul>\n");
	content.add(getStringContents(
		    "<html>\n" + 
		    "<p>" + created + "</p>\n" +
		    header +
		    biblSection + 
		    "</html>\n"));
    }

    // Constructor for use outside package.
    public HTMLwindow(String title, URL url) {
	this(title);
	this.context = null;
	Container content = getContentPane();
	content.add(getAddressContents(url));
    }

    // Listen if window to be closed. Merely make invisible.
    private class Listener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    setVisible(false);
	}
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("quit")) 
	    setVisible(false);
    }

    // Actual content, read from address.
    // Ensure that text is printed with antialiasing.
    private JComponent getAddressContents(String address) {
	URL url = FileAux.fromBase(address);
	return getAddressContents(url);
    }
    private JComponent getAddressContents(URL url) {
	JComponent content;
	if (url == null)
	    content = new JLabel("Address not found: " + url);
	else {
	    JTextPane urlText = new JTextPane() {
		public void paintComponent(Graphics g) {
		    Graphics2D g2 = (Graphics2D) g;
		    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
			    RenderingHints.VALUE_RENDER_QUALITY);
		    super.paintComponent(g2);
		}
	    };
	    urlText.setEditable(false);
	    urlText.addHyperlinkListener(new BrowserListener());
	    try {
		urlText.setPage(url);
	    } catch (IOException e) {
		System.err.println(e.getMessage());
	    }
	    content = new JScrollPane(urlText);
	}
	return content;
    }

    // Actual content, which is input string.
    private JComponent getStringContents(String str) {
	JTextPane htmlText = new JTextPane() {
	    public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY);
		super.paintComponent(g2);
	    }
	};
	htmlText.setEditable(false);
	htmlText.addHyperlinkListener(new BrowserListener());
	htmlText.setContentType("text/html");
	htmlText.setText(str);
	JComponent content = new JScrollPane(htmlText);
	return content;
    }

    // Hyperlinks lead to browser looking at URL.
    private class BrowserListener implements HyperlinkListener {
	public void hyperlinkUpdate(HyperlinkEvent e) {
	    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
		    context != null) {
		URL url = e.getURL();
		context.showDocument(url, "_blank");
	    }
	}
    }
}
