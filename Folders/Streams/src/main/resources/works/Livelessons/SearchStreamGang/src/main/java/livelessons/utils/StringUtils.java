package livelessons.utils;

/**
 * Useful utility methods that aren't included in the Java String class.
 */
public class StringUtils {
    /**
     * The constructor should always be private in a utility class.
     */
    private StringUtils() {
    }

    /**
     * Returns the index within this string of the first occurrence of
     * the specified character, starting the search at the specified
     * index.  If @a caseSensitive is false the case is ignore,
     * otherwise it is honored.
     */
    public static int indexOf(String context,
                              boolean caseSensitive,
                              String toFind,
                              int from) {
        int toFindIndex = 0;

        char l = toFind.charAt(toFindIndex); // char to match now.

        if (!caseSensitive) 
            l = Character.isLetter(l) ? Character.toLowerCase(l) : l;

        for (int i = from; i < context.length(); i++) {
            char c = context.charAt(i);

            if (!caseSensitive) 
                c = Character.isLetter(c) ? Character.toLowerCase(c) : c;

            if (c != l) 
                toFindIndex = 0;
            else if (toFindIndex + 1 == toFind.length()) 
                // All characters have been found, return index of
                // match.
                return i - toFind.length() + 1; 
            else 
                ++toFindIndex;

            l = toFind.charAt(toFindIndex);// set character to find

            if (!caseSensitive) 
                l = Character.isLetter(l) ?  Character.toLowerCase(l) : l;
        }

        return -1;
    }
}
