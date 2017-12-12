package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.multi.*;

public class GuiAux {

    // See if OS is Mac.
    private static String os = System.getProperty("os.name").toLowerCase();
    private static boolean isMac = os.indexOf("mac") >= 0;

    public static void setLookAndFeel() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		try {
		    UIManager.setLookAndFeel(
			    new MetalLookAndFeel());
		} catch (Exception e) {
		    System.err.println(e.getMessage());
		    // ignore
		}
	    }
	});
    }

    // Always choose ALT as modifier, unless it is mac.
    public static KeyStroke shortcut(int key) {
	if (isMac)
	    return 
		KeyStroke.getKeyStroke(key,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	else
	    return 
		KeyStroke.getKeyStroke(key, ActionEvent.ALT_MASK);
    }

}
