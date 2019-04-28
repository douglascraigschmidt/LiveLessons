package edu.vandy.main;

import edu.vandy.utils.Options;
import edu.vandy.utils.SimpleSet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.jsoup.select.Selector.select;

/**
 * This class sequentially counts the number of images in a
 * recursively-defined folder structure.  The root folder can either
 * reside locally (filesystem-based) or remotely (web-based).
 */
class ImageCounter {
    /**
     * Debugging tag.
     */
    private final String TAG = this.getClass().getName();

    /**
     * A cache of unique URIs that have already been processed.
     */
    private final Set<String> mUniqueUris =
        new SimpleSet<>();

    /**
     * Constructor counts all the images reachable from the root URI.
     */
    ImageCounter() {
        // Get the URI to the root of the page/folder being traversed.
        String rootUri = Options.instance().getRootUri();

        // Perform the image counting starting at the root Uri, which
        // is given an initial depth count of 1.
        int totalImages = countImages(rootUri, 1);

        print(TAG
              + ": there are "
              + totalImages
              + " total image(s) reachable from "
              + rootUri);
    }

    /**
     * Main entry point into the logic for counting images
     * sequentially.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images counted
     */
    private int countImages(String pageUri,
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

            return 0;
        }

        // Atomically check to see if we've already visited the
        // pageUri so we don't try to revisit it again unnecessarily.
        else if (mUniqueUris.contains(pageUri)) {
            print(TAG + 
                  ": Already processed " 
                  + pageUri);

            // Return 0 if we've already examined this url.
            return 0;
        }

        // The pageUri hasn't been visited yet.
        else {
            // Add the new url to the set.
            mUniqueUris.add(pageUri);

            // Return a count of the number of images on this page
            // plus the number of images reachable from other links
            // accessible via this page.
            return countImagesImpl(pageUri,
                                   depth);
        }
    }

    /**
     * Helper method that performs image counting.
     *
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images counted
     */
    private int countImagesImpl(String pageUri,
                                int depth) {
        try {
            Document page = getStartPage(pageUri);

            // Count the number of images in this page.
            int imagesInPage = getImagesOnPage(page).size();

            List<Integer> listOfImagesInLinks =
                // Return a list containing the number of images
                // accessible via links in this page.
                crawlLinksInPage(page,
                                 depth);

            // Count the number of images accessible via links in this page.
            for (int count : listOfImagesInLinks)
                imagesInPage += count;

            // Return the number of images on this page plus the
            // number of images accessible via links in this page.
            return imagesInPage;
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
     * @return The page at the root URI
     */
    private Document getStartPage(String pageUri) {
        return Options
            .instance()
            .getJSuper()
            .getPage(pageUri);
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
     * @return A list of longs, which counts how many images were in
     * each hyperlink on the page
     */
    private List<Integer> crawlLinksInPage(Document page,
                                           int depth) {
        List<Integer> results = new ArrayList<>();

        // Loop through all the hyperlinks on this page.
        for (Element hyperLink : page.select("a[href]"))
            // Recursively visit all the hyperlinks on this page.
            results.add(countImages(Options
                                    .instance()
                                    .getJSuper()
                                    .getHyperLink(hyperLink),
                                    depth + 1));
                
        // Return a list of counts of the # of nested hyperlinks in
        // the page.
        return results;
    }
    
    /**
     * Conditionally prints the @a string depending on the current
     * setting of the Options singleton.
     */
    private void print(String string) {
        if (Options.instance().getDiagnosticsEnabled())
            System.out.println(string);
    }

    /**
     * Prints out all the Uris that were visited.
     */
    void printUris() {
        if (Options.instance().getDiagnosticsEnabled()) {
            System.out.println("\nUris visited during the crawl:");

            // Printout all the Uris in the set.
            for (Iterator<String> iter = mUniqueUris.iterator();
                 iter.hasNext();
                 )
                System.out.println(iter.next());
        }
    }
}
