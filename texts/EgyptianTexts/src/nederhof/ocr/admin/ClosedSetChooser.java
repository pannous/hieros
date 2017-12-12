package nederhof.ocr.admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;

import nederhof.util.*;

// Frame from choosing symbol for closed set.
public abstract class ClosedSetChooser extends JFrame implements ActionListener {

	public ClosedSetChooser(Vector<String> names) {
		setTitle("Choose symbol");
		setJMenuBar(new QuitMenu(this));
		Container content = getContentPane();
		content.setLayout(new SpringLayout());
		int sqrt = names.size() <= 1 ? 1 : (int) Math.ceil(Math.sqrt(names.size()));
		for (int i = 0; i < names.size(); i++)  {
			JButton b = new JButton(names.get(i));
			b.addActionListener(this);
			content.add(b);
		}
		for (int i = 0; i < sqrt * sqrt - names.size(); i++) 
			content.add(new JPanel());
		SpringUtilities.makeCompactGrid(content, sqrt, sqrt, 5, 5, 5, 5);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new ConservativeListener(this));
		pack();
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("quit")) 
			setVisible(false);
		else {
			receive(e.getActionCommand());
			setVisible(false);
		}
	}

	// Send to caller.
	protected abstract void receive(String name);

}
