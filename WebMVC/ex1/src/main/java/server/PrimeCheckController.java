package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import utils.Options;

import java.net.http.HttpResponse;
import java.util.List;

import static common.Constants.EndPoint.CHECK_IF_PRIME;
import static common.Constants.EndPoint.CHECK_IF_PRIME_LIST;

/**
 * This Spring controller demonstrates how WebMVC can be used to
 * handle HTTP GET requests via Java parallel streams programming.
 * These requests are mapped to methods that determine the primality
 * of large random {@link Integer} objects.
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
 * requests onto methods in the {@link PrimeCheckController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser or
 * client app) or command-line utility (e.g., Curl or Postman).
 *
 * The {@code @ResponseBody} annotation tells a controller that the
 * object returned is automatically serialized into JSON and passed
 * back within the body of an {@link HttpResponse} object.
 */
@RestController
@ResponseBody
public class PrimeCheckController {
    /**
     * This auto-wired field connects the {@link
     * PrimeCheckController} to the {@link
     * PrimeCheckService}.
     */
    @Autowired
    PrimeCheckService mService;

    /**
     * Checks the {@code primeCandidate} param for primality,
     * returning 0 if it's prime or the smallest factor if it's not.
     *
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * CHECK_IF_PRIME} endpoint to this method.
     *
     * @param primeCandidate The {@link Integer} to check for
     *                       primality
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    @GetMapping(CHECK_IF_PRIME)
    public Integer checkIfPrime(Integer primeCandidate) {
        Options.debug("checkIfPrime()");

        return mService
            // Forward to the service.
            .checkIfPrime(primeCandidate);
    }

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * List} param for primality and return a corresponding {@link
     * List} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * CHECK_IF_PRIME_LIST} endpoint to this method.
     *
     * @param primeCandidates The {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if primality checking should run in
     *                 parallel, else false if it should run sequentially
     * @return An {@link List} whose elements are 0 if the
     *         corresponding element in {@code primeCandidate} is
     *         prime or its smallest factor if it's not prime
     */
    @GetMapping(CHECK_IF_PRIME_LIST)
    public List<Integer> checkIfPrimeList(@RequestParam List<Integer> primeCandidates,
                                          Boolean parallel) {
        Options.debug("checkIfPrimeList()");

        return mService
            // Forward to the service.
            .checkIfPrimeList(primeCandidates,
                              parallel);
    }
}
