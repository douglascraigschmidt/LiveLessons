import reactor.core.publisher.Flux;
import utils.Options;

/**
 * This example shows how to asynchronously and concurrently counts
 * the number of images in a recursively-defined folder structure
 * using a range of Project Reactor features, including Mono features
 * (e.g., just(), fromCallable(), blockOptional(), doOnSuccess(),
 * subscribeOn(), map(), flatMap(), zipWith(), transformDeferred(),
 * defaultIfEmpty()), and Flux features (e.g., fromIterable(),
 * flatMap(), and reduce()).  The root folder can either reside
 * locally (filesystem-based) or remotely (web-based).
 */
public class Main {
    /**
     * This static main() entry point runs the example.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Create an object that count the images rooted at the
        // page/folder being traversed.
        new ImageCounter(Options.instance().getRootUri());
    }
}
