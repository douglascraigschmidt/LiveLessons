import com.sun.istack.internal.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static java.lang.Character.toLowerCase;

/**
 * This example shows a simple example of a Java 8 Spliterator to
 * traverse each word in a list containing a quote from a famous
 * Shakespeare play.
 */
public class ex13 {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Create a list of strings containing words from a famous
        // quote from the Shakespeare play "Hamlet."
        List<String> bardQuote =
            Arrays.asList("This ", "above ", "all- ", "to ", "thine ", "own ", "self ", "be ", "true", ",\n",
                          "And ", "it ", "must ", "follow ", "as ", "the ", "night ", "the ", "day", ",\n",
                          "Thou ", "canst ", "not ", "then ", "be ", "false ", "to ", "any ", "man.");

        // Iterate through all the words in the quote and print each
        // one.
        for (Spliterator<String> s = bardQuote.spliterator();
             // Keep iterating until there are no more words.
             s.tryAdvance(System.out::print) != false;
             )
            continue;
    }
}

