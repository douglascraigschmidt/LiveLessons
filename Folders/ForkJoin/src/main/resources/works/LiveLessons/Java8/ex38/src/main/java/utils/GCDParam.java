package utils;

import java.util.Comparator;

/**
 * Define a Java record that holds the "plain old data" (POD) to pass
 * as the params to the greatest common divisor (GCD) method.
 */
public record GCDParam
(
 /*
  * The first integer to process.
  */
 int first,

 /*
  * The second integer to process.
  */
 int second) implements Comparable<GCDParam> {
    /**
     * @return 0 if the params are equal, < 0 if the first param is
     * less than the second, and > 0 if the first param is greater
     * than the second
     */
    @Override
    public int compareTo(GCDParam rhs) {
        // Subtract the second element from the first element.
        int r1 = this.first - rhs.first;

        // If the first comparison != 0 then return the result.
        if (r1 != 0)
            return r1;
        else
            // Continue on to compare the second elements.
            return this.second - rhs.second;
    }
}

