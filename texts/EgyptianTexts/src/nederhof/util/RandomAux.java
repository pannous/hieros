package nederhof.util;

import java.util.*;

public class RandomAux {

    // Produce random password of length, with letters between min and
    // max, but not those excepted.
    public static String charPassword(int len, char min, char max,
	    char[] except) {
	Random random = new Random();
	String password = "";
	while (password.length() < len) {
	    char c = (char) (min + random.nextInt(max-min+1));
	    boolean excepted = false;
	    for (int j = 0; j < except.length; j++) 
		if (c == except[j]) {
		    excepted = true;
		    break;
		}
	    if (!excepted)
		password += c;
	}
	return password;
    }

    // Produce random password of length, with letters.
    public static String letterPassword(int len) {
	return charPassword(len, 'A', 'z', 
		new char[] {'[', '\\', ']', '^', '_', '`'});
    }

    // For testing.
    public static void main(String[] args) {
	System.out.println(letterPassword(10));
    }

}
