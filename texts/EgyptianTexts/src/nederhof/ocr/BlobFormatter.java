package nederhof.ocr;

import java.util.*;

public interface BlobFormatter {

	public Vector<LineFormat> toFormats(Vector<Blob> glyphs, String dir);

}
