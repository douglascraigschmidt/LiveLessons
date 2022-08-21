package rxjavatests;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.ConcurrentHashSet;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * This class contains RxJava tests.
 */
public class RxJavaTests{
    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using the {@code flatMap()} concurrency
     * idiom and the {@code Observable.just} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runFlatMapTestJust(List<CharSequence> words,
                                         List<CharSequence> commonWords) {
        return Objects
            .requireNonNull(Observable
                            // Convert The List into a Flux.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom to
                            // map each string to lower case using the
                            // given Scheduler.
                            .flatMap(word -> Observable
                                     // Emit the word in the assembly
                                     // thread.
                                     .just(word)

                                     // Run each computation in the
                                     // parallel thread pool.
                                     .subscribeOn(Schedulers.computation())

                                     // Map each word to lower case.
                                     .map(___ ->
                                          word.toString().toLowerCase())
                                          
                                     // Filter out common words.
                                     .filter(lowerCaseWord ->
                                             !commonWords.contains(lowerCaseWord)))

                            // Collect unique words into a Set.
                            .collect(toSet())

                            // Wait until all computations are done. 
                            .blockingGet())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using the {@code flatMap()} concurrency
     * idiom and the {@code Observable.fromCallable} operator.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runFlatMapTestFromCallable(List<CharSequence> words,
                                                 List<CharSequence> commonWords) {
        return Objects
            .requireNonNull(Observable
                            // Convert The List into an Observable.
                            .fromIterable(words)

                            // Use the flatMap() concurrency idiom
                            // to map each string to lower case
                            // using the given Scheduler.
                            .flatMap(word -> Observable
                                     // Emit the word in a thread from
                                     // the parallel thread pool.
                                     .fromCallable(() -> word)

                                     // Run each computation in the
                                     // parallel thread pool.
                                     .subscribeOn(Schedulers.computation())

                                     // Map each word to lower case.
                                     .map(___ ->
                                          word.toString().toLowerCase())

                                     // Filter out common words.
                                     .filter(lowerCaseWord ->
                                             !commonWords.contains(lowerCaseWord)))

                            // Collect unique words into a Set.
                            .collect(toSet())

                            // Wait until all computations are done. 
                            .blockingGet())

            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using the canonical means of collecting
     * results from a {@link ParallelFlowable} into a {@link Set}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallelFlowableTest1(List<CharSequence> words,
                                           List<CharSequence> commonWords) {
        return Objects
            .requireNonNull(Flowable
                            // Convert The List into a Flowable.
                            .fromIterable(words)

                            // Convert the Flowable to a ParallelFlowable.
                            .parallel()

                            // Run all the rails in the parallel Scheduler.
                            .runOn(Schedulers.computation())

                            // Transform each string to lower case.
                            .map(word ->
                                 word.toString().toLowerCase())

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

                            // Convert the ParallelFlowable back to a Flowable.
                            .sequential()

                            // Collect the words into a Set.
                            .collect(toSet())

                            // Block until all the processing is done.
                            .blockingGet())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeare's works using a {@link ParallelFlowable} that collects
     * into a single {@link ConcurrentHashSet}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallelFlowableTest2(List<CharSequence> words,
                                           List<CharSequence> commonWords) {
        var set = new ConcurrentHashSet<String>();

        return Objects
            .requireNonNull(Flowable
                            // Convert The List into a Flowable.
                            .fromIterable(words)

                            // Convert the Flowable to a ParallelFlowable.
                            .parallel()

                            // Run all the rails in the parallel Scheduler.
                            .runOn(Schedulers.computation())

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

                            // Convert the ParallelFlowable into a Flowable.
                            .sequential()

                            // Block until all the processing is done.
                            .blockingLast())
            
            // Return the number of unique words in this input.
            .size();
    }

    /**
     * Compute the number of unique words in a portion of
     * Shakespeares' works using a {@link ParallelFlowable} that collects
     * into a series of {@link ArrayList} objects that are then merged
     * together to create a {@link Set}.
     *
     * @param words A {@link List} of words to lowercase
     * @return The number of unique words in this portion of
     *         Shakespeare's works
     */
    public static int runParallelFlowableTest3(List<CharSequence> words,
                                               List<CharSequence> commonWords) {
        return Objects
            .requireNonNull(Flowable
                            // Convert The List into a Flowable.
                            .fromIterable(words)

                            // Convert the Flowable to a ParallelFlowable.
                            .parallel()

                            // Run all the rails in the parallel
                            // Scheduler.
                            .runOn(Schedulers.computation())

                            // Transform each string to lower case.
                            .map(word ->
                                 word.toString().toLowerCase())

                            // Filter out common words.
                            .filter(lowerCaseWord ->
                                    !commonWords.contains(lowerCaseWord))

                            // Collect each rail into a Set.
                            .collect(HashSet<String>::new,
                                     Set::add)

                            // Convert the ParallelFlowable into a Flowable.
                            .sequential()

                            // Concatenate all the Set objects together.
                            .flatMapIterable(x -> x)

                            // Collect the words into a Set.
                            .collect(toSet())

                            // Block until all the processing is done.
                            .blockingGet())
            
            // Return the number of unique words in this input.
            .size();
    }
}
