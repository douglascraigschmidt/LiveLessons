import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * This test driver showcases how various subclasses of the StreamGang
 * framework use different Java barrier synchronizers to implement an
 * "embarrassingly parallel" application that concurrently searches
 * for words in one or more Lists of input Strings.
 */
public class SearchStreamGangTest {
	/**
	 * Enumerate the tests to run.
	 */
	enum TestsToRun {
		SEQUENTIAL_STREAM,
	}

	/**
	 * If this is set to true then lots of debugging output will be generated.
	 */
	public static boolean diagnosticsEnabled = true;

	/**
	 * Print debugging output if @code diagnosticsEnabled is true.
	 */
	static void printDebugging(String output) {
		if (diagnosticsEnabled)
			System.out.println(output);
	}

	/**
	 * Array of words to search for in the input.
	 */
	private final static String[] mWordList = {
            "do",
            "re",
            "mi",
            "fa",
            "so",
            "la",
            "ti",
            "do" };

	/**
	 * This input array contains the list of strings used by all
	 * the tests.
	 */
	private final static String[][] mInputStrings = {
            { "xfaofao" }, 
            { "xsoo", "xlaolao", "xtio", "xdoodoo" },
            { "xdoo", "xreoreo" }, 
            { "xreoreo", "xdoo" },
            { "xdoodoo", "xreo", "xmiomio" },
            {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
        };

	/**
	 * Factory method that creates the desired type of StreamGang
	 * subclass implementation.
	 */
	private static Runnable makeStreamGang(String[] wordList, TestsToRun choice) {
		switch (choice) {
		case SEQUENTIAL_STREAM:
			return new SearchWithSequentialStream(wordList, mInputStrings);
		}
		return null;
	}

	/**
	 * This is the entry point into the test program.
	 */
	static public void main(String[] args) throws Throwable {
            /*
              Stream.of(mOneShotInputStrings)
              .parallel()
              .forEach(arrayOfStrings -> 
              Stream.of(arrayOfStrings)
              .parallel()
              .forEach(string -> 
              Stream.of(mWordList)
              .parallel()
              .map(word -> WordMatcher.search(word, string))
              .forEach(results -> results.print())));
              } */
                     
            printDebugging("Starting SearchStreamGangTest");
                     
            // Create/run appropriate type of StreamGang to search for words.
            for (TestsToRun test : TestsToRun.values()) {
                printDebugging("Starting " + test); 
                makeStreamGang(mWordList, test).run(); printDebugging("Ending " + test); 
            }
                      
            printDebugging("Ending SearchStreamGangTest");             
	}
}
