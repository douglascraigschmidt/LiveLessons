import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows several techniques for concatenating a
 * {@link List} of {@link String} together multiple times
 * via Java Streams and RxJava.
 */
public class ex32 {
    /**
     * The number of times to concatenate.
     */
    private static final int sMAX_CONCAT = 3;

    /**
     * Use the RxJava repeat() method to concatenate the contents of
     * {@code list} together {@code n} times.
     */
    static List<String> concatRxJava(List<String> list, int n) {
        return Observable
            // Convert the List into an Observable.
            .fromIterable(list)
            
            // Repeat the Observable n times.
            .repeat(n)

            // Collect the results into a list.
            .collect(toList())
            
            // Block until the processing is done and return the list.
            .blockingGet();
    }

    /**
     * Use the Java Stream concat() method to concatenate the contents
     * of {@code list} together {@code n} times.
     */
    static List<String> concatStream1(List<String> list, int n) {
        // Create an empty stream.
        Stream<String> s = Stream.empty();

        while (--n >= 0)
            // Concatenate the contents of the List to the
            // end of the Stream n times.
            s = Stream.concat(s, list.stream());

        // Collect the results into a List and return it.
        return s.toList();
    }

    /**
     * Use the Java Stream concat() method to concatenate the contents
     * of {@code list} together {@code n} times.
     */
    static List<String> concatStream2(List<String> list, int n) {
        return IntStream
            // Create a stream of integers of size n.
            .rangeClosed(1, n)

            // Repeatedly emit the list 'n' times.
            .mapToObj(__ -> list)

            // Flatmap the Stream of List of String objects
            // into a single Stream of String objects.
            .flatMap(List::stream)

            // Collect the results into a List of Strings.
            .toList();
    }

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) throws InterruptedException {
        // Create a List.
        List<String> list = Arrays.asList("1", "2", "3");

        // Perform all the concatenations.
        System.out.println(concatRxJava(list, sMAX_CONCAT));
        System.out.println(concatStream1(list, sMAX_CONCAT));
        System.out.println(concatStream2(list, sMAX_CONCAT));
    }
}

