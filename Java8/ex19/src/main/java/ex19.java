import utils.Options;

/**
 * This example shows how to count the number of images in a
 * recursively-defined folder structure using a range of
 * CompletableFuture features.
 */
public class ex19 {
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
