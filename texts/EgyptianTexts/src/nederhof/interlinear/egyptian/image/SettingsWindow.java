package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.util.*;

// Controls threshold for bilevel image, and
// threshold for number of pixels in connected components.
public class SettingsWindow extends JFrame implements ActionListener {

    // Mode buttons.
    private ConnectedRadio colorButton;
    private ConnectedRadio bilevelButton;
    private ConnectedRadio moveButton;
    private ConnectedRadio rectButton;
    private ConnectedRadio polyButton;
    private ConnectedRadio taggingButton;
    private ConnectedCheck componentsButton;
    private ConnectedRadio hlrButton;
    private ConnectedRadio hrlButton;
    private ConnectedRadio vlrButton;
    private ConnectedRadio vrlButton;

    // Constants.
    private final int STRUT_SIZE = 6;
    public final static int tDefault = 50;
    public final static int rDefault = 33;
    public final static int gDefault = 33;
    public final static int bDefault = 34;
    public final static int connectDefault = 1;
    public final static int sizeDefault = 5;

    // The sliders, for threshold (white/black) and color weightings summing
    // to 100.
    private JSlider sliderT = 
	new TickSlider(0, 100, tDefault, 5, 20, 
		new ColorListener(), Color.BLACK);
    private JSlider sliderR = 
	new TickSlider(0, 100, rDefault, 5, 20, 
		new ColorListener(), Color.RED);
    private JSlider sliderG = 
	new TickSlider(0, 100, gDefault, 5, 20, 
		new ColorListener(), Color.GREEN);
    private JSlider sliderB = 
	new TickSlider(0, 100, bDefault, 5, 20, 
		new ColorListener(), Color.BLUE);
    private JSlider sliderConnect = 
	new TickSlider(0, 30, connectDefault, 1, 5, 
		new ComponentListener(), Color.BLACK);
    private JSlider sliderSize = 
	new TickSlider(1, 31, sizeDefault, 1, 5, 
		new ComponentListener(), Color.BLACK);

    // Avoid that changes of slides trigger more.
    private boolean programmicSlide = true;

    // Constructor.
    public SettingsWindow() {
	setTitle("Settings");
	setJMenuBar(new QuitMenu(this));
	Container content = getContentPane();
	content.setLayout(new BorderLayout());
	JPanel modePanel = new JPanel();
	JPanel sliderPanel = new JPanel();
	// JPanel buttonsPanel = new JPanel();
	content.add(modePanel, BorderLayout.WEST);
	content.add(sliderPanel, BorderLayout.EAST);
	// content.add(buttonsPanel, BorderLayout.SOUTH);
	modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
	sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
	// buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

	// Modes.
	modePanel.add(new AppearancePanel());
	modePanel.add(new MousePanel());
	modePanel.add(new ComponentsPanel());
	modePanel.add(new DirectionPanel());

	// Sliders.
	sliderPanel.add(new SliderPanel());

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new ConservativeListener(this));
	pack();
	setResizable(false);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		programmicSlide = false;
		}
	});
    }

    // Panel with sliders.
    private class SliderPanel extends JPanel {
	// Constructor.
	public SliderPanel() {
	    super(new SpringLayout());
	    setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Bilevel"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    add(new JLabel("threshold"));
	    add(sliderT);
	    add(new JLabel("red/green/blue weights"));
	    add(sliderR);
	    add(sliderG);
	    add(sliderB);
	    add(new JLabel("component connectedness"));
	    add(sliderConnect);
	    add(new JLabel("minimal component size"));
	    add(sliderSize);
	    SettingButton defaultButton =
		new SettingButton(SettingsWindow.this,
		    "<html><u>D</u>efaults</html>", "defaults", KeyEvent.VK_D);
	    add(defaultButton);
	    SpringUtilities.makeCompactGrid(this, 11, 1, 5, 5, 5, 5);
	}
    }

    // Panel for choosing appearance.
    private class AppearancePanel extends JPanel {
	// Constructor.
	public AppearancePanel() {
	    super(new SpringLayout());
	    setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Appearance"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    ButtonGroup group = new ButtonGroup();
	    colorButton = 
		new ConnectedRadio("color", this, group, true);
	    bilevelButton = 
		new ConnectedRadio("bilevel", this, group, false);
	    setLayout(new GridLayout(2,1));
	}
    }

    // Panel for choosing mouse mode.
    private class MousePanel extends JPanel {
	// Constructor.
	public MousePanel() {
	    super(new SpringLayout());
	    setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Mouse"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    ButtonGroup group = new ButtonGroup();
	    moveButton =
		new ConnectedRadio("move", this, group, true);
	    rectButton =
		new ConnectedRadio("rectangle", this, group, false);
	    polyButton =
		new ConnectedRadio("polygon", this, group, false);
	    taggingButton =
		new ConnectedRadio("tagging", this, group, false);
	    setLayout(new GridLayout(4,1));
	}
    }

    // Panel for components.
    private class ComponentsPanel extends JPanel {
	// Constructor.
	public ComponentsPanel() {
	    setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Components"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    componentsButton =
		new ConnectedCheck("show", this, true);
	    setLayout(new GridLayout(1,1));
	}
    }


    // Panel for indicating direction of writing.
    private class DirectionPanel extends JPanel {
        // Constructor.
        public DirectionPanel() {
            super(new SpringLayout());
            setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Direction"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
            ButtonGroup group = new ButtonGroup();
            hlrButton =
                new ConnectedRadio("hlr", this, group, true);
            hrlButton =
                new ConnectedRadio("hrl", this, group, false);
            vlrButton =
                new ConnectedRadio("vlr", this, group, false);
            vrlButton =
                new ConnectedRadio("vrl", this, group, false);
            setLayout(new GridLayout(2,2));
        }
    }

    // Put radio button in group in panel.
    class ConnectedRadio extends JRadioButton {
      	// Constructor.
      	public ConnectedRadio(String name, JPanel panel, ButtonGroup group,
			boolean selected) {
	    super(name);
	    setSelected(selected);
	    setActionCommand(name);
	    panel.add(this);
	    group.add(this);
	    addActionListener(SettingsWindow.this);
       }
    }

    // Put check box in panel.
    class ConnectedCheck extends JCheckBox {
      	// Constructor.
      	public ConnectedCheck(String name, JPanel panel, boolean selected) {
	    super(name);
	    setSelected(selected);
	    setActionCommand(name);
	    panel.add(this);
	    addActionListener(SettingsWindow.this);
       }
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("defaults")) 
	    setDefaultSliders();
	else if (e.getActionCommand().equals("color")) 
	    reportBilevel(false);
	else if (e.getActionCommand().equals("bilevel")) 
	    reportBilevel(true);
	else if (e.getActionCommand().equals("move")) 
	    reportMove();
	else if (e.getActionCommand().equals("rectangle")) 
	    reportRect();
	else if (e.getActionCommand().equals("polygon")) 
	    reportPoly();
	else if (e.getActionCommand().equals("tagging")) 
	    reportTagging();
	else if (e.getActionCommand().equals("show")) 
	    reportComponents(componentsButton.isSelected());
	else if (e.getActionCommand().equals("hlr") ||
	       e.getActionCommand().equals("hrl") ||
	       e.getActionCommand().equals("vlr") ||
	       e.getActionCommand().equals("vrl"))
	    reportDirection(e.getActionCommand());
	else if (e.getActionCommand().equals("quit")) 
	    setVisible(false);
    }

    /////////////////////////////////////////////////////////
    // Sliders.

    // Customized slider.
    private class TickSlider extends JSlider {
	public TickSlider(int min, int max, int init, 
		int smallStep, int bigStep, 
		ChangeListener listen, Color color) {
	    super(JSlider.HORIZONTAL, min, max, init);
	    setForeground(color);
	    setPaintTicks(true);
	    setMinorTickSpacing(smallStep);
	    setMajorTickSpacing(bigStep);
	    setPaintLabels(true);
	    addChangeListener(listen);
	}
    }

    // Set sliders to default position programmatically.
    private void setDefaultSliders() {
	programmicSlide = true;
	sliderT.setValue(tDefault);
	sliderR.setValue(rDefault);
	sliderG.setValue(gDefault);
	sliderB.setValue(bDefault);
	sliderConnect.setValue(connectDefault);
	sliderSize.setValue(sizeDefault);
	reportAll();
	programmicSlide = false;
    }

    // Listeners.
    private class ColorListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    if (((JSlider) e.getSource()).getValueIsAdjusting())
		return;
	    if (e.getSource() == sliderT) {
		reportColorChange();
		return;
	    }
	    if (programmicSlide)
		return;
	    programmicSlide = true;
	    int sum = sliderR.getValue() + sliderG.getValue() + sliderB.getValue();
	    if (sum > 100) {
		int diff = sum - 100;
		JSlider lowest = lowestSlider(e.getSource());
		int lowestDiff = Math.min(lowest.getValue(), diff / 2);
		lowest.setValue(lowest.getValue() - lowestDiff);
		diff -= lowestDiff;
		JSlider other = otherSlider(e.getSource(), lowest);
		other.setValue(other.getValue() - diff);
	    } else if (sum < 100) {
		int diff = 100 - sum;
		JSlider highest = highestSlider(e.getSource());
		int highestDiff = Math.min(100 - highest.getValue(), diff / 2);
		highest.setValue(highest.getValue() + highestDiff);
		diff -= highestDiff;
		JSlider other = otherSlider(e.getSource(), highest);
		other.setValue(other.getValue() + diff);
	    }
	    reportColorChange();
	    programmicSlide = false;
	}
    }
    private class ComponentListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    if (((JSlider) e.getSource()).getValueIsAdjusting())
		return;
	    reportComponentChange();
	}
    }

    // What is slider with lowest value (except given one).
    private JSlider lowestSlider(Object except) {
	int r = sliderR.getValue();
	int g = sliderG.getValue();
	int b = sliderB.getValue();
	if (except == sliderR)
	    r = Integer.MAX_VALUE; 
	else if (except == sliderG)
	    g = Integer.MAX_VALUE;
	else if (except == sliderB)
	    b = Integer.MAX_VALUE;
	if (r < g) {
	    if (r < b)
		return sliderR;
	    else
		return sliderB;
	} else {
	    if (g < b)
		return sliderG;
	    else
		return sliderB;
	}
    }
    // What is color slider with highest value (except given one).
    private JSlider highestSlider(Object except) {
	int r = sliderR.getValue();
	int g = sliderG.getValue();
	int b = sliderB.getValue();
	if (except == sliderR)
	    r = Integer.MIN_VALUE; 
	else if (except == sliderG)
	    g = Integer.MIN_VALUE;
	else if (except == sliderB)
	    b = Integer.MIN_VALUE;
	if (r < g) {
	    if (g < b)
		return sliderB;
	    else
		return sliderG;
	} else {
	    if (r < b)
		return sliderB;
	    else
		return sliderR;
	}
    }
    // What is third slider.
    private JSlider otherSlider(Object except1, Object except2) {
	if (sliderR != except1 && sliderR != except2)
	    return sliderR;
	else if (sliderG != except1 && sliderG != except2)
	    return sliderG;
	else
	    return sliderB;
    }

    ////////////////////////////////////////////////////
    // Communication with caller.

    // Caller may override.
    public void reportBilevel(boolean b) {
    }
    public void reportMove() {
    }
    public void reportRect() {
    }
    public void reportPoly() {
    }
    public void reportTagging() {
    }
    public void reportComponents(boolean b) {
    }
    public void reportDirection(String dir) {
    }

    // Caller may override.
    public void reportColorChange(int t, int r, int g, int b) {
    }
    public void reportComponentChange(int connect, int size) {
    }
    // Caller may call.
    public void reportAll() {
	reportColorChange();
	reportComponentChange();
    }

    // Auxiliary.
    private void reportColorChange() {
	reportColorChange(sliderT.getValue(),
		sliderR.getValue(),
		sliderG.getValue(),
		sliderB.getValue());
    }
    private void reportComponentChange() {
	reportComponentChange(sliderConnect.getValue(), sliderSize.getValue());
    }

    // Set wait cursor.
    public void setBusy(boolean busy) {
	if (busy)
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	else
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Set to modes.
    public void setBilevel(boolean b) {
	if (b) {
	    if (bilevelButton.isSelected())
		return;
	    bilevelButton.setSelected(true);
	} else {
	    if (colorButton.isSelected())
		return;
	    colorButton.setSelected(true);
	}
	reportBilevel(b);
    }
    public void setMove() {
	if (moveButton.isSelected())
	    return;
	moveButton.setSelected(true);
	reportMove();
    }
    public void setRect() {
	if (rectButton.isSelected())
	    return;
	rectButton.setSelected(true);
	reportRect();
    }
    public void setPoly() {
	if (polyButton.isSelected())
	    return;
	polyButton.setSelected(true);
	reportPoly();
    }
    public void setTagging() {
	if (taggingButton.isSelected())
	    return;
	taggingButton.setSelected(true);
	reportTagging();
    }
    public void toggleComponents() {
	componentsButton.setSelected(!componentsButton.isSelected());
	reportComponents(componentsButton.isSelected());
    }
    public void setDirection(String dir) {
	if (dir.equals("hrl")) {
	    if (hrlButton.isSelected())
		return;
	    hrlButton.setSelected(true);
	} else if (dir.equals("vlr")) {
	    if (vlrButton.isSelected())
		return;
	    vlrButton.setSelected(true);
	} else if (dir.equals("vrl")) {
	    if (vrlButton.isSelected())
		return;
	    vrlButton.setSelected(true);
	} else {
	    if (hlrButton.isSelected())
		return;
	    hlrButton.setSelected(true);
	}
    }

    // Has been placed before?
    private boolean placed = false;
    //

    // Place for the first time.
    public void setLocationFirstTime(JFrame parent) {
	if (!placed) {
	    placed = true;
	    setLocationRelativeTo(parent);
	}
    }

    //////////////////////////////////////////
    // For testing.

    public static void main(String arg[]) {
	SettingsWindow settings = new SettingsWindow();
	settings.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	settings.setVisible(true);
    }

}
