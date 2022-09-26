import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import utils.Options;

/**
 * Unit tests for the ImageCounter class.
 */
public class ImageCounterTests {
    /**
     * Ensure that the ImageCounter.countImages() method works
     * properly.
     */
    @Test
    public void testCountImages() {
        // Enable debugging mode.
        String[] argv = new String[] { "-d", "true" };

        // Initializes the Options singleton.
        Options.instance().parseArgs(argv);

        // Count the number of images available via the root Uri.
        Mono<Integer> counter = new ImageCounter()
            .countImages(Options
                         .instance()
                         .getRootUri(), 1);

        StepVerifier
            // Prepare a new StepVerifier that subscribes to the
            // counter Mono.
            .create(counter)
            
            // There should be 21 total images downloaded from the
            // pathUri.
            .expectNext(21)

            // The Mono should complete normally.
            .expectComplete()

            // Verify the signals received by this subscriber.
            .verify();
    }
}
