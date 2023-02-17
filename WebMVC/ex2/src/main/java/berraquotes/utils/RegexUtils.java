package berraquotes.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

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
        // (?i).*(\Qquery_1\E)|(\Qquery_2\E)...(\Qquery_n\Q).*
        return queries
            // Convert the List to a Stream.
            .stream()

            // Map each element of the stream to a quoted pattern.
            .map(Pattern::quote)

            // Use the joining() collector to concatenate the keywords
            // into a single string separated by the | (OR) operator,
            // and add the prefix (?i).*( and suffix ).* to ensure
            // that the regular expression matches any string that
            // contains any of the keywords as whole words, regardless
            // of case.
            .collect(joining("|", "(?i).*(", ").*"));
    }

    /**
     * Return the first line of the {@code input}.
     */
    public static String getFirstLine(String work) {
        // Create a Matcher.
        Matcher m = Pattern
            // Compile a regex that matches only the first line in the
            // input.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(work);

        // Find/return the first line in the String.
        return m.find()
            // Return the title string if there's a match.
            ? m.group()

            // Return an empty String if there's no match.
            : "";
    }
}
