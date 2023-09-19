package utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
     *         regular expression form and matches any string that
     *         contains any of the keywords as whole words, regardless
     *         of case.
     */
    public static String makeRegex(List<String> queries) {
        // Combine the 'queries' List into a lowercase String and
        // convert into a regex of the following style:
        // (?i).*\b(\Qquery_1\E)|(\Qquery_2\E)...(\Qquery_n\Q)\b.*
        return queries
            // Convert the List to a Stream.
            .stream()

            // Map each element of the stream to a quoted pattern,
            // which escapes all special regex characters in each
            // query string, allowing them to be treated as literals
            // in a regular expression.
            .map(Pattern::quote)

            // Use the joining() collector to concatenate the keywords
            // into a single string separated by the | (OR) operator.
            .collect(joining("|",
                             // Add prefix "(?i).*\\b(" and suffix
                             // ")\\b.*" to ensure the regex matches
                             // any string containing any keywords as
                             // whole words, regardless of case.
                             "(?i).*\\b(",
                             ")\\b.*"));
    }

    /**
     * @return The first line of the {@code input} {@link String}
     */
    public static String getFirstLine(String input) {
        return Stream
            // Create a Stream containing a single Matcher.
            .of(Pattern
                 // Compile a regex that matches only the first line
                 // of multi-line input.
                .compile("(?m)^.*$") 

                 // Create a Matcher for this pattern.
                .matcher(input))     

            // Only keep a Matcher if it successfully finds a
            // match in the input String.
            .filter(Matcher::find)

            // Transform the Stream of one Matcher into a Stream of
            // one Strings.
            .map(Matcher::group)

            // Return an Optional describing the first matched line,
            // if any. 
            .findFirst()

            // Provide a default value in case no match was found.
            .orElse("");
    }
}
