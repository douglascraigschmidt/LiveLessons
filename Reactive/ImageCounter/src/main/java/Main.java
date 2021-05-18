import utils.Options;

/**
 * This example shows how to asynchronously and concurrently count the
 * number of images in a recursively-defined folder structure using a
 * range of Project Reactor features, including Mono features (e.g.,
 * just(), block(), doOnSuccess(), map(), transformDeferred(),
 * subscribeOn(), flatMap(), zipWith(), defaultIfEmpty()s) and Flux
 * features (e.g., fromIterable(), flatMap(), reduce()).  The root
 * folder can either reside locally (filesystem-based) or remotely
 * (web-based).
 */
public class Main {
    /**
     * This static main() entry point runs the example.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Create an object that count the images.
        new ImageCounter();
    }
}
