 
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.FuturesCollectorIntStream;
import utils.Options;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class counts the number of images in a recursively-defined
 * folder structure using a range of asynchronous features in the Java
 * completable futures framework.  The root folder can either reside
 * locally (filesystem -based) or remotely (web-based).
 */
class ImageCounter {
    /**
     * Debugging tag.
     */
    private final String TAG = this.getClass().getName();

    /**
     * A cache of unique URIs that have already been processed.
     */
    private final KeySetView<String, Boolean> mUniqueUris =
        ConcurrentHashMap.newKeySet();

    /**
     * Stores a completed future with value of 0.
     */
    private final CompletableFuture<Integer> mZero = 
        CompletableFuture.completedFuture(0);

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        var rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root Uri, which
        // is given an initial depth count of 1.
        countImages(rootUri, 1)

            // Handle outcome of previous stage by converting any
            // exceptions into 0 and printing the total # of images.
            .handle((totalImages, ex) -> {
                    // Something's gone wrong here!
                    if (ex != null)
                        totalImages = 0;

                    print(TAG + ": " + totalImages
                          + " total image(s) are reachable from "
                          + rootUri);

                    return 0;
                })

            /*
            // Handle any exception that occurred.
            .exceptionally(ex -> 0) // Indicate no images were counted due to the exception.

            // When the future completes print the total number of images.
            .thenAccept(totalImages ->
            print(TAG
            + ": " 
            + totalImages
            + " total image(s) are reachable from "
            + rootUri))
            */

            // join() blocks until all futures complete!
            .join();
    }

    /**
     * Main entry point into the logic for counting images
     * asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the number of images counted
     */
    private CompletableFuture<Integer> countImages(String pageUri,
                                                   int depth) {
        // Return 0 if we've reached the depth limit of the crawling.
        if (depth > Options.instance().maxDepth()) {
            print(TAG 
                  + "[Depth"
                  + depth
                  + "]: Exceeded max depth of "
                  + Options.instance().maxDepth());

            return mZero;
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset to avoid revisiting it
        // again unnecessarily.
        else if (!mUniqueUris.add(pageUri)) {
            print(TAG 
                  + "[Depth"
                  + depth
                  + "]: Already processed "
                  + pageUri);

            // Return 0 if we've already examined this url.
            return mZero;
        }

        // Use completable futures to asynchronously (1) count the
        // number of images on this page and (2) crawl other
        // hyperlinks accessible via this page and count their images.
        else 
            return countImagesAsync(pageUri,
                                    depth)
                // Printout what's happened, regardless of whether an
                // exception occurred or not.
                .whenComplete((totalImages, ex) -> {
                        if (totalImages != null)
                            print(TAG
                                  + "[Depth"
                                  + depth
                                  + "]: found "
                                  + totalImages
                                  + " images for "
                                  + pageUri
                                  + " in thread " 
                                  + Thread.currentThread().getId());
                        else 
                            print(TAG + ": exception " + ex.getMessage());
                    });

    }

    /**
     * Helper method that performs image counting asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A {@link CompletableFuture} to the number of images counted
     */
    private CompletableFuture<Integer> countImagesAsync(String pageUri,
                                                        int depth) {
        return Stream
            // Use a factory method to create a one-element stream
            // containing just the pageUri.
            .of(pageUri)

            // Get a future to the page at the root URI.
            .map(this::getStartPage)

            // Asynchronously process the page.
            .map(cf ->
                 // Return a count of the # of images on this page plus the
                 // # of images on hyperlinks accessible via this page.
                 combineImageCounts(// Asynchronously count the # of
                                    // images on this page and return
                                    // a future to the count.
                                    countImagesInPageAsync(cf),

                                    // Asynchronously count the # of
                                    // images in link on this page and
                                    // returns a future to this count.
                                    crawlLinksInPageAsync(cf,
                                                    depth + 1))

                 // Handle any exception that occurs.
                 .handle((count, ex) -> {
                         print("For '" 
                               + pageUri 
                               + "': " 
                               + ex.getMessage());
                         // Return 0 if an exception happens.
                         return 0;
                     }))

            // Use a terminal operation to get the total number of
            // images from the one-element stream.
            .findFirst()

            // If something goes wrong return mZero.
            .orElse(mZero);
    }

    /**
     * Asynchronously count all the images in the page associated with
     * {@code pageFuture} after it is triggered.
     *
     * @param pageFuture A {@link CompletableFuture} to the page being
     *                   downloaded
     * @return A {@link CompletableFuture} to an {@link Integer}
     *         containing the # of images on this page
     */
    protected CompletableFuture<Integer>
        countImagesInPageAsync(CompletableFuture<Document> pageFuture) {
        // Return a CompletableFuture to an Integer containing the #
        // of images processed on this page.
        return pageFuture
            // Asynchronously get the array of image URLs to process
            // on this page.
            .thenApplyAsync(this::getImagesInPage)

            // Count the number of images on this page.
            .thenApply(List::size);
    }

    /**
     * Asynchronously obtain a {@link CompletableFuture} to the # of
     * images on pages linked from this page.
     *
     * @param pageFuture A {@link CompletableFuture} to the page
     *                   that's being downloaded
     * @param depth      The current depth of the recursive processing
     * @return A {@link CompletableFuture} to an {@link Integer}
     *         containing the # of images on pages linked from this
     *         page
     */
    protected CompletableFuture<Integer>
        crawlLinksInPageAsync(CompletableFuture<Document> pageFuture,
                              int depth) {
        // Return a CompletableFuture to an Integer containing the #
        // of images processed on pages linked from this page.

        return pageFuture
            // Asynchronously/recursively crawl all hyperlinks in a
            // page.
            .thenComposeAsync(page ->
                              // This method is synchronous, so it's
                              // called via thenComposeAsync().
                              crawlLinksInPage(page, depth));
    }

    /**
     * Asynchronously count of the # of images on this page plus the #
     * of images on hyperlinks accessible via this page.
     *
     * @param imagesInPageFuture A {@link CompletableFuture} to a count
     *                           of the # of images on this page
     * @param imagesInLinksFuture A {@link CompletableFuture} to a count
     *                            of the # of images in links on this page
     * @return A {@link CompletableFuture} to the number of images counted
     */
    private CompletableFuture<Integer> combineImageCounts
        (CompletableFuture<Integer> imagesInPageFuture,
         CompletableFuture<Integer> imagesInLinksFuture) {

        // Return a completable future to the results of adding the
        // two futures params after they both complete.
        return imagesInPageFuture
            // Sum the results when both futures complete.
            .thenCombine(imagesInLinksFuture,
                         Integer::sum);
    }

    /**
     * @return A {@link CompletableFuture} to the page at the {@code pageURI}
     */
    private CompletableFuture<Document> getStartPage(String pageUri) {
        return CompletableFuture
            // Asynchronously get the contents of the page.
            .supplyAsync(() -> Options
                         .instance()
                         .getJSuper()
                         .getPage(pageUri));
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
     * Recursively crawl through hyperlinks that are in {@code page}.
     *
     * @param page The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A completable future to an integer that counts how many
     * images were in each hyperlink on the page
     */
    private CompletableFuture<Integer> crawlLinksInPage(Document page,
                                                        int depth) {
        // Return a completable future to a list of counts of the # of
        // nested hyperlinks in the page.
        return page
            // Find all the hyperlinks on this page.
            .select("a[href]")

            // Convert the hyperlink elements into a stream.
            .stream()

            // Map each hyperlink to a completable future containing a
            // count of the number of images found at that hyperlink.
            .map(hyperLink ->
                 // Recursively visit all the hyperlinks on this page.
                 countImages(Options
                             .instance()
                             .getJSuper()
                             .getHyperLink(hyperLink),
                             depth + 1))

            // Trigger intermediate operation processing and return a
            // future to a CompletableFutures<IntStream>.
            .collect(FuturesCollectorIntStream.toFuture())

            // After all the futures in the stream complete then sum
            // all the integers in the stream of results.
            .thenApply(IntStream::sum);
    }

    /**
     * Conditionally prints the {@link String} depending on the current
     * setting of the {@link Options} singleton.
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println(string);
    }
}
