package nederhof.ocr.hiero.admin;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.ocr.admin.*;
import nederhof.util.*;
import nederhof.util.xml.*;

// Frame from choosing non-hieroglyphs.
public abstract class NonHieroChooser extends ClosedSetChooser {

	public NonHieroChooser() {
		super(NonHiero.getExtras());
	}
}
