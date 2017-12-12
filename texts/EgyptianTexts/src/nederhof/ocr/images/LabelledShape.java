package nederhof.ocr.images;

import java.awt.*;

// Shape together with label of type L.
public class LabelledShape<L> {

    public Shape shape;
    public L label;

    public LabelledShape(Shape shape, L label) {
	this.shape = shape;
	this.label = label;
    }

}
