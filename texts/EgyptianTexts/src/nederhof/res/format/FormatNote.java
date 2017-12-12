/***************************************************************************/
/*                                                                         */
/*  FormatNote.java                                                        */
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

package nederhof.res.format;

import java.awt.*;
import java.util.*;

import nederhof.res.*;

public class FormatNote extends ResNote {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatNote(ResNote note, HieroRenderContext context) {
	super(note.string, note.color);
        this.context = context;
    }

    // Make formatted note, unless it is null.
    public static FormatNote makeNote(ResNote note, HieroRenderContext context) {
	if (note == null)
	    return null;
	else
	    return new FormatNote(note, context);
    }

    // Make formatted notes.
    public static Vector makeNotes(Vector notes, HieroRenderContext context) {
        Vector formatNotes = new Vector(notes.size());
        for (int i = 0; i < notes.size(); i++) {
            ResNote note = (ResNote) notes.get(i);
            formatNotes.add(new FormatNote(note, context));
        }
        return formatNotes;
    }

    //////////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
        return context.effectDir(dirHeader());
    }

    private boolean effectIsH() {
        return ResValues.isH(effectDir());
    }

    private Color16 color() {
	if (color.isColor())
	    return color;
	else
	    return context.noteColor();
    }

    // If no indication at note, take implementation-defined color.
    private Color effectColor() {
	    return context.effectColor(color()).getColor();
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Where was note placed?
    public Rectangle rect;

    // Place one note about rectangle (for emptyglyph).
    // Note is preferably placed in the middle if possible, but not
    // if there is shading, in which case it is to be placed near the edges
    // if possible.
    public void place(Rectangle mainRect, boolean shading,
	    boolean under, boolean over, FlexGraphics im) {
	OptionalGlyphs glyph = glyph();
	Dimension dim = glyph.dimension();
	int xRef = mainRect.x; // points of reference
	int yRef = mainRect.y;
	int x = mainRect.x + mainRect.width/2 - dim.width/2; 
	int y = mainRect.y + mainRect.height/2 - dim.height/2; 
	rect = new Rectangle(x-xRef, y-yRef, dim.width, dim.height);
	if (!shading && dim.width <= mainRect.width &&
		dim.height <= mainRect.height) 
	    ; /* keep above assignment of rect */
	else if (!over && context.lineMode() != ResValues.OVERLINE) {
	    if (effectIsH())
		rect.y = mainRect.y - context.noteDistPix() - dim.height;
	    else
		rect.x = mainRect.x + mainRect.width + context.noteDistPix();
	} else if (!under && context.lineMode() != ResValues.UNDERLINE) {
	    if (effectIsH())
		rect.y = mainRect.y + mainRect.height + context.noteDistPix();
	    else
		rect.x = mainRect.x - dim.width - context.noteDistPix();
	} /* else: keep above assignment of rect */
	im.renderStraight(glyph, rect.x + xRef, rect.y + yRef);
    }

    // Place (multiple) notes about rectangle.
    public static void place(Rectangle mainRect, Vector notes, boolean shading, 
	    boolean under, boolean over, boolean isH,
	    FlexGraphics im, HieroRenderContext context) {
	if (notes.size() < 1)
	    return;
	int xRef = mainRect.x; // points of reference
	int yRef = mainRect.y;
	if (!over && context.lineMode() != ResValues.OVERLINE) {
	    if (isH) {
		int sep = widthSep(notes, mainRect, context);
		int x = mainRect.x + widthOffset(notes, mainRect, context);
		int y = mainRect.y - context.noteDistPix();
		for (int i = 0; i < notes.size(); i++) {
		    FormatNote note = (FormatNote) notes.get(i);
		    OptionalGlyphs glyph = note.glyph();
		    Dimension dim = glyph.dimension();
		    int yTop = y - dim.height;
		    note.rect = new Rectangle(x-xRef, yTop-yRef, dim.width, dim.height);
		    im.renderStraight(glyph, x + xRef, yTop + yRef);
		    x += dim.width + sep;
		}
	    } else {
		int sep = heightSep(notes, mainRect, context);
		int x = mainRect.x + mainRect.width + context.noteDistPix();
		int y = mainRect.y + heightOffset(notes, mainRect, context);
		for (int i = 0; i < notes.size(); i++) {
		    FormatNote note = (FormatNote) notes.get(i);
		    OptionalGlyphs glyph = note.glyph();
		    Dimension dim = glyph.dimension();
		    note.rect = new Rectangle(x-xRef, y-yRef, dim.width, dim.height);
		    im.renderStraight(glyph, x + xRef, y + yRef);
		    y += dim.height + sep;
		}
	    }
	} else if (!under && context.lineMode() != ResValues.UNDERLINE) {
	    if (isH) {
		int sep = widthSep(notes, mainRect, context);
		int x = mainRect.x + widthOffset(notes, mainRect, context);
		int y = mainRect.y + mainRect.height + context.noteDistPix();
		for (int i = 0; i < notes.size(); i++) {
		    FormatNote note = (FormatNote) notes.get(i);
		    OptionalGlyphs glyph = note.glyph();
		    Dimension dim = glyph.dimension();
		    note.rect = new Rectangle(x-xRef, y-yRef, 
			    dim.width, dim.height);
		    im.renderStraight(glyph, x + xRef, y + yRef);
		    x += dim.width + sep;
		}
	    } else {
		int sep = heightSep(notes, mainRect, context);
		int x = mainRect.x - context.noteDistPix();
		int y = mainRect.y + heightOffset(notes, mainRect, context);
		for (int i = 0; i < notes.size(); i++) {
		    FormatNote note = (FormatNote) notes.get(i);
		    OptionalGlyphs glyph = note.glyph();
		    Dimension dim = glyph.dimension();
		    int xEdge = x - dim.width;
		    note.rect = new Rectangle(xEdge-xRef, y-yRef, 
			    dim.width, dim.height);
		    im.renderStraight(glyph, xEdge + xRef, y + yRef);
		    y += dim.height + sep;
		}
	    }
	} else {
	    boolean heightFavoured =
		(heightSep(notes, mainRect, context) >= heightMin(context) &&
		 widthSep(notes, mainRect, context) < widthMin(context));
	    if (heightFavoured ||
		    !tryPlaceAbove(notes, 0, xRef, yRef,
			mainRect.x, mainRect.width, 
			mainRect.y, mainRect.y + mainRect.height / 2,
			im, context))
		if (!tryPlaceRight(notes, 0, xRef, yRef,
			    mainRect.x + mainRect.width, mainRect.x + mainRect.width / 2,
			    mainRect.y, mainRect.height,
			    im, context))
		    placeRight(notes, xRef, yRef,
			    mainRect.x + mainRect.width, mainRect.x + mainRect.width / 2,
			    mainRect.y + heightOffset(notes, mainRect, context), 
			    heightSep(notes, mainRect, context),
			    im, context);
	}
    }

    // Try placing notes above rectangle, starting with the i-th. 
    // First, we attempt to place them right-most.
    // If that doesn't work, we go some distance to the left.
    // Return whether successful.
    private static boolean tryPlaceAbove(Vector notes, int i, int xRef, int yRef,
	    int xStart, int width, int yStart, int yMax, 
	    FlexGraphics im, HieroRenderContext context) {
	if (i >= notes.size())
	    return true;
	FormatNote note = (FormatNote) notes.get(i);
	OptionalGlyphs glyph = note.glyph();
	Dimension dim = glyph.dimension();
	int middle = xStart + width - dim.width;
	while (middle >= xStart) {
	    int yMin = yStart - context.noteDistPix() - dim.height;
	    int move = downEmptySquare(im, context.noteDistPix(),
		    middle - context.noteDistPix()/2, yMin, 
		    dim.width + context.noteDistPix(), dim.height, yMax);
	    int xStartNew = middle + dim.width + widthMin(context);
	    int widthNew = width - (xStartNew - xStart);
	    if (move >= 0 &&
		    tryPlaceAbove(notes, i+1, xRef, yRef,
			xStartNew, widthNew, yStart, yMax,
			im, context)) {
		note.rect = new Rectangle(middle-xRef, yMin + move-yRef, 
			dim.width, dim.height);
		im.renderStraight(glyph, note.rect.x + xRef, note.rect.y + yRef);
		return true;
	    }
	    middle -= Math.max(1, Math.round(0.2f * context.noteSize()));
	}
	return false;
    }

    // Try placing notes right of rectangle. Return whether successful.
    // This is much as above, but we start at top, which leads to 
    // different kind of search for appropriate position.
    private static boolean tryPlaceRight(Vector notes, int i, int xRef, int yRef,
	    int xStart, int xMax, int yStart, int height,
	    FlexGraphics im, HieroRenderContext context) {
	if (i >= notes.size())
	    return true;
	FormatNote note = (FormatNote) notes.get(i);
	OptionalGlyphs glyph = note.glyph();
	Dimension dim = glyph.dimension();
	int middle = yStart;
	while (middle + dim.height <= yStart + height) {
	    int move = leftEmptySquare(im, context.noteDistPix(),
		    xStart + context.noteDistPix(),
		    middle - context.noteDistPix()/2,
		    dim.width, dim.height + context.noteDistPix(),
		    xMax);
	    int yStartNew = middle + dim.height + heightMin(context);
	    int heightNew = height - (yStartNew - yStart);
	    if (move >= 0 &&
		    tryPlaceRight(notes, i+1, xRef, yRef,
			xStart, xMax, yStartNew, heightNew, 
			im, context)) {
		note.rect = new Rectangle(xStart + context.noteDistPix() - move - xRef,
			middle - yRef, 
			dim.width, dim.height);
		im.renderStraight(glyph, note.rect.x + xRef, note.rect.y + yRef);
		return true;
	    }
	    middle += Math.max(1, Math.round(0.2f * context.noteSize()));
	}
	return false;
    }

    // If nothing else works, place notes right of rectangle
    // without too much care.
    private static void placeRight(Vector notes, int xRef, int yRef,
	    int xStart, int xMax, int yStart, int sep,
	    FlexGraphics im, HieroRenderContext context) {
	int y = yStart;
	for (int i = 0; i < notes.size(); i++) {
	    FormatNote note = (FormatNote) notes.get(i);
	    OptionalGlyphs glyph = note.glyph();
	    Dimension dim = glyph.dimension();
	    int move = leftEmptySquare(im, context.noteDistPix(),
		    xStart + context.noteDistPix(),
		    y - context.noteDistPix()/2,
		    dim.width, dim.height + context.noteDistPix(),
		    xMax);
	    if (move < 0)
		move = context.noteDistPix();
	    note.rect = new Rectangle(
		    xStart + context.noteDistPix() - move - xRef,
		    y - yRef, 
		    dim.width, dim.height);
	    im.renderStraight(glyph, note.rect.x + xRef, note.rect.y + yRef);
	    y += dim.height + sep;
	}
    }

    // There are 4 sides where notes can be placed.
    // All notes are placed at the same side.
    // We first compute the total width and height.
    // Between two notes horizontally, we try to insert
    // between widthMin and widthMax; vertically, we try to
    // insert between heightMin and heightMax.
    private static int widthMin(HieroRenderContext context) {
	return Math.round(0.6f * context.noteSize());
    }
    private static int widthMax(HieroRenderContext context) {
	return Math.round(1.0f * context.noteSize());
    }
    private static int heightMin(HieroRenderContext context) {
	return Math.round(0.3f * context.noteSize());
    }
    private static int heightMax(HieroRenderContext context) {
	return Math.round(0.8f * context.noteSize());
    }

    // Separation between notes at glyph.
    private static int widthSep(Vector notes, Rectangle mainRect, 
	    HieroRenderContext context) {
	if (notes.size() > 1) {
	    int surplus = Math.max(0, mainRect.width - width(notes, context));
	    return Math.max(1,
		    Math.min(widthMax(context), surplus / (notes.size()-1)));
	} else
	    return 0;
    }
    private static int heightSep(Vector notes, Rectangle mainRect, 
	    HieroRenderContext context) {
	if (notes.size() > 1) {
	    int surplus = Math.max(0, mainRect.height - height(notes, context));
	    return Math.max(1,
		    Math.min(heightMax(context), surplus / (notes.size()-1)));
	} else
	    return 0;
    }

    // Where do notes start?
    private static int widthOffset(Vector notes, Rectangle mainRect,
	    HieroRenderContext context) {
	return (mainRect.width - width(notes, context) -
	    widthSep(notes, mainRect, context) * (notes.size()-1)) / 2;
    }
    private static int heightOffset(Vector notes, Rectangle mainRect,
	    HieroRenderContext context) {
	return (mainRect.height - height(notes, context) -
	    heightSep(notes, mainRect, context) * (notes.size()-1)) / 2;
    }

    // Sum of widths of notes.
    private static int width(Vector notes, HieroRenderContext context) {
	int width = 0;
	for (int i = 0; i < notes.size(); i++) {
	    FormatNote note = (FormatNote) notes.get(i);
	    OptionalGlyphs glyph = note.glyph();
	    Dimension dim = glyph.dimension();
	    width += dim.width;
	}
	return width;
    }
    private static int height(Vector notes, HieroRenderContext context) {
	int height = 0;
	for (int i = 0; i < notes.size(); i++) {
	    FormatNote note = (FormatNote) notes.get(i);
	    OptionalGlyphs glyph = note.glyph();
	    Dimension dim = glyph.dimension();
	    height += dim.height;
	}
	return height;
    }

    // Get glyph for note.
    private OptionalGlyphs glyph() {
	Font f = context.getNoteFont();
	int n = context.getNoteFontNumber();
	Color c = effectColor();
	int s = context.noteSize();
	if (!context.canDisplay(n, string))
	    return new ErrorGlyphs(context.emSizePix()/2, context);
	else 
	    return new Glyphs(string, f, c, s, context);
    }

    ////////////////////////////////////////////////////////////
    // Render.

    // Render note, whose position has been determined by previous
    // call of placeNote(s). Displace by reference point.
    public void render(UniGraphics im, int xRef, int yRef) {
	OptionalGlyphs glyph = glyph();
	im.renderStraight(glyph, rect.x + xRef, rect.y + yRef);
    }

    ////////////////////////////////////////////////////////////////////////
    // Operations on pixel level.

    // Try to move the square down, and find a place in im where
    // there are no non-white pixels in that square, as far
    // as possible, but not beyond yMax.
    // Return how many pixels the square was moved down, or -1
    // if no blank square was found.
    private static int downEmptySquare(FlexGraphics im, int noteDist,
	    int x, int y, int width, int height, int yMax) {
	int move = 0;
	int yBot = y + height - 1;
	int nBlank = 0;
	for (int i = 0; i < height; i++) {
	    if (!blankHor(im, x, yBot - i, width))
		break;
	    nBlank++;
	}
	if (nBlank < height) {
	    do {
		yBot++;
		move++;
		if (blankHor(im, x, yBot, width))
		    nBlank++;
		else if (yBot >= y + height + noteDist)
		    return -1;
		else
		    nBlank = 0;
	    } while (nBlank < height && yBot < yMax);
	    if (nBlank < height)
		return -1;
	}
	int aboveMargin = 0;
	for (int i = 0; i < 2 * noteDist; i++) {
	    if (!blankHor(im, x, yBot - height - i, width))
		break;
	    aboveMargin++;
	}
	int belowMargin = 0;
	for (int i = yBot+1; i < yMax; i++) {
	    if (!blankHor(im, x, i, width))
		break;
	    belowMargin++;
	}
	int totalMargin = Math.min(3 * noteDist, belowMargin + aboveMargin);
	if (totalMargin/3 < belowMargin)
	    move += belowMargin - totalMargin/3;
	return move;
    }

    // As above, but move square left.
    private static int leftEmptySquare(FlexGraphics im, int noteDist,
	    int x, int y, int width, int height, int xMax) {
	int move = 0;
	int xLeft = x;
	int nBlank = 0;
	for (int i = 0; i < width; i++) {
	    if (!blankVert(im, x + i, y, height))
		break;
	    nBlank++;
	}
	if (nBlank < width) {
	    do {
		xLeft--;
		move++;
		if (blankVert(im, xLeft, y, height))
		    nBlank++;
		else if (xLeft <= x - noteDist)
		    return -1;
		else
		    nBlank = 0;
	    } while (nBlank < width && xLeft > xMax);
	    if (nBlank < width)
		return -1;
	}
	int rightMargin = 0;
	for (int i = 0; i < 2 * noteDist; i++) {
	    if (!blankVert(im, xLeft + width + i, y, height))
		break;
	    rightMargin++;
	}
	int leftMargin = 0;
	for (int i = xLeft-1; i > xMax; i--) {
	    if (!blankVert(im, i, y, height))
		break;
	    leftMargin++;
	}
	int totalMargin = Math.min(3 * noteDist, leftMargin + rightMargin);
	if (totalMargin/3 < leftMargin)
	    move += leftMargin - totalMargin/3;
	return move;
    }

    // Is there blank horizontal line in image?
    private static boolean blankHor(FlexGraphics im, int x, int y, int width) {
	for (int i = 0; i < width; i++)
	    if (!im.isWhite(x+i, y))
		return false;
	return true;
    }

    // Is there blank vertical line in image?
    private static boolean blankVert(FlexGraphics im, int x, int y, int height) {
	for (int i = 0; i < height; i++)
	    if (!im.isWhite(x, y+i))
		return false;
	return true;
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Turn note into RESlite.
    public void toResLite(int x, int y, int xRef, int yRef, Vector notes) {
	REScodeNotes n = new REScodeNotes();
	n.string = string;
	n.fileNumber = context.getNoteFontNumber();
	n.color = color();
	n.size = context.pixToMilEm(context.noteSize());
	n.x = Math.round((1000.0f * (rect.x + xRef - x) + 500.0f * rect.width) /
		context.emSizePix());
	n.y = Math.round((1000.0f * (rect.y + yRef - y) + 500.0f * rect.height) /
		context.emSizePix());
	notes.add(n);
    }

}
