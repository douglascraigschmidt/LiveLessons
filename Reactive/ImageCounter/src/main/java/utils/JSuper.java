package utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

/**
 * This wrapper class works around deficiencies in the Jsoup library,
 * which lacks the ability to make web-based crawling and local
 * filesystem crawling transparent to clients.
 */
public class JSuper {
    /**
     * Keeps track of whether we're crawling a remote (web-based) or
     * local (filesystem-based) directory structure.
     */
    private final boolean mIsLocal;

    /**
     * Constructor initializes the field.
     */
    JSuper(boolean isLocal) {
        mIsLocal = isLocal;
    }

    /**
     * @return The HTML {@link Document} associated with the {@code pageUri}
     */
    public Document getPage(String pageUri) {
        // Determine whether to go local or remote to access the HTML
        // file.
        if (mIsLocal) {
            try {
                // This function (1) gets a system resource and (2)
                // converts checked exceptions to runtime exceptions.
                Function<String, URI> getUri = ExceptionUtils
                    .rethrowFunction(uri
                                     -> ClassLoader.getSystemResource(uri)
                                     .toURI());

                URI uri = getUri
                    // Convert to a Uri.
                    .apply(pageUri);

                // This function (1) parses an HTML file and gets its
                // contents and (2) converts checked exceptions to
                // runtime exceptions.
                Function<File, Document> parse =
                    ExceptionUtils
                    .rethrowFunction(rootFile -> Jsoup
                                     .parse(rootFile, "UTF-8"));
 
                return parse
                    // Return the contents of the HTML file accessed
                    // via the local filesystem.
                    .apply(new File(uri));
            } catch (Exception e) {
                System.out.println(pageUri + " got exception " + e);
                throw e;
            }
        } else {
            // This function (1) connects to a URL and gets its
            // contents and (2) converts checked exceptions to runtime
            // exceptions.
            Function<String, Document> connect =
                ExceptionUtils.rethrowFunction(url
                                               -> Jsoup.connect(url).get());

            return connect
                // Return the contents of the HTML file downloaded
                // from the web.
                .apply(pageUri);
        }
    }

    /**
     * @return The Uri of the {@code image} on the given {@code
     * pageUri}
     */
    public URL getImageUri(Element image, String pageUri) {
        // Determine whether to go local or remote to access the HTML
        // file.
        if (mIsLocal) {
            int splitPos = pageUri.lastIndexOf('/');

            final String prefix = splitPos > 0
                ? pageUri.substring(0, splitPos) + "/"
                : "";

            // This function (1) gets a system resource and (2)
            // converts checked exceptions to runtime exceptions.
            Function<Element, URL> getUrl = ExceptionUtils
                .rethrowFunction(img -> ClassLoader
                                 .getSystemResource(prefix
                                                    + img.attr("src")));

            return getUrl
                // Return the Uri of the image on the given pageUri.
                .apply(image);
        } else {
            // Create a function that (1) returns a new URL and (2)
            // converts checked URL exceptions into runtime
            // exceptions.
            Function<String, URL> urlFactory =
                ExceptionUtils.rethrowFunction(URL::new);

            return urlFactory
                // Return the Uri of the image on the given pageUri.
                .apply(image.attr("abs:src"));
        }
    }

    /**
     * @return A string containing the hyperlink on the {@code page}
     * element
     */
    public String getHyperLink(Element page) {
        // Determine which format to use to access the hyperlink.
        if (mIsLocal)
            return page.attr("href");
        else 
            return page.attr("abs:href");

    }
}    

