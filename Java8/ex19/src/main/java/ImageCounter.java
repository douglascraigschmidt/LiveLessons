 
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

        this
            // Perform the image counting starting at the root Uri,
            // which is given an initial depth count of 1.
            .countImagesAsync(rootUri, 1)

            // Handle outcome of previous stage by converting any
            // exceptions into 0 and printing the total # of images.
            .handle((totalImages, ex) -> {
                    // Something's gone wrong here!
                    if (ex != null)
                        totalImages = 0;

                    print(1,
                          ": " + totalImages
                          + " total image(s) are reachable from "
                          + rootUri);

                    return 0;
                })

            // join() blocks until all futures complete!
            .join();

            /*
             * Here's another way to handle exceptions:

            // Handle any exception that occurred by indicating no
            // images were counted due to the exception.
            .exceptionally(ex -> 0)

            // When the future completes print the total number of images.
            .thenAccept(totalImages ->
                        print(1,
                              ": " 
                              + totalImages
                              + " total image(s) are reachable from "
                              + root Uri))
            */
    }

    /**
     * Main entry point into the logic for counting images
     * asynchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A {@link CompletableFuture} that emits the number of
     *         images counted
     */
    private CompletableFuture<Integer> countImagesAsync(String pageUri,
                                                        int depth) {
        return Stream
            // Use a factory method to create a one-element stream
            // containing just the pageUri.
            .of(pageUri)

            // Check to ensure that pageUri does not exceed the max
            // depth or has already been visited.
            .filter (___ -> passChecks(pageUri, depth))

            // Get a CompletableFuture to the Document at pageUri.
            .map(this::getStartPage)

            // Process the Document.
            .map(pageFuture -> this
                 // Asynchronously (1) count the number of images on
                 // this page and (2) crawl other hyperlinks
                 // accessible via this page and count their images.
                 .countImagesOnPageAndPageLinksAsync(pageFuture,
                                                     pageUri,
                                                     depth))

            // Use a terminal operation to get a CompletableFuture
            // that emits the total number of processed images from
            // the one-element stream.
            .findFirst()
            .orElse(mZero);
    }

    /**
     * Performs image counting asynchronously.
     *
     * @param pageFuture A {@link CompletableFuture} that emits a
     *                   {@link Document} when triggered
     * @param depth The current depth of the recursive processing
     * @return A {@link CompletableFuture} that emits the number of
     *         images counted
     */
    private CompletableFuture<Integer> countImagesOnPageAndPageLinksAsync
        (CompletableFuture<Document> pageFuture,
         String pageUri,
         int depth) {
        return
            // Return a count of the # of images on this page plus the
            // # of images on hyperlinks accessible via this page.
            combineImageCounts(this
                               // Asynchronously count the # of images
                               // on this page and return a future to
                               // the count.
                               .countImagesInPageAsync(pageFuture)

                               // Log what's happened, regardless of
                               // whether an exception occurred or
                               // not.
                               .whenComplete((totalImages, ex) ->
                                             logResults(totalImages,
                                                        ex,
                                                        pageUri,
                                                        depth)),

                               // Asynchronously count the # of images
                               // in link on this page and returns a
                               // future to this count.
                               crawlLinksInPageAsync(pageFuture,
                                                     depth + 1));
    }

    /**
     * Return a {@link CompletableFuture} that emits a count of the
     * image objects in the page associated with {@code pageFuture}
     * after it is triggered.
     *
     * @param pageFuture A {@link CompletableFuture} to the page being
     *                   downloaded
     * @return A {@link CompletableFuture} that emits the number of
     *         images on this page
     */
    protected CompletableFuture<Integer>
        countImagesInPageAsync(CompletableFuture<Document> pageFuture) {
        // Return a CompletableFuture to an Integer containing the #
        // of images processed on this page.
        return pageFuture
            // Asynchronously get the collection of image URLs to
            // process on this page.
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
     * @return A {@link CompletableFuture} that emits the # of images
     *         on pages linked from this page
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
     * @return A {@link CompletableFuture} that emits the number of
     *         images counted
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
     * @param page The {@link Document} containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A {@link CompletableFuture} that emits how many
     *         images were in each hyperlink on this page
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
                 countImagesAsync(Options
                                  .instance()
                                  .getJSuper()
                                  .getHyperLink(hyperLink),
                                  depth))

            // Trigger intermediate operation processing and return a
            // future to a CompletableFutures<IntStream>.
            .collect(FuturesCollectorIntStream.toFuture())

            // After all the futures in the stream complete then sum
            // all the integers in the stream of results.
            .thenApply(IntStream::sum);
    }

    /**
     * Check to ensure that {@code pageUri} does not exceed the max
     * depth or has already been visited.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return {@code true} if the checks pass, else {@code false}
     */
    private boolean passChecks(String pageUri,
                               int depth) {
        // Return false if we've reached the depth limit of the
        // crawling.
        if (depth > Options.instance().maxDepth()) {
            print(depth,
                  ": Exceeded max depth of "
                  + Options.instance().maxDepth()
                  + " "
                  + pageUri);
            return false;
        }
        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset and return false to
        // avoid revisiting it again unnecessarily.
        else if (!mUniqueUris.add(pageUri)) {
            print(depth,
                  ": Already processed "
                  + pageUri);
            return false;
        } else
            return true;
    }

    /**
     * Log the results, regardless of whether an exception occurred or
     * not.
     *
     * @param totalImages The total number of images at this level
     *                    (this param may be null if an exception was
     *                    thrown)
     * @param ex The exception that occurred (this param may be null
     *           if no exception was thrown)
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     */
    private void logResults(Integer totalImages,
                            Throwable ex,
                            String pageUri,
                            int depth) {
        if (totalImages != null)
            print(depth,
                  ": found "
                  + totalImages
                  + " images "
                  + pageUri);
        else
            print(depth,
                  ": exception " 
                  + ex.getMessage());
    }

    /**
     * Conditionally prints the {@link String} depending on the current
     * setting of the {@link Options} singleton.
     */
    private void print(int depth, String string) {
        if (Options.instance().getDiagnosticsEnabled()) {
            String s = "[thr "
                + Thread.currentThread().getId()
                + ", depth " 
                + depth 
                + "]"
                + string;
            System.out.println(s);
        }
    }
}
