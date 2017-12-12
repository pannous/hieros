/***************************************************************************/
/*                                                                         */
/*  LxListener.java                                                        */
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

// If the user clicks on a button belonging to a lexical entry,
// there is an action. The default implemented here is that a 
// browser is directed to a URL. Different actions can however
// be implemented.

package nederhof.align;

import java.awt.event.*;
import java.applet.*;
import java.net.*;

class LxListener implements ActionListener {
    // AppletContext, which is null if there is no such context.
    private AppletContext context;
    // Link.
    private String url;

    // Construct, and remember appletContext and link.
    // The rest is here ignored.
    public LxListener(AppletContext context, 
	    String texthi, String textal, String texttr, String textfo,
	    String cite, String href,
	    String keyhi, String keyal, String keytr, String keyfo,
	    String dicthi, String dictal, String dicttr, String dictfo) {
	this.context = context;
	this.url = href;
    }

    // Upon action, open browser.
    public void actionPerformed(ActionEvent e) {
	if (context != null) {
	    try {
		context.showDocument(new URL(url), "_blank");
	    } catch (MalformedURLException err) {
		System.err.println("In preamble: " + err.getMessage());
	    }
	}
    }
}
