import static java.util.stream.Collectors.toList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import streamgangs.SearchStreamGang;
import streamgangs.SearchStreamGangSync;
import streamgangs.SearchWithCompletableFuturesInputs;
import streamgangs.SearchWithCompletableFuturesWords;
import streamgangs.SearchWithParallelStreamInputs;
import streamgangs.SearchWithParallelStreamWords;
import streamgangs.SearchWithParallelStreamWordsAndInputs;
import streamgangs.SearchWithSequentialStream;
import utils.SearchResults;

/**
 * This test driver showcases how various subclasses customize the
 * StreamGang framework with different Java concurrency and
 * synchronization mechanisms to implement an "embarrassingly
 * parallel" program that searches for words in a list of input data.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        COMPLETABLE_FUTURES_WORDS,
        COMPLETABLE_FUTURES_INPUTS,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_WORDS,
        PARALLEL_STREAM_WORDS_AND_INPUTS,
    }

    /**
     * If this is set to true then lots of debugging output will be generated.
     */
    private static boolean diagnosticsEnabled = true;

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    private static String sINPUT_DATA_FILE = "./input/hamlet.txt";
    private static String sWORD_LIST_FILE = "./input/wordList.txt";

    /**
     * Return the input data in the filename as an array of Strings.
     */
    private static String[] getInputData(String filename,
                                         String splitter) {
        // The Stream and file will be closed here.
        try {
            return Pattern.compile(splitter)
                          .split(new String(Files.readAllBytes
                                            (Paths.get(ClassLoader.getSystemResource
                                                       (sINPUT_DATA_FILE).toURI()))));
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        }
    }

    /**
     * Return the word list in the filename as an array of Strings.
     */
    private static List<String> getWordList(String filename) {
        // The Stream and file will be closed here.
        try {
            return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(filename).toURI()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Factory method that creates the desired type of StreamGang
     * subclass implementation.
     */
    private static Runnable makeStreamGang(List<String> wordList,
                                           String[][] inputData,
                                           TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_STREAM:
            return new SearchWithSequentialStream(wordList, 
                                                  inputData);
        case PARALLEL_STREAM_INPUTS:
            return new SearchWithParallelStreamInputs(wordList,
                                                     inputData);
        case PARALLEL_STREAM_WORDS:
            return new SearchWithParallelStreamWords(wordList,
                                                     inputData);
        case PARALLEL_STREAM_WORDS_AND_INPUTS:
            return new SearchWithParallelStreamWordsAndInputs(wordList,
                                                              inputData);
        case COMPLETABLE_FUTURES_WORDS:
            return new SearchWithCompletableFuturesWords(wordList,
                                                         inputData);
        case COMPLETABLE_FUTURES_INPUTS:
            return new SearchWithCompletableFuturesInputs(wordList,
                                                          inputData);
        }
        return null;
    }

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) throws Throwable {
        // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        printDebugging("Starting SearchStreamGangTest");
                     
        String[][] inputData = new String[][] { getInputData(sINPUT_DATA_FILE, "@") };

        List<String> wordList = getWordList(sWORD_LIST_FILE);

        // Create/run StreamGangs to search for words.
        runTests(wordList,
                 inputData);

        // Test a hard-coded parallel streams solution.
        hardCodedParallelStreamsSolution(wordList, inputData);

        // Test a hard-coded sequential solution.
        hardCodedSequentialSolution(wordList, inputData);
 
        printDebugging("Ending SearchStreamGangTest");             
    }

    /**
     * Create/run appropriate type of StreamGang to search for words.
     */
    private static void runTests(List<String> wordList, 
                                 String[][] inputData) {
        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting " + test); 

            // Run the next test.
            makeStreamGang(wordList,
                           inputData,
                           test)
                .run(); 
 
            printDebugging("Ending " + test);

            // Try to run the garbage collector to avoid
            // perturbing the test itself.
            System.gc();
        }
    }

    /**
     * A hard-coded sequential solution.
     */
    private static void hardCodedSequentialSolution(List<String> wordList,
                                                    String[][] inputData) {
        // Create a SearchStreamGang that's used below to find the #
        // of times each word in wordList appears in the inputData.
        SearchStreamGang searchStreamGang =
            new SearchStreamGangSync(wordList,
                                     inputData);

        for (String[] arrayOfStrings : inputData) {
            List<List<SearchResults>> listOfListOfResults = new ArrayList<>();

            // Note the start time.
            long start = System.nanoTime();

            for (String inputString : arrayOfStrings) {
                // Get the section title.
                String title = searchStreamGang.getTitle(inputString);

                // Skip over the title.
                String input = inputString.substring(title.length());

                List<SearchResults> listOfResults = new ArrayList<>();

                for (String word : wordList) {
                    SearchResults results = searchStreamGang.searchForWord(word,
                                                                           input,
                                                                           title);
                    if (results.size() > 0)
                        listOfResults.add(results);
                }
                
                if (listOfResults.size() > 0)
                    listOfListOfResults.add(listOfResults);
            }

            // Print the total processing time.
            System.out.println("hardCodedSequentialSolution"
                               + ": Done in " 
                               + (System.nanoTime() - start) / 1_000_000
                               + " msecs");

            // Determine how many word matches we obtained.
            int totalWordsMatched = listOfListOfResults
                .stream()
                .map(listOfSearchResults -> {
                        // list.stream().forEach(SearchResults::print);
                        	                      	
                        /*
                        // Print the number of words that
                        // matched for each section.
                        System.out.println("matched " 
                        + listOfSearchResults.size()
                        + " words in section " 
                        + listOfSearchResults.get(0).getTitle());
                        */
                        return listOfSearchResults;
                    })               
                        // Compute the total number of times each word
                        // matched the input string.
                        .mapToInt(listOfSearchResults -> listOfSearchResults
                                  .stream()
                                  .mapToInt(SearchResults::size)
                                  .sum())
                        // Sum the results.
                        .sum();

            System.out.println("hardCodedStreamsSolution"
                               + "The search returned = "
                               + totalWordsMatched
                               + " word matches for "
                               + listOfListOfResults.stream().count()
                               + " input strings");
        }
    }

    /**
     * A hard-coded solution that demonstrates how parallel Java 8
     * Streams work without using the StreamGang framework.
     */
    private static void hardCodedParallelStreamsSolution(List<String> wordList,
                                                         String[][] inputData) {
        printDebugging("Starting hardCodedParallelStreamsSolution");

        // Create a SearchStreamGang that's used below to find the #
        // of times each word in wordList appears in the inputData.
        SearchStreamGang searchStreamGang =
            new SearchStreamGangSync(wordList,
                                     inputData);
                                 
        // Convert inputData "array of arrays" into Stream of arrays.
        Stream.of(inputData)
            // Process the stream of input arrays parallel.
            .parallel()

            // Iterate for each array of input strings.
            .forEach(arrayOfInputStrings -> {
                    // Note the start time.
                    long start = System.nanoTime();

                    // The results are stored in a list of input
                    // streams, where each input string is associated
                    // with a list of SearchResults corresponding to
                    // words that matched the input string.
                    List<List<SearchResults>> listOfListOfResults = Stream
                        .of(arrayOfInputStrings)
                        // Process the stream of input data in parallel.
                        .parallel()
                        
                        // Concurrently search each input string for
                        // all occurrences of the words to find.
                        .map(inputString -> {
                                // Get the section title.
                                String title = searchStreamGang.getTitle(inputString);

                                // Skip over the title.
                                String input = inputString.substring(title.length());

                                return wordList
                                // Process the stream of words in parallel.
                                .parallelStream()

                                // Search for all places in the input
                                // String where the word appears and
                                // return a SearchResults object.
                                .map(word -> 
                                     searchStreamGang.searchForWord(word,
                                                                    input,
                                                                    title))

                                // Filter out SearchResults for words
                                // that don't appear.
                                .filter(result -> result.size() > 0)

                                // Collect a list of SearchResults for
                                // each word that matched this input
                                // string.
                                .collect(toList());
                            })
                                              
                        // Collect a list of containing the list of
                        // SearchResults for each input string.
                        .collect(toList());
                
                    // Print the total processing time.
                    System.out.println("hardCodedParallelStreamsSolution"
                                       + ": Done in " 
                                       + (System.nanoTime() - start) / 1_000_000
                                       + " msecs");

                    // Determine how many word matches we obtained.
                    int totalWordsMatched = listOfListOfResults
                        .stream()
                        .map(listOfSearchResults -> {
                                // list.stream().forEach(SearchResults::print);
                        	                      	
                                /*
                                // Print the number of words that
                                // matched for each section.
                                System.out.println("matched " 
                                                   + listOfSearchResults.size()
                                                   + " words in section " 
                                                   + listOfSearchResults.get(0).getTitle());
                                */
                                return listOfSearchResults;
                            })               
                        // Compute the total number of times each word
                        // matched the input string.
                        .mapToInt(listOfSearchResults -> listOfSearchResults
                                  .stream()
                                  .mapToInt(SearchResults::size)
                                  .sum())
                        // Sum the results.
                        .sum();

                    System.out.println("hardCodedStreamsSolution"
                                       + "The search returned = "
                                       + totalWordsMatched
                                       + " word matches for "
                                       + listOfListOfResults.stream().count()
                                       + " input strings");
                });        

        printDebugging("Ending hardCodedParallelStreamsSolution");
    }    
}
