import java.util.List;
import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.StreamSupport;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.util.stream.Collectors.joining;

/**
 * This example shows several examples of using Java Spliterators and
 * both sequential and parallel streams to traverse each word in a
 * list containing a classic quote from a famous Shakespeare play.
 */
@SuppressWarnings({"UnnecessaryContinue", "SimplifyStreamApiCallChains"})
public class ex13 {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Create a list of strings containing words from a famous
        // quote from the Shakespeare play "Hamlet."
        List<String> bardQuote =
            List.of("This ", "above ", "all- ", 
                    "to ", "thine ", "own ", "self ", "be ", "true", ",\n",
                    "And ", "it ", "must ", "follow ", 
                    "as ", "the ", "night ", "the ", "day", ",\n",
                    "Thou ", "canst ", "not ", "then ", 
                    "be ", "false ", "to ", "any ", "man.");

        // Show the various spliterator examples.
        showTryAdvance(bardQuote);
        showTrySplit(bardQuote);
        showStream(bardQuote);
        showParallelStream(bardQuote);
        showStreamSupport(bardQuote);
        showRegexSearch(bardQuote);
    }

    /**
     * Show how the Spliterator tryAdvance() method can be use
     * to traverse through a collection.
     */
    private static void showTryAdvance(List<String> quote) {
        System.out.println("\n++Showing the tryAdvance() method:");

        // Traverse through the words in the quote and print each one.
        for (Spliterator<String> s = quote.spliterator();
             // Keep iterating until there are no more words.
             s.tryAdvance(ex13::display);
             )
            continue;
    }

    /**
     * Show how the Spliterator trySplit() method can be use to
     * traverse through two halves of a collection.
     */
    private static void showTrySplit(List<String> quote) {
        System.out.println("\n\n++Showing the trySplit() method:");

        // Create a spliterator.
        Spliterator<String> secondHalf = quote.spliterator();

        // Split the spliterator in half.
        Spliterator<String> firstHalf = secondHalf.trySplit();

        System.out.println("--Traversing the first half of the spliterator");

        // Use the bulk forEachRemaining() method to print out the
        // first half of the spliterator.
        firstHalf.forEachRemaining(ex13::display);

        System.out.println("\n--Traversing the second half of the spliterator");

        // Use the bulk forEachRemaining() method to print out the
        // second half of the spliterator.
        secondHalf.forEachRemaining(ex13::display);
    }

    /**
     * Show how the stream() factory method can implicitly use a
     * spliterator to create a sequential stream.
     */
    private static void showStream(List<String> quote) {
        System.out.println("\n\n++Showing the stream() factory method:");

        quote
            // Implicitly use a spliterator to create a sequential
            // stream.
            .stream()

            // Print out each element of the stream.
            .forEach(ex13::display);
    }

    /**
     * Show how the parallelStream() factory method can implicitly use a
     * spliterator to create a parallel stream.
     */
    private static void showParallelStream(List<String> quote) {
        System.out.println("\n\n++Showing the parallel() factory method:");

        quote
                // Implicitly use a spliterator to create a parallel
                // stream.
                .parallelStream()

                // Print out each element of the stream.
                .forEach(ex13::display);
    }

    /**
     * Show how the StreamSupport.stream() method can be used to
     * explicitly create a stream from a spliterator.
     */
    private static void showStreamSupport(List<String> quote) {
        System.out.println("\n\n++Showing the StreamSupport.stream() method:");

        StreamSupport
            // Explicitly use a spliterator to create a sequential
            // stream.
            .stream(quote.spliterator(), 
                    false)

            // Print out each element of the stream.
            .forEach(ex13::display);
    }

    /**
     * Show how the Java regular expression methods can be used to
     * search a String for certain words.
     */
    private static void showRegexSearch(List<String> quote) {
        System.out.println("\n\n++Showing Java regular expression methods:");

        // The word to search for.
        String word = "be";

        // The regular expression to compile, which matches the phrase
        // "be" followed by either "true" or "false".
        String regex = "\\b"
            + word
            + ".*(true|false)"
            + "\\b";

        // Make sure the phrase "be" appears in the 'quote' param.
        if (quote
            // Convert List to a Stream.
            .stream()
            // Return true if there's any match of 'be' in the Stream.
            .anyMatch(string -> string
                      .toLowerCase().contains(word))) {

            // Convert the List into a String.
            var quoteString = quote
                // Convert the List to a Stream.
                .stream()

                // Convert the Stream into a single String.
                .collect(joining(""));

            // Print the quoteString.
            System.out.println(quoteString);

            // Compile the regular expression.
            Pattern pattern = Pattern
                .compile(regex,
                         Pattern.CASE_INSENSITIVE);

            // Create a Matcher that associates the regex with
            // the quoteString.
            Matcher matcher = pattern.matcher(quoteString);

            // Keep looping as long as there's a match.
            while (matcher.find()) {
                System.out.println("found \""
                                   // Print the match
                                   + matcher.group()
                                   + "\" at location "
                                   // Print where the match occurred.
                                   + matcher.start());
            }
        }

        // Encode the regex so it can be
        // passed as a URL.
        var encodedRegex = URLEncoder
            .encode(regex,
                    StandardCharsets.UTF_8);

        // Decode the encoded regex so it can be
        // used as a String.
        var decodedRegex = URLDecoder
            .decode(encodedRegex,
                    StandardCharsets.UTF_8);

        // Print the results.
        System.out.println("Original regex = "
                           + regex);
        System.out.println("Encoded regex = "
                           + encodedRegex);
        System.out.println("Decoded regex = "
                           + decodedRegex);
    }

    /**
     * Print the {@link String}.
     */
    private static void display(String string) {
        System.out.println("["
                + Thread.currentThread().getId()
                + "] "
                + string);
    }
}

