package expressiontree.input;

import expressiontree.commands.UserCommand;
import expressiontree.input.InputHandler;
import expressiontree.platspecs.Platform;

/**
 * Provides a concrete interface for verbosely handling mInput
 * events associated with the expression tree application. 
 *
 * This class overrides several hook methods for use in the Template
 * Method pattern.
 */
public class VerboseModeInputHandler 
       extends InputHandler {
    /** 
     * Keeps track of whether we've prompted the user already. 
     */
    private boolean prompted;
	
    /**
     * Constructor.
     */
    VerboseModeInputHandler() {
        prompted = false;
    }

    /** 
     * This hook method verbosely prompts the user for mInput.
     */
    public void promptUser() {
        Platform platform = Platform.instance();

        if (!prompted) {
            platform.disableAll(sVerboseField);
            platform.outputMenu("", "", "");  
            platform.outputMenu("1a.", "format", "[in-order | post-order]");
            platform.outputMenu("1b.", "set", "[variable=value]");
            platform.outputMenu("2.", "expr", "[expression]");
            platform.outputMenu("3a.", "eval", "[post-order]");
            platform.outputMenu("3b.", "print", "[in-order | pre-order | post-order| level-order]");
            platform.outputMenu("0c.", "quit", "");
            platform.outputMenu("", "", "");

            prompted = true;
        }

        // Output the '>' prompt character. 
        platform.outputString("> ");
    }

    /** 
     * This hook method makes the appropriate command based on the
     * user mInput.
     */
    public UserCommand makeUserCommand(String userInput) {
        return mUserCommandFactory.makeUserCommand(userInput);
    }	
}
