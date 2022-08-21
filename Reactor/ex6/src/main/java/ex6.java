import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactortests.ReactorTests;
import rxjavatests.RxJavaTests;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * This example shows performance differences between Project Reactor
 * concurrency/parallelism features when creating {@link Set} objects
 * containing the unique words appearing in the complete work of
 * William Shakespeare.  These tests compare (1) two implementations
 * based on the {@code flatMap()} concurrency idiom and (2) two
 * implementations based on {@link ParallelFlux}.
 */
public class ex6 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 10;

    /**
     * A file containing the complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_WORKS_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A regular expression that matches whitespace and punctuation to
     * split the text of the complete works of Shakespeare into
     * individual words.
     */
    private static final String sSPLIT_BARD_WORDS =
        "[\\t\\n\\x0B\\f\\r'!()\"#&-.,;0-9:@<>\\[\\]? ]+";

    /**
     * A file containing common words.
     */
    private static final String sCOMMON_WORDS_FILE =
        "commonWords.txt";
    /**
     * A regular expression that's used to.
     */
    private static final String sSPLIT_COMMON_WORDS =
        "[\\n\\r]+";

    /**
     * A {@link Set} containing common words to filter out.
     */
    private static final List<CharSequence> sCommonWords = TestDataFactory
            .getInput(sCOMMON_WORDS_FILE,
                      // Split into "words".
                      sSPLIT_COMMON_WORDS);

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Entering the test program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Warm up the threads in the parallel thread pool so timing
        // results will be more accurate.
        warmUpThreadPool();

        // Run test that records the performance of a sequential
        // Flux-based solution.
        runTests("ReactorTests::timeSequential",
                 ReactorTests::runSequential);

        // Run test that records the performance of the flatMap()
        // concurrency idiom using Mono.just().
        runTests("ReactorTests::timeFlatMapJust",
                 ReactorTests::runFlatMapTestJust);

        // Run test that records the performance of the flatMap()
        // concurrency idiom using Mono.fromCallable().
        runTests("ReactorTests::timeFlatMapFromCallable",
                 ReactorTests::runFlatMapTestFromCallable);

        runTests("ReactorTests::timeParallelFlux1",
                 ReactorTests::runParallelFluxTest1);

        runTests("ReactorTests::timeParallelFlux2",
                 ReactorTests::runParallelFluxTest2);

        runTests("ReactorTests::timeParallelFlux3",
                 ReactorTests::runParallelFluxTest3);

        /*
        // Run test that records the performance of the flatMap()
        // concurrency idiom using Observable.fromCallable().
        runTests("RxJavaTests::timeFlatMapFromCallable",
                 RxJavaTests::runFlatMapTestFromCallable);

        // Run test that records the performance of the flatMap()
        // concurrency idiom using Observable.just().
        runTests("RxJavaTests::timeFlatMapJust",
                RxJavaTests::runFlatMapTestJust);

         */

        runTests("RxJavaTests::timeParallelFlowable1",
                RxJavaTests::runParallelFlowableTest1);

        runTests("RxJavaTests::timeParallelFlowable2",
                RxJavaTests::runParallelFlowableTest2);

        runTests("RxJavaTests::timeParallelFlowable3",
                RxJavaTests::runParallelFlowableTest3);

        // Print the results.
        System.out.println("Printing test results for the largest number of input words\n"
                           + RunTimer.getTimingResults());

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate the performance differences bewteen
     * Project Reactor concurrency/parallelism features when creating
     * {@link Set} objects containing the unique words appearing in
     * the complete work of William Shapespeare.
     * 
     * @param testType The type of test
     * @param test A {@link Function} that performs the test
     */
    private static void runTests
        (String testType,
         BiFunction<List<CharSequence>, List<CharSequence>, Integer> test) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 1_000_000)

            // Run the tests for various input data sizes.
            .forEach (limit -> {
                    // Create a List of CharSequences containing
                    // 'limit' words from the works of Shakespeare.
                    List<CharSequence> bardWords = TestDataFactory
                        .getInput(sSHAKESPEARE_WORKS_FILE,
                                  // Split input into "words" by
                                  // ignoring whitespace.
                                  sSPLIT_BARD_WORDS,
                                  limit);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + bardWords.size() 
                                       + " words..");

                    // Run the tests using the "standard"
                    // implementation.
                    timeTest(testType,
                             bardWords,
                             sCommonWords,
                             test);

                });
    }

    /**
     * Determines how long it takes to lowercase a {@link List} of
     * {@code words} and collect the results using the given {@code
     * test}.
     *
     * @param testType The type of test
     * @param words A {@link List} of words to lowercase
     * @param test  The test to run
     */
    private static void timeTest
        (String testType,
         List<CharSequence> words,
         List<CharSequence> commonWords,
         BiFunction<List<CharSequence>, List<CharSequence>, Integer> test) {
        // Run the garbage collector before each test.
        System.gc();

        // Record the number of unique words.
        int[] uniqueWords = new int[1];

        RunTimer
            // Time how long it takes to run the test.
            .timeRun(() -> {
                    for (int i = 0; i < sMAX_ITERATIONS; i++) 
                        uniqueWords[0] += test.apply(words, commonWords);
                },
                testType);

        System.out.println("Number of unique words for "
                           + testType
                           + " = "
                           + uniqueWords[0] / sMAX_ITERATIONS);
    }

    /**
     * Warm up the threads in the parallel thread pool so the timing
     * results will be more accurate.
     */
    private static void warmUpThreadPool() {
        System.out.println("\nWarming up the parallel thread pool\n");

        List<CharSequence> words = TestDataFactory
            .getInput(sSHAKESPEARE_WORKS_FILE,
                      sSPLIT_BARD_WORDS);

        // Create an empty list.
        List<String> list = new ArrayList<>();

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            list
                .addAll(Objects
                        .requireNonNull(Flux
                                        .fromIterable(words)
                                        .flatMap(word -> Mono
                                                 .fromCallable(() -> word)
                                                 .subscribeOn(Schedulers.parallel())
                                                 .map(___ ->
                                                      word.toString().toLowerCase())
                                                 .filter(lowerCaseWord ->
                                                         !sCommonWords.contains(lowerCaseWord)))
                                        .collect(toSet())
                                        .block()));
    }
}
