package quotes.responder.quoter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import quotes.common.model.RandomRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;

import static quotes.common.Constants.*;

/**
 * This controller enables RSocket clients to get random Shakespeare
 * quotes, subscribe to receive Flux streams of these quotes, as well
 * as cancel earlier subscriptions.  It demonstrates the following
 * RSocket interaction models
 *
 * <ul>
 * <li>Request/Response, where each two-way async request receives a
 * single async response from the server.</li>
 *
 * <li>Fire-and-Forget, where each one-way message receives no response
 * from the server.</li>
 *
 * <li>Request/Stream, where each async request receives a stream of
 * responses from the server.</li>
 *
 * <li>Channel, where a stream of async messages can be sent in both
 * directions between client and server.</li>
 * </ul>
 * <p>
 * Spring enables the integration of RSockets into a controller via
 * the {@code @Controller} annotation, which enables the autodetection
 * of implementation classes via classpath scanning, and
 * the {@code @MessageMapping annotation}, which maps a message onto a
 * message-handling method by matching the declared patterns to a
 * destination extracted from the message.
 *
 * Combining the {@code @Controller} annotation with the
 * {@code @MessageMapping} annotation enables this class to declare
 * service endpoints, which in this case map to RSocket endpoints that
 * each take one {@link Mono} or {@link Flux} parameter and can return
 * a {@link Mono} or {@link Flux} result.  These Project Reactor
 * reactive types enable client and server code to run reactively
 * across a communication channel.
 */
@Controller
public class QuotesMessageController {
    /**
     * The {@link QuotesMessageService} that's associated with this
     * {@link QuotesMessageController} via Spring's dependency
     * injection facilities, where an object receives other objects
     * that it depends on (in this case, the {@link
     * QuotesMessageService}).
     */
    @Autowired
    private QuotesMessageService mService;

    /**
     * @return A {@link Mono} that emits the total number of 
     *         Shakespeare quotes
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping(GET_NUMBER_OF_QUOTES)
    Mono<Long> getNumberOfQuotes
        (@AuthenticationPrincipal UserDetails user) {
        return mService
            // Forward to the service.
            .getNumberOfQuotes(user);
    }

    /**
     * A client must call this method to subscribe before attempting
     * to receive a {@link Flux} stream of {@link Quote} objects.  
     *
     * It implements a two-way async RSocket request/response call
     * that sends a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription}
     * @return A {@link Mono} that emits the result of the {@link
     *         Subscription} request
     */
    @MessageMapping(SUBSCRIBE)
    public Mono<Subscription> subscribe
        (Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .subscribe(subscriptionRequest);
    }

    /**
     * Cancel a {@link Subscription} in an unconfirmed manner, i.e.,
     * any errors are not returned to the client.
     *
     * This method implements a one-way async RSocket fire-and-forget
     * call that sends no response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     */
    @MessageMapping(CANCEL_UNCONFIRMED)
    public void cancelSubscriptionUnconfirmed
        (Mono<Subscription> subscriptionRequest) {
        mService
            // Forward to the service.
            .cancelSubscriptionUnconfirmed(subscriptionRequest);
    }

    /**
     * Cancel a {@link Subscription} in a confirmed manner, i.e., any
     * errors are indicated to the client.
     *
     * This method implements a two-way async RSocket request/response
     * call that sends a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     *         indicating if the cancel request succeeded or failed
     */
    @MessageMapping(CANCEL_CONFIRMED)
    public Mono<Subscription> cancelSubscriptionConfirmed
        (Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .cancelSubscriptionConfirmed(subscriptionRequest);
    }

    /**
     * Get a {@link Flux} that emits all the {@link Quote} objects
     * associated with the {@link Subscription}.
     *
     * This method implements the async RSocket request/stream model,
     * where each request receives a stream of responses from the
     * server.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Flux} that emits a {@link Quote} of the play
     *         associated with a {@link Subscription} or an empty
     *         {@link Flux} otherwise.
     */
    @MessageMapping(GET_ALL_QUOTES)
    public Flux<Quote> getAllQuotes
        (Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .getAllQuotes(subscriptionRequest);
    }

    /**
     * Get a {@link Flux} that emits the requested Shakespeare quotes
     * if the client has subscribed.
     *
     * This method implements a two-way async RSocket request/stream
     * call where a {@link RandomRequest} non-reactive type is sent to
     * the server and the server returns a {@link Flux} in response.
     *
     * @param randomRequest A non-reactive {@link RandomRequest} that
     *                      contains {@link Subscription} and random
     *                      indices array
     * @return A {@link Flux} that emits the requested Zippy quotes
     * once every second
     */
    @MessageMapping(GET_QUOTES_SUBSCRIBED)
    public Flux<Quote> getQuotesSubscribed
        (RandomRequest randomRequest) {
        return mService
            // Forward to the service.
            .getQuotesSubscribed(randomRequest);
    }
}
