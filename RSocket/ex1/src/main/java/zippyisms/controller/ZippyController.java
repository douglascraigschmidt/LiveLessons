package zippyisms.controller;

import zippyisms.utils.Constants;
import zippyisms.datamodel.ZippyQuote;
import zippyisms.datamodel.SubscriptionRequest;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.service.ZippyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * This controller enables RSocket clients to get random Zippy th'
 * Pinhead quotes, subscribe to receive Flux streams of these quotes,
 * as well as cancel earlier subscriptions.  It demonstrates the
 * following RSocket interaction models
 * 
 * . Request/Response, where each two-way async request receives a
 *   single async response from the server.
 *
 * . Fire-and-Forget, where each one-way message receives no response
 *   from the server.
 *
 * . Request/Stream, where each async request receives a stream of
 *   responses from the server.
 *
 * . Channel, where a stream of async messages can be sent in both
 *   directions between client and server.
 *
 * Spring enables the integration of RSockets into a controller via
 * the @Controller annotation, which enables the autodetection of
 * implementation classes via classpath scanning, and
 * the @MessageMapping annotation, which maps a message onto a
 * message-handling method by matching the declared patterns to a
 * destination extracted from the message.
 */
@Controller
public class ZippyController {
    /**
     * The ZippyService that's associated with this controller via
     * Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, the
     * ZippyService).
     */
    @Autowired
    private ZippyService mZippyService;

    /**
     * Subscribe to receive a Flux stream of Zippy quotes.  This
     * method implements a two-way async RSocket request/response call
     * that blocks the client until the response is received.
     *
     * @param request A {@link Mono} that emits a {@link
     *                SubscriptionRequest}
     * @return A {@link Mono} that confirms the subcription request.
     */
    @MessageMapping(Constants.SUBSCRIBE)
    public Mono<SubscriptionRequest> subscribe(Mono<SubscriptionRequest> request) {
        // Return a Mono whose status has been updated to confirm the
        // subscription request.
        return request
            // Set the request status to confirm the subscription.
            .doOnNext(r -> r.setStatus(SubscriptionStatus.CONFIRMED))

            // Print the subscription information as a diagnostic.
            .doOnNext(r ->
                      System.out.println("subscribe::"
                                         + r.getRequestId() + " : "
                                         + r.getStatus()));
    }

    /**
     * Cancel a {@link SubscriptionRequest}.  This method implements a
     * one-way async RSocket fire-and-forget call that does not block
     * the client.
     *
     * @param request A {@link Mono} that emits a {@link
     *                SubscriptionRequest}
     */
    @MessageMapping(Constants.CANCEL)
    public void cancelSubscription(Mono<SubscriptionRequest> request) {
        // Cancel the subscription asynchronously.
        request
            // Set the request status to indicate the subscription has
            // been cancelled.
            .doOnNext(r -> r.setStatus(SubscriptionStatus.CANCELLED))

            // Print the subscription information as a diagnostic.
            .doOnNext(r ->
                      System.out.println("cancelSubscription::"
                                         + r.getRequestId() + " : " + r.getStatus()))

            // Initiate the cancellation, which is necessary since no
            // response is sent back to the client.
            .subscribe();
    }

    /**
     * Get a {@link Flux} that emits Zippy quotes once a second.  This
     * method implements the async RSocket request/stream model, where
     * each request receives a stream of responses from the server.
     *
     * @param request A {@link Mono} that emits a {@link
     *                SubscriptionRequest}
     * @return A {@link Flux} that emits Zippy quote every second
     */
    @MessageMapping(Constants.GET_QUOTES)
    public Flux<ZippyQuote> getQuotes(Mono<SubscriptionRequest> request) {
        return request
            // Check to ensure the subscription request is confirmed.
            .flatMapMany(t ->
                         t.getStatus().equals(SubscriptionStatus.CONFIRMED)
                 // If the request is confirmed return a Flux that
                 // emits the list of quotes.
                 ? Flux.fromIterable(mZippyService.getQuotes())

                 // If the request is not confirmed return an empty
                 // Flux.
                 : Flux.empty())

            // Delay each emission by one second to demonstrate the
            // streaming capability to clients.
            .delayElements(Duration.ofSeconds(1));
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
     */
    @MessageMapping(Constants.GET_QUOTE)
    public Flux<ZippyQuote> getQuote(Flux<Integer> quoteIds){
        return quoteIds
            // Get the Zippy th' Pinhead quote at each quote id.
            .map(mZippyService::getQuote);
    }

    /**
     * @return The total number of Zippy th' Pinhead quotes.
     */
    @MessageMapping(Constants.GET_NUMBER_OF_QUOTES)
    public Mono<Integer> getNumberOfQuotes(){
        return Mono
            // Return the total number of Zippy th' Pinhead quotes.
            .just(mZippyService.getNumberOfQuotes());
    }
}
