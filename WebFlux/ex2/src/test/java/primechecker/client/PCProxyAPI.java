package primechecker.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME_FLUX;

/**
 * This interface provides the contract for the RESTful {@code
 * PCServerController} API.  It defines the HTTP GET and POST methods
 * that can be used to interact with the {@code PCServerController}
 * API, along with the expected request and response parameters for
 * each method.
 *
 * This interface uses Spring HTTP interface annotations that provide
 * metadata about the API, such as the type of HTTP request (i.e.,
 * {@code GET} or {@code POST}), the parameter types (which are
 * annotated with {@code GetExchange}, {@code PostExchange},
 * {@code @RequestPath}, {@code RequestBody}, or {@code RequestParam}
 * tags), and the expected response format.  HTTP interface uses these
 * annotations and method signatures to generate an implementation of
 * the interface that the client uses to make HTTP requests to the
 * API.
 */
public interface PCProxyAPI {
    /**
     * Checks the {@code primeCandidate} param for primality,
     * returning 0 if it's prime or the smallest factor if it's not.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param primeCandidate The {@link Integer} to check for
     *                       primality
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    @GetExchange(CHECK_IF_PRIME)
    Integer checkIfPrime(@RequestParam Integer strategy,
                         @RequestParam Integer primeCandidate);

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * Flux} param for primality and return a corresponding {@link
     * Flux} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     * 
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param primeCandidates The {@link Flux} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link Flux} emitting elements that are 0 if the
     *         corresponding element in {@code primeCandidate} is
     *         prime or its smallest factor if it's not prime
     */
    @PostExchange(value = CHECK_IF_PRIME_FLUX,
                  // Enables passing Flux as a param.
                  contentType = APPLICATION_NDJSON_VALUE)
    Flux<Integer> checkIfPrimeFlux
        (@RequestParam Integer strategy,
         @RequestBody Flux<Integer> primeCandidates);
}
