package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import nederhof.util.*;

public abstract class SettingsWindow extends JFrame implements ActionListener {

	// In which directory are prototypes?
	private JTextField fileField;
	// Should segments be detected after line is selected?
	private JCheckBox autoSegmentBox;
	// Should OCR start right after segments are added?
	private JCheckBox autoOcrBox;
	// Should formatting happing after OCR?
	private JCheckBox autoFormatBox;
	// Number of candidates investigated.
	private JComboBox beamCombo;
	// Number of candidates returned by OCR.
	private JComboBox candidatesCombo;

	// Chooser for directory of prototypes.
	private DirectoryChoosingWindow dirChooseWindow = null;

	// Constructor.
	public SettingsWindow() {
		final int STRUT_SIZE = 6;
		setTitle("OCR settings");
		setJMenuBar(new QuitMenu(this));
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JPanel filePanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		content.add(filePanel, BorderLayout.NORTH);
		content.add(leftPanel, BorderLayout.WEST);
		content.add(Box.createHorizontalStrut(STRUT_SIZE), BorderLayout.CENTER);
		content.add(rightPanel, BorderLayout.EAST);
		content.add(buttonsPanel, BorderLayout.SOUTH);
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

		// Location of prototypes.
		JPanel protoPanel = new JPanel(new SpringLayout());
		protoPanel.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Prototypes directory"),
					BorderFactory.createEmptyBorder(5,5,5,5)));
		fileField = new JTextField();
		fileField.setColumns(35);
		JButton fileChoose = new SettingButton(this, "<html><u>S</u>elect</html>", "select", KeyEvent.VK_S);
		protoPanel.add(new JLabel("Location:"));
		protoPanel.add(fileField);
		protoPanel.add(fileChoose);
		filePanel.add(protoPanel);
		SpringUtilities.makeCompactGrid(protoPanel, 1, 3, 5, 5, 5, 5);
		filePanel.add(Box.createVerticalStrut(STRUT_SIZE));

		// Automatic settings.
		JPanel autoPanel = new JPanel(new SpringLayout());
		autoPanel.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Auto"),
					BorderFactory.createEmptyBorder(5,5,5,5)));
		autoSegmentBox = new JCheckBox();
		autoPanel.add(new JLabel("Segment detection after selecting line:"));
		autoPanel.add(autoSegmentBox);
		autoPanel.add(Box.createHorizontalGlue());
		autoOcrBox = new JCheckBox();
		autoPanel.add(new JLabel("OCR after creating segments:"));
		autoPanel.add(autoOcrBox);
		autoPanel.add(Box.createHorizontalGlue());
		autoFormatBox = new JCheckBox();
		autoPanel.add(new JLabel("Formatting after OCR:"));
		autoPanel.add(autoFormatBox);
		autoPanel.add(Box.createHorizontalGlue());
		leftPanel.add(autoPanel);
		SpringUtilities.makeCompactGrid(autoPanel, 3, 3, 0, 0, 5, 5);
		leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

		// Parameters.
		JPanel paramPanel = new JPanel(new SpringLayout());
		paramPanel.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Parameters"),
					BorderFactory.createEmptyBorder(5,5,5,5)));
		beamCombo = new JComboBox(new String[] 
				{"5", "6", "7", "8", "9", "10", 
				"15", "20", "25", "30", "35", "40", "45", "50", "55", "60"});
		beamCombo.setMaximumRowCount(9);
		beamCombo.setMaximumSize(beamCombo.getPreferredSize());
		paramPanel.add(new JLabel("Candidates investigated:"));
		paramPanel.add(beamCombo);
		paramPanel.add(Box.createHorizontalGlue());
		candidatesCombo = new JComboBox(new String[] 
				{"1", "2", "3", "4", "5", "6", "7", "8", "9"});
		candidatesCombo.setMaximumRowCount(9);
		candidatesCombo.setMaximumSize(candidatesCombo.getPreferredSize());
		paramPanel.add(new JLabel("Candidates returned:"));
		paramPanel.add(candidatesCombo);
		paramPanel.add(Box.createHorizontalGlue());
		rightPanel.add(paramPanel);
		SpringUtilities.makeCompactGrid(paramPanel, 2, 3, 0, 0, 5, 5);
		rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

		// Confirmation buttons.
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
		buttonsPanel.add(new SettingButton(this,
					"<html><u>A</u>pply</html>", "apply", KeyEvent.VK_A));
		buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
		buttonsPanel.add(new SettingButton(this,
					"<html><u>D</u>efaults</html>", "defaults", KeyEvent.VK_D));
		buttonsPanel.add(Box.createHorizontalGlue());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new ConservativeListener(this));
		pack();
		makeSettingsVisible();
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("select")) {
			if (dirChooseWindow == null) {
				dirChooseWindow = 
					new DirectoryChoosingWindow() {
						protected void choose(File file) {
							fileField.setText(file.getAbsolutePath());
						}
					};
			}
			dirChooseWindow.setCurrentDirectory(getPrototypeDir());
			dirChooseWindow.setVisible(true);
		} else if (e.getActionCommand().equals("apply")) {
			applySettings();
			setVisible(false);
		} else if (e.getActionCommand().equals("defaults")) {
			makeDefaultsVisible();
		} else if (e.getActionCommand().equals("quit")) {
			makeSettingsVisible();
			setVisible(false);
		}
	}

	// Kill window and windows depending on it.
	public void dispose() {
		if (dirChooseWindow != null)
			dirChooseWindow.dispose();
		super.dispose();
	}

	// Apply values from screen.
	private void applySettings() {
		String oldDir = getPrototypeDir().getAbsolutePath();
		if (!oldDir.equals(fileField.getText())) {
			File dir = new File(fileField.getText());
			if (dir.exists()) 
				setPrototypeDir(fileField.getText());
		}
		setAutoSegment(autoSegmentBox.isSelected());
		setAutoOcr(autoOcrBox.isSelected());
		setAutoFormat(autoFormatBox.isSelected());
		try {
			String item = (String) beamCombo.getSelectedItem();
			int n = Integer.parseInt(item);
			setBeam(n);
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
		}
		try {
			String item = (String) candidatesCombo.getSelectedItem();
			int n = Integer.parseInt(item);
			setNCandidates(n);
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
		}
	}

	// Put default values in window.
	private void makeDefaultsVisible() {
		autoSegmentBox.setSelected(getDefaultAutoSegment());
		autoOcrBox.setSelected(getDefaultAutoOcr());
		autoFormatBox.setSelected(getDefaultAutoFormat());
		beamCombo.setSelectedItem("" + getDefaultBeam());
		candidatesCombo.setSelectedItem("" + getDefaultNCandidates());
	}

	// Put current values in window.
	private void makeSettingsVisible() {
		fileField.setText(getPrototypeDir().getAbsolutePath());
		autoSegmentBox.setSelected(getAutoSegment());
		autoOcrBox.setSelected(getAutoOcr());
		autoFormatBox.setSelected(getAutoFormat());
		beamCombo.setSelectedItem("" + getBeam());
		candidatesCombo.setSelectedItem("" + getNCandidates());
	}

	////////////////////////////////////////////////
	// Communication from caller.

	// Switch on OCR if off, or off if on.
	public void toggleAutoOcr() {
		autoOcrBox.setSelected(!autoOcrBox.isSelected());
		setAutoOcr(autoOcrBox.isSelected());
	}

	////////////////////////////////////////////////
	// Communication to caller. Caller to override.

	// Directory of prototypes.
	public abstract File getPrototypeDir();
	public abstract void setPrototypeDir(String dir);

	// Detect segments after selecting line?
	public abstract boolean getDefaultAutoSegment();
	public abstract boolean getAutoSegment();
	public abstract void setAutoSegment(boolean b);
	// OCR after selecting line?
	public abstract boolean getDefaultAutoOcr();
	public abstract boolean getAutoOcr();
	public abstract void setAutoOcr(boolean b);
	// Formatting after OCR?
	public abstract boolean getDefaultAutoFormat();
	public abstract boolean getAutoFormat();
	public abstract void setAutoFormat(boolean b);

	// How many glyphs investigated?
	public abstract int getDefaultBeam();
	public abstract int getBeam();
	public abstract void setBeam(int n);
	// How many candidate glyphs returned?
	public abstract int getDefaultNCandidates();
	public abstract int getNCandidates();
	public abstract void setNCandidates(int n);

}
