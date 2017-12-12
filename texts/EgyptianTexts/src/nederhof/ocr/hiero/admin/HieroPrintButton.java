package nederhof.ocr.hiero.admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;

import nederhof.ocr.admin.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

public class HieroPrintButton extends PrintGlyphButton {

	// Name of glyph, used as a small piece of RES.
	private String res;
    // Short text.
    private String shortText;
    // Formatted hieroglyphic.
    private FormatFragment format;

    // Contexts for hieroglyphic.
    private static HieroRenderContext hieroContext = null;
    private static ParsingContext parsingContext = null;
    // We have to get a graphics from somewhere, so we create a dummy image.
    private static BufferedImage dummyImage = null;
    private static Graphics dummyGraphics = null;
    private static FontMetrics dummyMetrics = null;

    // Dimensions.
    private int margin = 4;
    private int nameWidth;
    private int nameHeight;
    private int glyphWidth;
    private int glyphHeight;

	public HieroPrintButton(String text) {
		super(text);
        shortText = NonHiero.modLongToShort(text);
        res = text;

        getContexts();
        nameWidth = dummyMetrics.stringWidth(shortText);
        nameHeight = dummyMetrics.getAscent();
        if (!NonHiero.isExtra(text)) {
            ResFragment frag = ResFragment.parse(res, parsingContext);
            format = new FormatFragment(frag, hieroContext);
            glyphWidth = format.width();
            glyphHeight = format.height();
        } else {
            glyphWidth = 2 * nameWidth;
            glyphHeight = 2 * nameHeight;
        }
	}

    // Make contexts (only once).
    private void getContexts() {
        if (dummyMetrics == null) {
            hieroContext = new HieroRenderContext(HieroAdminSettings.hieroFontSize, true);
            parsingContext = new ParsingContext(hieroContext, true);
            dummyImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            dummyGraphics = dummyImage.createGraphics();
            dummyMetrics = dummyGraphics.getFontMetrics(HieroAdminSettings.labelFont);
        }
    }

    // Get dimensions of hieroglyphic.
    public int getGlyphWidth() {
        return glyphWidth;
    }
    public int getGlyphHeight() {
        return glyphHeight;
    }

    // Paint glyph and name of glyph.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(HieroAdminSettings.labelFont);
        g2.setColor(Color.BLACK);
        g2.drawString(shortText, extraHorMargin() + margin,
                (int) (getSize().height * 0.5f + nameHeight / 2.0f));
        if (!NonHiero.isExtra(text))
            format.write(g2, extraHorMargin() + margin * 2 + nameWidth,
                    (int) (getSize().height * 0.5f - glyphHeight / 2.0f));
    }

    // Extra margin due to excess size.
    private int extraHorMargin() {
        return (int) ((getSize().width - getMinimumSize().width) / 2.0f);
    }

    // Preferred and minimum size.
    public Dimension getMinimumSize() {
        if (NonHiero.isExtra(text))
            return new Dimension(margin * 2 + nameWidth, margin * 2 + nameHeight);
        else
            return new Dimension(margin * 3 + nameWidth + glyphWidth,
                    margin * 2 + Math.max(nameHeight, glyphHeight));
    }
    public Dimension getPreferredSize() {
        if (NonHiero.isExtra(text))
            return new Dimension(margin * 2 + nameWidth, margin * 2 + nameHeight);
        else
            return new Dimension(margin * 3 + nameWidth + glyphWidth,
                    margin * 2 + Math.max(nameHeight, glyphHeight));
    }

}
