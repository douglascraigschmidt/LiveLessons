import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import utils.Options;
import utils.ReactorUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * This class asynchronously and concurrently counts the number of
 * images in a recursively-defined folder structure using a range of
 * Project Reactor features, including Mono features (e.g., just(),
 * fromCallable(), blockOptional(), doOnSuccess(), subscribeOn(),
 * map(), flatMap(), zipWith(), transformDeferred(),
 * defaultIfEmpty()), and Flux features (e.g., fromIterable(),
 * flatMap(), and reduce()).  The root folder can either reside
 * locally (filesystem-based) or remotely (web-based).
 */
public class ImageCounter {
    /**
     * Debugging tag.
     */
    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getName();

    /**
     * A thread-safe cache of URIs that have already been processed.
     */
    private final KeySetView<String, Boolean> mUniqueUris =
        ConcurrentHashMap.newKeySet();

    /**
     * Stores a completed Mono with value of 0.
     */
    private static final Mono<Integer> sZero =
        Mono.just(0);

    /**
     * 10 second duration to wait for all operations to complete.
     */
    private static final Duration sTIMEOUT_DURATION =
        Duration.ofSeconds(10);

    /**
     * Default constructor (used for testing).
     */
    public ImageCounter() {}

    /**
     * Count all the images reachable from the root URI.
     *
     * @param rootUri The root URI at the page/folder being traversed
     */
    public ImageCounter(String rootUri) {
        Optional<Integer> totalImages =
            // Perform the image counting starting at the root URI,
            // which is given an initial depth count of 1.
            countImages(rootUri, 1)

            // Block until the stream completes, encounters an error,
            // or times out.
            .blockOptional(sTIMEOUT_DURATION);

        // Print the final results of the traversal.
        print("(depth 0) "
              + totalImages.orElse(0)
              + " total image(s) are reachable from "
              + rootUri);
    }

    /**
     * Main entry point into the logic for counting images
     * asynchronously.
     *
     * @param pageUri The URI being counted at this point
     * @param depth The current depth of the recursive processing
     * @return A Mono that emits the number of images counted starting
     *         from {@code pageUri}
     */
    public Mono<Integer> countImages(String pageUri,
                                     int depth) {
        // Stop traversing if the current depth in recursion
        // exceeds the maximum depth.
        if (depth > Options.instance().maxDepth()) {
            print("(depth "
                  + depth
                  + ") Exceeded max depth of "
                  + Options.instance().maxDepth());
            return sZero;
        }
        // Atomically check to see if we've already visited pageUri
        // and if not add it to the hashset to avoid revisiting
        // it again unnecessarily.
        else if (mUniqueUris
                 .getMap()
                 .putIfAbsent(pageUri,
                              mUniqueUris.getMappedValue()) != null) {
            print("(depth "
                  + depth
                  + ") Already processed "
                  + pageUri);
            return sZero;
        } else
            // Asynchronously (1) count the number of images at this
            // pageUri and (2) recursively crawl other hyperlinks
            // accessible via this pageUri and count their images.
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
     * @param pageUri The URI being counted at this point
     * @param depth The current depth of the recursive processing
     * @return A Mono that emits the number of images counted starting
     *         from {@code pageUri}
     */
    private Mono<Integer> countImagesAsync(String pageUri,
                                           int depth) {
        try {
            // Asynchronously get a Mono to the page at pageUri.
            var pageMono = getStartPage(pageUri);

            // Asynchronously count the # of images on this page and
            // return a Mono that emits the count.
            var imagesInPageMono = pageMono
                // getImagesInPage() runs synchronously, so call it in
                // the common fork-join pool (see next operator).
                .map(this::getImagesInPage)

                // Run the operations in the common fork-join pool.
                .transformDeferred(ReactorUtils.commonPoolMono())

                // Count the number of images on this page.
                .map(List::size);

            // Asynchronously count the # of images accessible via links
            // on this page and return a Mono that emits this count.
            var imagesInLinksMono = pageMono
                // crawlLinksInPage() runs synchronously, so call it
                // in the common fork-join pool (see next operator).
                .flatMap(page ->
                         crawlLinksInPage(page, depth))

                // Run the operations in the common fork-join pool.
                .transformDeferred(ReactorUtils.commonPoolMono());

            // Return a Mono that emits a count of the # of images on
            // this page plus the # of images on hyperlinks accessible
            // via this page.
            return combineImageCounts(imagesInPageMono,
                                      imagesInLinksMono);
        } catch (Exception e) {
            print("For '"
                  + pageUri
                  + "': "
                  + e.getMessage());
            // Return 0 if an exception happens.
            return sZero;
        }
    }

    /**
     * Count the # of images on this page plus the # of images on
     * hyperlinks accessible via this page.
     *
     * @param imagesInPageM A Mono that emits a count of the # of
     *                      images on this page
     * @param imagesInLinksM A Mono that emits a count of the # of
     *                       images in links on this page
     * @return A Mono that emits the total number of images counted
     */
    private Mono<Integer> combineImageCounts(Mono<Integer> imagesInPageM,
                                             Mono<Integer> imagesInLinksM) {
        // Return a Mono that emits the results of adding the two Mono
        // params after they both complete their async processing.
        return imagesInPageM
            // Sum the results when both Monos complete.
            .zipWith(imagesInLinksM,
                     Integer::sum);
    }

    /**
     * Factory method returns a Mono that emits the HTML page rooted
     * at {@code pageUri}.
     *
     * @param pageUri A Uri to the start page containing HTML
     * @return A Mono that emits the HTML page at {@code pageUri}
     */
    private Mono<Document> getStartPage(String pageUri) {
        return Mono
            // Factory method that downloads an HTML page
            // asynchronously (see next operator).
            .fromCallable(() -> Options
                .instance()
                .getJSuper()
                .getPage(pageUri))

            // Run the operation in the common fork-join pool.
            .transformDeferred(ReactorUtils.commonPoolMono());
    }

    /**
     * Factory method that returns a collection containing any IMG SRC
     * URLs located in this HTML {@code page}.
     *
     * @param page An HTML page that may contain embedded images
     * @return A collection of IMG SRC URLs in this page
     */
    private Elements getImagesInPage(Document page) {
        // Return a collection containing any IMG SRC URLs found on
        // this page.
        return page
            // Select all the image elements in the page.
            .select("img");
    }

    /**
     * Recursively crawl through any hyperlinks that are accessible on
     * the {@code page}.
     *
     * @param page An HTML page that may contain embedded hyperlinks
     * @param depth The depth of the level of web page traversal
     * @return A Mono that emits the total number of images
     *         found at each hyperlink on the {@code page}
     */
    private Mono<Integer> crawlLinksInPage(Document page,
                                           int depth) {
        // Return a Mono that emits a count of the # of hyperlinks
        // accessible on the page.
        return Flux
            // Find all hyperlinks on this page.
            .fromIterable(page.select("a[href]"))

            // Use the flapMap() concurrency idiom to process each
            // hyperlink to a Mono containing a count of the number of
            // images found at that hyperlink.
            .flatMap(hyperLink -> Mono
                     // Emit this hyperlink.
                     .fromCallable(() -> hyperLink)

                     // Run operations in the common fork-join pool.
                     .transformDeferred(ReactorUtils.commonPoolMono())

                     // De-nest the results.
                     .flatMap(url ->
                              // Recursively visit hyperlink(s) on
                              // this uri.
                              countImages(Options.instance()
                                          .getJSuper()
                                          .getHyperLink(url),
                                          depth + 1)))

            // Sum all the counts (if any).
            .reduce(Integer::sum)

            // Return 0 if there were no hyperlinks.
            .defaultIfEmpty(0);
    }

    /**
     * Conditionally prints the {@code string} depending on the
     * current setting of the Options singleton.  The current
     * thread Id is prepended in front of the string.
     *
     * @param string The string to print
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println("Thread["
                               + Thread.currentThread().getId()
                               + "]: "
                               + string);
    }
}
