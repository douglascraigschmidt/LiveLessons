package edu.vandy.main;

import edu.vandy.utils.Options;

/**
 * This example shows how to count the number of images in a
 * recursively-defined folder structure.
 */
public class Main {
    /**
     * This static main() entry point runs the example.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Create an object that count the images.
        ImageCounter imageCounter = new ImageCounter();

        // Print the URIs that were visited.
        imageCounter.printUris();
    }
}
