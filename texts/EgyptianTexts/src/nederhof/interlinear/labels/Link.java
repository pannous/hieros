// Two labelled positions, and their types.

package nederhof.interlinear.labels;

public class Link {

    public String id1;
    public String type1;
    public String id2;
    public String type2;

    // Constructor.
    public Link(String id1, String type1,
            String id2, String type2) {
        this.id1 = id1;
        this.type1 = type1;
        this.id2 = id2;
        this.type2 = type2;
    }

    // Equals if all fields are equal.
    public boolean equals(Object o) {
        if (o instanceof Link) {
            Link other = (Link) o;
            return id1.equals(other.id1) &&
                type1.equals(other.type1) &&
                id2.equals(other.id2) &&
                type2.equals(other.type2);
        } else
            return false;
    }

    // For testing.
    public String toString() {
        return id1 + "," + type1 + "," +
            id2 + "," + type2;
    }

}
