package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.align.SimpleTextWindow;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

public class HieroInfoWindow extends SimpleTextWindow {

	// Structure containing sign information.
	private Document signInfo = null;

	// Mapping from names to elements in structure.
	private Map signFinder = new HashMap();

	// Construct.
	public HieroInfoWindow(int width, int height) {
		super(width, height);
		loadSignInfo();
		setFocusableWindowState(false);
	}

	// Parse sign information.
	private void loadSignInfo() {
		try {
			InputStream in = FileAux.addressToStream(Settings.infoFile);
			DocumentBuilder parser = SimpleXmlParser.construct(false, false);
			signInfo = parser.parse(in);
			NodeList signs = signInfo.getElementsByTagName("sign");
			for (int i = 0; i < signs.getLength(); i++) {
				Element sign = (Element) signs.item(i);
				String name = sign.getAttribute("name");
				signFinder.put(name, sign);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Could not read: " + Settings.infoFile);
			System.err.println(e.getMessage());
		}
	}

	// Send sign information to window.
	public void lookupSignInfo(String sign) {
		Element elem = (Element) signFinder.get(sign);
		Element descr = null;
		Element info = null;
		if (elem != null) {
			NodeList descrs = elem.getElementsByTagName("descr");
			NodeList infos = elem.getElementsByTagName("info");
			if (descrs.getLength() > 0)
				descr = (Element) descrs.item(0);
			if (infos.getLength() > 0)
				info = (Element) infos.item(0);
		}
		setText(sign, descr, info);
	}

}
