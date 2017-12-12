package nederhof.util;

// Used to simulate call by reference.
public class WrappedBool {

    private boolean b;

    public WrappedBool(boolean b) {
	this.b = b;
    }

    public void set(boolean b) {
	this.b = b;
    }

    public boolean get() {
	return b;
    }

}
