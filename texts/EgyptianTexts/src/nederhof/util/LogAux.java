package nederhof.util;

import javax.swing.*;

public class LogAux {

    // Print information when System.out cannot be
    // used.
    public static void report(String message) {
	        JOptionPane.showMessageDialog(null,
			message, "information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Print information when System.err cannot be
    // used.
    public static void reportError(String message) {
	JOptionPane.showMessageDialog(null,
		message, "error", JOptionPane.ERROR_MESSAGE);
    }

}
