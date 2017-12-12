// Separate frame for viewing single resource.
// This can show focus, and report back to caller focus changes.
package nederhof.interlinear.frame;

import java.awt.*;
import javax.swing.*;

import nederhof.interlinear.*;

public abstract class ResourceViewer extends JFrame {

    // The resource.
    protected TextResource resource;
    // Get the resource.
    public TextResource getResource() {
	return resource;
    }

    // Listener to changes to focus.
    protected TextPane listener = null;

    // Set listener.
    public void setListener(TextPane listener) {
	this.listener = listener;
    }

    // Report focus to listener.
    public void reportFocus(int i) {
	if (listener != null) {
	    listener.scrollTo(resource, i);
	}
    }

    // Set focus to i-th sign.
    public abstract void setFocus(int i);
    // Set focus to no sign.
    public abstract void setNoFocus();

    // Maybe refresh, after possible modification of
    // resource.
    public abstract void refresh();

}
