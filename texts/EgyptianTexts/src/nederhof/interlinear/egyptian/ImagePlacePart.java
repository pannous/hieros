package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.res.*;
import nederhof.res.format.*;

// Sign in text/image linkage resource.
public class ImagePlacePart extends EgyptianTierAwtPart {

    // Parsing context.
    public static final HieroRenderContext hieroContext =
        new HieroRenderContext(20); // fontsize arbitrary
    private static final ParsingContext parsingContext =
        new ParsingContext(hieroContext, true);

    // The context used to format it the last time.
    // null if none.
    private HieroRenderContext lastHieroContext;
    // Parsed RES. 
    public ResFragment parsed;
    // Formatted RES.
    private FormatFragment formatted;

    // All information on sign.
    public ImageSign info;

    // ID, for manual alignment links.
    public String id = "";

    // Only for edit mode does it have width.
    // Otherwise, pretend it has zero width.
    private boolean edit = true;

    // Constructor.
    public ImagePlacePart(ImageSign info, String id) {
	this.info = info;
	this.id = id;
        parsed = ResFragment.parse(info.getName(), parsingContext);
    }

    public void setEdit(boolean edit) {
	this.edit = edit;
    }

    // If not formatted with latest context, then do again.
    private void ensureFormatted() {
        if (lastHieroContext != context()) {
            lastHieroContext = context();
            if (parsed != null)
                formatted = new FormatFragment(parsed, lastHieroContext);
        }
    }

    // How many symbols.
    public int nSymbols() {
	return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
        return true;
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	return Penalties.spacePenalty;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
        return 0;
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
        ensureFormatted();
	if (!edit)
	    return 0;
	else if (i == j || formatted == null)
            return 0;
        else {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j-1);
	    return formatted.width(groupI, groupJ);
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        ensureFormatted();
	if (!edit)
	    return 0;
	else if (j == nSymbols() && formatted != null) {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j);
	    return formatted.width(groupI, groupJ) +
		context().emToPix(context().fontSep());
	} else
            return dist(i, j);
    }

    // Spaces at beginning.
    public float leadSpaceAdvance() {
        return context().emToPix(context().fontSep());
    }

    // Font metrics.
    public float leading() {
        return ascent() * 0.2f;
    }
    public float ascent() {
        ensureFormatted();
        if (formatted != null)
            return formatted.height();
        else
            return 0;
    }
    public float descent() {
        return 0;
    }

    // Draw.
    public void draw(int i, int j, int x, int y, Graphics2D g) {
        ensureFormatted();
        if (i != j && formatted != null) {
            int height = formatted.height();
            int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
            int groupJ = formatted.glyphToGroup(j-1);
            formatted.write(g, groupI, groupJ, x, y-height);
            drawHighlight(i, j, x, y, g);
        }
    }

    // Draw highlights.
    public void drawHighlight(int i, int j, int x, int y, Graphics2D g) {
        for (Iterator it = highlights.iterator(); it.hasNext(); ) {
            int high = ((Integer) it.next()).intValue();
            if (i <= high && high < j) {
                int height = formatted.height();
                Vector rects = formatted.glyphRectangles();
                Rectangle rect;
                if (high < rects.size())
                    rect = (Rectangle) rects.get(high);
                else
                    rect = new Rectangle(0, 0,
                            formatted.width(), formatted.height());
                Rectangle highRect = new Rectangle(
                        x + rect.x,
                        y + rect.y - height,
                        rect.width,
                        rect.height);
                g.setColor(Color.BLUE);
                g.draw(highRect);
            }
        }
        for (Iterator it = highlightsAfter.iterator(); it.hasNext(); ) {
            int high = ((Integer) it.next()).intValue();
            if (i <= high && high < j) {
                int height = formatted.height();
                Vector rects = formatted.glyphRectangles();
                Rectangle rect;
                if (high < rects.size())
                    rect = (Rectangle) rects.get(high);
                else
                    rect = new Rectangle(0, 0,
                            formatted.width(), formatted.height());
                g.setColor(Color.BLUE);
                g.fillRect(x + rect.x + rect.width +
                                context().emToPix(context().fontSep()),
                        y + rect.y - height,
                        highlightBarWidth,
                        rect.height);
            }
        }
    }

    public int getPos(int i, int j, int x, int y) {
        ensureFormatted();
        if (i != j && formatted != null && x >= 0 && x < advance(i, j)) {
            int height = formatted.height();
            Vector rects = formatted.glyphRectangles();
            if (rects.isEmpty())
                return 0;
            else
                for (int k = i; k < j; k++) {
                    Rectangle rect = (Rectangle) rects.get(k);
                    if (rect.contains(x, y+height))
                        return k-i;
                }
        }
        return -1;
    }

    public Rectangle getRectangle(int i, int j) {
        if (formatted != null) {
            int height = formatted.height();
            Vector rects = formatted.glyphRectangles();
            Rectangle rect = (Rectangle) rects.get(j);
            rect = (Rectangle) rect.clone();
            rect.translate(0, -height);
            return rect;
        } else
            return new Rectangle();
    }

    ///////////////////////////////////
    // Auxiliary.

    // Context for hieroglyphic.
    public HieroRenderContext context() {
        return renderParams.hieroContext;
    }

}
