import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java List/ArrayList removeIf() method.
 */
public class ex2 {
    private static BlockingQueue<CompletableFuture<Integer>> completed =
            new LinkedBlockingQueue<>();

    static void test(Stream<CompletableFuture<Integer>> futures) {
            int initialSize = (int) futures
                    .map(f ->
                         f.whenComplete((__, ___)
                                        -> completed.add(f)))
                    .count();
            System.out.println("size = " + initialSize);

        List<CompletableFuture<Integer>> c = futures
                .map(f ->
                        f.whenComplete((__, ___)
                                -> completed.add(f)))
                .collect(toList());
    }

    static public void main(String[] argv) {
        List<Integer> list =
            new ArrayList<Integer>(List.of(1, 2, 3, 4, 5));

        Random rand = new Random();
        test(Stream
             .generate(() -> CompletableFuture
                       .supplyAsync(rand::nextInt))
             .limit(10));
        // Print the count of the items in the stream.
        countIt(list.stream());
        System.out.println(list);
        
        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(i -> i % 2 == 0);

        // Print the count of the items in the stream.
        countIt(list.stream());
        System.out.println(list);
    }

    /**
     * Print the count of items in the {@code stream}.
     */
    private static void countIt(Stream<Integer> stream) {
        System.out.println("count = " + stream.count());
    }
}

