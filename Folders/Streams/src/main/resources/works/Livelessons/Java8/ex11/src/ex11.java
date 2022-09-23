import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the improper use of the Stream.peek() aggregate
 * operation to interfere with a running stream.
 */
public class ex11 {
    static public void main(String[] argv) {
        boolean useSequentialStream = argv.length > 0;

        // Create a list of 10 integers in the open range [0..10).
        List<Integer> list = IntStream
            .range(0, 10)
            .boxed()
            .collect(toList());

        Stream<Integer> stream = list
            // Convert the list of integers into a stream of integers.
            .stream();

        if (!useSequentialStream)
            stream
                // Convert the list into parallel stream.
                .parallel();

        stream
            // Improperly modify the stream during its processing.
            // This should generate a ConcurrentModificationException
            .peek(list::remove)

            // Print out the results of the stream.
            .forEach(System.out::println);
    }
}

