package utils;

import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * This utility class contains static methods that check whether a
 * number is prime.
 */
public final class PrimeCheckers {
    /**
     * This method provides a brute-force determination of whether
     * number primeCandidate is prime.  Returns 0 if it is prime, or
     * the smallest factor if it is not prime.
     */
    public static SimpleImmutableEntry<Long, Long> bruteForceChecker(Long primeCandidate) {
        long n = primeCandidate;

        if (n > 3)
            for (long factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    System.out.println("Prime checker thread interrupted "
                                       + Thread.currentThread());
                    break;
                } else if (n / factor * factor == n)
                    return new SimpleImmutableEntry<>(primeCandidate, factor);

        return new SimpleImmutableEntry<>(primeCandidate, 0L);
    }

    /**
     * This method provides a more efficient check whether number @a
     * primeCandidate is prime.  Returns 0 if it is prime, or the
     * smallest factor if it is not prime.
     */
    public static SimpleImmutableEntry<Long, Long> efficientChecker(Long primeCandidate) {
        long n = primeCandidate;

        // Check if n is a multiple of 2.
        if (n % 2 == 0) 
            return new SimpleImmutableEntry<>(primeCandidate, 2L);

        // If not, then just check the odds.
        for (long factor = 3;
             factor * factor <= n;
             factor += 2)
            if (Thread.interrupted()) {
                    System.out.println("Prime checker thread interrupted "
                                       + Thread.currentThread());
                    break;
            } else if (n % factor == 0)
                return new SimpleImmutableEntry<>(primeCandidate, factor);

        return new SimpleImmutableEntry<>(primeCandidate, 0L);
    }
}    
