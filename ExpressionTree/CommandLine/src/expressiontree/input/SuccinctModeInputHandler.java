package expressiontree.input;

import expressiontree.commands.UserCommand;
import expressiontree.input.InputHandler;
import expressiontree.platspecs.Platform;

/**
 * Provides a concrete interface for less verbosely handling mInput
 * events associated with the expression tree application.  This class
 * plays the role of "Concrete Class" in the Template Method pattern.
 */
public class SuccinctModeInputHandler 
       extends InputHandler {
    /** 
     * This hook method succinctly prompts the user for mInput.
     */
    public void promptUser() {
        Platform.instance().outputString("> ");
    }

    /** 
     * This hook method makes the appropriate set of macro commands
     * based on the user mInput.
     */
    public UserCommand makeUserCommand(String userInputExpression) {
        // Create a MacroCommand by prefixing the keyword "macro" in
        // front of the userInputExpression.
        return mUserCommandFactory
            .makeUserCommand("macro "
                             + userInputExpression);
    }
}
