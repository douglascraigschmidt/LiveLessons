import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.DownloadUtils;
import utils.Options;

import java.util.stream.Collectors;

/**
 * This class shows how to apply RxJava features to perform a range of
 * {@link Flowable} operators (such as fromIterator(), parallel(), and
 * collect()), {@link ParallelFlowable} operators (such as runOn(),
 * map(), and sequential()), and {@link Single} operators (such as
 * doOnSuccess() and ignoreElement()), as well as the Schedulers.io()
 * thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class FlowableEx {
    /**
     */
    public static Completable testParallelDownloads() {
        StringBuilder sb =
            new StringBuilder(">> Calling testParallelDownloads()\n");

        // Add some useful diagnostic output.
        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Starting parallel processing.\n");

        return Flowable
            // Convert collection into a flowable.
            .fromIterable(Options.instance().getUrlList())

            // Create a ParallelFlowable.
            .parallel()

            // Run this flow in the I/O thread pool.
            .runOn(Schedulers.io())

            // Transform each url to a file via downloadAndStoreImage,
            // which downloads each image.
            .map(DownloadUtils::downloadAndStoreImage)

            // Merge the values back into a single flowable.
            .sequential() 

            // Collect the downloaded images into a list.
            .collect(Collectors.toList())

            // Process the list.
            .doOnSuccess(imageFiles -> Options
                // Print the # of image files that were downloaded.
                .printStats("testParallelDownloads", imageFiles.size()))

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

}
