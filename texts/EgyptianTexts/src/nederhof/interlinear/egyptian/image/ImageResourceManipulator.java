package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.util.*;

// Helping access to text/image resource, for editor.
public class ImageResourceManipulator {

    // Always for this kind of resource.
    private static final int nTiers = 1;

    // The resource being edited.
    private EgyptianImage resource;

    // The current sign (number). Or negative if none.
    private int current = -1;

    // Constructor.
    public ImageResourceManipulator(EgyptianImage resource,
	    int current) {
        this.resource = resource;
        setCurrentSilent(current);
    }

    /////////////////////////////////////////////////////////
    // Feedback to caller.

    // Inform that resource has changed.
    public void recordChange() {
	// caller should override
    }

    // Inform that focus has changed.
    public void showFocus() {
	// caller should override
    }

    // Inform that focus should not be shown for current.
    // (This is preceding change of focus.)
    public void unshowFocus() {
	// caller should override
    }

    // Inform that one should scroll to focus.
    public void scrollToFocus() {
	// caller should override
    }

    // Inform that focus has changed.
    public void changeFocus() {
	// caller should override
    }

    // Inform that refresh is needed (e.g. when signs have been
    // removed).
    public void refresh() {
	// caller should override
    }
    // Inform that refresh is needed for sign.
    public void refresh(int i, ImageSign sign) {
	// caller should override
    }
    // Inform that refresh is needed for current sign.
    public void refresh(ImageSign sign) {
	// caller should override
    }
    // Inform tbat button belonging to index is to be removed. 
    public void removeButton(int i) {
	// caller should override
    }
    // Inform tbat button belonging to index is to be added. 
    public void addButton(int i, ImageSign sign) {
	// caller should override
    }

    /////////////////////////////////////////////////////////
    // Access of images.

    // Safe way of getting images.
    private Vector<String> getImages() {
	Vector<String> images = (Vector<String>) resource.getProperty("images");
	if (images == null)
	    images = new Vector<String>();
	return images;
    }

    // The number of images.
    public int nImages() {
	Vector<String> images = (Vector<String>) resource.getProperty("images");
	return images == null ? 0 : images.size();
    }

    // Get i-th image address.
    // Convert to absolute path.
    public String imagePath(int i) {
	if (i < 0 || i >= nImages())
	    return null;
	else
	    return FileAux.resolve(resource.getLocation(), getImages().get(i));
    }

    // Add image after i-th. Make it relative to resource location.
    public void appendImage(int i, String address) {
	File addressFile = new File(address);
	appendImage(i, addressFile);
    }
    public void appendImage(int i, File addressFile) {
	if (i < 0 || i > nImages())
	    return;
	File resourceFile = new File(resource.getLocation());
	File relativeFile = FileAux.getRelativePath(addressFile,
		resourceFile.getParentFile());
	Vector<String> images = getImages();
	String path = relativeFile.getPath();
	if (images.contains(path))
	    return;
	images.add(i, path);
	incrImagePlaces(i);
	resource.setProperty("images", images);
	recordChange();
    }

    // Remove i-th image.
    public void cutImage(int i) {
	if (i < 0 || i >= nImages())
	    return;
	Vector<String> images = getImages();
	images.remove(i);
	removeImagePlaces(i);
	resource.setProperty("images", images);
	recordChange();
    }

    /////////////////////////////////////////////////////////
    // Access of tagged areas.

    // Safe way of getting tagged areas.
    public Vector<TaggedBezier> getAreas() {
	Vector<TaggedBezier> areas = (Vector<TaggedBezier>) resource.getProperty("areas");
	if (areas == null)
	    areas = new Vector<TaggedBezier>();
	return areas;
    }

    // Setting areas.
    public void setAreas(Vector<TaggedBezier> areas) {
	resource.setProperty("areas", areas);
	recordChange();
    }

    // Get one of the areas.
    public TaggedBezier getArea(int i) {
	return getAreas().get(i);
    }

    // The number of areas.
    public int nAreas() {
        Vector<TaggedBezier> areas = (Vector<TaggedBezier>) resource.getProperty("areas");
        return areas == null ? 0 : areas.size();
    }

    // Add area.
    public void addArea(TaggedBezier area) {
	Vector<TaggedBezier> areas = getAreas();
        areas.add(area);
        resource.setProperty("areas", areas);
        recordChange();
    }
    // Remove area.
    public void removeArea(TaggedBezier area) {
	Vector<TaggedBezier> areas = getAreas();
	areas.remove(area);
        resource.setProperty("areas", areas);
        recordChange();
    }

    /////////////////////////////////////////////////////////
    // Access of signs.

    // The number of signs in resource.
    public int nSigns() {
        return resource.nPhrases();
    }

    // Get current sign number.
    public int current() {
        return current;
    }

    // Get i-th sign (as part of tier). Or null if none.
    private ImagePlacePart part(int i) {
	if (i < 0 || i >= nSigns())
	    return null;
	TextPhrase phrase = resource.getPhrase(i);
	Vector tier = phrase.getTier(nTiers - 1);
	if (tier.size() == 0)
	    return null;
	return (ImagePlacePart) tier.get(0);
    }
    // Get i-th sign itself.
    public ImageSign sign(int i) {
	ImagePlacePart part = part(i);
	return part == null ? null : part.info;
    }
    // and current sign, or null if none.
    public ImageSign sign() {
	return current >= 0 ? sign(current) : null;
    }

    // Get name of i-th sign.
    public String signName(int i) {
	ImageSign sign = sign(i);
	return sign == null ? "\"?\"" : sign.getName();
    }

    // Get places of i-th sign.
    private Vector<ImagePlace> places(int i) {
	ImageSign sign = sign(i);
	return sign == null ? new Vector<ImagePlace>() : sign.getPlaces();
    }
    // and of current sign.
    public Vector<ImagePlace> places() {
	return current >= 0 ? places(current) : new Vector<ImagePlace>();
    }

    /////////////////////////////////////////////////////////
    // Navigation.

    // Move to position. 
    public void setCurrent(int i) {
	int oldCurrent = current;
	if (i < 0) 
	    i = nSigns() > 0 ? 0 : -1;
	else if (i >= nSigns()) 
	    i = nSigns() > 0 ? nSigns()-1 : -1;
	if (oldCurrent >= 0 && oldCurrent != i)
	    unshowFocus();
        current = i;
	if (current >= 0 && oldCurrent != current) {
	    showFocus();
	    changeFocus();
	}
    }
    // As above, but do not notify change.
    private void setCurrentSilent(int i) {
	if (i < 0) 
	    i = nSigns() > 0 ? 0 : -1;
	else if (i >= nSigns()) 
	    i = nSigns() > 0 ? nSigns()-1 : -1;
        current = i;
	changeFocus();
    }
    // Set to no current sign.
    public void setNoCurrent() {
	current = -1;
	changeFocus();
    }

    // Move to position with sign.
    public void setCurrent(ImageSign sign) {
	for (int i = 0; i < nSigns(); i++) 
	    if (sign(i) == sign) {
		setCurrent(i);
		break;
	    }
    }

    // Move left/right.
    public void left() {
	setCurrent(current-1);
	scrollToFocus();
    }
    public void right() {
	setCurrent(current+1);
	scrollToFocus();
    }

    /////////////////////////////////////////////////////////
    // Removing, adding, updating.

    // Remove i-th sign. Return whether successful.
    private boolean removeSign(int i) {
	if (i < 0 || i >= nSigns())
	    return false;
	resource.removePhrase(i);
	recordChange();
	return true;
    }
    // Remove current.
    public void removeSign() {
	int oldCurrent = current;
	if (!removeSign(current))
	    return;
	setCurrentSilent(current);
	removeButton(oldCurrent);
	// Less error-prone alternative to above line but much slower:
	// refresh();
    }

    // Swap current and next.
    public void swapSigns() {
	if (current < 0 || current >= nSigns()-1)
	    return;
	String name1 = signName(current);
	String name2 = signName(current+1);
	Vector<ImagePlace> places1 = places(current);
	Vector<ImagePlace> places2 = places(current+1);
	updateSign(current, name2, places2);
	updateSign(current+1, name1, places1);
	refresh(current, sign(current));
	refresh(current+1, sign(current+1));
	changeFocus();
    }

    // Combine elements into sign.
    private TextPhrase makeSign(String name, Vector<ImagePlace> places) {
        Vector<ResourcePart> tier = new Vector<ResourcePart>();
	ImagePlacePart part = new ImagePlacePart(new ImageSign(name, places), freshId());
        tier.add(part);
        Vector<ResourcePart>[] tiers = new Vector[nTiers];
        tiers[nTiers - 1] = tier;
        return new TextPhrase(resource, tiers);
    }

    // Insert before i-th sign.
    // If i is number of existing signs, then append.
    private void insertSign(int i, String name) {
        resource.insertPhrase(makeSign(name, new Vector<ImagePlace>()), i);
        recordChange();
    }
    // Insert empty.
    public boolean insertSign(int i) {
	if (i < 0 || i > nSigns())
	    return false;
	insertSign(i, "\"?\"");
	refresh();
	setCurrent(i);
	return true;
    }
    // Insert empty before current.
    public void insertSign() {
	if (current < 0)
	    insertSign(0, "\"?\"");
	else
	    insertSign(current, "\"?\"");
	refresh();
    }
    // Insert empty after current.
    public void addSign() {
	if (current < 0) {
	    insertSign(0, "\"?\"");
	    setCurrentSilent(0);
	    addButton(0, sign());
	    // Less error-prone alternative to above line but much slower:
	    // refresh();
	    // setCurrent(0);
	} else {
	    insertSign(current+1, "\"?\"");
	    setCurrentSilent(current+1);
	    addButton(current, sign());
	    // Less error-prone alternative to above line but much slower:
	    // refresh();
	    // setCurrent(current+1);
	}
    }
    // Insert empty at beginning.
    public void addInitialSign() {
	insertSign(0, "\"?\"");
	setCurrentSilent(0);
	addButton(0, sign());
	// Less error-prone alternative to above line but much slower:
	// refresh();
	// setCurrent(0);
    }

    // Update i-th segment.
    private void updateSign(int i, String name, Vector<ImagePlace> places) {
        resource.setPhrase(makeSign(name, places), i);
        recordChange();
    }
    // Update current sign.
    public void updateSign(String name, Vector<ImagePlace> places) {
	if (current >= 0)
	    updateSign(current, name, places);
    }
    // Update current sign with name only.
    public void updateSign(String name) {
	if (current >= 0) {
	    updateSign(current, name, places());
	    refresh(sign());
	}
    }
    // Update current sign with places only.
    public void updateSign(Vector<ImagePlace> places) {
	if (current >= 0) {
	    updateSign(current, signName(current), places);
	    refresh(sign());
	}
    }

    private String freshId() {
        HashSet<String> ids = new HashSet<String>();
        for (int i = 0; i < nSigns(); i++)
            ids.add(part(i).id);
        int id = 0;
        while (ids.contains("" + id))
            id++;
        return "" + id;
    }

    /////////////////////////////////////////////////////////
    // Images and sign places.

    // Get place (if any) of current sign for image number.
    public ImagePlace placeForImage(int num) {
	Vector<ImagePlace> places = places();
	for (ImagePlace place : places)
	    if (place.getNum() == num)
		return place;
	return null;
    }

    // Remove places from sign with image number in it.
    public void removeImageNumber(int num) {
	Vector<ImagePlace> places = places();
	ImagePlace toBeRemoved = placeForImage(num);
	if (toBeRemoved != null) {
	    places.remove(toBeRemoved);
	    updateSign(places);
	}
    }
    // Add place.
    public void addPlace(ImagePlace place) {
	Vector<ImagePlace> places = places();
	places.add(place);
	updateSign(places);
    }

    // Any sign place associated with image?
    public boolean imageHasPlaces(int image) {
	for (int i = 0; i < nSigns(); i++) 
	    for (ImagePlace place : places(i)) 
		if (place.getNum() == image)
		    return true;
	return false;
    }

    // Remove image in places. Higher-numbered images are decremented.
    public void removeImagePlaces(int image) {
	for (int i = 0; i < nSigns(); i++) {
	    ImageSign sign = sign(i);
	    Vector<ImagePlace> pruned = new Vector<ImagePlace>();
	    for (ImagePlace place : sign.getPlaces()) {
		int im = place.getNum();
		if (im < image) 
		    pruned.add(place);
		else if (im > image) {
		    place.setNum(im-1);
		    pruned.add(place);
		}
	    }
	    sign.setPlaces(pruned);
	}
	Vector<TaggedBezier> toBeRemoved = new Vector<TaggedBezier>();
	for (int i = 0; i < nAreas(); i++) {
	    TaggedBezier area = getArea(i);
	    int im = area.getNum();
	    if (im == image)
		toBeRemoved.add(area);
	    else if (im > image)
		area.setNum(im-1);
	}
	for (TaggedBezier bezier : toBeRemoved)
	    removeArea(bezier);
    }

    // Increment all references to image from number upwards.
    public void incrImagePlaces(int image) {
	for (int i = 0; i < nSigns(); i++) {
	    ImageSign sign = sign(i);
	    for (ImagePlace place : sign.getPlaces()) {
		int im = place.getNum();
		if (im >= image) 
		    place.setNum(im+1);
	    }
	}
	for (TaggedBezier area : getAreas()) {
	    int im = area.getNum();
	    if (im >= image)
		area.setNum(im+1);
	}
    }

    /////////////////////////////////////////////////////////
    // Import.

    // Put hieroglyphic from resource after current position.
    public void incorporateHiero(EgyptianResource hieroResource) {
        Vector<String> joinedHiero = joinHiero(hieroResource);
	int pos = Math.max(current, 0);
	for (String name : joinedHiero) {
	    resource.insertPhrase(makeSign(name, new Vector<ImagePlace>()), pos);
	    pos += 1;
	}
	setCurrentSilent(pos);
        recordChange();
	refresh();
    }

    /**
     * From resource, get hieroglyphic.
     * 
     * @param resource
     * @return
     */
    private static Vector<String> joinHiero(EgyptianResource resource) {
        for (int i = 0; i < resource.nTiers(); i++)
            if (resource.tierName(i).equals("hieroglyphic"))
                return joinHiero(resource, i);
        return new Vector<String>();
    }

    /**
     * From resource and tier number, get hieroglyphic.
     * 
     * @param resource
     * @param tierNo
     * @return
     */
    private static Vector<String> joinHiero(EgyptianResource resource, int tierNo) {
        ResFragment joined = new ResFragment();
        for (int i = 0; i < resource.nPhrases(); i++) {
            TextPhrase phrase = resource.getPhrase(i);
            Vector tier = phrase.getTier(tierNo);
            for (int j = 0; j < tier.size(); j++) {
                ResourcePart part = (ResourcePart) tier.get(j);
                if (part instanceof HiPart) {
                    HiPart hiPart = (HiPart) part;
                    ResFragment next = hiPart.parsed;
                    joined = ResComposer.append(joined, next);
                }
            }
        }
        return joined.glyphNames();
    }

}
