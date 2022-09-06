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
        urls = new ArrayList<>(urls);

        for (int i = 0; i < urls.size(); ++i) {
            if (!urls.get(i).contains("cse.wustl")) {
                urls.remove(i);
                continue;
            }
            urls.set(i,
                     urls.get(i).replace("cse.wustl","dre.vanderbilt"));
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
            .stream()
            .filter(s -> s.contains("cse.wustl"))
            .map(s -> s.replace("cse.wustl", "dre.vanderbilt"))
            .collect(toList());
    }

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Convert the array into a List.
        List<String> list = Arrays.asList(sUrlArray);
        System.out.println(list);

        // Perform the replacements and print the results.
        System.out.println(java7Replace(list));
        System.out.println(modernJavaReplace((list)));
    }
}
