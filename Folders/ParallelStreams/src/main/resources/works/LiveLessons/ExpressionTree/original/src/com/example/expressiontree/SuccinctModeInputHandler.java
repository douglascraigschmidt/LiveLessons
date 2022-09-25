package com.example.expressiontree;

/**
 * @class SuccinctModeInputHandler
 * 
 * @brief Provides a concrete interface for less verbosely handling
 *        input events associated with the expression tree
 *        application.  This class plays the role of "Concrete Class"
 *        in the Template Method pattern.
 */
public class SuccinctModeInputHandler extends InputHandler {
    /** This hook method succinctly prompts the user for input. */
    public void promptUser() {
        if (Platform.instance().isCommandLinePlatform())
            Platform.instance().outputString("> ");
    }

    /** 
     * This hook method makes the appropriate set of macro commands
     * based on the user input.
     */
    public UserCommand makeUserCommand(String userInputExpression) {
        /**
         * Create a MacroCommand by prefixing the keyword "macro" in
         * front of the userInputExpression.
         */
        return userCommandFactory.makeUserCommand("macro " + userInputExpression);
    }
}
