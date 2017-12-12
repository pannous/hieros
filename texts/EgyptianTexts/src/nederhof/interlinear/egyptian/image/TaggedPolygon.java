package nederhof.interlinear.egyptian.image;

import java.awt.*;

// Polygon with name attached to it.
public class TaggedPolygon extends Polygon {

    // Number of image in which this occurs.
    private int num = 0;

    // Name of polygon.
    private String name = "";

    public void setNum(int num) {
	this.num = num;
    }

    public int getNum() {
	return num;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    // Constructor of empty polygon.
    public TaggedPolygon() {
    }

    // Constructor from existing polygon.
    public TaggedPolygon(Polygon poly, int num, String name) {
	for (int i = 0; i < poly.npoints; i++) {
	    int x = poly.xpoints[i];
	    int y = poly.ypoints[i];
	    addPoint(x, y);
	}
	setNum(num);
	setName(name);
    }

}


