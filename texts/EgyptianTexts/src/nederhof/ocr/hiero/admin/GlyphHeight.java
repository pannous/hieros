package nederhof.ocr.hiero.admin;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.util.*;
import nederhof.util.xml.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.ocr.hiero.admin.*;

// Maps glyph name (plus extension) to size.
public class GlyphHeight extends HashMap<String,Float> {

	// Contexts for hieroglyphic.
	private HieroRenderContext hieroContext = 
		new HieroRenderContext(HieroAdminSettings.hieroFontSize, false);
	private ParsingContext parsingContext = 
		new ParsingContext(hieroContext, true);

	// Constructor.
	public GlyphHeight() {
		try {
			InputStream in = FileAux.addressToStream(HieroAdminSettings.infoFile);
			DocumentBuilder parser = SimpleXmlParser.construct(false, false);
			Document signInfo = parser.parse(in);
			NodeList signs = signInfo.getElementsByTagName("sign");
			for (int i = 0; i < signs.getLength(); i++) {
				Element sign = (Element) signs.item(i);
				String name = sign.getAttribute("name");
				for (String mod : NonHiero.modifiers)
					if (!mod.equals("[part]"))
						put(name+mod, heightOf(name+mod));
			}
			Vector<String> extras = NonHiero.getExtras();
			for (int i = 0; i < extras.size(); i++) {
				String extra = extras.get(i);
				put(extra, NonHiero.height(extra));
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Could not read: " + HieroAdminSettings.infoFile);
			System.err.println(e.getMessage());
		}
	}

	// Give height relative to unit height.
	private float heightOf(String res) {
		ResFragment frag = ResFragment.parse("[vlr]" + res, parsingContext);
		FormatFragment format = new FormatFragment(frag, hieroContext);
		float glyphHeight = format.height();
		return 1.0f * glyphHeight / HieroAdminSettings.hieroFontSize;
	}

}
