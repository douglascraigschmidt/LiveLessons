import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.ConcurrentHashSet;
import utils.FuturesCollector;
import utils.Options;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class counts the number of images in a recursively-defined
 * folder structure using a range of CompletableFuture features.  The
 * root folder can either reside locally (filesystem-based) or
 * remotely (web-based).
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
     * Stores a completed future with value of 0.
     */
    private CompletableFuture<Integer> mZero = 
        CompletableFuture.completedFuture(0);

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    public ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        String rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root Uri, which
        // is given an initial depth count of 1.
        countImages(rootUri, 1)

        // Handle outcome of previous stage by converting any
        // exceptions into 0 and printing the total # of images.
        .handle((totalImages, ex) -> {
            if (totalImages == null)
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
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
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
     * @return A future to the number of images counted
     */
    private CompletableFuture<Integer> countImagesAsync(String pageUri,
                                                        int depth) {
        try {
            // The following three lambdas are passed to the
            // countImagesMapReduce() method below.

            // This function asynchronously counts the number of
            // images on this page and return a future to the count.
            Function<Document, CompletableFuture<Integer>> imagesInPage =
                page -> CompletableFuture
                // Asynchronously get a collection of IMG SRC URLs in this page.
                .supplyAsync(() ->
                             // This method runs synchronously, so
                             // call it via supplyAsync().
                             getImagesOnPage(page))
                // Asynchronously count the number of images on
                // this page.
                .thenApply(Elements::size);

            // A function that asynchronously counts # of images in
            // links on this page and returns a future to this count.
            Function<Document,
                     CompletableFuture<Integer>> imagesInLinks =
                page -> CompletableFuture
                .supplyAsync(() ->
                             // This call runs synchronously, so call
                             // supplyAsync() to avoid blocking.
                             crawlLinksInPage(page,
                                                   depth))
                // When the future completes return its value to
                // avoid an extra join.
                .thenCompose(Function.identity());

            // Asynchronously count the number of images on this page
            // and (2) crawls other hyperlinks accessible via this
            // page and counts their images.
            return countImagesMapReduce(pageUri,
                                        imagesInPage,
                                        imagesInLinks);
        } catch (Exception e) {
            print("For '" 
                  + pageUri 
                  + "': " 
                  + e.getMessage());
            // Return 0 if an exception happens.
            return CompletableFuture.completedFuture(0);
        }
    }

    /**
     * Asynchronously count the number of images on this page and
     * crawl links accessible on this page to count their images.
     *
     * @param pageUri The URL that we're counting at this point
     * @param imagesInPage A function that asynchronously counts the number of
     *                     images on this page and return a future to the count
     * @param imagesInLinks A function that asynchronously counts # of images in
     *                      links on this page and returns a future to the count
     * @return A future to the number of images counted
     */
    private CompletableFuture<Integer> countImagesMapReduce
        (String pageUri,
         Function<Document, CompletableFuture<Integer>> imagesInPage,
         Function<Document, CompletableFuture<Integer>> imagesInLinks) {
        // Get a future to the page at the root URI.
        CompletableFuture<Document> pageFuture = 
            getStartPage(pageUri);

        // The following two asynchronous method calls run
        // concurrently in the common fork-join pool.

        // After contents of the document are obtained get a future to
        // the number of images processed on this page.
        CompletableFuture<Integer> imagesInPageFuture = pageFuture
            // thenCompose() returns a completable future to
            // the count of URLs in this page.
            .thenCompose(imagesInPage);

        // Obtain a future to the number of images processed
        // on pages linked from this page.
        CompletableFuture<Integer> imagesInLinksFuture = pageFuture
            // thenCompose() returns a completable future to the count
            // of URLs reachable via hyperlinks in this page.
            .thenCompose(imagesInLinks);

        // Return a completable future to the results of adding the
        // two futures params after they both complete.
        return imagesInPageFuture
            // When both futures complete add the results.
            .thenCombine(imagesInLinksFuture,
                         (imgsInPage, imgsInLinks)
                          -> imgsInPage + imgsInLinks);
    }

    /**
     * @return A future to the page at the root URI
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
    private Elements getImagesOnPage(Document page) {
        // Return a collection IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .select("img");
    }

    /**
     * Recursively crawl through hyperlinks that are in a @a page.
     *
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
            // future to a list of completable futures.
            .collect(FuturesCollector.toFuture())

            // After all the futures in the list complete then sum all
            // the integers in the list of results.
            .thenApply(list -> list
                       // Convert list to a stream.
                       .stream()

                       // Sum all results in the list.
                       .reduce(0, Integer::sum));
    }

    /**
     * Conditionally prints the @a string depending on the current
     * setting of the Options singleton.
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println(string);
    }
}
