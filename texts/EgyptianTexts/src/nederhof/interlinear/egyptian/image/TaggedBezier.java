package nederhof.interlinear.egyptian.image;

import java.awt.*;

import nederhof.util.math.*;

// Bezier with name attached to it.
public class TaggedBezier extends Bezier {

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

    // Constructor of empty shape.
    public TaggedBezier() {
    }

    // Constructor from existing shape.
    public TaggedBezier(Bezier bezier, int num, String name) {
	super(bezier);
	setNum(num);
	setName(name);
    }

    // Constructor from rectangle.
    public TaggedBezier(Rectangle rect, int num, String name) {
	super(rect);
	setNum(num);
	setName(name);
    }

}


