import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This example shows how the Java regular expressions can be encoded
 * and decoded so they can be sent and received as part of a URL.
 */
public class ex46 {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Show how to encode and decode a regular expression string
        // so it can be sent and received as part of a URL.

        String splitWords ="[\\t\\n\\x0B\\f\\r'!()\"#&-.,;0-9:@<>\\[\\]}_|? ]+";
        String bePhrase = "\\bbe\\b.*(true|false)";

        showEncodingAndDecoding(splitWords);
        showEncodingAndDecoding(bePhrase);
    }

    /**
     * Show how to encode and decode a regular expression string so it
     * can be sent and received as part of a URL.
     */
    private static void showEncodingAndDecoding(String regex) {
        // Encode the regex so it can be passed as a URL.
        var encodedRegex = URLEncoder
            .encode(regex,
                    StandardCharsets.UTF_8);

        // Decode the encoded regex so it can be used as a String.
        var decodedRegex = URLDecoder
            .decode(encodedRegex,
                    StandardCharsets.UTF_8);

        // Print the results of encoding and decoding.
        System.out.println("Original regex = "
                           + regex);
        System.out.println("Encoded regex = "
                           + encodedRegex);
        System.out.println("Decoded regex = "
                           + decodedRegex);
    }
}
