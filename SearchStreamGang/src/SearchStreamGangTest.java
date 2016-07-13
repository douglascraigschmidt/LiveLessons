import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        PARALLEL_STREAM_WORDS,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_WORDS_AND_INPUTS,
            //    COMPLETABLE_FUTURES,
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

            /*
        case COMPLETABLE_FUTURES
            return new SearchWithCompletableFutures(wordList,
                                                    inputData);
            */
        }
        return null;
    }

    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) throws Throwable {
        // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        printDebugging("Starting SearchStreamGangTest");
                     
        String[][] inputData = new String[][] { getInputData(sINPUT_DATA_FILE, "@") };

        List<String> wordList = getWordList(sWORD_LIST_FILE);

        // Test a hard-coded streams solution.
        hardCodedStreamsSolution(wordList,
                                 inputData);

        // Create/run appropriate type of StreamGang to search for words.
        Stream.of(TestsToRun.values())
              .forEach(test -> {
                       printDebugging("Starting " + test); 

                       // Run the next test.
                       makeStreamGang(wordList,
                                      inputData,
                                      test)
                           .run(); 
 
                       printDebugging("Ending " + test);
                  });

        printDebugging("Ending SearchStreamGangTest");             
    }

    /**
     * A hard-coded solution.
     */
    private static void hardCodedStreamsSolution(List<String> wordList,
                                                 String[][] inputData) {
        printDebugging("Starting hardCodedStreamsSolution");

        // Create a SearchStreamGang that's used to find the number of
        // times each word in wordList appears in the inputData.
        SearchStreamGang searchStreamGang =
            new SearchStreamGang(wordList,
                                 inputData);
                                 
        // Convert inputData "array of arrays" into Stream of arrays.
        Stream.of(inputData)
            // Process the stream of input arrays parallel.
            .parallel()

            // Iterate for each array of input data.
            .forEach(arrayOfInputData -> {
                    // Note the start time.
                    long start = System.nanoTime();

                    List<Long> results = Stream
                        .of(arrayOfInputData)
                        // Process the stream of input data in parallel.
                        .parallel()

                        // Search the inputData for all occurrences of the words to find.
                        .map(inputString -> {
                                // Get the section title.
                                String title = searchStreamGang.getTitle(inputString);

                                return wordList
                                // Process the stream in parallel.
                                .parallelStream()

                                // Concurrently search for all places
                                // where the word matches the input data.
                                .map(word -> 
                                     searchStreamGang.searchForWord(word,
                                                                    // Skip over the title.
                                                                    inputString.substring(title.length()),
                                                                    title))

                                // Only keep a result that has at least one match.
                                .filter(result -> result.size() > 0)

                                // @@ Fix me!
                                // .sorted(SearchResults::getTitle)

                                // .map(SearchResults::print)                           

                                // Perform a reduce operation that counts
                                // the number of times each word occurred.
                                .collect(Collectors.toList()).stream()
                                .mapToLong(SearchResults::size)
                                .sum();
                            })

                        // Return a list of the number of strings that were processed.
                        .collect(Collectors.toList());

                    // Print the processing time.
                    System.out.println("hardCodedStreamsSolution" 
                                       + ": Done in " 
                                       + (System.nanoTime() - start) / 1_000_000
                                       + " msecs");

                    System.out.println("hardCodedStreamsSolution"
                                       + "The search returned = "
                                       + results.stream().mapToLong(Long::longValue).sum()
                                       + " word matches for "
                                       + results.stream().count()
                                       + " input strings");
                });        

        printDebugging("Ending hardCodedStreamsSolution");
    }    
}
