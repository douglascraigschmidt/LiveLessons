import org.jsoup.nodes.Document;
import utils.Options;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import static java.util.stream.Collectors.*;

/**
 * This class counts the number of images in a recursively-defined
 * folder structure using the Java sequential stream framework.  The
 * root folder can either reside locally (filesystem -based) or
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
    private final KeySetView<Object, Boolean> mUniqueUris =
        ConcurrentHashMap.newKeySet();

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        var rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root Uri, which
        // is given an initial depth count of 1.
        var totalImages = countImages(rootUri, 1);

        print(TAG + ": " + totalImages
              + " total image(s) are reachable from "
              + rootUri);
    }

    /**
     * Main entry point into the logic for counting images
     * synchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images counted at this {@code depth}
     */
    private long countImages(String pageUri,
                             int depth) {
        // Return 0 if we've reached the depth limit of the crawling.
        if (depth > Options.instance().maxDepth()) {
            print(TAG
                  + "[Depth"
                  + depth
                  + "]: Exceeded max depth of "
                  + Options.instance().maxDepth());

            return 0;
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset, so we don't try to
        // revisit it again unnecessarily.
        else if (mUniqueUris
                 .getMap()
                 .putIfAbsent(pageUri,
                              mUniqueUris.getMappedValue()) != null) {
            print(TAG
                  + "[Depth"
                  + depth
                  + "]: Already processed "
                  + pageUri);

            // Return 0 if we've already examined this url.
            return 0;
        }

        // Synchronously (1) count the number of images on this page
        // and (2) crawl other hyperlinks accessible via this page and
        // count their images.
        else {
            long count = countImagesImpl(pageUri,
                                         depth);
            print(TAG
                  + "[Depth"
                  + depth
                  + "]: found "
                  + count
                  + " images for "
                  + pageUri
                  + " in thread "
                  + Thread.currentThread().getId());
            return count;
        }
    }

    /**
     * Helper method that performs image counting synchronously.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images encountered
     */
    private long countImagesImpl(String pageUri,
                                 int depth) {
        try {
            return Stream
                // Get a one-element stream containing the page at the
                // root URI.
                .of(getStartPage(pageUri))

                // Trigger the intermediate operations and collect the
                // results via the teeing Collector. 
                .collect(// Sum the results of counting the # of
                         // images on the page and the number of
                         // images reachable from links on this page.
                         teeing(// Synchronously count the # of images
                                // on this page.
                                mapping(this::getCountOfImagesInPage,
                                        summingLong(Long::longValue)),
                                // Synchronously count the # of images
                                // reachable from links on tha page.
                                mapping(page -> crawlLinksInPage(page, depth),
                                        summingLong(Long::longValue)),
                                // Return a count of the # of images
                                // on this page plus the # of images
                                // on hyperlinks accessible via this
                                // page.
                                Long::sum));
        } catch (Exception e) {
            print("For '"
                  + pageUri
                  + "': "
                  + e.getMessage());
            // Return 0 if an exception happens.
            return 0;
        }
    }

    /**
     * @return The page at the root {@code pageUri}
     */
    private Document getStartPage(String pageUri) {
        // Synchronously get the contents of the page.
        return Options
            .instance()
            .getJSuper()
            .getPage(pageUri);
    }

    /**
     * @return A count of all the IMG SRC URLs in this page
     */
    private long getCountOfImagesInPage(Document page) {
        // Return a count of the IMG SRC URLs in this page.
        return page.select("img").size();
    }

    /**
     * Recursively crawl through hyperlinks that are in {@code page}.
     *
     * @param page The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A count of how many images were in each hyperlink on
     *         the page
     */
    private Long crawlLinksInPage(Document page,
                                  int depth) {
        return page
            // Find all the hyperlinks on this page.
            .select("a[href]")

            // Convert the Elements to a Stream.
            .stream()

            // Count of the number of images found at that hyperlink
            // by recursively visiting all hyperlinks on this page.
            .mapToLong(hyperLink ->
                       countImages(Options
                                   .instance()
                                   .getJSuper()
                                   .getHyperLink(hyperLink),
                                   depth + 1))

            // Sum the results.
            .sum();
    }

    /**
     * Conditionally prints the {@link String} depending on the
     * current setting of the Options singleton.
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println(string);
    }
}
