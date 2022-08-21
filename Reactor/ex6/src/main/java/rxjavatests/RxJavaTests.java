package rxjavatests;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;
import java.util.Objects;

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

}
