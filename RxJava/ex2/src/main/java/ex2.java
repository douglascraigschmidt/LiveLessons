import tests.ReactorTests;
import tests.RxJavaTests;
import tests.StreamsTests;
import utils.DownloadUtils;
import utils.Options;
import utils.RunTimer;

import java.util.concurrent.ForkJoinPool;

/**
 * This example shows how to download many images from a remote web
 * server via several different Java concurrency/parallelism
 * frameworks, including the parallel streams, RxJava, and Project
 * Reactor.  It also compares the performance of the Java parallel
 * streams framework with and without the {@code
 * ForkJoinPool.ManagedBlocker} interface and the Java common
 * fork-join pool.
 */
public class ex2 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex2.class.getName();

    /**
     * The Java execution environment requires a static {@code main()}
     * entry point method to run the app.
     */
    public static void main(String[] args) {
        System.out.println("Entering the download tests program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Warm up the common fork-join pool.
        warmUpThreadPool();

        // Run all the Streams tests.
        runStreamsTests();

        // Run all the Project Reactor tests.
        runReactorTests();

        // Run all the RxJava tests.
        runRxJavaTests();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the download tests program");
    }

    /**
     * Run all the Project Reactor tests.
     */
    static private void runReactorTests() {
        // Run the tests using Project Reactor's flatMap() parallelism
        // mechanism along with the {@link BlockingTask} wrapper for
        // the Java fork-join framework's {@link ManagedBlocker}
        // mechanism, which adds new worker threads to the pool
        // adaptively when blocking on I/O occurs.

        ReactorTests.runFlatMap
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehaviorReactorflatMap[CFJP]()",
             reactor.core.scheduler.Schedulers.fromExecutor(ForkJoinPool.commonPool()),
             Options.instance().loggingEnabled());

        ReactorTests.runFlatMap
            (DownloadUtils::downloadAndStoreImage,
             "testDefaultDownloadBehaviorReactorflatMap[parallel]()",
             reactor.core.scheduler.Schedulers.parallel(),
             Options.instance().loggingEnabled());

        // Run tests using Reactor's {@link ParallelFlux} mechanism
        // along with the {@link BlockingTask} wrapper for Java
        // fork-join's {@link ManagedBlocker} mechanism, which adds
        // new worker threads to pool adaptively when blocking on I/O
        // occurs.
        ReactorTests.runParallelFlux
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehaviorReactorParallelFlux[all cores, CFJP]()",
             Runtime.getRuntime().availableProcessors(),
             reactor.core.scheduler.Schedulers.fromExecutor(ForkJoinPool.commonPool()),
             Options.instance().loggingEnabled());

        ReactorTests.runParallelFlux
            (DownloadUtils::downloadAndStoreImage,
             "testDefaultDownloadBehaviorReactorParallelFlux[all cores, parallel]()",
             Runtime.getRuntime().availableProcessors(),
             reactor.core.scheduler.Schedulers.parallel(),
             Options.instance().loggingEnabled());

        ReactorTests.runParallelFlux
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehaviorReactorParallelFlux[1 core, CFJP]()",
             1,
             reactor.core.scheduler.Schedulers.fromExecutor(ForkJoinPool.commonPool()),
             Options.instance().loggingEnabled());
    }

    /**
     * Run all the RxJava tests.
     */
    private static void runRxJavaTests() {
        // Run the tests using RxJava's flatMap() parallelism
        // mechanism along with the {@link BlockingTask} wrapper for
        // the Java fork-join framework's {@link ManagedBlocker}
        // mechanism, which adds new worker threads to the pool
        // adaptively when blocking on I/O occurs.
        RxJavaTests.runFlatMap
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehaviorRxflatMap()",
             io.reactivex.rxjava3.schedulers.Schedulers.io());

        // Run the tests using RxJava's ParallelFlowable mechanism
        // along with the {@link BlockingTask} wrapper for the Java
        // fork-join framework's {@link ManagedBlocker} mechanism,
        // which adds new worker threads to the pool adaptively when
        // blocking on I/O occurs.
        RxJavaTests.runParallelFlowable
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehaviorRxParallelFlowable()");
    }

    /**
     * Run all the Java parallel streams tests.
     */
    private static void runStreamsTests() {
        // Runs the tests using the using the Java fork-join
        // framework's default behavior, which does not add any new
        // worker threads to the pool when blocking on I/O occurs.
        StreamsTests.runParallelStreams
            (DownloadUtils::downloadAndStoreImage,
             "testDefaultDownloadBehavior()",
             Options.instance().loggingEnabled());

        // Run the tests using the using the Java fork-join
        // framework's {@link ManagedBlocker} mechanism, which adds
        // new worker threads to the pool adaptively when blocking on
        // I/O occurs.
        StreamsTests.runParallelStreams
            (DownloadUtils::downloadAndStoreImageMB,
             "testAdaptiveMBDownloadBehavior()",
             Options.instance().loggingEnabled());

        // Run the tests using the using the {@link BlockingTask}
        // wrapper for the Java fork-join framework's {@link
        // ManagedBlocker} mechanism, which adds new worker threads to
        // the pool adaptively when blocking on I/O occurs.
        StreamsTests.runParallelStreams
            (DownloadUtils::downloadAndStoreImageBT,
             "testAdaptiveBTDownloadBehavior()",
             Options.instance().loggingEnabled());
    }

    /**
     * This method warms up the default thread pool.
     */
    private static void warmUpThreadPool() {
        StreamsTests.testDownloadStreams
            (DownloadUtils::downloadAndStoreImage,
             "warmup");
    }
}
