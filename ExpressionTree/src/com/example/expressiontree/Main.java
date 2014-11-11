package com.example.expressiontree;

/**
 * @class Main
 *
 * @brief This class is the main entry point for the command-line
 *        version of the expression tree processing app.
 */
public class Main {
    /**
     * The Java virtual machine requires the instantiation of a main
     * method to run the command line version of the encapsulated
     * expression tree.
     */
    public static void main(String[] args) {
        /** 
         * Initializes the Platform singleton with the appropriate
         * Platform strategy, which in this case will be the
         * CommandLinePlatform.
         */
        Platform.instance (new PlatformFactory(System.in,
                                               System.out,
                                               null).makePlatform());

        /** Initializes the Options singleton. */
        Options.instance().parseArgs(args);
		
        if (Options.instance().verbose())
            /** Print a welcome message via the platform strategy object. */
            Platform.instance().outputString("Welcome!\n");
    
        /**
         * Create an InputHandler to process the user input expression
         * where the edittext contains the input and the textview will
         * be the output.
         */
        InputDispatcher.instance().makeHandler (Options.instance().verbose(),
                                                System.in,
                                                System.out,
                                                null);

        /** Process all user input expressions. */
        InputDispatcher.instance().dispatchAllInputs();
    }
}
