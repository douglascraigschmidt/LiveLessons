package zippyisms.client;

import ch.qos.logback.classic.Level;
import zippyisms.datamodel.Constants;
import zippyisms.datamodel.ZippyQuote;
import zippyisms.datamodel.SubscriptionRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import zippyisms.service.ZippyService;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * This class tests the endpoints provided by the Zippy th' Pinhead
 * microservice for each of the four interaction models provided by
 * RSocket.
 */
public class ZippyMicroserviceTest {
    /**
     * The number of random Zippy th' Pinhead quotes to request.
     */
    private static final int sNUMBER_OF_RANDOM_QUOTES = 10;

    /**
     * The number of seconds to receive Zippy th' Pinhead quotes.
     */
    private static final int sQUOTES_DURATION = 10;

    // @@ Monte, can you help me replace this with the
    // appropriate @bean?!
    private final Mono<RSocketRequester> zippyQuoteRequester = Mono
        // Initialize the an RSocket client requester for the Zippy th' Pinhead microservice.
        .just(RSocketRequester.builder()
              .rsocketConnector(rSocketConnector -> 
                                rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2))))
              .dataMimeType(MediaType.APPLICATION_CBOR)
              .rsocketStrategies(RSocketStrategies.builder()
                                 .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                                 .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                                 .build())
              .tcp("localhost", Constants.SERVER_PORT));

    static {
        /*
         *  Disable the verbose/annoying Spring "debug" logging.
         */
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));
    }

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("tests are starting");

        // Create a new test object.
        ZippyMicroserviceTest test = new ZippyMicroserviceTest();

        // Get/print a specified number of random Zippy th' Pinhead quotes.
        test.getRandomQuotes();

        // Subscribe to receive a Flux stream of Zippy th' Pinhead quotes.
        test.subscribe();

        // Receive sQUOTES_DURATION seconds-worth of Zippy quotes.
        test.getQuotes(Duration.ofSeconds(sQUOTES_DURATION));

        // Cancel the subscription.
        test.cancelSubscription();

        System.out.println("tests are finished");
    }

    /**
     * Get/print a specified number of random Zippy th' Pinhead
     * quotes.  This method demonstrates a two-way RSocket
     * bi-directional channel call where a Flux stream is sent to the
     * server and the server returns a Flux in response.
     */
    public void getRandomQuotes(){
        System.out.println("Entering getRandomQuotes()");

        // Create a Flux that emits indices for random Zippy th'
        // Pinhead quotes.
        Flux<Integer> randomZippyQuotes = Flux
            .fromStream(new Random()
                        // Create the given number of random Zippyisms
                        // whose IDs are between 1 and the total
                        // number of quotes.
                        .ints(sNUMBER_OF_RANDOM_QUOTES,
                              1,
                              ZippyService.quotes.size())
                        // Convert the IntStream into a Stream.
                        .boxed());

        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the GET_QUOTE endpoint.
                 .route(Constants.GET_QUOTE)

                 // Pass the Flux of random indices as the param.
                 .data(randomZippyQuotes))

            // Convert the result to a Flux<ZippyQuote>.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(m ->
                      System.out.println("Quote ("
                                         + m.getQuoteId() + ") = "
                                         + m.getZippyism()));

        // Block until all the random Zippyisms are processed.
        zippyQuotes.blockLast();
    }

    /**
     * Subscribe to receive Zippyisms.  This method demonstrates a
     * two-way RSocket request/response call that blocks the client
     * until the response is received.
     */
    public void subscribe(){
        System.out.println("Entering subscribe()");

        Mono<SubscriptionRequest> subscriptionRequest = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Block until subscription request has completed.
            .flatMap(r -> r.retrieveMono(SubscriptionRequest.class))

            // Print the results.
            .doOnNext(r ->
                      System.out.println(r.getRequestId() 
                                         + ":" + r.getStatus()));

        // Block the client until the subscription request is
        // processed.
        subscriptionRequest.block();
    }

    /**
     * Receive {@code duration} seconds-worth of Zippy th' Pinhead
     * quotes.  This method demonstrates the RSocket request/stream
     * model, where each request receives a stream of responses from
     * the server.
     *
     * @param duration Number of seconds to receive Zippyisms.
     */
    public void getQuotes(Duration duration){
        System.out.println("Entering getQuotes()");
        
        // Get a confirmed SubscriptionRequest from the server.
        Mono<SubscriptionRequest> subscriptionRequest = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Send the request to the client and block until a
            // SubscriptionRequest is received.
            .flatMap(r -> r.retrieveMono(SubscriptionRequest.class));

        // Get a Flux that emits ZippyQuote objects from the server.
        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            .map(tuple -> tuple
                 // Send this request to the GET_QUOTES endpoint.
                 .getT1().route(Constants.GET_QUOTES)

                 // Pass the SubscriptionRequest as the param.
                 .data(tuple.getT2()))

            // Send the request to the client and block until a
            // Flux<ZippyQuote> is received in response.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print each Zippyism emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote: " + m.getZippyism()));

        try {
            // Receive Zippyisms for the given duration.
            zippyQuotes.blockLast(duration);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Cancel a previous subscription.  This method demonstrates a
     * one-way RSocket fire-and-forget call that does not block the
     * client.
     */
    public void cancelSubscription(){
        System.out.println("Entering cancelSubscription()");

        Mono<Void> mono = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL endpoint.
                 .route(Constants.CANCEL)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new SubscriptionRequest(UUID.randomUUID())))

            // Send the request to the client, but don't block the
            // client.
            .flatMap(RSocketRequester.RetrieveSpec::send);

        // This call will not block the caller since the Mono returns
        // no value (i.e., it's Void).
        Optional<Void> result = mono.blockOptional();
        if (result.isEmpty())
            System.out.println("Do not block for Void result!");
    }
}
