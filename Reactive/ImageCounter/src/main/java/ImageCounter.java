import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.ConcurrentHashSet;
import utils.Options;
import utils.ReactorUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * This class asynchronously and concurrently counts the number of
 * images in a recursively-defined folder structure using a range of
 * Project Reactor features, including Mono features (e.g., just(),
 * fromCallable(), blockOptional(), doOnSuccess(), map(), subscribeOn(),
 * transformDeferred(), flatMap(), zipWith(), defaultIfEmpty()), and
 * Flux features (e.g., fromIterable(), flatMap(), and reduce()).  The
 * root folder can either reside locally (filesystem-based) or remotely
 * (web-based).
 */
public class ImageCounter {
    /**
     * Debugging tag.
     */
    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getName();

    /**
     * A cache of unique URIs that have already been processed.
     */
    private final ConcurrentHashSet<String> mUniqueUris =
        new ConcurrentHashSet<>();

    /**
     * Stores a completed single with value of 0.
     */
    private static final Mono<Integer> sZero =
        Mono.just(0);

    /**
     * 10 second duration to wait for all operations to
     * complete.
     */
    private static final Duration sTIMEOUT_DURATION =
        Duration.ofSeconds(10);

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    public ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        var rootUri = Options.instance().getRootUri();

        Optional<Integer> totalImages =
            // Perform the image counting starting at the root
            // Uri, which is given an initial depth count of 1.
            countImages(rootUri, 1)

            // Block until the stream completes, encounters an
            // error, or times out.
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
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return A mono containing the number of images counted
     */
    private Mono<Integer> countImages(String pageUri,
                                      int depth) {
        // Filter out page if it exceeds the maximum depth
        // or has already been visited.
        if (depth > Options.instance().maxDepth()) {
            print("(depth "
                  + depth
                  + ") Exceeded max depth of "
                  + Options.instance().maxDepth());
            return sZero;
        }
        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            print("(depth "
                  + depth
                  + ") Already processed "
                  + pageUri);
            return sZero;
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
     * @return A mono to the number of images counted
     */
    private Mono<Integer> countImagesAsync(String pageUri,
                                           int depth) {
        try {
            // Get a mono to the page at the root URI.
            var pageMono = getStartPage(pageUri);

            // Asynchronously count the # of images on this page and
            // return a mono to the count.
            var imagesInPageMono = pageMono
                // The getImagesInPage() method runs synchronously, so
                // call it in the common fork-join pool (see next line).
                .map(this::getImagesInPage)

                // Run the operations in the common fork-join pool.
                .transformDeferred(ReactorUtils.commonPoolMono())

                // Count the number of images on this page.
                .map(List::size);

            // Asynchronously count the # of images in link on this
            // page and returns a mono to this count.
            var imagesInLinksMono = pageMono
                // The crawlLinksInPage() methods runs synchronously, so
                // call it in the common fork-join pool (see next line).
                .flatMap(page ->
                         crawlLinksInPage(page,
                                          depth))

                // Run the operations in the common fork-join pool.
                .transformDeferred(ReactorUtils.commonPoolMono());

            // Return a count of the # of images on this page plus the
            // # of images on hyperlinks accessible via this page.
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
     * Count of the # of images on this page plus the # of images on
     * hyperlinks accessible via this page.
     *
     * @param imagesInPageMono An Mono to a count of the # of
     *                           images on this page
     * @param imagesInLinksMono An Mono to a count of the # of
     *                            images in links on this page
     * @return A Mono to the total number of images counted
     */
    private Mono<Integer> combineImageCounts
        (Mono<Integer> imagesInPageMono,
         Mono<Integer> imagesInLinksMono) {
        // Return a Mono to the results of adding the two
        // Mono params after they both complete.
        return imagesInPageMono
            // Sum the results when both Monos complete.
            .zipWith(imagesInLinksMono,
                     Integer::sum);
    }

    /**
     * Factory method returns a mono to the page at the root URI
     *
     * @param pageUri URI to the start page containing HTML
     * @return A mono to the page at the root URI
     */
    private Mono<Document> getStartPage(String pageUri) {
        return Mono
            // Factory method that creates a mono to download an HTML page.
            .fromCallable(() -> Options
                .instance()
                .getJSuper()
                .getPage(pageUri))

            // Run the operation in the common fork-join pool.
            .transformDeferred(ReactorUtils.commonPoolMono());
    }

    /**
     * Factory method that returns a collection of IMG SRC URLs in this page
     *
     * @param page An HTML page that may contain embedded images
     * @return A collection of IMG SRC URLs in this page
     */
    private Elements getImagesInPage(Document page) {
        // Return a collection IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .select("img");
    }

    /**
     * Recursively crawl through hyperlinks that are in a {@code page}.
     *
     * @param page An HTML page that may contain embedded hyperlinks
     * @param depth The depth of the level of web page traversal
     * @return A mono to an integer that counts how many images
     * were in each hyperlink on the page
     */
    private Mono<Integer> crawlLinksInPage(Document page,
                                           int depth) {
        // Return a mono to a list of counts of the # of nested
        // hyperlinks in the page.
        return Flux
            // Find all the hyperlinks on this page.
            .fromIterable(page.select("a[href]"))

            // Use the flapMap() concurrency idiom to process each hyperlink
            // to a mono containing a count of the number of images found at
            // that hyperlink.
            .flatMap(hyperLink -> Mono
                     // Emit just this one object.
                     .fromCallable(() -> hyperLink)

                     // Run operations in the common fork-join pool.
                     .transformDeferred(ReactorUtils.commonPoolMono())

                     // Recursively visit hyperlink(s) on this url.
                     .flatMap(url ->
                              countImages(Options.instance()
                                          .getJSuper()
                                          .getHyperLink(url),
                                          depth + 1)))

            // Sum all the counts (if any).
            .reduce(Integer::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0);
    }

    /**
     * Conditionally prints the {@code string} depending on the
     * current setting of the Options singleton.
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
