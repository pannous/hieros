/***************************************************************************/
/*                                                                         */
/*  NamedPropertyEditor.java                                               */
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

// Some element that is used to edit a named property of a resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.*;

public abstract class NamedPropertyEditor extends PropertyEditor {

    // Resource.
    protected TextResource resource;

    // Name of property.
    private String prop;

    // Construct editor of property of resource, with name.
    public NamedPropertyEditor(TextResource resource, String prop) {
	this.resource = resource;
	this.prop = prop;
    }

    //////////////////////////////
    // Value manipulation.

    // Put value of property in element.
    public void initValue() {
	putValue(resource.getProperty(prop));
    }

    // Subclass to be specify how to initialize.
    public abstract void putValue(Object val);

    // Save value from element.
    public void saveValue() {
	Object val = retrieveValue();
	resource.setProperty(prop, val);
    }

    // Subclass to specify how to retrieve.
    public abstract Object retrieveValue();

}
