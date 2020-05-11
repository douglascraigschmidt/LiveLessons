package folder.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import folder.Dirent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tests.FolderTests;
import utils.ReactorUtils;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET requests via asynchronous reactive programming.
 * These GET requests are mapped to various methods that perform
 * operations on recursively-structured folders containing documents
 * and/or sub-folders.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@code FolderController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser)
 * or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/folders")
public class FolderController {
    /**
     * Memoized copy of the Folder.
     */
    private Mono<Dirent> mMemoizedDirent = null;

    /**
     * This method returns a count of the number of times a {@code
     * word} appears in the folder starting at {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_search
     * endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A count of the number of times {@code word} appears in
     *         the folder starting at {@code rootDir}  
     */
    @GetMapping("/{rootDir}/_search")
    public Mono<Long> searchWord(@PathVariable String rootDir,
                                 @RequestParam String word,
                                 @RequestParam Boolean concurrent) {
        return FolderTests
            // Asynchronously and concurrently count the # of
            // times word appears in folder starting at rootDir.
            .performFolderSearch(rootDir, word, concurrent);
    }
	
    /**
     * This method returns a count of the number of entries in the
     * folder starting at {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_count
     * endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently or not
     * @return A count of the number of entries in the folder starting
     *         at {@code rootDir}
     */
    @GetMapping("/{rootDir}/_count")
    public Mono<Long> countEntries(@PathVariable String rootDir,
                                   @RequestParam Boolean concurrent) {
        return FolderTests
            // Asynchronously and concurrently count the # of entries
            // in the folder starting at rootDir.
            .performCount(rootDir, concurrent);
    }
	
    /**
     * This method returns all the entries in the folder starting at
     * {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_create
     * endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param memoize True if the created folder should be cached
     * @param concurrent True if the folder should be created concurrently or not
     * @return Returns all the entries in the folder starting 
     *         at {@code rootDir}
     */
    @GetMapping("/{rootDir}/_create")
    public Mono<Dirent> createFolder(@PathVariable String rootDir,
                                     @RequestParam Boolean memoize,
                                     @RequestParam Boolean concurrent) {
        if (memoize) {
            if (mMemoizedDirent != null)
                // Return the cached folder contents.
                return mMemoizedDirent;
            else {
                mMemoizedDirent = FolderTests
                    // Asynchronously and concurrently create and
                    // return a folder starting at rootDir.
                    .createFolder(rootDir, concurrent);

                // Cache the results.
                //.cache();
            }
            return mMemoizedDirent;     
        } else 
            return FolderTests
                // Asynchronously and concurrently create and return a
                // folder starting at rootDir.
                .createFolder(rootDir, true);
    }
}
