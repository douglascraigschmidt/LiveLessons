package edu.vandy.quoteservices.utils;

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
     * the {@code queries} into a single regular expression
     * {@link String} that matches any of the queries.
     *
     * @param queries The {@link List} of queries
     * @return A {@link String} that encodes the {@code queries} in
     *         regular expression form
     */
    public static String makeAnyMatchRegex(List<String> queries) {
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
            // contains any of the keywords, regardless of case.
            .collect(joining("|", "(?i).*(", ").*"));
    }

    /**
     * Convert the {@link List} of {@link String} objects containing
     * the {@code queries} into a single regular expression {@link
     * String} that matches all the queries.
     *
     * @param queries The {@link List} of queries
     * @return A {@link String} that encodes the {@code queries} in
     *         regular expression form
     */
    public static String makeAllMatchRegex(List<String> queries) {
        // Create a regular expression pattern that matches any string
        // containing all the queries as whole words.
        var regex = queries
            // Convert the List to a Stream.
            .stream()

            // Use the map operation to transform each query into a
            // positive lookahead pattern that matches the query as
            // a whole word.
            .map(query -> "(?=.*\\b" + query + "\\b)")

            // Concatenate all the positive lookahead patterns into a
            // single string using the joining() collector.
            .collect(Collectors.joining());

            // Complete the regular expression by prepending the
            // wildcard pattern "^" and appending the wildcard pattern
            // ".+." to match any string of any length that contains
            // all the keywords as whole words, regardless of case.
        return "(?i)^" + regex + ".+";
    }

    /**
     * @return The first line of the {@code input}
     */
    public static String getFirstLine(String input) {
        // Create a Matcher.
        Matcher matcher = Pattern
            // Compile a regex that matches only the first line of
            // multi-line input.
            .compile("(?m)^.*$")

            // Create a Matcher for this pattern.
            .matcher(input);

        // Find/return the first line in the String.
        return matcher.find()
            // Return the first String if there's a match.
            ? matcher.group()

            // Return an empty String if there's no match.
            : "";
    }
}
