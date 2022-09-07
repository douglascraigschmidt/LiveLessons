import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This program shows the difference between using traditional Java 7 features
 * to replace substrings vs. using modern Java features.
 */
class ex41 {
    /**
     * An array of strings to replace "cse.wustl" with "dre.vanderbilt".
     */
    static String[] sUrlArray = {
        "http://www.cse.wustl.edu/~schmidt/gifs/ka.png",
        "http://www.cse.wustl.edu/~schmidt/gifs/robot.png",
        "http://www.cse.wustl.edu/~schmidt/gifs/kitten.png"
    };

    /**
     * Use traditional Java 7 features to replace substrings in a {@link List}.
     *
     * @param urls The {@link List} of URLs
     * @return The modified {@link List} of URLs
     */
    private static List<String> java7Replace(List<String> urls) {
        // Make a copy of the urls.
        urls = new ArrayList<>(urls);

        // Loop through all the urls.
        for (int i = 0; i < urls.size(); ++i) {
            // Remove the url at index 'i' if it doesn't
            // match what's expected.
            if (!urls.get(i).contains("cse.wustl")) {
                urls.remove(i);
                continue;
            }
            // Replace the url at index 'i'.
            urls.set(i,
                     urls.get(i).replace("cse.wustl",
                             "dre.vanderbilt"));
        }
        return urls;
    }

    /**
     * Use modern Java features to replace substrings in a {@link List}.
     *
     * @param urls The {@link List} of URLs
     * @return The modified {@link List} of URLs
     */
    private static List<String> modernJavaReplace(List<String> urls) {
        return urls
            // Convert the List to a Stream.
            .stream()

            // Remove items from the Stream if they don't match what's expected.
            .filter(s -> s.contains("cse.wustl"))

            // Perform the replacement on each item remaining in the stream.
            .map(s -> s.replace("cse.wustl", "dre.vanderbilt"))

            // Trigger intermediate processing and collect the results
            // into a List.
            .collect(toList());
    }

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Convert the array into a List.
        List<String> list = Arrays.asList(sUrlArray);
        System.out.println(list);

        // Perform the Java 7 replacements and print the results.
        System.out.println(java7Replace(list));

        System.out.println(list);
        // Perform the modern Java replacements and print the results.
        System.out.println(modernJavaReplace((list)));
    }
}
