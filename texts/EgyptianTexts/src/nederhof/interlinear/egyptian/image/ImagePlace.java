package nederhof.interlinear.egyptian.image;

// Places of sign within the images. Indicated is number of image.
public class ImagePlace {
    private int num;
    private int x;
    private int y;
    private int width;
    private int height;

    public ImagePlace(int num, int x, int y,
	    int width, int height) {
	this.num = num;
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }

    public int getNum() {
	return num;
    }
    public void setNum(int num) {
	this.num = num;
    }
    public int getX() {
	return x;
    }
    public int getY() {
	return y;
    }
    public int getWidth() {
	return width;
    }
    public int getHeight() {
	return height;
    }

}
