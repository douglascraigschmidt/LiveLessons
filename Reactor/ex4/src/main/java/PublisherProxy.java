import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import utils.Options;

/**
 * This class serves as a proxy to the Publisher micro-service.
 */
class PublisherProxy {
    /**
     * The URI that creates the random number generator.
     */
    private final String mCreatePublishingURI = "/publisher/_create";

    /**
     * The URI that starts the random number generator.
     */
    private final String mStartPublishingURI = "/publisher/_start";

    /**
     * The URI that stops the random number generator.
     */
    private final String mStopPublishingURI = "/publisher/_stop";

    /**
     * The WebClient provides the means to access the publisher
     * micro-service.
     */
    private final WebClient mPublisher;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8080";

    /**
     * Constructor initializes the fields to initialize a publisher
     * that emits a stream of random numbers.
     */
    public PublisherProxy() {
        mPublisher = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();

        mPublisher
            // Create an HTTP POST request.
            .post()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mCreatePublishingURI)
                 .queryParam("count", Options.instance().count())
                 .queryParam("maxValue", Options.instance().maxValue())
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a flux of integers.
            .bodyToMono(void.class)

            // Wait until the processing is done.
            .block();
    }

    /**
     * Start publishing a stream of random numbers.
     *
     * @param scheduler Scheduler to publish the numbers on.
     * @return Return a flux that publishes random numbers
     */
    public Flux<Integer> startPublishing(Scheduler scheduler) {
        // Return a flux to the publisher initialized remotely.
        return mPublisher
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(mStartPublishingURI)

            // Retrieve the response.
            .retrieve()

            // Convert it to a flux of integers.
            .bodyToFlux(Integer.class)
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * Stop publishing a stream of random numbers.
     */
    public void stopPublishing() {
        mPublisher
            // Create an HTTP DELETE request.
            .delete()

            // Add the uri to the baseUrl.
            .uri(mStopPublishingURI)

            // Retrieve the response.
            .retrieve()

            // Convert it to a void mono.
            .bodyToMono(Void.class)
            
            // Wait until the processing is done.
            .block();
    }
}
