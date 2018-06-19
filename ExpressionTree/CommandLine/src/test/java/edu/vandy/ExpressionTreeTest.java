package test.java.edu.vandy;

import static org.junit.Assert.assertEquals;

import expressiontree.input.InputDispatcher;
import expressiontree.input.InputHandler;
import expressiontree.input.SuccinctModeInputHandler;
import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test program for the expression tree processing app.
 */
public class ExpressionTreeTest {
    class TestModeInputHandler
        extends SuccinctModeInputHandler {

        private List<String> mInputExpressions =
            Arrays.asList("-5 * (3 + 4)",
                          "(-5 * (6 + -(30 + 4)))",
                          "((61 + 8) - (((330 / 33) + (4 * 4) - 9))) * (6 - 1)",
                          "-5 * -(3 - (4 + -3))",
                          "-(-((13-6-11-(6/3))+(7/1+(9+(11-2+5)/14)*96/12)))");

        private int mIndex = 0;

        /**
         * This hook method becomes a no-op.
         */
        public void promptUser() {}

        /**
         * This hook method retrieves the input.
         */
        protected String retrieveInput() {
            if (mIndex++ < mInputExpressions.size())
                return mInputExpressions.get(mIndex - 1);
            else
                return "";
        }
    }

    /**
     * Main entry point that tests the expression tree processing app.
     */
    @Test(timeout=10000)
    public void testExpressionTree() throws Exception {
        // Initializes the Platform singleton with the appropriate
        // Platform strategy, which in this case will be the
        // CommandLinePlatform.
        Platform.instance (new PlatformFactory(null,
                                               System.out,
                                               null).makePlatform());

        // Create an InputHandler to process the user input expression
        // where System.in contains the input and the System.out will
        // be the output.
        InputDispatcher.instance().makeHandler(Platform.instance(),
                                               new TestModeInputHandler());

        // Process all user input expressions.
        InputDispatcher.instance().dispatchAllInputs();
    }
}
