package zippyisms.controller;

import zippyisms.datamodel.Constants;
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
 * This controller enables RSocket clients to subscribe to receive
 * Zippy th' Pinhead quotes, as well as cancel earlier subscriptions.
 * It demonstrates the following RSocket interaction models
 * 
 * . Request/Response, where each two-way request receives a single
 *   response from the server and blocks until the response is
 *   received.
 * 
 * . Fire-and-Forget, where each one-way request receives no response
 *   from the server and thus do not block the client.
 * 
 * . Request/Stream, where each request receives a stream of responses
 *   from the server.
 *
 * Spring enables the integration of RSockets into a controller via
 * the @MessageMapping annotation, as shown below.
 */
@Controller
public class ZippyController {
    /**
     * The ZippyService that's associated with this controller.
     */
    @Autowired
    private ZippyService zippyService;

    /**
     * Subscribe to receive a Flux stream of Zippy quotes.
     *
     * @param request The subscription request
     * @return A {@link Mono} that confirms the subcription request.
     */
    @MessageMapping(Constants.SUBSCRIBE)
    public Mono<SubscriptionRequest> subscribe(Mono<SubscriptionRequest> request) {
        // Return a Mono whose status has been updated to confirm the
        // subscription request.
        return request
            // Set the request status to confirm the subscription.
            .doOnNext(r -> r.setStatus(SubscriptionStatus.SUBSCRIPTION_CONFIRMED))

            // Print the subscription information.
            .doOnNext(r ->
                      System.out.println("subscribe::"
                                         + r.getRequestId() + " : "
                                         + r.getStatus()));
    }

    @MessageMapping(Constants.CANCEL)
    public void cancelSubscription(Mono<SubscriptionRequest> request) {
        // Cancel the subscription asynchronously
        request
            // Set the status of the request to indicate the
            // subscription has been cancelled.
            .doOnNext(r -> r.setStatus(SubscriptionStatus.SUBSCRIPTION_CANCELLED))

            // Print the subscription information.
            .doOnNext(r ->
                      System.out.println("cancelSubscription::"
                                         + r.getRequestId() + " : " + r.getStatus()))

            // Initiate the cancellation, which is necessary since no
            // response is sent back to the client.
            .subscribe();
    }

    /**
     * Get a {@link Flux} that emits Zippy quotes once a second.
     *
     * @param request A subscription request
     * @return A {@link Flux} that emits Zippy quote every second
     */
    @MessageMapping(Constants.GET_QUOTES)
    public Flux<ZippyQuote> getQuotes(Mono<SubscriptionRequest> request) {
        return request
            // Check to ensure that the subscription request is valid.
            .flatMapMany(t -> t.getStatus().equals(SubscriptionStatus.SUBSCRIPTION_CONFIRMED)
                 // If the request is valid return a Flux that emits
                 // the list of quotes.
                 ? Flux.fromIterable(this.zippyService.getQuotes())

                 // if the request is invalid return an empty Flux.
                 : Flux.empty())

            // Delay each emission by one second.
            .delayElements(Duration.ofSeconds(1));
    }

    /**
     * Get a {@link Flux} that emits the requested Zippy quotes.
     *
     * @param quoteIds A Flux containing the requested Zippy {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     */
    @MessageMapping(Constants.GET_QUOTE)
    public Flux<ZippyQuote> getQuote(Flux<Integer> quoteIds){
        return quoteIds
            // Get the Zippy th' Pinhead quote at the designated quote id.
            .map(this.zippyService::getQuote);
    }
}
