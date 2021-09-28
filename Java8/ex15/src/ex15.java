import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the limitations of using inherently sequential
 * Java streams operations (such as iterate() and limit()) in the
 * context of parallel streams.
 */
public class ex15 {
    /**
     * Number of times to iterate the tests.
     */
    private static long sNUMBER = 1000000;

    /**
     * Main entry point into the program.
     */
    public static void main(String[] args) {
        // Override the number of iterations if user requests it.
        sNUMBER = args.length > 0 ? Long.parseLong(args[0]) : sNUMBER;

        ex15 test = new ex15();

        test.warmUpForkJoinPool(sNUMBER);

        // Run the tests.

        // This parallel variant will perform very poorly since
        // iterate() and limit() are inherently sequentially and
        // cannot be split effectively.
        test.testStreamIterate(true, sNUMBER);

        // This sequential variant will perform poorly since iterate()
        // is relatively inefficient.
        test.testStreamIterate(false, sNUMBER);

        // This sequential variant will perform better than the
        // previous one since range() is efficient.
        test.testStreamRange(false, sNUMBER);

        // This parallel variant will perform very well since range()
        // can be split effectively.
        test.testStreamRange(true, sNUMBER);
    }

    /**
     * Use a stream and the Stream.iterate() operation to compute the
     * sqrt of the first {@code number} even numbers.  If {@code parallel}
     * is true use a parallel stream, else use a sequential stream.
     */
    private void testStreamIterate(boolean parallel,
                                   long number) {
        // Record the start time.
        long startTime = System.nanoTime();

        Stream<Long> stream = Stream
                // Generate a stream of numbers starting at 2.
                .iterate(2L, l -> l + 1);

        if (parallel)
            // Run the stream concurrently.
            stream.parallel();

        List<Double> result = stream
            // Remove all the odd numbers from the stream.
            .filter(this::isEven)

            // Limit the # of elements in the stream to @a number.
            .limit(number)

            // Compute the sqrt of each even number in the stream.
            .map(this::findSQRT)

            // Terminate the stream and collect results into a list.
            .collect(toList());

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        assert number == result.size();

        // System.out.println(result);
        System.out.println("The "
                           + (parallel ? "parallel" : "sequential") 
                           + " stream using Stream.iterate() took "
                           + stopTime
                           + " milliseconds to find the sqrt of the first "
                           + number
                           + " even numbers");

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Use a stream and the LongStream.range() operation to compute
     * the sqrt of the first {@code number} even numbers.  If {@code
     * parallel} is true use a parallel stream, else use a sequential
     * stream.
     */
    private void testStreamRange(boolean parallel,
                                 long number) {
        // Record the start time.
        long startTime = System.nanoTime();

        LongStream stream = LongStream
            // Generate a stream of numbers starting at 2 and
            // continuing up to @a number * 2.
            .range(2, (number * 2) + 1);

        if (parallel)
            // Run the stream concurrently.
            stream.parallel();

        List<Double> result = stream
            // Remove all the odd numbers from the stream.
            .filter(this::isEven)

            // Compute the sqrt of each even number in the stream.
            .mapToObj(this::findSQRT)

            // Terminate the stream and collect results into a list.
            .collect(toList());

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        assert number == result.size();

        // System.out.println(result);
        System.out.println("The "
                           + (parallel ? "parallel" : "sequential") 
                           + " stream using IntStream.range() took "
                           + stopTime
                           + " milliseconds to find the sqrt of the first "
                           + number
                           + " even numbers");

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private void warmUpForkJoinPool(long number) {
        System.out.println("Warming up the fork/join pool");

        List<Double> result= Stream
            // Generate a stream of numbers starting at 2.
            .iterate(2, i -> i + 1)

            // Run the stream concurrently.
            .parallel()

            // Remove all the odd numbers from the stream.
            .filter(this::isEven)

            // Limit the # of elements in the stream to @a number.
            .limit(number)

            // Compute the sqrt of each even number in the stream.
            .map(this::findSQRT)

            // Terminate the stream and collect results into a list.
            .collect(toList());
    }

    /**
     * Return the sqrt of @a number.
     */
    private Double findSQRT(long number){
        var v = Math.sqrt(number);
        /*
          System.out.println("findSQRT:: "
          + number
          + " in "
          + Thread.currentThread()
          + " = "
          + v);
        */
        return v;
    }

    /**
     * Returns true of @a number is even, else false.
     */
    private boolean isEven(long number){
        /*
          System.out.println("isEven:: "
                               + number
                               + " in "
                               + Thread.currentThread());
        */
        // Use the bit-wise operator to determine if a number is even
        // or odd.
        return (number & 1) == 0;
    }
}

