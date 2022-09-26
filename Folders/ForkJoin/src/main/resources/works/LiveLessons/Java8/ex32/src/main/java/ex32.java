import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows several techniques for concatenating a list of
 * strings together multiple times via Java Streams and RxJava.
 */
public class ex32 {
    /**
     * The number of times to concatenate.
     */
    private static int sMAX_CONCAT = 3;

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
            // Concatenate the contents of the list to the end of the
            // stream n times.
            s = Stream.concat(s, list.stream());

        // Collect the results into a list and return it.
        return s.collect(toList());
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

            // Flatmap the stream of lists of strings into a stream of
            // strings.
            .flatMap(List::stream)

            // Collect the results into a list of strings.
            .collect(toList());
    }

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) throws InterruptedException {
        // Create a list.
        List<String> list = Arrays.asList("1", "2", "3");

        // Generate all the concatenations.
        System.out.println(concatRxJava(list, sMAX_CONCAT));
        System.out.println(concatStream1(list, sMAX_CONCAT));
        System.out.println(concatStream2(list, sMAX_CONCAT));
    }
}

