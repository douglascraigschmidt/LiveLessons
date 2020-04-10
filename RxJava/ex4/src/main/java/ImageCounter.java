import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.ConcurrentHashSet;
import utils.Options;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * This class concurrently counts the number of images in a
 * recursively-defined folder structure using a range of RxJava
 * features.  The root folder can either reside locally
 * (filesystem-based) or remotely (web-based).
 */
class ImageCounter {
    /**
     * Debugging tag.
     */
    private final String TAG = this.getClass().getName();

    /**
     * A cache of unique URIs that have already been processed.
     */
    private final ConcurrentHashSet<String> mUniqueUris =
        new ConcurrentHashSet<>();

    /**
     * Stores a completed observable with value of 0.
     */
    private final Single<Integer> mZero =
        Single.just(0);

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        var rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root Uri, which
        // is given an initial depth count of 1.
        countImages(rootUri, 1)
            // Use blockingSubscribe() here to ensure the main thread
            // doesn't exit prematurely.
            .blockingSubscribe(totalImages ->
                               print("(depth 0) "
                                     + totalImages
                                     + " total image(s) are reachable from "
                                     + rootUri));
    }

    /**
     * Main entry point into the logic for counting images
     * asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return An observable containing the number of images counted
     */
    private Single<Integer> countImages(String pageUri,
                                        int depth) {
        // Filter out page if it exceeds the maximum depth
        // or has already been visited.
        if (depth > Options.instance().maxDepth()) {
            print("(depth "
                  + depth
                  + ") Exceeded max depth of "
                  + Options.instance().maxDepth());
            return mZero;
        }
        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            print("(depth "
                  + depth
                  + ") Already processed "
                  + pageUri);
            return mZero;
        } else
            // Asynchronously (1) count the number of images on this
            // page and (2) crawl other hyperlinks accessible via this
            // page and count their images.
            return countImagesAsync(pageUri, depth)
                // Print this output on success.
                .doOnSuccess(totalImages ->
                             print("(depth "
                                   + depth
                                   + ") found "
                                   + totalImages
                                   + " images for "
                                   + pageUri));
    }

    /**
     * Helper method that performs image counting asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return An observable to the number of images counted
     */
    private Single<Integer> countImagesAsync(String pageUri,
                                             int depth) {
        try {
            // Get an observable to the page at the root URI.
            var pageObservable = getStartPage(pageUri);

            // Asynchronously count the # of images on this page and
            // return an observable to the count.
            var imagesInPageObservable = pageObservable
                // The getImagesInPage() method runs synchronously, so
                // call it in the common fork-join pool (see next line).
                .map(this::getImagesInPage)

                // Run the operations in the common fork-join pool.
                .compose(applySchedulers())

                // Count the number of images on this page.
                .map(List::size);

            // Asynchronously count the # of images in link on this
            // page and returns an observable to this count.
            var imagesInLinksObservable = pageObservable
                // The crawlLinksInPage() methods runs synchronously, so
                // call it in the common fork-join pool (see next line).
                .flatMap(page ->
                         crawlLinksInPage(page,
                                          depth))

                // Run the operations in the common fork-join pool.
                .compose(applySchedulers());

            // Return a count of the # of images on this page plus the
            // # of images on hyperlinks accessible via this page.
            return combineImageCounts(imagesInPageObservable,
                                      imagesInLinksObservable);
        } catch (Exception e) {
            print("For '"
                  + pageUri
                  + "': "
                  + e.getMessage());
            // Return 0 if an exception happens.
            return mZero;
        }
    }

    /**
     * Count of the # of images on this page plus the # of images on
     * hyperlinks accessible via this page.
     *
     * @param imagesInPageObservable An observable to a count of the # of
     *                           images on this page
     * @param imagesInLinksObservable An observable to a count of the # of
     *                            images in links on this page
     * @return An observable to the number of images counted
     */
    private Single<Integer> combineImageCounts
        (Single<Integer> imagesInPageObservable,
         Single<Integer> imagesInLinksObservable) {
        // Return an observer to the results of adding the two
        // observable params after they both complete.
        return Single
            // Sum the results when both observables complete.
            .zip(imagesInPageObservable,
                 imagesInLinksObservable,
                 Integer::sum);
    }

    /**
     * @return An observable to the page at the root URI
     */
    private Single<Document> getStartPage(String pageUri) {
        return Single
            // Factory method that creates an observable to download
            // the page.
            .<Document>create(s -> s
                              .onSuccess(Options
                                         .instance()
                                         .getJSuper()
                                         .getPage(pageUri)))

            // Run the operation in the common fork-join pool.
            .compose(applySchedulers());
    }

    /**
     * @return A collection of IMG SRC URLs in this page.
     */
    private Elements getImagesInPage(Document page) {
        // Return a collection IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .select("img");
    }

    /**
     * Recursively crawl through hyperlinks that are in a @a page.
     *
     * @param page The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return An observable to an integer that counts how many images
     * were in each hyperlink on the page
     */
    private Single<Integer> crawlLinksInPage(Document page,
                                             int depth) {
        // Return an observable to a list of counts of the # of nested
        // hyperlinks in the page.
        return Observable
            // Find all the hyperlinks on this page.
            .fromIterable(page.select("a[href]"))

            // Map each hyperlink to an observable containing a count
            // of the number of images found at that hyperlink.
            .flatMap(hyperLink -> Observable
                     // Just omit this one object.
                     .just(hyperLink)

                     // Run operations in the common fork-join pool.
                     .compose(applySchedulers1())

                     // Recursively visit hyperlink(s) on this url.
                     .flatMap(url ->
                              countImages(Options.instance()
                                          .getJSuper()
                                          .getHyperLink(url),
                                          depth + 1)
                              // Convert the single to an observable.
                              .toObservable()
                              ))

            // Sum all the counts.
            .reduce(Integer::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0);
    }

    /**
     * @return Schedule a single to run on the common fork-join pool.
     */
    <T> SingleTransformer<T, T> applySchedulers() {
        return single -> single
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()));
    }

    /**
     * @return Schedule an observable to run on the common fork-join
     * pool.
     */
    <T> ObservableTransformer<T, T> applySchedulers1() {
        return observable -> observable
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()));
    }

    /**
     * Conditionally prints the {@code string} depending on the
     * current setting of the Options singleton.
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println("Thread["
                               + Thread.currentThread().getId()
                               + "]: "
                               + string);
    }
}
