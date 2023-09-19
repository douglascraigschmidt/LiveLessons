import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example demonstrates what goes wrong when reduce() performs a
 * mutable reduction on a parallel stream and also shows how to fix
 * this problem by using collect().
 */
public class ex48 {
    /**
     * Main entry point into the program.
     */
    public static void main(String[] argv) {
        // Reduce partial results into a string using a sequential
        // stream and the three-parameter version of reduce() along
        // with StringBuilder.
        buggyStreamReduce3a(false);

        // Reduce partial results into a string using a parallel
        // stream and the three-parameter version of reduce() along
        // with StringBuilder..
        buggyStreamReduce3a(true);

        // Reduce partial results into a string using a sequential
        // stream and the three-parameter version of reduce() along
        // with StringBuffer.
        buggyStreamReduce3b(false);

        // Reduce partial results into a string using a parallel
        // stream and the three-parameter version of reduce() along
        // with StringBuffer.
        buggyStreamReduce3b(true);

        // Reduce partial results into a string using a parallel
        // stream and a working version that uses StringBuilder.
        streamReduceStringBuilder(true);

        // Reduce partial results into a string using a sequential
        // stream and string concatenation with reduce().
        streamReduceConcat(false);

        // Reduce partial results into a string using a parallel
        // stream and string concatenation with reduce().
        streamReduceConcat(true);

        // Collect partial results into a string using a sequential
        // stream together with collect() and StringJoiner.
        streamCollectJoining(false);

        // Collect partial results into a string using a parallel
        // stream together with collect() and StringJoiner.
        streamCollectJoining(true);
    }

    /**
     * Reduce partial results into a StringBuilder using the three
     * parameter version of reduce().  If {@code parallel} is true
     * then a parallel stream is used, else a sequential stream is
     * used.  When a sequential stream is used the results of this
     * test will be correct even though a mutable object
     * (StringBuilder) is used with reduce().  When a parallel stream
     * is used, however, the results of this test will be incorrect
     * due to the use of a mutable object (StringBuilder) with
     * reduce(), which expects an immutable object.
     */
    private static void buggyStreamReduce3a(boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + "buggyStreamReduce3a implementation");

        List<String> allStrings =
            List.of("The quick brown fox jumps over the lazy dog\n",
                    "A man, a plan, a canal: Panama\n",
                    "Now is the time for all good people\n",
                    "to come to the aid of their party\n");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> stringStream = allStrings
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();

        // A "real" application would likely do something interesting
        // with the strings at this point.

        // Create a string that contains all the strings appended together.
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

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + allStrings.size()
                           + " strings into "
                           + reducedString.split("\\n").length
                           + " strings took "
                           + stopTime
                           + " milliseconds.  Here are the strings:\n"
                           + reducedString);
    }

    /**
     * Reduce partial results into a StringBuffer using the three
     * parameter version of reduce().  If {@code parallel} is true
     * then a parallel stream is used, else a sequential stream is
     * used.  When a sequential stream is used the results of this
     * test will be correct even though a mutable object
     * (StringBuffer) is used with reduce().  When a parallel stream
     * is used, however, the results of this test will be incorrect
     * due to the use of a mutable object (StringBuffer) with
     * reduce(), which expects an immutable object.
     */
    private static void buggyStreamReduce3b(boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + "buggyStreamReduce3b implementation");

        List<String> allStrings =
            List.of("The quick brown fox jumps over the lazy dog\n",
                    "A man, a plan, a canal: Panama\n",
                    "Now is the time for all good people\n",
                    "to come to the aid of their party\n");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> stringStream = allStrings
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();

        // A "real" application would likely do something interesting
        // with the strings at this point.

        // Create a string that contains all the strings appended together.
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

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + allStrings.size()
                           + " strings into "
                           + reducedString.split("\\n").length
                           + " strings took "
                           + stopTime
                           + " milliseconds.  Here are the strings:\n"
                           + reducedString);
    }

    /**
     * Reduce partial results into a String using reduce() with a
     * working version that uses {@link StringBuilder}.  If {@code
     * parallel} is true then a parallel stream is used, else a
     * sequential stream is used.  This solution is correct since it
     * creates a new instance of {@link StringBuilder} rather than
     * sharing one mutable instance.
     */
    private static void streamReduceStringBuilder(boolean parallel) {
        System.out.println("\n++Running the "
                + (parallel ? "parallel" : "sequential")
                + "streamReduceStringBuilder implementation");

        List<String> allStrings =
                List.of("The quick brown fox jumps over the lazy dog\n",
                        "A man, a plan, a canal: Panama\n",
                        "Now is the time for all good people\n",
                        "to come to the aid of their party\n");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> stringStream = allStrings
                // Convert the list into a stream (which uses a
                // spliterator internally).
                .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();

        // A "real" application would likely do something interesting
        // with the strings at this point.

        // Create a string that contains all the strings appended
        // together.
        String reducedString = stringStream
                // Use reduce() to append all the strings in the
                // stream.  This implementation will work when used
                // with a parallel stream different instances of
                // StringBuilder are used.
                .reduce(new StringBuilder(),
                        (acc, s) -> new StringBuilder(acc).append(s),
                        (sb1, sb2) -> new StringBuilder(sb1).append(sb2))
                // Create a string.
                .toString();

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                + allStrings.size()
                + " strings into "
                + reducedString.split("\\n").length
                + " strings took "
                + stopTime
                + " milliseconds.  Here are the strings:\n"
                + reducedString);
    }

    /**
     * Reduce partial results into a String using reduce() with string
     * concatenation (i.e., the '+' operator).  If {@code parallel} is
     * true then a parallel stream is used, else a sequential stream
     * is used.  This solution is correct, but inefficient due to the
     * overhead of string concatenation.
     */
    private static void streamReduceConcat(boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + "streamReduceConcat implementation");

        List<String> allStrings =
            List.of("The quick brown fox jumps over the lazy dog\n",
                    "A man, a plan, a canal: Panama\n",
                    "Now is the time for all good people\n",
                    "to come to the aid of their party\n");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> stringStream = allStrings
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();

        // A "real" application would likely do something interesting
        // with the strings at this point.

        // Create a string that contains all the strings appended
        // together.
        String reducedString = stringStream
            // Use reduce() to append all the strings in the stream.
            // This implementation works with both sequential and
            // parallel streams, but it's inefficient since it
            // requires string concatenation.
            .reduce("",
                    (x, y) -> x + y);

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + allStrings.size()
                           + " strings into "
                           + reducedString.split("\\n").length
                           + " strings took "
                           + stopTime
                           + " milliseconds.  Here are the strings:\n"
                           + reducedString);
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
    private static void streamCollectJoining(boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + "streamCollectJoining implementation");

        List<String> allStrings =
            List.of("The quick brown fox jumps over the lazy dog\n",
                    "A man, a plan, a canal: Panama\n",
                    "Now is the time for all good people\n",
                    "to come to the aid of their party\n");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> stringStream = allStrings
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            stringStream.parallel();

        // A "real" application would likely do something interesting
        // with the strings at this point.

        // Create a string that contains all the strings appended
        // together.
        String reducedString = stringStream
            // Use collect() to append all the strings in the stream.
            // This implementation works when used with either a
            // sequential or a parallel stream.
            .collect(joining());

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + allStrings.size()
                           + " strings into "
                           + reducedString.split("\\n").length
                           + " strings took "
                           + stopTime
                           + " milliseconds.  Here are the strings:\n"
                           + reducedString);
    }
}
