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
     * Factory that creates an InputHandler configured according to
     * the parameters.
     *
     * @param verbose Determine whether to use verbose (true) or succinct (false) mode.
     * @param platform Provides input and output according to the configured runtime platform.
     */
    public void makeHandler(boolean verbose,
                            Platform platform) {
        mInputHandler = InputHandler.makeHandler(verbose,
                                                 platform);
    }

    /**
     * Factory that initializes the InputHandler from an existing
     * {@code inputHandler}.
     *
     * @param platform Provides input and output according to the configured runtime platform.
     * @param inputHandler InputHandler to use for testing.
     */
    public void makeHandler(Platform platform,
                            InputHandler inputHandler) {
        mInputHandler = InputHandler.makeHandler(platform, inputHandler);
    }

    /**
     * Factory that creates an InputHandler configured according to
     * the parameters and that prompts the user for input.
     *
     * @param verbose Determine whether to use verbose (true) or succinct (false) mode.
     * @param platform Provides input and output according to the configured runtime platform.
     */
    public void makeHandlerAndPromptUser(boolean verbose,
                                         Platform platform) {
        mInputHandler = InputHandler.makeHandler(verbose,
                                                 platform);
        mInputHandler.promptUser();
    }

    /**
     * Dispatch one input event.
     */
    public void dispatchOneInput() {
        try {
            mInputHandler.handleInput();
        } catch (Exception e) {
            Platform.instance().outputLine(e.getMessage());
        }
    }

    /**
     * Dispatch all the input events until endInputDispatching() is
     * called.
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
