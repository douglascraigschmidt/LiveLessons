import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.DownloadUtils;
import utils.Options;

import java.util.stream.Collectors;

/**
 * This class shows how to apply RxJava features to download and store
 * images from remote web servers by showcasing a range of {@link
 * Flowable} operators (such as fromIterator(), parallel(), and
 * collect()), {@link ParallelFlowable} operators (such as runOn(),
 * map(), and sequential()), and {@link Single} operators (such as
 * doOnSuccess() and ignoreElement()), as well as the Schedulers.io()
 * thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class FlowableEx {
    /**
     * Use a {@link ParallelFlowable} to download and store images
     * from remote web servers in parallel.
     */
    public static Completable testParallelDownloads() {
        StringBuilder sb =
            new StringBuilder(">> Calling testParallelDownloads()\n");

        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Starting parallel processing.\n");

        return Options.instance()
            // Get a Flowable that emits URLs to download.
            .getUrlFlowable()

            // Create a ParallelFlowable.
            .parallel()

            // Run this flow in the I/O thread pool.
            .runOn(Schedulers.io())

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

            // Merge the parallel results back into a single Flowable.
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

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
