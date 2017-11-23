package expressiontree.input;

import expressiontree.platspecs.Platform;

/**
 * This class runs the event loop.
 */
public class InputDispatcher {
    /** 
     * Handles mInput events and commands associated with the
     * expression tree app.
     */
    private InputHandler mInputHandler;

    /** 
     * Is the mInput dispatching done?
     */
    private boolean mInputDispatchingDone = false;

    /**
     * The singleton @a InputDispatcher instance. 
     */
    private static InputDispatcher uniqueInstance = null;

    /**
     * Method to return the one and only singleton instance. 
     */
    public static InputDispatcher instance() {
        if (uniqueInstance == null)
            uniqueInstance = new InputDispatcher();

        return uniqueInstance;
    }

    /**
     * ...
     */
    public void makeHandler(boolean verbose,
                            Object input,
                            Object output,
                            Object activity) {
        // Create an InputHandler that takes the following parameters:
        // boolean (to determine verbose or succinct)
        // mInput   (An EditText object for the android GUI) NULL for commandLine
        // output  (A TextView object for the android GUI)  NULL for commandLine
        // activity(An Activity object for the android GUI) NULL for commandLine
        mInputHandler = InputHandler.makeHandler(verbose,
                                                input,
                                                output,
                                                activity);
    }

    /**
     * ...
     */
    public void makeHandlerAndPromptUser(boolean verbose,
                                         Object input,
                                         Object output,
                                         Object activity) {
        // Create an InputHandler that takes the following parameters:
        // boolean (to determine verbose or succinct)
        // mInput   (An EditText object for the android GUI) NULL for commandLine
        // output  (A TextView object for the android GUI)  NULL for commandLIne
        // activity(An Activity object for the android GUI) NULL for commandLine
        mInputHandler = InputHandler.makeHandler(verbose,
                                                input,
                                                output,
                                                activity);
        mInputHandler.promptUser();
    }

    /**
     * ...
     */
    public void dispatchOneInput() {
        try {
            mInputHandler.handleInput();
        } catch (Exception e) {
            Platform.instance().outputLine(e.getMessage());
        }
    }

    /**
     * ...
     */
    public void dispatchAllInputs() {
        // Continuously runs the InputHandler.
        while (!mInputDispatchingDone) {
            try {
                mInputHandler.handleInput();
            } catch (Exception e) {
                Platform.instance().outputLine(e.getMessage());
            }
        }
    }

    /** 
     * End the mInput dispatching loop.
     */
    public void endInputDispatching() {
        mInputDispatchingDone = true;
    }

    /**
     * Make the constructor private for a singleton.
     */
    private InputDispatcher() {
    }
}
