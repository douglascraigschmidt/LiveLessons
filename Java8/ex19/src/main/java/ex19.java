import org.jsoup.nodes.Document;
import utils.ConcurrentHashSet;
import utils.FuturesCollector;
import utils.Options;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This example shows how to count the number of images in a
 * recursively-defined folder structure using a range of
 * CompletableFuture features.  The folder can either reside locally
 * (filesystem-based) or remotely (web-based).
 */
public class ex19 {
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
     * The JVM requires a static main() entry point to run the
     * example.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Count the images.
        new ex19().countImages();
    }

    /**
     * Count all the images reachable from the root URI.
     */
    private void countImages() {
        // Get the total number of images.
        long totalImages =
            // Perform the image counting starting at the root URL,
            // which is given an initial depth count of 1.
            performCrawl(Options.instance().getRootUri(),
                         1)
            // join() blocks until all futures complete!
            .join();
                         
        printDiagnostics(TAG
                           + ": there are "
                           + totalImages
                           + " total image(s) reachable from "
                           + Options.instance().getRootUri());
    }

    /**
     * Perform the image counting asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the number of images counted
     */
    private CompletableFuture<Long> performCrawl(String pageUri,
                                                 int depth) {
        printDiagnostics(TAG
                           + ":>> Depth: " 
                           + depth 
                           + " [" 
                           + pageUri
                           + "]" 
                           + " (" 
                           + Thread.currentThread().getId() 
                           + ")");

        // Return 0 if we've reached the depth limit of the crawling.
        if (depth > Options.instance().maxDepth()) {
            printDiagnostics(TAG 
                               + ": Exceeded max depth of "
                               + Options.instance().maxDepth());

            return CompletableFuture.completedFuture(0L);
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            printDiagnostics(TAG + 
                               ": Already processed " 
                               + pageUri);

            // Return 0 if we've already examined this url.
            return CompletableFuture.completedFuture(0L);
        }

        // Use completable futures to asynchronously (1) count the
        // number of images on this page and (2) crawl other
        // hyperlinks accessible via this page and count their images.
        else {
            try {
                // The following three lambdas are used as actions in
                // the chain of completion stages below.

                // Initiate an async task to count the number of
                // images on this page and return a completable future
                // to the count.
                Function<Document, CompletableFuture<Long>> countImagesInPage =
                    page -> CompletableFuture
                    // Asynchronously count the number of images on
                    // this page.
                    .supplyAsync(() ->
                                 // This method runs synchronously, so
                                 // call it via supplyAsync().
                                 countImagesOnPage(page));

                // Initiate an async task to count the number of
                // images in hyperlinks on this page and return a
                // completable future to the count.
                Function<Document, 
                        CompletableFuture<List<Long>>> countImagesInHyperlinks =
                    page -> CompletableFuture
                    .supplyAsync(() ->
                                 // This method runs synchronously, so
                                 // call it via supplyAsync().
                                 crawlHyperLinksOnPage(page,
                                                       depth))
                    // When the future completes return its value to
                    // avoid an extra join.
                    .thenCompose(Function.identity());

                // Sum up the number of images encountered.
                BiFunction<Long, List<Long>, Long> sumCounts = (i, li) ->
                    i + li
                    // Convert list to a stream.
                    .stream()
                    
                    // Convert each entry to a long.
                    .mapToLong(Long::longValue)

                    // Sum all results in the list.
                    .sum();

                // Get a future to the page at the root URI.
                CompletableFuture<Document> pageFuture = getStartPage(pageUri);

                // The following two asynchronous method calls run
                // concurrently in the common fork-join pool.

                // After contents of the document are obtained get a
                // future to the # of images processed on this page.
                CompletableFuture<Long> imagesOnPageFuture = pageFuture
                    // thenCompose() returns a completable future to
                    // the count of URLs in this page.
                    .thenCompose(countImagesInPage);

                // Obtain a future to the number of images processed
                // on pages linked from this page.
                CompletableFuture<List<Long>> imagesOnLinksFuture = pageFuture
                    // thenCompose() returns a completable future to
                    // the list of longs that count URLs on hyperlinks
                    // in this page.
                    .thenCompose(countImagesInHyperlinks);

                // Return a completable future to the combined results
                // of the two futures params whenever they complete.
                return imagesOnPageFuture
                    // When both futures complete combine/sum results.
                    .thenCombine(imagesOnLinksFuture,
                                 sumCounts);
            } catch (Exception e) {
                printDiagnostics("For '" 
                                   + pageUri 
                                   + "': " 
                                   + e.getMessage());
                // Return 0 if an exception happens.
                return CompletableFuture.completedFuture(0L);
            }
        }
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
     * @return The number of IMG SRC URLs in this page.
     */
    private long countImagesOnPage(Document page) {
        // Return the number of IMG SRC URLs in this page.  Trigger
        // intermediate operations and return the count of the number
        // of images.
        return (long) page
                // Select all the image elements in the page.
                .select("img")

                // Convert the elements to a stream.
                .size();
    }

    /**
     * Recursively crawl through hyperlinks that are in a @a page.
     *
     * @return A completable future to an list of longs, which counts
     * how many images were in each hyperlink on the page
     */
    private CompletableFuture<List<Long>> crawlHyperLinksOnPage
        (Document page,
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
                 performCrawl(Options
                              .instance()
                              .getJSuper()
                              .getHyperLink(hyperLink),
                              depth + 1))

            // Trigger intermediate operation processing and return a
            // list of completable futures.
            .collect(FuturesCollector.toFutures());
    }

    /**
     * Conditionally prints the @a string depending on the current
     * setting of the Options singleton.
     */
    private void printDiagnostics(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println(string);
    }
}
