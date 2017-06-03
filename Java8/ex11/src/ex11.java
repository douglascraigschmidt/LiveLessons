import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the improper use of the Stream.peek() aggregate
 * operation to interfere with a running stream.
 */
public class ex11 {
    static public void main(String[] argv) {
        // Create a list of 10 integers in the range 0..9.
        List<Integer> list = IntStream
            .range(0, 10)
            .boxed()
            .collect(toList());

        list
            // Convert the list into parallel stream.  This example
            // will also fail if a sequential stream is used.
            .parallelStream()

            // Improperly modify the stream during its processing.
            // This should generate a ConcurrentModificationException
            .peek(list::remove)

            // Print out the results of the stream.
            .forEach(System.out::println);

    }
}

