import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.DownloadUtils;
import utils.Options;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static utils.BigFractionUtils.logBigFraction;
import static utils.BigFractionUtils.sBigReducedFraction;

/**
 * This class demonstrates how to use various {@link Flux}, {@link
 * ParallelFlux}, and {@link Mono} operators to perform BigFraction
 * multiplications and additions in parallel, as well as download and
 * store images from remote web servers in parallel.  The {@link Flux}
 * operators include fromArray(), parallel(), doOnComplete(), and
 * collect().  The {@link ParallelFlux} operators include runOn(),
 * map(), doOnNext(), reduce(), sequential().  The {@link Mono}
 * operators include doOnSuccess() and then().
 */
@SuppressWarnings("ALL")
public class ParallelFluxEx {
    /**
     * Use a {@link ParallelFlux} and a pool of threads to perform
     * {@link BigFraction} multiplications in parallel.
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // Create an array of reduced BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(1000, 30),
            BigFraction.valueOf(1000, 40),
            BigFraction.valueOf(1000, 20),
            BigFraction.valueOf(1000, 10)
        };

        var multiplied = Flux
            // Emit a stream of reduced big fractions.
            .fromArray(bigFractionArray)

            // Convert the Flux to a ParallelFlux.
            .parallel()

            // Run subsequent processing in the parallel Scheduler pool.
            .runOn(Schedulers.parallel())

            // Use a ParallelFlux to multiply these BigFraction
            // objects in parallel Scheduler pool.
            .map(bf -> bf
                 .multiply(sBigReducedFraction))

            // Convert the ParallelFlux to a Flux.
            .sequential();

            // Return a Mono<Void> to synchronize with the
            // AsyncTaskBarrier framework.
        return BigFractionUtils
            .displayResults(multiplied,
                            sBigReducedFraction,
                            Flux.fromArray(bigFractionArray),
                            sb);
    }

    /**
     * Use a {@link ParallelFlux} and a pool of threads to perform
     * {@link BigFraction} multiplications and additions in parallel.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications2()\n");

        // Create an array of reduced BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(1000, 30),
            BigFraction.valueOf(1000, 40),
            BigFraction.valueOf(1000, 20),
            BigFraction.valueOf(1000, 10)
        };

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("["
                      + Thread.currentThread().getId()
                      + "] sum of BigFractions = "
                      + result
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Flux
            // Emit a stream of reduced big fractions.
            .fromArray(bigFractionArray)

            // Convert the Flux to a ParallelFlux.
            .parallel()

            // Run subsequent processing in the parallel Scheduler pool.
            .runOn(Schedulers.parallel())

            // Use a ParallelFlux to multiply these BigFraction
            // objects in parallel Scheduler pool.
            .map(bf -> bf
                 .multiply(sBigReducedFraction))

            // Log the BigFractions.
            .doOnNext(bf ->
                      logBigFraction(bf, sBigReducedFraction, sb))

            // Reduce all values within a 'rail' and across 'rails'
            // via BigFraction::add into a Flux sequence with one
            // element.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Mono<Void> to synchronize with the
            // AsyncTaskBarrier framework.
            .then(); 
    }

    /**
     * Use a {@link ParallelFLux} to download and store images from
     * remote web servers in parallel.
     */
    public static Mono<Void> testParallelDownloads() {
        StringBuffer sb =
            new StringBuffer(">> Calling testParallelDownloads()\n");

        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Starting parallel processing.\n");

        return Options.instance()
            // Get a Flux that emits URLs to download.
            .getUrlFlux(Options.instance().maxImages())

            // Create a ParallelFlux.
            .parallel()

            // Run this flow in the I/O thread pool.
            .runOn(Schedulers.boundedElastic())

            // Transform each url to a file via downloadAndStoreImage,
            // which downloads each image from a remote web server and
            // stores it on the local computer.
            .map(DownloadUtils::downloadAndStoreImage)

            // Record information about each downloaded image.
            .doOnNext(file -> sb
                      .append("["
                              + Thread.currentThread().getId()
                              + "] "
                              + "downloaded file "
                              + file.getName()
                              + " of size "
                              + file.length()
                              + "\n"))

            // Convert the ParallelFlux to a Flux.
            .sequential()

            // Print information about downloaded images.
            .doOnComplete(() -> System.out.println(sb))

            // Collect the downloaded images into a List.
            .collect(Collectors.toList())

            // Process the List on success.
            .doOnSuccess(imageFiles -> Options
                // Print the # of image files that were downloaded.
                .printStats("testParallelDownloads",
                            imageFiles.size()))

            // Return a Mono<Void> to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }
}
