package com.example.expressiontree;

/**
 * @class InputDispatcher
 *
 * @brief This class runs the event loop.
 */
public class InputDispatcher {
    /** 
     * Handles input events and commands associated with the
     * expression tree app.
     */
    private InputHandler inputHandler;

    /** Is the input dispatching done? */
    private boolean inputDispatchingDone = false;

    /** The singleton @a InputDispatcher instance. */
    private static InputDispatcher uniqueInstance = null;

    /** Method to return the one and only singleton instance. */
    public static InputDispatcher instance() {
        if (uniqueInstance == null)
            uniqueInstance = new InputDispatcher();

        return uniqueInstance;
    }

    public void makeHandler(boolean verbose,
                            Object input,
                            Object output,
                            Object activity) {
        /**
         * Create an InputHandler that takes the following parameters: 
         * boolean (to determine verbose or succinct)
         * input   (An EditText object for the android GUI) NULL for commandLine
         * output  (A TextView object for the android GUI)  NULL for commandLIne
         * activity(An Activity object for the android GUI) NULL for commandLine
         */
        inputHandler = InputHandler.makeHandler(verbose,
                                                input,
                                                output,
                                                activity);
    }

    public void makeHandlerAndPromptUser(boolean verbose,
                                         Object input,
                                         Object output,
                                         Object activity) {
        /**
         * Create an InputHandler that takes the following parameters: 
         * boolean (to determine verbose or succinct)
         * input   (An EditText object for the android GUI) NULL for commandLine
         * output  (A TextView object for the android GUI)  NULL for commandLIne
         * activity(An Activity object for the android GUI) NULL for commandLine
         */
        inputHandler = InputHandler.makeHandler(verbose,
                                                input,
                                                output,
                                                activity);
        inputHandler.promptUser();
    }

    public void dispatchOneInput() {
        try {
            inputHandler.handleInput();
        } catch (Exception e) {
            Platform.instance().outputLine(e.getMessage());
        }
    }

    public void dispatchAllInputs() {
        /** Continuously runs the InputHandler. */
        while (inputDispatchingDone == false) {
            try {
                inputHandler.handleInput();
            } catch (Exception e) {
                Platform.instance().outputLine(e.getMessage());
            }
        }
    }

    /** End the input dispatching loop. */
    public void endInputDispatching() {
        this.inputDispatchingDone = true;
    }

    /**
     * Make the constructor private for a singleton.
     */
    private InputDispatcher() {
    }
}
