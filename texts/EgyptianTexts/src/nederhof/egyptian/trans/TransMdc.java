package nederhof.egyptian.trans;

import java.util.*;

// MdC representation of Egyptological transliteration.
public class TransMdc implements Comparable<TransMdc> {
    
    // The bare string.
    private String s;

    // Constructor given string in MdC notation. May include carets.
    public TransMdc(String s) {
	this.s = s;
    }   

    public boolean equals(TransMdc other) {
        return s.equals(other.s);
    }

    public boolean equals(TransLow other) {
        return s.equals(other.toString());
    }

    public int hashCode() {
        return s.hashCode();
    }

    public String toString() {
	return s;
    }

    public int compareTo(TransMdc other) {
        return s.compareTo(other.s);
    }

}
