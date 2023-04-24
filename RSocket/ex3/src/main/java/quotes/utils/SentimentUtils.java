package quotes.utils;

import quotes.common.Options;
import quotes.common.model.Quote;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SentimentUtils {
    /**
     * Debugging tag used by Options.
     */
    private final static String TAG = SentimentUtils
        .class.getSimpleName();

    /**
     * Adds newlines to a string to fit within a specified width.
     * Newlines are added only between words, not within words.
     *
     * @param sentiment  The {@link String} to add newlines to
     * @param lineLength The width to fit the {@link String} within
     * @return The formatted {@link String} with newlines
     */
    public static String formatSentiment(String sentiment,
                                         int lineLength) {
        // Create a string builder to store the result
        StringBuilder sb = new StringBuilder();

        List<String> lines = Stream
            // Split the 'sentiment' string into words using a regular
            // expression that matches any non-word character preceded
            // by any character.
            .of(sentiment.split("(?<=\\W)"))

            // Group the words into lines based on the maximum line
            // length.
            .collect(Collectors
                .groupingBy(s -> sb.append(s).length() / lineLength))

            // Get the values of the grouping map (i.e., the words in
            // each line).
            .values()

            // Convert the Collection to a Stream.
            .stream()

            // Join the words in each line into a String.
            .map(list -> String.join("", list))

            // Collect the lines into a List.
            .toList();

        // Join the lines with the system line separator and return
        // the result.
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Generate a prompt for the ChatGPT web service.
     *
     * @param quote The {@link Quote}
     * @return The prompt to use in the ChatGPT web service
     */
    public static String generatePrompt(Quote quote) {
        return "please analyze the sentiment of this quote '"
            + quote.getQuote()
            + "' from the play "
            + quote.getPlay()
            + " in one paragraph with no more than three sentences";
    }

    /**
     * Print the sentiment analysis of a {@link Quote}.
     *
     * @param quote The {@link Quote} to print
     */
    public static void displaySentimentAnalysis
        (Quote quote) {
        Options.print(TAG,
            "The sentiment analysis of the quote\n\""
            + quote.getQuote()
            + "\"\nfrom \""
            + quote.getPlay()
            + "\" is as follows:\n'"
            + formatSentiment(quote.getSentiment(), 70)
            + "'");
    }

}
