import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

/**
 * This example shows two zap*() method implementations that remove
 * a string from a list of strings.  One method uses traditional Java 7
 * features and the other uses basic modern Java features.
 */
public class ex0 {
    /**
     *
     */
    private static final long sMAX_COUNT = 100;

    static public void main(String[] argv) {
        // The array of names.
        String[] nameArray = {
            "Barbara",
            "James",
            "Mary",
            "John",
            "Robert",
            "Michael",
            "Linda",
            "james",
            "mary"
        };

        // Remove "Robert" from the list created from nameArray.
        List<String> l1 = zap7(List.of(nameArray),
                               "Robert");

        // Remove "Robert" from the list created from nameArray.
        List<String> l2 = zapModern(List.of(nameArray),
                                    "Robert");

        // Check to ensure the zap*() methods work.
        if (l1.contains("Robert") 
            || l2.contains("Robert"))
            System.out.println("Test failed");
        else
            System.out.println("Test succeeded");

        // This method checks {@code sMAX_COUNT} odd random numbers
        // and prints which are prime and which are not.
        checkRandomNumbersForPrimes();
    }        

    /**
     * Remove any strings matching {@code omit} from the list of
     * strings using basic Java 7 features.
     */
    static List<String> zap7(List<String> lines,
                             String omit) {
        // Create an array list return result.
        List<String> res = 
            new ArrayList<>(); 

        // Iterate through all the lines in the list and remove any
        // that match omit.
        for (String line : lines) 
            if (!omit.equals(line))
                res.add(line);   

        // Return the list.
        return res; 
    }

    /**
     * Remove any strings matching {@code omit} from the list of
     * strings using basic modern Java features.
     */
    static List<String> zapModern(List<String> lines,
                                  String omit) {
        return lines
            // Convert the list to a stream.
            .stream()

            // Remove any strings that match omit.
            .filter(not(omit::equals))

            // Trigger intermediate operation processing and return
            // new list of results.
            .collect(toList());
    }

    /**
     * Define a Java record that holds the "plain old data" (POD) for the
     * result of a primality check.
     */
    public record PrimeResult(/*
                               * Value that was evaluated for primality.
                               */
                              int primeCandidate,

                              /*
                               * Result of the isPrime() method.
                               */
                              int smallestFactor) {}

    /**
     * This method checks {@code sMAX_COUNT} odd random numbers
     * and prints which are prime and which are not.
     */
    static void checkRandomNumbersForPrimes() {
        new Random()
            // Generate a stream of random positive ints.
            .ints(1, Integer.MAX_VALUE)

            // Only allow odd numbers.
            .filter(ex0::isOdd)

            // Check each random number to see if it's prime.
            .mapToObj(ex0::checkIfPrime)

            // Limit the stream to sMAX_COUNT items.
            .limit(sMAX_COUNT)

            // Print the results.
            .forEach(ex0::printResult);
    }

    /**
     * This method returns true if the {@code integer} param
     * is an odd number, else false.
     *
     * @param integer The parameter to check for oddness
     * @return true if the {@code integer} param
     *         is an odd number, else false
     */
    private static boolean isOdd(int integer) {
        // Use the bit-wise or operator to determine if
        // 'integer' is odd or not.
        return (integer & 1) == 1;
    }

    /**
     * Check if {@code primeCandidate} is prime or not.
     * 
     * @param primeCandidate The number to check for primality
     * @return A {@link PrimeResult} record that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    private static PrimeResult checkIfPrime(int primeCandidate) {
        // Return a record containing the prime candidate and the
        // result of checking if it's prime.
        return new PrimeResult(primeCandidate,
                               isPrime(primeCandidate));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.
     *
     * @return 0 if it is prime or the smallest factor if it is not prime
     */
    private static Integer isPrime(int primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            // This "brute force" algorithm is intentionally
            // inefficient to burn lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (n / factor * factor == n)
                    // primeCandidate was not prime.
                    return factor;

        // primeCandidate was prime.
        return 0;
    }

    /**
     * Handle the result by printing it if debugging is enabled.
     *
     * @param result The result of checking if a number is prime
     */
    private static void printResult(PrimeResult result) {
        // Print the results.
        if (result.smallestFactor() != 0) {
            System.out.println(result.primeCandidate()
                               + " is not prime with smallest factor "
                               + result.smallestFactor());
        } else {
            System.out.println(result.primeCandidate()
                               + " is prime");
        }
    }
}

