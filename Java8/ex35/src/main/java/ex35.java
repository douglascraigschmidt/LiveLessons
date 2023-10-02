import tests.IntStreamTests;
import tests.StreamTests;
import utils.Options;
import utils.RunTimer;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This example demonstrates how the {@code flatMap()} intermediate
 * operation doesn't scale in a Java parallel stream since it forces
 * sequential processing.  It then shows how a combination of either
 * (1) {@code map()}, {@code reduce()}, and {@code Stream.concat()}
 * and/or (2) {@code mapMulti()} can fix this problem.  Examples of
 * streams with reference types for {@link Stream} and primitive types
 * with {@link IntStream} are demonstrated.
 */
public class ex35 {
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Entering the program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Runs the specified tests using IntStream.
        runIntStreamTests();

        // Runs the specified tests using Stream.
        runStreamTests();

        // Print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the program");
    }

    /**
     * Run the test named {@code testName} by applying the {@code
     * test} {@link Runnable} and timing its performance.
     *
     * @param test The {@link Runnable} test to run
     * @param testName Name of the test
     */
    private static void runTest(Runnable test,
                                String testName) {
        // Let the system garbage collect to start on an even playing
        // field.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(test,
                         testName);
    }

    /**
     * Runs the specified tests using {@link IntStream}.
     */
    private static void runIntStreamTests() {
        // Warmup the flatMap() test, which demonstrates the
        // limitations of flatMap() with Java parallel streams.
        runTest(IntStreamTests::testFlatMap,
                "IntStreamTests::testFlatMap()");

        // Run the flatMap() test, which demonstrates the limitations
        // of flatMap() with Java parallel streams.
        runTest(IntStreamTests::testFlatMap,
                "IntStreamTests::testFlatMap()");

        // Warmup the map() test, which shows one way to overcome the
        // limitations with flatMap().
        runTest(IntStreamTests::testMap,
                "IntStreamTests::testMap()");

        // Run the map() test, which shows one way to overcome the
        // limitations with flatMap().
        runTest(IntStreamTests::testMap,
                "IntStreamTests::testMap()");

        // Warmup the mapMulti() test, which shows another way to
        // overcome the limitations with flatMap().
        runTest(IntStreamTests::testMapMulti,
                "IntStreamTests::testMapMulti()");

        // Run the mapMulti() test, which also shows another way to
        // overcome the limitations with flatMap().
        runTest(IntStreamTests::testMapMulti,
                "IntStreamTests::testMapMulti()");
    }

    /**
     * Runs the specified tests using {@link Stream}.
     */
    private static void runStreamTests() {
        // Warmup the flatMap() test, which demonstrates the
        // limitations of flatMap() with Java parallel streams.
        runTest(StreamTests::testFlatMap,
                "StreamTests::testFlatMap()");

        // Run the flatMap() test, which demonstrates the limitations
        // of flatMap() with Java parallel streams.
        runTest(StreamTests::testFlatMap,
                "StreamTests::testFlatMap()");

        // Warmup the reduce()/Stream.concat() test, which shows one
        // way to overcome the limitations with flatMap().
        runTest(StreamTests::testReduceConcat,
                "StreamTests::testReduceConcat()");

        // Run the reduce()/Stream.concat() test, which shows one way
        // to overcome the limitations with flatMap().
        runTest(StreamTests::testReduceConcat,
                "StreamTests::testReduceConcat()");

        // Warmup the mapMulti() test, which shows another way to
        // overcome the limitations with flatMap().
        runTest(StreamTests::testMapMulti,
                "StreamTests::testMapMulti()");
        
        // Run the mapMulti() test, which also shows another way to
        // overcome the limitations with flatMap().
        runTest(StreamTests::testMapMulti,
                "StreamTests::testMapMulti()");
    }
}
