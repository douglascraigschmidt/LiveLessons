import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import utils.ConcurrentHashSet;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This example shows performance differences between Project Reactor
 * concurrency/parallelism features when creating {@link Set} objects
 * containing the unique words appearing in the complete work of
 * William Shapespeare.  These tests compare (1) two implementations
 * based on the {@code flatMap()} concurrency idiom and (2) two
 * implementations based on {@link ParallelFlux}.
 */
public class ex6 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 10;

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A regular expression that matches whitespace and punctuation to
     * split the text of the complete works of Shakespeare into
     * individual words.
     */
    private static final String sSPLIT_WORDS =
        "[\\t\\n\\x0B\\f\\r'!()\"#&-.,;0-9:@<>\\[\\]? ]+";

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
        runTests("timeSequential",
                 ex6::runSequential);

        // Run test that records the performance of the flatMap()
        // concurrency idiom using Mono.just().
        runTests("timeFlatMapJust",
                 ex6::runFlatMapTestJust);

        // Run test that records the performance of the flatMap()
        // concurrency idiom using Mono.fromCallable().
        runTests("timeFlatMapFromCallable",
                 ex6::runFlatMapTestFromCallable);

        runTests("timeParallelFlux1",
                 ex6::runParallelFluxTest1);

        runTests("timeParallelFlux2",
                 ex6::runParallelFluxTest2);

        runTests("timeParallelFlux3",
                 ex6::runParallelFluxTest3);

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
         Function<List<CharSequence>, Integer> test) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 1_000_000)

            // Run the tests for various input data sizes.
            .forEach (limit -> {
                    // Create a List of CharSequences containing
                    // 'limit' words from the works of Shakespeare.
                    List<CharSequence> arrayWords = TestDataFactory
                        .getInput(sSHAKESPEARE_DATA_FILE,
                                  // Split input into "words" by
                                  // ignoring whitespace.
                                  sSPLIT_WORDS,
                                  limit);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + arrayWords.size() 
                                       + " words..");

                    // Run the tests using the "standard"
                    // implementation.
                    timeTest(testType,
                             arrayWords,
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
         Function<List<CharSequence>, Integer> test) {
        // Run the garbage collector before each test.
        System.gc();

        // Record the number of unique words.
        int[] uniqueWords = new int[1];

        RunTimer
            // Time how long it takes to run the test.
            .timeRun(() -> {
                    for (int i = 0; i < sMAX_ITERATIONS; i++) 
                        uniqueWords[0] += test.apply(words);
                },
                testType);

        System.out.println("Number of unique words for "
                           + testType
                           + " = "
                           + uniqueWords[0] / sMAX_ITERATIONS);
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using a sequential {@link Flux}-based
     * implementation.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runSequential(List<CharSequence> words) {
        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom to
                            // map each string to lower case using the
                            // given Scheduler.
                            .map(word ->
                                 // Map each word to lower case.
                                 word.toString().toLowerCase())

                            // Collect unique words into a Set.
                            .collect(Collectors.toSet())

                            // Wait until all computations are done. 
                            .block())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using the {@code flatMap()} concurrency
     * idiom and the {@link Mono.just} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runFlatMapTestJust(List<CharSequence> words) {
        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom to
                            // map each string to lower case using the
                            // given Scheduler.
                            .flatMap(word -> Mono
                                     // Emit the word in the assembly
                                     // thread.
                                     .just(word)

                                     // Run each computation in the
                                     // parallel thread pool.
                                     .subscribeOn(Schedulers.parallel())

                                     // Map each word to lower case.
                                     .map(___ ->
                                          word.toString().toLowerCase()))

                            // Collect unique words into a Set.
                            .collect(Collectors.toSet())

                            // Wait until all computations are done. 
                            .block())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using the {@code flatMap()} concurrency
     * idiom and the {@link Mono.fromCallable} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runFlatMapTestFromCallable(List<CharSequence> words) {
        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom
                            // to map each string to lower case
                            // using the given Scheduler.
                            .flatMap(word -> Mono
                                     // Emit the word in a thread from
                                     // the parallel thread pool.
                                     .fromCallable(() -> word)

                                     // Run each computation in the
                                     // parallel thread pool.
                                     .subscribeOn(Schedulers.parallel())

                                     // Map each word to lower case.
                                     .map(___ ->
                                          word.toString().toLowerCase()))

                            // Collect unique words into a Set.
                            .collect(Collectors.toSet())

                            // Wait until all computations are done. 
                            .block())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using the canonical means of collecting
     * results from a {@link ParallelFlux} into a {@link Set}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runParallelFluxTest1(List<CharSequence> words) {
        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Convert the Flux to a ParallelFlux.
                            .parallel()

                            // Run all the rails in the parallel Scheduler.
                            .runOn(Schedulers.parallel())

                            // Transform each string to lower case.
                            .map(word ->
                                 word.toString().toLowerCase())

                            // Convert the ParallelFlux back to a Flux.
                            .sequential()

                            // Collect the words into a Set.
                            .collect(Collectors.toSet())

                            // Block until all the processing is done.
                            .block())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using a {@link ParallelFlux} that collects
     * into a single {@link ConcurrentHashSet}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runParallelFluxTest2(List<CharSequence> words) {
        var set = new ConcurrentHashSet<String>();

        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Convert the Flux to a ParallelFlux.
                            .parallel()

                            // Run all the rails in the parallel Scheduler.
                            .runOn(Schedulers.parallel())

                            // Transform each string to lower case.
                            .map(word ->
                                 word.toString().toLowerCase())

                            // Concurrently collect the words into a
                            // single ConcurrentHashSet.
                            .collect(() -> set,
                                     ConcurrentHashSet<String>::add)

                            // Convert the ParallelFlux into a Flux.
                            .sequential()

                            // Block until all the processing is done.
                            .blockLast())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using a {@link ParallelFlux} that collects
     * into a series of {@link ArrayList} objects that are then merged
     * together to create a {@link Set}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    private static int runParallelFluxTest3(List<CharSequence> words) {
        return Objects
            .requireNonNull(Flux
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Convert the Flux to a ParallelFlux.
                            .parallel()

                            // Run all the rails in the parallel
                            // Scheduler.
                            .runOn(Schedulers.parallel())

                            // Transform each string to lower case.
                            .map(word ->
                                 word.toString().toLowerCase())

                            // Collect each rail into a List.
                            .collect(ArrayList<String>::new,
                                     List::add)

                            // Convert the ParallelFlux into a Flux.
                            .sequential()

                            // Concatenate all the List objects together.
                            .flatMapIterable(Function.identity())

                            // Collect the words into a Set.
                            .collect(Collectors.toSet())

                            // Block until all the processing is done.
                            .block())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Warm up the threads in the parallel thread pool so the timing
     * results will be more accurate.
     */
    private static void warmUpThreadPool() {
        System.out.println("\nWarming up the parallel thread pool\n");

        List<CharSequence> words = TestDataFactory
            .getInput(sSHAKESPEARE_DATA_FILE,
                      // Split input into "words" by ignoring
                      // whitespace.
                      sSPLIT_WORDS);

        // Create an empty list.
        List<String> list = new ArrayList<>();

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            list
                // Append the new words to the end of the list.
                .addAll(Objects
                        .requireNonNull(Flux
                                        // Convert The List into a Flux.
                                        .fromIterable(words)

                                        // Use the flatMap() concurrency idiom to map
                                        // each string to lower case using the given
                                        // Scheduler.
                                        .flatMap(word -> Mono
                                                 .fromCallable(() -> word)
                                                 .subscribeOn(Schedulers.parallel())
                                                 .map(___ ->
                                                      word.toString().toLowerCase()))

                                        // Collect unique words into a Set.
                                        .collect(Collectors.toSet())
                                        .block()));
    }
}
