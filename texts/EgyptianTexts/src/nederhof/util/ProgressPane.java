/***************************************************************************/
/*                                                                         */
/*  ProgressPane.java                                                      */
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

package nederhof.util;

import java.awt.*;
import javax.swing.*;

public class ProgressPane extends JFrame {

    private String str;
    private JLabel label = new JLabel();

    public ProgressPane(String str) {
	// JProgressBar bar = new JProgressBar();
	// bar.setIndeterminate(true);
	label.setText(str);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	content.add(label);
	// content.add(bar);
	pack();
	setVisible(true);
    }

    // Set new message.
    public void setString(String str) {
	label.setText(str);
	pack();
	validate();
	repaint();
    }

}
