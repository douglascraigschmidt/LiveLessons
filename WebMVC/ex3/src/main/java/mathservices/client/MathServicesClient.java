package mathservices.client;

import mathservices.common.GCDResult;
import mathservices.common.PrimeResult;
import mathservices.server.gcd.GCDController;
import mathservices.server.primality.PrimalityController;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link GCDController} and {@link
 * PrimalityController} web services to compute the Greatest Common
 * Divisor (GCD) and determine the primality of a {@link List} of
 * large {@link Integer} objects.  These invocations are made in
 * parallel using the Java structured concurrency framework.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class MathServicesClient {
    /**
     * This auto-wired field connects the {@link MathServicesClient}
     * to the {@link GCDProxy} that performs HTTP requests
     * synchronously.
     */
    // @Autowired
    private final GCDProxy mGCDProxy =
        new GCDProxy();

    /**
     * This auto-wired field connects the {@link MathServicesClient}
     * to the {@link PrimalityProxy} that performs HTTP requests
     * synchronously.
     */
    // @Autowired
    private final PrimalityProxy mPrimalityProxy =
        new PrimalityProxy();

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * List} param for primality and return a corresponding {@link
     * List} whose {@link PrimeResult} elements indicate 0 if an
     * element is prime or the smallest factor if it's not.
     *
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * CHECK_PRIMALITIES} endpoint to this method.
     *
     * @param primeCandidates The {@link List} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link List} of {@link PrimeResult} objects
     */
    public List<PrimeResult> checkPrimalities
        (List<Integer> primeCandidates) {
        return mPrimalityProxy
            // Forward to the proxy.
            .checkPrimalities(primeCandidates);
    }

    /**
     * Compute the GCD of the {@code integers} param.
     *
     * @param integers The {@link List} of {@link Integer} objects
     *                 upon which to compute the GCD
     * @return A {@link List} of {@link GCDResult} objects
     */
    public List<GCDResult> computeGCDs(List<Integer> integers) {
        return mGCDProxy
            // Forward to the proxy.
            .computeGCDs(integers);
    }
}
