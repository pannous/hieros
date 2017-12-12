package nederhof.interlinear.egyptian.image;

import java.util.*;

// Sign in images, with places within the images.
public class ImageSign {
    private String name;
    private Vector<ImagePlace> places;

    public ImageSign(String name, Vector<ImagePlace> places) {
	this.name = name;
	this.places = places;
    }

    public String getName() {
	return name;
    }

    public Vector<ImagePlace> getPlaces() {
	return places;
    }

    public void setPlaces(Vector<ImagePlace> places) {
	this.places = places;
    }


}
