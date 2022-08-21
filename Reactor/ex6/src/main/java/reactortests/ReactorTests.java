package reactortests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import utils.ConcurrentHashSet;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * This class contains Project Reactor tests.
 */
public class ReactorTests {
    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a sequential {@link Flux}-based
     * implementation.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runSequential(List<CharSequence> words,
                                    List<CharSequence> commonWords) {
        return Objects
            .requireNonNull(Flux
                            // Convert the List into a Flux.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom to
                            // map each string to lower case using the
                            // given Scheduler.
                            .map(word ->
                                 // Map each word to lower case.
                                 word.toString().toLowerCase())

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

                            // Collect unique words into a Set.
                            .collect(toSet())

                            // Wait until all computations are done. 
                            .block())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using the {@code flatMap()} concurrency
     * idiom and the {@code Mono.just} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runFlatMapTestJust(List<CharSequence> words,
                                         List<CharSequence> commonWords) {
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
                                          word.toString().toLowerCase())
                                          
                                     // Filter out common words.
                                     .filter(lowerCaseWord ->
                                             !commonWords.contains(lowerCaseWord)))

                            // Collect unique words into a Set.
                            .collect(toSet())

                            // Wait until all computations are done. 
                            .block())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using the {@code flatMap()} concurrency
     * idiom and the {@code Mono.fromCallable} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runFlatMapTestFromCallable(List<CharSequence> words,
                                                 List<CharSequence> commonWords) {
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
                                          word.toString().toLowerCase())

                                     // Filter out common words.
                                     .filter(lowerCaseWord ->
                                             !commonWords.contains(lowerCaseWord)))

                            // Collect unique words into a Set.
                            .collect(toSet())

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
    public static int runParallelFluxTest1(List<CharSequence> words,
                                           List<CharSequence> commonWords) {
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

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

                            // Convert the ParallelFlux back to a Flux.
                            .sequential()

                            // Collect the words into a Set.
                            .collect(toSet())

                            // Block until all the processing is done.
                            .block())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a {@link ParallelFlux} that collects
     * into a single {@link ConcurrentHashSet}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallelFluxTest2(List<CharSequence> words,
                                           List<CharSequence> commonWords) {
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

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

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
    public static int runParallelFluxTest3(List<CharSequence> words,
                                           List<CharSequence> commonWords) {
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

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

                            // Collect each rail into a Set.
                            .collect(HashSet<String>::new,
                                     Set::add)

                            // Convert the ParallelFlux into a Flux.
                            .sequential()

                            // Concatenate all the Set objects together.
                            .flatMapIterable(Function.identity())

                            // Collect the words into a Set.
                            .collect(toSet())

                            // Block until all the processing is done.
                            .block())
            
            // Return the number of unique words in this input.
            .size();
    }
}
