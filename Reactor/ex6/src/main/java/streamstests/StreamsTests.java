package streamstests;

import utils.ConcurrentHashSet;
import utils.ConcurrentSetCollector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * This class contains Java streams tests.
 */
public class StreamsTests {
    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a Java sequential {@link
     * Stream}-based implementation.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runSequential(List<CharSequence> words,
                                    List<CharSequence> commonWords) {
        return words
            // Convert the List into a Stream.
            .stream()

            // Map each string to lower case.
            .map(word ->
                 // Map each word to lower case.
                 word.toString().toLowerCase())

            // Filter out common words.
            .filter(lowerCaseWord ->
                    !commonWords.contains(lowerCaseWord))

            // Collect unique words into a Set.
            .collect(toSet())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a Java parallel {@link Stream}-based
     * implementation with a non-concurrent collector.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallel1(List<CharSequence> words,
                                   List<CharSequence> commonWords) {
        return words
            // Convert the List into a ParallelStream.
            .parallelStream()

            // Map each string to lower case.
            .map(word ->
                 // Map each word to lower case.
                 word.toString().toLowerCase())

            // Filter out common words.
            .filter(lowerCaseWord ->
                    !commonWords.contains(lowerCaseWord))

            // Collect unique words into a Set.
            .collect(toSet())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a Java parallel {@link Stream}-based
     * implementation with a concurrent collector.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallel2(List<CharSequence> words,
                                   List<CharSequence> commonWords) {
        return words
            // Convert the List into a ParallelStream.
            .parallelStream()

            // Map each string to lower case.
            .map(word ->
                 // Map each word to lower case.
                 word.toString().toLowerCase())

            // Filter out common words.
            .filter(lowerCaseWord ->
                    !commonWords.contains(lowerCaseWord))

            // Collect unique words into a Set.
            .collect(ConcurrentSetCollector.toSet(ConcurrentHashMap::newKeySet))

            // Return the number of unique words in this input.
            .size();
    }
}
