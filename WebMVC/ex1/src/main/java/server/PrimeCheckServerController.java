package server;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import static common.Constants.EndPoint.CHECK_IF_PRIME;
import static common.Constants.EndPoint.CHECK_IF_PRIME_LIST;
import static java.util.stream.Collectors.toList;

/**
 * This Spring controller demonstrates how WebMVC can be used to
 * handle HTTP GET requests via Java parallel streams programming.
 * These requests are mapped to methods that determine the primality
 * of large integers.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the {@code @RestController} annotation below.
 *
 * WebMVC uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@code ServerController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser or
 * client app) or command-line utility (e.g., Curl or Postman).
 *
 * The {@code @ResponseBody} annotation tells a controller that the
 * object returned is automatically serialized into JSON and passed
 * back within the body of HttpResponse object.
 */
@RestController
@ResponseBody
public class PrimeCheckServerController {
    /**
     * This method determines
     *
     * WebFlux maps HTTP GET requests sent to the /_start endpoint to
     * this method.
     *
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    @GetMapping(CHECK_IF_PRIME)
    public Integer checkIfPrime(Integer primeCandidate) {
        System.out.println("checkIfPrime()");
        return isPrime(primeCandidate);
    }

    @GetMapping(CHECK_IF_PRIME_LIST)
    public List<Integer> checkIfPrimeList(List<Integer> primeCandidates,
                                          Boolean parallel) {
        System.out.println("checkIfPrimeList()");

        var stream = primeCandidates
            .stream();

        if (parallel)
            stream.parallel();

        var results = stream

            .map(this::isPrime)

            .collect(toList());

        /*
        results
            .forEach(System.out::println);
         */

        return results;
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    private Integer isPrime(Integer primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    // Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }
}
