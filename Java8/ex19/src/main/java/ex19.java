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
 * This example shows how to count the number of images in a
 * recursively-defined folder structure using a range of
 * CompletableFuture features.  The root folder can either reside
 * locally (filesystem-based) or remotely (web-based).
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
        new ex19();
    }

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    private ex19() {
        // Get the URI to the root of the page/folder being traversed.
        String rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root URL, which
        // is given an initial depth count of 1.
        countImages(rootUri, 1)

        // Get the total number of images.
        .thenAccept(totalImages ->
                    print(TAG
                          + ": there are "
                          + totalImages
                          + " total image(s) reachable from "
                          + rootUri))

        // join() blocks until all futures complete!
        .join();                         
    }

    /**
     * Perform image counting asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the number of images counted
     */
    private CompletableFuture<Integer> countImages(String pageUri,
                                                   int depth) {
        print(TAG
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
            print(TAG 
                  + ": Exceeded max depth of "
                  + Options.instance().maxDepth());

            return CompletableFuture.completedFuture(0);
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            print(TAG + 
                  ": Already processed " 
                  + pageUri);

            // Return 0 if we've already examined this url.
            return CompletableFuture.completedFuture(0);
        }

        // Use completable futures to asynchronously (1) count the
        // number of images on this page and (2) crawl other
        // hyperlinks accessible via this page and count their images.
        else 
            return countImagesAsync(pageUri,
                                    depth);
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
            // The following three lambdas are used as actions in the
            // chain of completion stages below.

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
            // links on this page and returns a future to the count.
            Function<Document,
                     CompletableFuture<List<Integer>>> imagesInLinks =
                page -> CompletableFuture
                .supplyAsync(() ->
                             // This method runs synchronously, so
                             // call it via supplyAsync().
                             crawlLinksInPage(page,
                                                   depth))
                // When the future completes return its value to
                // avoid an extra join.
                .thenCompose(Function.identity());

            // Sum up the number of images encountered.
            BiFunction<Integer, List<Integer>, Integer> sumCounts = (i, li) ->
                i + li
                // Convert list to a stream.
                .stream()
                    
                // Convert each entry to an int.
                .mapToInt(Integer::intValue)

                // Sum all results in the list.
                .sum();

            return countImagesMapReduce(pageUri,
                                        imagesInPage,
                                        imagesInLinks,
                                        sumCounts);
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
     * Helper method that performs image counting asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param imagesInPage A function that asynchronously counts the number of
     *                     images on this page and return a future to the count
     * @param imagesInLinks A function that asynchronously counts # of images in
     *                      links on this page and returns a future to the count
     * @param sumCounts A bifunction that sums up the number of images encountered
     * @return A future to the number of images counted
     */
    private CompletableFuture<Integer> countImagesMapReduce
        (String pageUri,
         Function<Document, CompletableFuture<Integer>> imagesInPage,
         Function<Document, CompletableFuture<List<Integer>>> imagesInLinks,
         BiFunction<Integer, List<Integer>, Integer> sumCounts) {
        // Get a future to the page at the root URI.
        CompletableFuture<Document> pageFuture = 
            getStartPage(pageUri);

        // The following two asynchronous method calls run
        // concurrently in the common fork-join pool.

        // After contents of the document are obtained get a
        // future to the # of images processed on this page.
        CompletableFuture<Integer> imagesInPageFuture = pageFuture
            // thenCompose() returns a completable future to
            // the count of URLs in this page.
            .thenCompose(imagesInPage);

        // Obtain a future to the number of images processed
        // on pages linked from this page.
        CompletableFuture<List<Integer>> imagesInLinksFuture = pageFuture
            // thenCompose() returns a completable future to
            // the list of longs that count URLs on hyperlinks
            // in this page.
            .thenCompose(imagesInLinks);

        // Return a completable future to the combined results
        // of the two futures params whenever they complete.
        return imagesInPageFuture
            // When both futures complete combine/sum results.
            .thenCombine(imagesInLinksFuture,
                         sumCounts);
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
     * @return A completable future to an list of longs, which counts
     * how many images were in each hyperlink on the page
     */
    private CompletableFuture<List<Integer>> crawlLinksInPage
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
                 countImages(Options
                              .instance()
                              .getJSuper()
                              .getHyperLink(hyperLink),
                              depth + 1))

            // Trigger intermediate operation processing and return a
            // list of completable futures.
            .collect(FuturesCollector.toFuture());
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
