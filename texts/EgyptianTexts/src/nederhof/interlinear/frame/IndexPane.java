/***************************************************************************/
/*                                                                         */
/*  IndexPane.java                                                         */
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

// Part of interlinear frame containing index.
// Here the resource headers can be changed, and
// it is determined which tiers are to be printed.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.xml.sax.*;

import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public abstract class IndexPane extends JPanel 
	implements ActionListener, EditChainElement {

    // Directory of corpus.
    private String corpusDirectory;

    // The text being viewed.
    private Text text;

    // The textual resources, in panels.
    private Vector<ResourcePanel> resPanels = new Vector<ResourcePanel>();

    // The precedence resources, in panels.
    private Vector<PrecedencePanel> precPanels = new Vector<PrecedencePanel>();

    // The panel containing resource generators.
    private JPanel genPanel = new VertPanel();

    // The autoalignments, pairing tiers.
    // The location of the resources is replaced by
    // the resources themselves, to allow for relocation of
    // resources.
    private Vector<Object[]> autoaligns;

    // The panel containing edit or view, if any.
    private Preamble viewPanel;
    private PropertiesEditor editPanel;

    // Text elements that remain. To be enabled/disabled.
    private Vector stableElements = new Vector();

    // Parser of resource files.
    private static DocumentBuilder parser = 
	SimpleXmlParser.construct(false, false);

    // Construct pane. 
    public IndexPane(String corpusDirectory, Text text, Vector resourceGenerators) {
	this.corpusDirectory = corpusDirectory;
	this.text = text;
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	// resources
	Vector<String> resources = text.getResources();
	for (int i = 0; i < resources.size(); i++) {
	    String file = resources.get(i);
	    TextResource resource = toResource(file, resourceGenerators);
	    if (resource != null) 
		addResourcePanel(resource);
	}

	// precedences
	Vector<String[]> precedences = text.getPrecedences();
	for (int i = 0; i < precedences.size(); i++) {
	    String[] prec = precedences.get(i);
	    ResourcePrecedence precedence = toPrecedence(prec);
	    if (precedence != null)
		addPrecedencePanel(precedence);
	}

	// autoaligns
	Vector<String[]> autoStrings = text.getAutoaligns();
	setAutoaligns(autoStrings);

	// generators
	for (int i = 0; i < resourceGenerators.size(); i++) {
	    if (i > 0)
		genPanel.add(sep());
	    ResourceGenerator gen = (ResourceGenerator) resourceGenerators.get(i);
	    genPanel.add(new GeneratorPanel(gen));
	}

	makeIndex();
    }

    // Set precedences.
    public void setPrecedences(Vector precedences) {
	precPanels = new Vector<PrecedencePanel>();
	for (int i = 0; i < precedences.size(); i++) {
	    ResourcePrecedence precedence = 
		(ResourcePrecedence) precedences.get(i);
	    addPrecedencePanel(precedence);
	}
    }

    // Set autoaligns.
    public void setAutoaligns(Vector<String[]> autoStrings) {
	autoaligns = new Vector<Object[]>();
	for (int i = 0; i < autoStrings.size(); i++) {
	    String[] autoString = autoStrings.get(i);
	    Object[] autoResource = toAutoalign(autoString);
	    if (autoResource != null)
		autoaligns.add(autoResource);
	}
    }

    // Turn file into a resource, by trying all available resource generator
    // to parse it. These return a TextResource if successful, and null
    // otherwise.
    public static TextResource toResource(String location, Vector resourceGenerators) {
	if (FileAux.hasExtension(location, "xml")) {
	    try {
		InputStream in = FileAux.addressToStream(location);
		Document doc = parser.parse(in);
		for (int i = 0; i < resourceGenerators.size(); i++) {
		    ResourceGenerator gen = (ResourceGenerator) resourceGenerators.get(i);
		    TextResource resource = gen.interpret(location, doc);
		    if (resource != null) {
			in.close();
			return resource;
		    }
		}
		in.close();
	    } catch (SAXException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    } catch (IOException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    } catch (IllegalArgumentException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    } catch (NullPointerException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    }
	} else if (FileAux.hasExtension(location, "txt")) {
	    try {
		for (int i = 0; i < resourceGenerators.size(); i++) {
		    ResourceGenerator gen = (ResourceGenerator) resourceGenerators.get(i);
		    InputStream in = FileAux.addressToStream(location);
		    LineNumberReader reader = new LineNumberReader(
			    new InputStreamReader(in, "UTF-8"));
		    /*
		    LineNumberReader reader;
		    if (location.startsWith("jar:") || 
			    location.startsWith("http:") ||
			    location.startsWith("file:")) {
			URL url = new URL(location);
			reader =
			    new LineNumberReader(
				    new InputStreamReader(url.openStream(), "UTF-8"));
		    } else
			reader =
			    new LineNumberReader(
				    new InputStreamReader(new FileInputStream(location), "UTF-8"));
		    */
		    TextResource resource = gen.interpret(location, reader);
		    in.close();
		    if (resource != null) 
			return resource;
		}
	    } catch (MalformedURLException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    } catch (FileNotFoundException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    } catch (IOException e) {
		JOptionPane.showMessageDialog(null,
			"IndexPane.toResource: " + e.getMessage(),
			"Resource error", JOptionPane.ERROR_MESSAGE);
	    }
	} 
	return null;
    }

    // Create precedence between resources. Or null if not successful.
    private ResourcePrecedence toPrecedence(String[] prec) {
	try {
	    ResourcePrecedence precedence = new ResourcePrecedence(prec[0]);
	    TextResource resource1 = resourceOf(prec[1]);
	    TextResource resource2 = resourceOf(prec[2]);
	    if (resource1 != null && resource2 != null) {
		precedence.setResources(resource1, resource2);
		return precedence;
	    }
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(this,
		    "IndexPane.toPrecedence: " + e.getMessage(),
		    "Precedence error", JOptionPane.ERROR_MESSAGE);
	}
	return null;
    }

    // Create precedence between resources. Or null if not successful.
    public static ResourcePrecedence toPrecedence(String[] prec, Vector resources) {
	try {
	    ResourcePrecedence precedence = new ResourcePrecedence(prec[0]);
	    TextResource resource1 = resourceOf(prec[1], resources);
	    TextResource resource2 = resourceOf(prec[2], resources);
	    if (resource1 != null && resource2 != null) {
		precedence.setResources(resource1, resource2);
		return precedence;
	    }
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "IndexPane.toPrecedence: " + e.getMessage(),
		    "Precedence error", JOptionPane.ERROR_MESSAGE);
	}
	return null;
    }

    // In autoalign, replace locations by actual resource,
    private Object[] toAutoalign(String[] autoString) {
	return toAutoalign(autoString, getResourceList());
    }

    // In autoalign, replace locations by actual resource,
    public static Object[] toAutoalign(String[] autoString, Vector resources) {
	TextResource res1 = resourceOf(autoString[0], resources);
	String tier1 = autoString[1];
	TextResource res2 = resourceOf(autoString[2], resources);
	String tier2 = autoString[3];
	if (res1 != null && res2 != null) 
	    return new Object[] {res1, tier1, res2, tier2};
	else
	    return null;
    }

    // Get resource matching file name, or null if none.
    private TextResource resourceOf(String name) {
	return resourceOf(name, getResourceList());
    }

    // Get resource matching file name, or null if none.
    private static TextResource resourceOf(String name, Vector resources) {
	for (int i = 0; i < resources.size(); i++) {
	    TextResource resource = (TextResource) resources.get(i);
	    if (resource.getLocation().equals(name))
		return resource;
	}
	return null;
    }

    // Add resource, in panel.
    private void addResourcePanel(TextResource resource) {
	resPanels.add(new ResourcePanel(resource) {
		public void viewResource() {
		    IndexPane.this.viewResource(this);
		}
		public void editResource() {
		    IndexPane.this.editResource(this);
		}
		public void moveUp() {
		    IndexPane.this.moveUpResource(this);
		}
		public void moveDown() {
		    IndexPane.this.moveDownResource(this);
		}
		public void deleteResource() {
		    IndexPane.this.deleteResource(this);
		}
		public void makeTextChanged() {
		    IndexPane.this.makeTextChanged();
		}
		public void makeResourceChanged() {
		    IndexPane.this.makeResourceChanged();
		}
	});
    }

    // Add precedence, in panel.
    private void addPrecedencePanel(ResourcePrecedence precedence) {
	precPanels.add(new PrecedencePanel(precedence) {
		public void editPrecedence() {
		    IndexPane.this.editPrecedence(this);
		}
	});
    };

    // Panel for generator.
    private class GeneratorPanel extends HorPanel {
	private GenButton genButton;
	public GeneratorPanel(ResourceGenerator gen) {
	    genButton = new GenButton(gen);
	    add(sep());
	    add(new EnableLabel("Add "));
	    add(genButton);
	    add(sep());
	    add(new CommentLabel(gen));
	    add(horGlue());
	}
	public void setEnabled(boolean b) {
	    genButton.setEnabled(b);
	}
    }

    // Layout the pane.
    // One resource may be highlighted (< 0 if none).
    // For editing, all panels are disabled.
    private JPanel makeIndex(int highlighted, boolean editing) {
	removeAll();
	JPanel main = new VertPanel();
	for (int i = 0; i < resPanels.size(); i++) {
	    ResourcePanel res = resPanels.get(i);
	    res.setHighlight(i == highlighted);
	    res.setEditable(text.isEditable());
	    res.setEnabled(!editing);
	    res.setMoveableUp(i > 0);
	    res.setMoveableDown(i < resPanels.size() - 1);
	    res.refreshLayout();
	    main.add(res);
	}
	if (text.isEditable())
	    for (int i = 0; i < precPanels.size(); i++) {
		PrecedencePanel prec = precPanels.get(i);
		prec.setHighlight(i == highlighted - resPanels.size());
		prec.setEnabled(!editing);
		prec.refreshLayout();
		main.add(prec);
	    }
	if (text.isEditable()) {
	    main.add(Box.createVerticalStrut(10));
	    main.add(genPanel);
	    main.add(Box.createVerticalStrut(10));
	    for (int i = 0; i < stableElements.size(); i++) {
		JComponent but = (JComponent) stableElements.get(i);
		but.setEnabled(!editing);
	    }
	}
	main.add(vertGlue());
	return main;
    }

    // None being edited.
    public void makeIndex() {
	JPanel main = makeIndex(-1, false);
	JScrollPane scroll = new JScrollPane(main);
	scroll.getVerticalScrollBar().setUnitIncrement(10);
	add(scroll);
	viewPanel = null;
	editPanel = null;
	setResourceOpen(false);
	validate();
    }

    // Make index above viewed header.
    private void makeIndexAndView(int i) {
	JPanel left = makeIndex(i, false);
	Preamble right = new Preamble(i);
	createSplit(left, right);
	viewPanel = right;
	editPanel = null;
	setResourceOpen(true);
	validate();
    }

    // Make index above edited header.
    private void makeIndexAndEdit(int i) {
	JPanel left = makeIndex(i, true);
	PropertiesEditor right = properties(i); 
	createSplit(left, right);
	viewPanel = null;
	editPanel = right;
	setResourceOpen(true);
	validate();
	right.initValues();
    }
    
    // Called by editor of properties.
    public void returnFromEdit() {
	makeIndex();
	setResourceOpen(false);
    }

    // Put view in panel with close button.
    private class Preamble extends VertPanel {

	public Preamble(int i) {
	    ResourcePanel resourcePanel = resPanels.get(i);
	    Component preamble = resourcePanel.getResource().preamble();
	    add(sep());
	    add(preamble);
	}
    }

    // Put editors of properties in panel, one below the other.
    private PropertiesEditor properties(int i) {
	if (i < resPanels.size()) {
	    ResourcePanel resourcePanel = resPanels.get(i);
	    return resourcePanel.getResource().editor(this);
	} else {
	    PrecedencePanel panel = precPanels.get(i - resPanels.size());
	    return panel.getResource().editor(this);
	}
    }

    // Move panel of resource up.
    private void moveUpResource(ResourcePanel panel) {
	int i = resPanels.indexOf(panel);
	if (i > 0) {
	    resPanels.remove(i);
	    resPanels.insertElementAt(panel, i-1);
	}
	makeIndex();
	makeTextChanged();
    }

    // View properties of resource.
    private void viewResource(ResourcePanel panel) {
	int i = resPanels.indexOf(panel);
	if (i >= 0) 
	    makeIndexAndView(i);
    }

    // Edit properties of resource.
    private void editResource(ResourcePanel panel) {
	int i = resPanels.indexOf(panel);
	if (i >= 0) 
	    makeIndexAndEdit(i);
    }

    // Move panel of resource down.
    private void moveDownResource(ResourcePanel panel) {
	int i = resPanels.indexOf(panel);
	if (i >= 0 && i < resPanels.size() - 1) {
	    resPanels.remove(i);
	    resPanels.insertElementAt(panel, i+1);
	}
	makeIndex();
	makeTextChanged();
    }

    // Remove panel of resource.
    private void deleteResource(ResourcePanel panel) {
	resPanels.remove(panel);
	makeIndex();
	makeTextChanged();
    }

    // Edit properties of precedence.
    private void editPrecedence(PrecedencePanel panel) {
	int i = precPanels.indexOf(panel);
	if (i >= 0)
	    makeIndexAndEdit(i + resPanels.size());
    }

    // Create split pane.
    private void createSplit(Component left, Component right) {
	JSplitPane split = 
	    new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	split.setOneTouchExpandable(true);
	split.setDividerSize((int) (split.getDividerSize() * 1.3));
	split.setResizeWeight(0.2);
	split.setDividerLocation(0.2);
	JScrollPane scrollLeft = new ScrollConservative(left);
	JScrollPane scrollRight = new ScrollConservative(right);
	split.setLeftComponent(scrollLeft);
	split.setRightComponent(scrollRight);
	add(split);
    }

    /////////////////////////////////////////////////////
    // Management of resources.

    // For file that is to become resource, determine relative
    // path and check that no such file is currently used for
    // a resource.
    private File relativizeResource(File file) throws IOException {
	file = FileAux.getRelativePath(file);
	String loc = file.getPath();
	for (int i = 0; i < resPanels.size(); i++) {
	    ResourcePanel panel = resPanels.get(i);
	    String oldLoc = panel.getResource().getLocation();
	    if (oldLoc.equals(loc))
		throw new IOException("Resource already in text: " + oldLoc);
	}
	return file;
    }

    // Get resources (filenames) currently maintained.
    public Vector<String> getResources() {
	Vector resources = new Vector();
	for (int i = 0; i < resPanels.size(); i++) {
	    ResourcePanel panel = resPanels.get(i);
	    String loc = panel.getResource().getLocation();
	    resources.add(loc);
	}
	return resources;
    }

    // Get resources themselves.
    public Vector<TextResource> getResourceList() {
	Vector resources = new Vector();
	for (int i = 0; i < resPanels.size(); i++) {
	    ResourcePanel panel = resPanels.get(i);
	    resources.add(panel.getResource());
	}
	return resources;
    }

    // Get precedences currently maintained.
    public Vector<String[]> getPrecedences() {
	Vector<String[]> precedences = new Vector<String[]>();
	for (int i = 0; i < precPanels.size(); i++) {
	    PrecedencePanel panel = precPanels.get(i);
	    ResourcePrecedence precedence = panel.getResource();
	    String loc = precedence.getLocation();
	    TextResource resource1 = precedence.getResource1();
	    TextResource resource2 = precedence.getResource2();
	    String loc1 = resource1.getLocation();
	    String loc2 = resource2.getLocation();
	    precedences.add(new String[] {loc, loc1, loc2});
	}
	return precedences;
    }

    // Get precendences themselves.
    public Vector<ResourcePrecedence> getPrecedenceList() {
	Vector precedences = new Vector();
	for (int i = 0; i < precPanels.size(); i++) {
	    PrecedencePanel panel = precPanels.get(i);
	    precedences.add(panel.getResource());
	}
	return precedences;
    }

    // Get 4-tuples for autoalignment, with strings.
    public Vector<String[]> getAutoaligns() {
	Vector<String[]> autoStrings = new Vector<String[]>();
	for (int i = 0; i < autoaligns.size(); i++) {
	    Object[] autoRes = autoaligns.get(i);
	    TextResource resource1 = (TextResource) autoRes[0];
	    String loc1 = resource1.getLocation();
	    String tier1 = (String) autoRes[1];
	    TextResource resource2 = (TextResource) autoRes[2];
	    String loc2 = resource2.getLocation();
	    String tier2 = (String) autoRes[3];
	    String[] autoString = new String[] {loc1, tier1, loc2, tier2};
	    autoStrings.add(autoString);
	}
	return autoStrings;
    }

    // Get 4-tuples for autoalignment, with TextResources.
    public Vector<Object[]> getAutoalignList() {
	return autoaligns;
    }

    /////////////////////////////////////////////////////
    // Panel components.

    // Label in stable text.
    private class EnableLabel extends PropertyEditor.PlainLabel {
        public EnableLabel(String text) {
            super(text);
            stableElements.add(this);
        }
    }

    // Button for generator.
    private class GenButton extends JButton implements ActionListener {
	// The generator.
	private ResourceGenerator generator;

        public GenButton(ResourceGenerator generator) {
	    this.generator = generator;
	    String name = generator.getName();
            setActionCommand(name);
            setText(name);
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
            setFocusable(false);
            addActionListener(this);
	    stableElements.add(this);
        }

	// Create new resource, add it, and reformat.
	public void actionPerformed(ActionEvent e) {
	    setExternEdit(true);
	    FileChoosingWindow chooser =
		new FileChoosingWindow("resource files", 
			new String[] {"xml", "txt"}) {
		    public void choose(File file) {
			generateResource(file);
			setExternEdit(false);
			dispose();
		    }
		    public void exit() {
			setExternEdit(false);
			dispose();
		    }
		};
	    File textFile = new File(text.getLocation());
	    String textName = textFile.getName();
	    String base = FileAux.removeExtension(textName);
	    File resourceDir = resourceDir();
	    chooser.setSelectedFile(new File(resourceDir, base));
	}

	// Get directory for resources.
	private File resourceDir() {
	    File dir = new File(Settings.defaultResourceDir);
	    if (corpusDirectory != null) {
		dir = new File(corpusDirectory, Settings.defaultResourceDir);
		try {
		    if (!dir.exists()) 
			dir = new File(corpusDirectory);
		} catch(SecurityException e) { 
		    dir = new File(corpusDirectory);
		}
	    }
	    return dir;
	}

	private void generateResource(File file) {
	    try {
		file = relativizeResource(file);
		TextResource resource = generator.generate(file);
		if (resource != null)
		    addResourcePanel(resource);
		makeIndex();
		makeTextChanged();
	    } catch (IOException e) {
		JOptionPane.showMessageDialog(this,
			"Could not create resource:\n" + e.getMessage(),
			"File error", JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    // Label for comment of generator.
    private class CommentLabel extends PropertyEditor.PlainLabel {
	public CommentLabel(ResourceGenerator generator) {
	    super("(" + generator.getDescription() + ")");
	    stableElements.add(this);
	}
    }

    // Horizontal panel.
    private class HorPanel extends JPanel {
	public HorPanel() {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(backColor());
	}
    }
    // Vertical panel.
    private class VertPanel extends JPanel {
	public VertPanel() {
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    setBackground(backColor());
	}
    }

    // Color may depend on allowed editing.
    protected Color backColor() {
	return Color.WHITE;
    }

    ///////////////////////////////////////
    // Interaction between panels.

    public void allowEditing(boolean b) {
	setExternEdit(!b);
    }

    // Enable or disable buttons.
    public void enableDisable(boolean changed, boolean resourceOpen, boolean externEdit) {
	for (int i = 0; i < resPanels.size(); i++) {
	    ResourcePanel panel = resPanels.get(i);
	    panel.setEnabled(!resourceOpen);
	}
	for (int i = 0; i < precPanels.size(); i++) {
	    PrecedencePanel panel = precPanels.get(i);
	    panel.setEnabled(!resourceOpen);
	}
	for (int i = 0; i < genPanel.getComponentCount(); i++) 
	    genPanel.getComponent(i).setEnabled(!resourceOpen);
	if (viewPanel != null) 
	    viewPanel.setEnabled(!externEdit);
	if (editPanel != null) 
	    editPanel.setEnabled(!externEdit);
    }

    // Action from menu.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("resource close")) {
	    if (viewPanel != null)
		makeIndex();
	    if (editPanel != null) {
		makeResourceChanged();
		boolean globalChange = editPanel.save();
		if (globalChange)
		    makeTextChanged();
		returnFromEdit();
	    }
	}
    }

    ///////////////////////////////////////
    // Auxiliaries.

    // Horizontal glue.
    private Component horGlue() {
	return Box.createHorizontalGlue();
    }
    // Vertical glue.
    private Component vertGlue() {
	return Box.createVerticalGlue();
    }
    // Some separation between panels.
    private Component sep() {
	return Box.createRigidArea(new Dimension(10, 10));
    }

    //////////////////////////////////////
    // Information back to caller.

    // Inform of change of text.
    protected abstract void makeTextChanged();
    // Inform of change of resource.
    protected abstract void makeResourceChanged();

    // Inform of open resource editing.
    protected abstract void setResourceOpen(boolean b);

    // Inform of open file chooser.
    protected abstract void setExternEdit(boolean b);

}
