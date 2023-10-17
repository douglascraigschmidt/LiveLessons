import utils.RunTimer;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example demonstrates what goes wrong when reduce() performs a
 * mutable reduction on a parallel stream and also shows various ways
 * to fix this problem, e.g., by using collect().
 */
@SuppressWarnings({"resource", "ResultOfMethodCallIgnored", "StructuralWrap"})
public class ex48 {
    /**
     * Main entry point into the program.
     */
    public static void main(String[] argv) {
        List<String> allStrings =
            List.of("The quick brown fox jumps over the lazy dog\n",
                    "A man, a plan, a canal: Panama\n",
                    "Evil is a name of a foeman, as I live.\n",
                    "Able was I, ere I saw Elba\n",
                    "Now is the time for all good people\n",
                    "to come to the aid of their party\n");

        // Reduce partial results into a string using a sequential
        // stream and the three-parameter version of reduce() along
        // with StringBuilder.
        buggyStreamReduce3a(allStrings, false);

        // Reduce partial results into a string using a parallel
        // stream and the three-parameter version of reduce() along
        // with StringBuilder..
        buggyStreamReduce3a(allStrings, true);

        // Reduce partial results into a string using a sequential
        // stream and the three-parameter version of reduce() along
        // with StringBuffer.
        buggyStreamReduce3b(allStrings, false);

        // Reduce partial results into a string using a parallel
        // stream and the three-parameter version of reduce() along
        // with StringBuffer.
        buggyStreamReduce3b(allStrings, true);

        // Reduce partial results into a string using a parallel
        // stream and a working version that uses StringBuilder.
        streamReduceStringBuilder(allStrings, false);

        // Reduce partial results into a string using a parallel
        // stream and a working version that uses StringBuilder.
        streamReduceStringBuilder(allStrings, true);

        // Reduce partial results into a string using a sequential
        // stream and string concatenation with reduce().
        streamReduceConcat(allStrings, false);

        // Reduce partial results into a string using a parallel
        // stream and string concatenation with reduce().
        streamReduceConcat(allStrings, true);

        // Collect partial results into a string using a sequential
        // stream together with collect() and StringJoiner.
        streamCollectJoining(allStrings, false);

        // Collect partial results into a string using a parallel
        // stream together with collect() and StringJoiner.
        streamCollectJoining(allStrings, true);
    }

    /**
     * Reduce partial results into a StringBuilder using the
     * three-parameter version of reduce(). If {@code parallel} is
     * true then a parallel stream is used, else a sequential stream
     * is used.  When a sequential stream is used, the results of this
     * test will be correct even though a mutable object
     * (StringBuilder) is used with reduce().  When a parallel stream
     * is used, however, the results of this test will be incorrect
     * due to the use of a mutable object (StringBuilder) with
     * reduce(), which expects an immutable object.
     */
    private static void buggyStreamReduce3a(List<String> allStrings,
                                            boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + " buggyStreamReduce3a() implementation");

        Runnable runnable = () -> {
            // Convert allStrings into a Stream of String objects.
            Stream<String> stringStream = getStringStream(allStrings, parallel);

            // Create a string that contains all the strings appended
            // together.
            String reducedString = stringStream
            // Use reduce() to append all the strings in the stream.
            // This implementation will fail when used with a parallel
            // stream since reduce() expects to do "immutable"
            // reduction, but there's just a single StringBuilder!
            .reduce(new StringBuilder(),
                    StringBuilder::append,
                    StringBuilder::append)

            // Create a string.
            .toString();

            // Check the results to see if they succeeded or failed.
            checkResults(allStrings, reducedString);
        };

        // Run the runnable and time it.
        var elapsedTime = RunTimer.timeRun(runnable);;

        System.out.println("The time to reduce "
                           + allStrings.size()
                           + " strings took "
                           + elapsedTime
                           + " milliseconds.");
    }

    /**
     * Reduce partial results into a StringBuffer using the
     * three-parameter version of reduce(). If {@code parallel} is
     * true then a parallel stream is used, else a sequential stream
     * is used.  When a sequential stream is used the results of this
     * test will be correct even though a mutable object
     * (StringBuffer) is used with reduce().  When a parallel stream
     * is used, however, the results of this test will be incorrect
     * due to the use of a mutable object (StringBuffer) with
     * reduce(), which expects an immutable object.
     */
    private static void buggyStreamReduce3b(List<String> allStrings,
                                            boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + " buggyStreamReduce3b() implementation");

        Runnable runnable = () -> {
            // Convert allStrings into a Stream of String objects.
            Stream<String> stringStream = getStringStream(allStrings, parallel);

            // Create a string that contains all the strings appended
            // together.
            String reducedString = stringStream
            // Use reduce() to append all the strings in the stream.
            // This implementation will fail when used with a parallel
            // stream since reduce() expects to do "immutable"
            // reduction, but there's just a single StringBuffer!
            .reduce(new StringBuffer(),
                    StringBuffer::append,
                    StringBuffer::append)

            // Create a string.
            .toString();

            // Check the results to see if they succeeded or failed.
            checkResults(allStrings, reducedString);
        };

        // Run the runnable and time it.
        var elapsedTime = RunTimer.timeRun(runnable);;

        System.out.println("The time to reduce "
                           + allStrings.size()
                           + " strings took "
                           + elapsedTime
                           + " milliseconds.");
    }

    /**
     * Reduce partial results into a String using reduce() with a
     * working version that uses {@link StringBuilder}.  If {@code
     * parallel} is true then a parallel stream is used, else a
     * sequential stream is used.  This solution is correct since it
     * creates a new instance of {@link StringBuilder} rather than
     * sharing one mutable instance.
     */
    private static void streamReduceStringBuilder(List<String> allStrings,
                                                  boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + " streamReduceStringBuilder() implementation");

        Runnable runnable = () -> {
            // Convert allStrings into a Stream of String objects.
            Stream<String> stringStream = getStringStream(allStrings, parallel);

            // Create a string that contains all the strings appended
            // together.
            String reducedString = stringStream
            // Use reduce() to append all the strings in the
            // stream. This implementation will work when used
            // with a parallel stream different instances of
            // StringBuilder are used.
            .reduce(new StringBuilder(),
                    (acc, s) -> new StringBuilder(acc).append(s),
                    (sb1, sb2) -> new StringBuilder(sb1).append(sb2))

            // Create a string.
            .toString();

            // Check the results to see if they succeeded or failed.
            checkResults(allStrings, reducedString);
        };

        // Run the runnable and time it.
        var elapsedTime = RunTimer.timeRun(runnable);;

        System.out.println("The time to reduce "
                           + allStrings.size()
                           + " strings took "
                           + elapsedTime
                           + " milliseconds.");
    }

    /**
     * Reduce partial results into a String using reduce() with string
     * concatenation (i.e., the '+' operator).  If {@code parallel} is
     * true then a parallel stream is used, else a sequential stream
     * is used.  This solution is correct, but inefficient due to the
     * overhead of string concatenation.
     */
    private static void streamReduceConcat(List<String> allStrings,
                                           boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + " streamReduceConcat() implementation");

        Runnable runnable = () -> {
            // Convert allStrings into a Stream of String objects.
            Stream<String> stringStream = getStringStream(allStrings, parallel);

            // Create a string that contains all the strings appended
            // together.
            String reducedString = stringStream
            // Use reduce() to append all the strings in the stream.
            // This implementation works with both sequential and
            // parallel streams, but it's inefficient since it
            // requires string concatenation.
            .reduce("",
                    (x, y) -> x + y);

            // Check the results to see if they succeeded or failed.
            checkResults(allStrings, reducedString);
        };

        // Run the runnable and time it.
        var elapsedTime = RunTimer.timeRun(runnable);;

        System.out.println("The time to reduce "
                           + allStrings.size()
                           + " strings took "
                           + elapsedTime
                           + " milliseconds.");
    }

    /**
     * Collect partial results into a string using a parallel stream
     * together with collect() and joining().  If {@code parallel} is
     * true then a parallel stream is used, else a sequential stream
     * is used.  When a sequential stream or a parallel stream is used
     * the results of this test will be correct due to the use of a
     * mutable object (StringJoiner) with collect(), which works
     * correctly in this case.
     */
    private static void streamCollectJoining(List<String> allStrings,
                                             boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + " streamCollectJoining() implementation");

        Runnable runnable = () -> {
            // Convert allStrings into a Stream of String objects.
            Stream<String> stringStream = getStringStream(allStrings, parallel);

            // Create a string that contains all the strings appended
            // together.
            String reducedString = stringStream
            // Use collect() to append all the strings in the stream.
            // This implementation works when used with either a
            // sequential or a parallel stream.
            .collect(joining());

            // Check the results to see if they succeeded or failed.
            checkResults(allStrings, reducedString);
        };

        // Run the runnable and time it.
        var elapsedTime = RunTimer.timeRun(runnable);;

        System.out.println("The time to collect "
                           + allStrings.size()
                           + " strings took "
                           + elapsedTime
                           + " milliseconds.");
    }

    /**
     * Returns a {@link Stream} of the {@code allStrings} {@link String} objects.
     *
     * @param allStrings The {@link List} of {@link String} objects to
     *                   check against
     * @param parallel Whether to use a parallel stream or not
     * @return A {@link Stream} of {@link String} objects
     */
    private static Stream<String> getStringStream(List<String> allStrings,
                                                  boolean parallel) {
        Stream<String> stringStream = allStrings
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();
        return stringStream;
    }

    /**
     * Checks to see if the reduction was correct or incorrect.
     *
     * @param allStrings The {@link } of {@link String} objects to check against
     * @param reducedString The {@link String} to check against
     */
    private static void checkResults(List<String> allStrings,
                                     String reducedString) {
        // Determine how many reduced String objects were created.
        int reduceStrings = reducedString.split("\\n").length;

        // Determine if f the reduction was correct or incorrect.
        boolean correct = allStrings.size() == reduceStrings;

        // Print the results.
        System.out.println(allStrings.size()
                           + " strings were "
                           + (correct ? "correctly" : "incorrectly")
                           + " split into the following "
                           + reduceStrings
                           + " strings:\n"
                           + reducedString);
    }
}
