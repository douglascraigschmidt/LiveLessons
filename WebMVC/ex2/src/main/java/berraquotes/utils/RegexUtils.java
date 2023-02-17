package berraquotes.utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Java utility class that provides helper methods for Java regular
 * expressions.
 */
public final class RegexUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private RegexUtils() {}

    /**
     * Convert the {@link List} of {@link String} objects containing
     * the queries into a single regular expression {@link String}.
     *
     * @param queries The {@link List} of queries
     * @return A {@link String} that encodes the {@code queries} in
     *         regular expression form
     */
    public static String makeRegex(List<String> queries) {
        // Combine the 'queries' List into a lowercase String and
        // convert into a regex of the following style:
        // (\Qquery_1\E.*)|(\Qquery_2\E.*)...(\Qquery_n\Q.*)
        var regexString = queries
            // Convert the List to a Stream.
            .stream()

            // Map each string to a regular expression fragment that
            // matches the string followed by any number of characters.
            .map(str -> Pattern.quote(str.toLowerCase()) + ".*")

            // Insert the "|" delimiter between queries.
            .collect(Collectors.joining("|"));

        // Create a non-capturing group that groups the keywords
        // together.
        return ".*(?:"
            + regexString
            + ")";
    }
}
