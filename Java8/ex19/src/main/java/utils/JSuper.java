package utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

/**
 * This helper class works around some deficiencies in the Jsoup
 * library, which doesn't make web-based crawling and local filesystem
 * crawling transparent.
 */
public class JSuper {
    /**
     * Track whether we're crawling a remote (web-based) or local
     * (filesystem-based) directory structure.
     */
    private final boolean mIsLocal;

    /**
     * Constructor initializes the field.
     */
    JSuper(boolean isLocal) {
        mIsLocal = isLocal;
    }

    /**
     * @return The HTML {@link Document} associated with the {@code
     *         pageUri}
     */
    public Document getPage(String pageUri) {
        if (mIsLocal) {
            try {
                // This function (1) gets a system resource and (2)
                // converts checked exceptions to runtime exceptions.
                Function<String, URI> getUri = ExceptionUtils
                    .rethrowFunction(uri -> ClassLoader
                                     .getSystemResource(uri)
                                     .toURI());

                URI uri = getUri
                    // Apply the function to get the URI associated
                    // with the pageUri String.
                    .apply(pageUri);

                // This function (1) parses an HTML file and gets its
                // contents and (2) converts checked exceptions to
                // runtime exceptions.
                Function<File, Document> parse = ExceptionUtils
                    .rethrowFunction(rootFile -> Jsoup
                                     .parse(rootFile, "UTF-8"));
 
                return parse
                    // Apply the function to get the local HTML file.
                    .apply(new File(uri));
            } catch (Exception e) {
                System.out.println(pageUri + " got exception " + e);
                throw e;
            }
        } else {
            // This function (1) connects to a URL and gets its
            // contents and (2) converts checked exceptions to runtime
            // exceptions.
            Function<String, Document> connect = ExceptionUtils
                .rethrowFunction(url -> Jsoup
                                 .connect(url)
                                 .get());

            return connect
                // Apply the function to download the HTML page.
                .apply(pageUri);
        }
    }

    /**
     * @return The URL of the {@code image} on the given {@code
     *         pageUri}
     */
    public URL getImageUrl(Element image, String pageUri) {
        if (mIsLocal) {
            // Create a pathname.
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
                // Apply the function and return the URL of the image
                // on the given pageUri.
                .apply(image);
        } else {
            // Create a function that (1) returns a new URL and (2)
            // converts checked URL exceptions into runtime
            // exceptions.
            Function<String, URL> urlFactory = ExceptionUtils
                .rethrowFunction(URL::new);

            return urlFactory
                // Apply the function to return the URL of the image
                // on the given pageUri.
                .apply(image.attr("abs:src"));
        }
    }

    /**
     * @return A {@link String} containing the hyperlink on the 
     *         {@code page} {@link Element}
     */
    public String getHyperLink(Element page) {
        if (mIsLocal)
            return page.attr("href");
        else 
            return page.attr("abs:href");

    }
}    

