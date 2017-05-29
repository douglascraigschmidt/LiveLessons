package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.TestDataFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different Java 8 mechanisms to
 * implement an "embarrassingly parallel" program that searches for
 * phases in a list of input strings.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        SEQUENTIAL_LOOPS,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_PHASES,
        PARALLEL_STREAMS,
        PARALLEL_SPLITERATOR,
        COMPLETABLE_FUTURES_PHASES,
        COMPLETABLE_FUTURES_INPUTS,
        RXJAVA_PHASES,
        RXJAVA_INPUTS
    }

    /**
     * Factory method that creates the desired type of
     * SearchStreamGang subclass implementation strategy.
     */
    private static SearchStreamGang makeSearchStreamGang(List<String> phaseList,
                                                         List<List<String>> inputData,
                                                         TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_LOOPS:
            return new SearchWithSequentialLoops(phaseList,
                        inputData);
        case SEQUENTIAL_STREAM:
            return new SearchWithSequentialStream(phaseList,
                                                  inputData);
        case PARALLEL_SPLITERATOR:
            return new SearchWithParallelSpliterator(phaseList,
                                                     inputData);
        case PARALLEL_STREAM_INPUTS:
            return new SearchWithParallelStreamInputs(phaseList,
                                                      inputData);
        case PARALLEL_STREAM_PHASES:
            return new SearchWithParallelStreamPhrases(phaseList,
                                                     inputData);
        case PARALLEL_STREAMS:
            return new SearchWithParallelStreams(phaseList,
                                                 inputData);
        case COMPLETABLE_FUTURES_PHASES:
            return new SearchWithCompletableFuturesPhrases(phaseList,
                                                         inputData);
        case COMPLETABLE_FUTURES_INPUTS:
            return new SearchWithCompletableFuturesInputs(phaseList,
                                                          inputData);
        case RXJAVA_INPUTS:
            return new SearchWithRxJavaInputs(phaseList, inputData);
        case RXJAVA_PHASES:
            return new SearchWithRxJavaPhrases(phaseList, inputData);
        }
        return null;
    }

    /*
     * Various input files.
     */
    private static String sSHAKESPEARE_DATA_FILE = "completeWorksOfShakespeare.txt";
    private static String sPHASE_LIST_FILE = "phraseList.txt";

    /**
     * This is the entry point into the test driver program.
     */
    public static void main(String[] args) throws Throwable {
        System.out.println("Starting SearchStreamGangTest");

        // Create the input strings from famous Shakespeare plays,
        // using the '@' symbol as the "splitter" between scenes.
        List<List<String>> inputData =
            new ArrayList<List<String>>() {
                {
                    add(TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE, "#"));
                }
            };

        // Get the list of input phases to find.
        List<String> phaseList = 
            TestDataFactory.getPhrasesList(sPHASE_LIST_FILE);

        // Create/run StreamGangs to search for the phases to find.
        runTests(phaseList,
                 inputData);

        System.out.println("Ending SearchStreamGangTest");
    }

    /**
     * Create/run appropriate type of StreamGang to search for phases.
     */
    private static void runTests(List<String> phaseList,
                                 List<List<String>> inputData) {
        // Run all the SearchStreamGang tests.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test);

            // Use the factory method to make the appropriate
            // SearchStreamGang.
            SearchStreamGang streamGang =
                makeSearchStreamGang(phaseList,
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

            System.out.println("Ending " + test);
        }

        // Sort and display all the timing results.
        printTimingResults(mResultsMap);
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
}
