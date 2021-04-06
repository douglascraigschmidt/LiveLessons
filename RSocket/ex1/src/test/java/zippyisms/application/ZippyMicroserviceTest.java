package zippyisms.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import zippyisms.datamodel.SubscriptionRequest;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.datamodel.ZippyQuote;
import zippyisms.utils.Constants;

import java.util.Random;
import java.util.UUID;

/**
 * This class tests the endpoints provided by the Zippy th' Pinhead
 * microservice for each of the four interaction models provided by
 * RSocket.
 */
@SpringBootTest
public class ZippyMicroserviceTest {
    /**
     * The number of Zippy th' Pinhead quotes to process.
     */
    private static final int sNUMBER_OF_QUOTES = 5;

    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.
     */
    @Autowired
    private Mono<RSocketRequester> zippyQuoteRequester;

    /**
     * Get/print a specified number of random Zippy th' Pinhead
     * quotes.  This method demonstrates a two-way RSocket
     * bi-directional channel call where a Flux stream is sent to the
     * server and the server returns a Flux in response.
     */
    @Test
    public void getRandomQuotes() {
        System.out.println("Entering getRandomQuotes()");

        // Create an array of random indices.
        Integer[] randomIndices = zippyQuoteRequester
            // Invoke the call.
            .map(r -> r
                 // Send this request to the GET_NUMBER_OF_QUOTES
                 // endpoint.
                 .route(Constants.GET_NUMBER_OF_QUOTES))

            // Convert the response to a Mono<Integer> containing the
            // number of Zippyisms.
            .flatMap(r -> r.retrieveMono(Integer.class))

            // Create an Integer array containing random indices.
            .map(numberOfZippyisms -> new Random()
                 // Create the given number of random Zippyisms
                 // whose IDs are between 1 and the total
                 // number of quotes.
                 .ints(sNUMBER_OF_QUOTES,
                       1,
                       numberOfZippyisms)

                 // Convert the IntStream into a Stream.
                 .boxed()

                 // Trigger intermediate operations and store
                 // in an array.
                 .toArray(Integer[]::new))

            // Block until we've computed the randomIndices (we only
            // use block() since the StepVerifier below requires the
            // array of random indices).
            .block();

        // Double-check that randomIndices is properly initialized.
        assert randomIndices != null;

        // Create a Flux that emits Zippy th' Pinhead quotes at the
        // random indices emitted by the randomZippyQuotes Flux.
        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            // Invoke the call.
            .map(r ->
                 // Send this request to the GET_QUOTE endpoint.
                 r.route(Constants.GET_QUOTE)

                 // Create a Flux that emits indices for random Zippy
                 // th' Pinhead quotes and pass that as the param.
                 .data(Flux.fromArray(randomIndices)))

            // Convert the Mono result to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(m ->
                      System.out.println("Quote ("
                                         + m.getQuoteId() + ") = "
                                         + m.getZippyism()));

        // Ensure the results are correct, i.e., the returned quoteIds
        // match those sent to the GET_QUOTE endpoint.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[0])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[1])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[2])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[3])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[4])
            .verifyComplete();
    }

    /**
     * Subscribe to receive Zippyisms.  This method demonstrates a
     * two-way async RSocket request/response call that subscribes to
     * retrieve a stream of Zippy t' Pinhead quotes.
     */
    @Test
    public void subscribe() {
        System.out.println("Entering subscribe()");

        // Create a SubscriptionRequest.
        Mono<SubscriptionRequest> subscriptionRequest = zippyQuoteRequester
            // Invoke the call.
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(SubscriptionRequest.class))

            // Print the results.
            .doOnNext(r ->
                      System.out.println(r.getRequestId()
                                         + ":" + r.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(t -> t
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();
    }

    /**
     * Cancel a previous subscription.  This method demonstrates a
     * one-way RSocket fire-and-forget call that does not return a
     * response.
     */
    @Test
    public void cancelSubscription() {
        System.out.println("Entering cancelSubscription()");

        // Cancel the subscription.
        Mono<Void> mono = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL endpoint.
                 .route(Constants.CANCEL)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Send the request to the client, but don't receive a
            // response.
            .flatMap(RSocketRequester.RetrieveSpec::send);

        // Test that mono completed.
        StepVerifier
            .create(mono)
            .verifyComplete();
    }

    /**
     * Receive sNUMBER_OF_QUOTES of Zippy th' Pinhead quotes.  This
     * method demonstrates the async RSocket request/stream model,
     * where each request receives a Flux stream of responses from the
     * server.
     */
    @Test
    public void getQuotes() {
        System.out.println("Entering getQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<SubscriptionRequest> subscriptionRequest = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(SubscriptionRequest.class));

        // Get a Flux that emits ZippyQuote objects from the server.
        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Invoke the call.
            .map(tuple -> tuple
                 // Send this request to the GET_QUOTES endpoint.
                 .getT1().route(Constants.GET_QUOTES)

                 // Pass the SubscriptionRequest as the param.
                 .data(tuple.getT2()))

            // Conver the Mono response to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print each Zippyism emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote: " + m.getZippyism()))

            .take(sNUMBER_OF_QUOTES);

        // Ensure the first five results come in the right order.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m.getZippyism().equals("All of life is a blur of Republicans and meat!"))
            .expectNextMatches(m -> m.getZippyism().equals("..Are we having FUN yet...?"))
            .expectNextMatches(m -> m.getZippyism().equals("Life is a POPULARITY CONTEST!  I'm REFRESHINGLY CANDID!!"))
            .expectNextMatches(m -> m.getZippyism().equals("You were s'posed to laugh!"))
            .expectNextMatches(m -> m.getZippyism().equals("Fold, fold, FOLD!!  FOLDING many items!!"))
            .verifyComplete();
    }
}
