package nederhof.lexicon.egyptian;

import java.util.*;

// Comparison of strings, representing Egyptian transliteration.
public class TranslitComparator implements Comparator<String> {

    // Characters in their preferred order.
    public static final String alphabet =
	"AjiyawbpfmnrlhHxXzsSqkgtTdD0123456789";

    // Compare two strings, after mapping characters to
    // indices.
    public int compare(String s1, String s2) {
	return compareTranslit(s1, s2);
    }

    public static int compareTranslit(String s1, String s2) {
	s1 = s1.replaceAll("\\^", "");
	s2 = s2.replaceAll("\\^", "");
	s1 = s1.replaceAll("\\(", "");
	s2 = s2.replaceAll("\\(", "");
	s1 = s1.replaceAll("\\)", "");
	s2 = s2.replaceAll("\\)", "");
	s1 = s1.replaceAll("\\[", "");
	s2 = s2.replaceAll("\\[", "");
	s1 = s1.replaceAll("\\]", "");
	s2 = s2.replaceAll("\\]", "");
	s1 = s1.replaceAll("\\{.\\}", "");
	s2 = s2.replaceAll("\\{.\\}", "");
	if (s1.length() == 0 && s2.length() == 0)
	    return 0;
	else if (s1.length() == 0 && s2.length() > 0)
	    return -1;
	else if (s1.length() > 0 && s2.length() == 0)
	    return 1;
	else {
	    int[] nums1 = strToNums(s1);
	    int[] nums2 = strToNums(s2);
	    return numsCompare(nums1, nums2);
	}
    }

    // Map characters to numbers.
    private static int[] strToNums(String s) {
	int[] nums = new int[s.length()];
	for (int p = 0; p < s.length(); p++) {
	    char c = s.charAt(p);
	    int i = alphabet.indexOf(c);
	    if (i >= 0)
		nums[p] = i;
	    else
		nums[p] = 256 + c;
	}
	return nums;
    }

    // Lexicographical ordering on numbers.
    public static int numsCompare(int[] nums1, int[] nums2) {
	for (int i = 0; i < Math.min(nums1.length, nums2.length); i++) {
	    if (nums1[i] != nums2[i])
		return nums1[i] - nums2[i];
	}
	return nums1.length - nums2.length;
    }

    // Unused.
    public boolean equals(Object o) {
	return false;
    }
}
