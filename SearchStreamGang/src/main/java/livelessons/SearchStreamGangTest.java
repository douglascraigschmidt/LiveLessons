package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.SearchResults;
import livelessons.utils.TestDataFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different Java 8 mechanisms to
 * implement an "embarrassingly parallel" program that searches for
 * words in a list of input strings.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_WORDS,
        PARALLEL_STREAMS,
        COMPLETABLE_FUTURES_WORDS,
        COMPLETABLE_FUTURES_INPUTS,
        RXJAVA_WORDS,
        RXJAVA_INPUTS
    }

    /**
     * Factory method that creates the desired type of
     * SearchStreamGang subclass implementation strategy.
     */
    private static SearchStreamGang makeSearchStreamGang(List<String> wordList,
                                                         List<List<String>> inputData,
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
        case PARALLEL_STREAMS:
            return new SearchWithParallelStreams(wordList,
                                                 inputData);
        case COMPLETABLE_FUTURES_WORDS:
            return new SearchWithCompletableFuturesWords(wordList,
                                                         inputData);
        case COMPLETABLE_FUTURES_INPUTS:
            return new SearchWithCompletableFuturesInputs(wordList,
                                                          inputData);
        case RXJAVA_INPUTS:
            return new SearchWithRxJavaInputs(wordList, inputData);
        case RXJAVA_WORDS:
            return new SearchWithRxJavaWords(wordList, inputData); }

        return null;
    }

    /*
     * Various input files.
     */
    private static String sHAMLET_DATA_FILE = "hamlet.txt";
    private static String sMACBETH_DATA_FILE = "macbeth.txt";
    private static String sWORD_LIST_FILE = "wordList.txt";

    /**
     * This is the entry point into the test driver program.
     */
    public static void main(String[] args) throws Throwable {
        printDebugging("Starting SearchStreamGangTest");

        // Create the input strings from famous Shakespeare plays,
        // using the '@' symbol as the "splitter" between scenes.
        List<List<String>> inputData =
            new ArrayList<List<String>>() {
                {
                    add(TestDataFactory.getInput(sHAMLET_DATA_FILE,
                                                 "@"));
                    add(TestDataFactory.getInput(sMACBETH_DATA_FILE,
                                                 "@"));
                }
            };

        // Get the list of input words to find.
        List<String> wordList = 
            TestDataFactory.getWordsList(sWORD_LIST_FILE);

        // Create/run StreamGangs to search for the words to find.
        runTests(wordList,
                 inputData);

        printDebugging("Ending SearchStreamGangTest");
    }

    /**
     * Create/run appropriate type of StreamGang to search for words.
     */
    private static void runTests(List<String> wordList,
                                 List<List<String>> inputData) {
        // Measure the performance of a "hard-coded" sequential
        // streams implementation strategy.
        hardCodedSequentialSolution(wordList, inputData);

        // Measure the performance of a "hard-coded" parallel streams
        // implementation strategy.
        hardCodedParallelStreamsSolution(wordList, inputData);

        // Run all the SearchStreamGang tests.
        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting " + test);

            // Use the factory method to make the appropriate
            // SearchStreamGang.
            SearchStreamGang streamGang =
                makeSearchStreamGang(wordList,
                                     inputData,
                                     test);

            // Execute the test.
            assert streamGang != null;
            streamGang.run();

            // Store the execution times into the results map.
            mResultsMap.put(test.toString(),
                            streamGang.executionTimes());

            // Run the garbage collector to free up memory and
            // minimize timing perturbations on each test.
            System.gc();

            printDebugging("Ending " + test);
        }

        // Sort and display all the timing results.
        printTimingResults(mResultsMap);
    }

    /**
     * A hard-coded sequential solution.
     */
    private static void hardCodedSequentialSolution(List<String> wordList,
                                                    List<List<String>> inputData) {
        printDebugging("Starting hardCodedSequentialSolution");
        // Create a SearchStreamGang that's used below to find the #
        // of times each word in wordList appears in the inputData.
        SearchStreamGang streamGang =
            new SearchStreamGang(wordList,
                                 inputData);

        // Run the hard-coded sequential stream test on all the input
        // data.
        for (List<String> listOfStrings : inputData) {
            List<SearchResults> listOfSearchResults =
                new ArrayList<>();

            streamGang.startTiming();

            for (String inputString : listOfStrings) {
                // Get the section title.
                String title = streamGang.getTitle(inputString);

                // Skip over the title.
                String input = inputString.substring(title.length());

                for (String word : wordList) {
                    SearchResults results = streamGang.searchForWord(word,
                                                                     input,
                                                                     title);
                    if (results.size() > 0)
                        listOfSearchResults.add(results);
                }
            }

            streamGang.stopTiming();

            // Store the execution times.
            mResultsMap.put("hardCodedSequentialSolution",
                            streamGang.executionTimes());

            int totalWordsMatched = listOfSearchResults
                .stream()
                // Compute the total number of times each word
                // matched the input string.
                .mapToInt(SearchResults::size)
                // Sum the results.
                .sum();

            System.out.println("hardCodedSequentialSolution"
                               + ": The search returned = "
                               + totalWordsMatched
                               + " word matches for "
                               + listOfStrings.size()
                               + " input strings");
        }
    }

    /**
     * A hard-coded solution that demonstrates how parallel Java 8
     * Streams work without using the StreamGang framework.
     */
    private static void hardCodedParallelStreamsSolution(List<String> wordList,
                                                         List<List<String>> inputData) {
        printDebugging("Starting hardCodedParallelStreamsSolution");

        // Create a SearchStreamGang that's used below to find the #
        // of times each word in wordList appears in the inputData.
        SearchStreamGang streamGang =
            new SearchStreamGang(wordList,
                                 inputData);

        inputData
            // Process the stream of input strings in parallel.
            .parallelStream()

            // Iterate for each array of input strings.
            .forEach(listOfStrings -> {
                streamGang.startTiming();

                // The results are stored in a list of input streams,
                // where each input string is associated with a list
                // of SearchResults corresponding to words that
                // matched the input string.
                List<SearchResults> listOfSearchResults = listOfStrings
                    // Process the stream of input data in parallel.
                    .parallelStream()

                    // Concurrently search each input string for all
                    // occurrences of the words to find.
                    .map(inputString -> {
                        // Get the section title.
                        String title = 
                        streamGang.getTitle(inputString);

                        // Skip over the title.
                        String input =
                        inputString.substring(title.length());

                        return wordList
                            // Process the stream of words in parallel.
                            .parallelStream()

                            // Search for all places in the input
                            // String where the word appears and
                            // return a SearchResults object.
                            .map(word ->
                                 streamGang.searchForWord(word,
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

                    // Flatten the stream of lists of SearchResults
                    // into a stream of SearchResults.
                    .flatMap(List::stream)

                    // Collect a list of containing SearchResults
                    // for each input string.
                    .collect(toList());

                streamGang.stopTiming();

                // Store the execution times.
                mResultsMap.put("hardCodedParallelStreamsSolution",
                                streamGang.executionTimes());

                // Determine how many word matches we obtained.
                // SearchResults::print();
                int totalWordsMatched = listOfSearchResults
                    .stream()

                    // Compute the total number of times each word
                    // matched the input string.
                    .mapToInt(SearchResults::size)

                    // Sum the results.
                    .sum();

                System.out.println("hardCodedParallelStreamsSolution"
                                   + ": The search returned = "
                                   + totalWordsMatched
                                   + " word matches for "
                                   + listOfStrings.size()
                                   + " input strings");
                });

        printDebugging("Ending hardCodedParallelStreamsSolution");
    }

    /**
     * Print out all the timing results for all the test runs in order
     * from fastest to slowest.
     */
    private static void printTimingResults(Map<String, List<Long>> resultsMap) {
        // Determine how many runs of the tests took place.
        int numberOfRuns =
            resultsMap.entrySet().iterator().next().getValue().size();

        // This local class is needed to make the Java 8 compiler happy.
        final class ResultMap extends TreeMap<Long, String> {
        }

        // Create a list of TreeMaps to hold the timing results in
        // sorted order.
        List<ResultMap> listOfMaps =
            Stream.generate(ResultMap::new)
            .limit(numberOfRuns)
            .collect(toList());

        // Initialize the TreeMaps to contain the results from each
        // timing test.
        IntStream.range(0, numberOfRuns)
            // Iterate through each of the test runs.
            .forEach(treeIndex ->
                     // Get the entry set from the map.
                     // Iterate through each entry in the map.
                     resultsMap.forEach((key, value) -> {
                             // Get the appropriate tree map.
                             ResultMap map = listOfMaps.get(treeIndex);

                             // Store results into the tree map, whose
                             // key is time in msecs and whose value
                             // is test that ran.
                             map.put(value.get(treeIndex),
                                     key);
                         }));

        // Print the results of the test runs from fastest to slowest.
        IntStream.range(0, numberOfRuns)
            // Iterate through each of the test runs.
            .forEach(treeIndex -> {
                    System.out.println("\nPrinting results for input file "
                                       + (treeIndex + 1)
                                       + " from fastest to slowest");
                    // Print results of test run with name of the
                    // test first followed by time in msecs.
                    listOfMaps
                        // Get the appropriate TreeMap.
                        .get(treeIndex)

                        // Get the entry set from the map.
                        .forEach((key, value) -> System.out.println(""
                                                                    + value
                                                                    + " executed in "
                                                                    + key
                                                                    + " msecs"));
                });
    }

    /**
     * Keep track of which SearchStreamGang performed the best.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();

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
}
