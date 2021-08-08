package zippyisms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zippyisms.common.Constants;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.ZippyQuote;

import static zippyisms.common.Constants.*;

/**
 * This controller enables RSocket clients to get random Zippy th'
 * Pinhead quotes, subscribe to receive Flux streams of these quotes,
 * as well as cancel earlier subscriptions.  It demonstrates the
 * following RSocket interaction models
 *
 * . Request/Response, where each two-way async request receives a
 * single async response from the server.
 *
 * . Fire-and-Forget, where each one-way message receives no response
 * from the server.
 *
 * . Request/Stream, where each async request receives a stream of
 * responses from the server.
 *
 * . Channel, where a stream of async messages can be sent in both
 * directions between client and server.
 *
 * Spring enables the integration of RSockets into a controller via
 * the {@code @Controller} annotation, which enables the autodetection
 * of implementation classes via classpath scanning, and
 * the @MessageMapping annotation, which maps a message onto a
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
public class ZippyController {
    /**
     * The {@link ZippyService} that's associated with this {@link
     * ZippyController} via Spring's dependency injection facilities,
     * where an object receives other objects that it depends on (in
     * this case, the {@link ZippyService}).
     */
    @Autowired
    private ZippyService mService;

    /**
     * This method must be called before attempting to receive a Flux
     * stream of Zippy quotes.  It implements a two-way async RSocket
     * request/response call that sends a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link 
     *                            Subscription}
     * @return A {@link Mono} that emits the result of the {@link
     *         Subscription} request
     */
    @MessageMapping(SUBSCRIBE)
    Mono<Subscription> subscribe(Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .subscribe(subscriptionRequest);
    }

    /**
     * Cancel a {@link Subscription} in an unconfirmed manner, i.e.,
     * any errors are not returned to the client.  This method
     * implements a one-way async RSocket fire-and-forget call that
     * does not send a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     */
    @MessageMapping(CANCEL_UNCONFIRMED)
    void cancelSubscriptionUnconfirmed(Mono<Subscription> subscriptionRequest) {
        mService
            // Forward to the service.
            .cancelSubscriptionUnconfirmed(subscriptionRequest);
    }

    /**
     * Cancel a {@link Subscription} in a confirmed manner, i.e., any
     * errors are indicated to the client.  This method implements a
     * two-way async RSocket request/response call that sends a
     * response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     *         indicating if the cancel request succeeded or failed
     */
    @MessageMapping(CANCEL_CONFIRMED)
    Mono<Subscription> cancelSubscriptionConfirmed
        (Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .cancelSubscriptionConfirmed(subscriptionRequest);
    }

    /**
     * Get a {@link Flux} that emits Zippy quotes once a second.  This
     * method implements the async RSocket request/stream model, where
     * each request receives a stream of responses from the server.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Flux} that emits Zippy quote every second
     */
    @MessageMapping(GET_ALL_QUOTES)
    Flux<ZippyQuote> getAllQuotes(Mono<Subscription> subscriptionRequest) {
        return mService
            // Forward to the service.
            .getAllQuotes(subscriptionRequest);
    }

    /**
     * Get a {@link Flux} that emits the requested Zippy quotes.  This
     * method implements a two-way async RSocket bi-directional
     * channel call where a Flux stream is sent to the server and the
     * server returns a Flux in response.
     *
     * @param quoteIds A {@link Flux} that emits the given Zippy
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     *         once every second
     */
    @MessageMapping(GET_RANDOM_QUOTES)
    Flux<ZippyQuote> getRandomQuotes(Flux<Integer> quoteIds) {
        return mService
            // Forward to the service.
            .getRandomQuotes(quoteIds);
    }

    /**
     * @return A {@link Mono} that emits the total number of Zippy th'
     * Pinhead quotes
     */
    @MessageMapping(Constants.GET_NUMBER_OF_QUOTES)
    Mono<Integer> getNumberOfQuotes() {
        return mService
            // Forward to the service.
            .getNumberOfQuotes();
    }
}
