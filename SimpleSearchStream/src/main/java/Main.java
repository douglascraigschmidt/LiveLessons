import search.SearchResults;
import search.WordSearcher;
import utils.TestDataFactory;

import java.util.List;

/**
 * This program searches sequentially for the occurrence of words in a
 * string containing the contents of a file.  It demonstrates the use
 * of basic Java 8 functional programming features (such as lambda
 * expressions and method references) in conjunction with Java 8
 * sequential streams and a spliterator.
 */
public class Main {
    /*
     * Input files.
     */

    /**
     * The lyrics to the "Do-Re-Mi" song from the Sound of Music
     * musical.
     */
    private static final String sINPUT_FILE =
        "do-re-mi.txt";

    /**
     * A list of words to search for the input file.
     */
    private static final String sWORD_LIST_FILE =
        "wordList.txt";

    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SimpleSearchStream");

        // Create an input string containing the lyrics to the
        // do-re-mi song.
        String inputString = 
            TestDataFactory.getInput(sINPUT_FILE, "@").get(0);

        // Get the list of words to find.
        List<String> wordsToFind = 
            TestDataFactory.getWordList(sWORD_LIST_FILE);

        // Create an object that can be used to search for words in
        // the input.
        WordSearcher wordSearcher =
            new WordSearcher(inputString);
                                                             
        // Find all matching words.
        List<SearchResults> results = wordSearcher
            .findWords(wordsToFind);

        // Print all the results;
        wordSearcher.printResults(results);

        results = wordSearcher
            .findWordsEx(wordsToFind);

        // Print a "suffix slice" of the results starting at "La" and
        // continuing to the end of the list.
        wordSearcher.printSuffixSlice("La", results);

        // Print a "prefix slice" of the results starting at the
        // beginning of the list and continuing up to (but not
        // including) "La".
        wordSearcher.printPrefixSlice("La", results);

        System.out.println("Ending SimpleSearchStream");
    }
}

