/***************************************************************************/
/*                                                                         */
/*  HTMLWindow.java                                                        */
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

// Window with HTML.

package nederhof.util;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class HTMLWindow extends JFrame implements ActionListener {

    // Default initial dimensions.
    private static final int width = 500;
    private static final int height = 600;

    // Context of browser, if any. Needed for hyperlinks.
    private AppletContext context;

    // Main constructor.
    public HTMLWindow(String title, int width, int height) {
        setTitle(title);
        setJMenuBar(new QuitMenu(this));
        setSize(width, height);
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new Listener());
    }

    // Constructor with text.
    public HTMLWindow(String title, String text, int width, int height) {
	this(title, width, height);
	setText(text);
    }
    // default size
    public HTMLWindow(String title, String text) {
	this(title, text, width, height);
    }

    // Constructor with URL.
    public HTMLWindow(String title, URL url, int width, int height) {
	this(title, width, height);
	Container content = getContentPane();
	content.add(getAddressContents(url));
    }
    // default size
    public HTMLWindow(String title, URL url) {
	this(title, url, width, height);
    }

    // Set context for hyperlinks.
    public void setContext(AppletContext context) {
	this.context = context;
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

    // Actual content, which is input string.
    public void setText(String text) {
	Container content = getContentPane();
	content.removeAll();
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
        htmlText.setContentType("text/html");
        htmlText.setText(text);
        final JScrollPane scroll = new JScrollPane(htmlText);
	content.add(scroll);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMinimum());
	    }
	});
    }

    // Content read from address.
    // Ensure that text is printed with antialiasing.
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
