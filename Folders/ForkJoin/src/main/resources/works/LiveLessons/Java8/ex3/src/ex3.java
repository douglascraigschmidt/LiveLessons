import java.util.*;
import java.util.function.Function;

/**
 * This example shows how Function objects can be composed together
 * via andThen().
 */
public class ex3 {
    /**
     * This simple class contains methods that can be used to build an
     * HTML tag by adding '<' and '>' symbols.
     */
    static public class HtmlTagMaker {
        /**
         * Prepends the '<' symbol before {@code text}.
         */
        static String addLessThan(String text) {
            return "<" + text;
        }

        /**
         * Appends the '>' symbol after {@code text}.
         */
        static String addGreaterThan(String text) {
            return text + ">";
        }
    }

   static public void main(String[] argv) {
       // Create a simple pipeline that builds an HTML tag.
       Function<String, String> lessThan = HtmlTagMaker::addLessThan;
       Function<String, String> tagger = lessThan
           .andThen(HtmlTagMaker::addGreaterThan);

       // Apply the tagger pipeline multiple times to create a simple
       // HTML document.
       String html = tagger.apply("HTML")
               + tagger.apply("BODY")
               + tagger.apply("/BODY")
               + tagger.apply("/HTML");

       // Print the results.
       System.out.println(html);
   }
}

