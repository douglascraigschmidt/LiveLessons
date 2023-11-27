package livelessons.utils;

import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.parallel.ParallelTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.ForkJoinPool;

/**
 * A utility class containing helpful methods for manipulating various
 * RxJava features.
 */
public class RxUtils {
    /**
     * A utility class should always define a private constructor.
     */
    private RxUtils() {
    }


    /**
     * @return Schedule a single to run on the common fork-join pool.
     */
    public static <T> SingleTransformer<T, T> commonPoolSingle() {
        return single -> single
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()));
    }

    /**
     * @return Schedule a parallel flowable to run on the common
     * fork-join pool.
     */
    public static <T> ParallelTransformer<T, T> commonPoolFlowable() {
        return flowable -> flowable
            .runOn(Schedulers.from(ForkJoinPool.commonPool()));
    }

    /**
     * @return Schedule an observable to run on the common fork-join
     * pool.
     */
    public static <T> ObservableTransformer<T, T> commonPoolObservable() {
        return observable -> observable
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()));
    }
}
