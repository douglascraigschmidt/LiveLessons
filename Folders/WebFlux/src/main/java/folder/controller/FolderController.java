package folder.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import folder.Dirent;
import reactor.core.publisher.Mono;
import tests.FolderTests;

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
     * This method returns a count of the number of times a {@code
     * word} appears in the folder starting at {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_search
     * endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for starting at {@code rootDir}
     * @return A count of the number of times {@code word} appears in
     *         the folder starting at {@code rootDir}  
     */
    @GetMapping("/{rootDir}/_search")
    public Mono<Long> searchWord(@PathVariable String rootDir,
                                 @RequestParam String word) {
        return FolderTests
            // Asynchronously and concurrently count the number of
            // times word appears in the folder starting at rootDir.
            .performFolderSearch(rootDir, word, true);
    }
	
    /**
     * This method returns a count of the number of entries in the
     * folder starting at {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_count
     * endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @return A count of the number of entries in the folder starting
     * at {@code rootDir}
     */
    @GetMapping("/{rootDir}/_count")
    public Mono<Long> countEntries(@PathVariable String rootDir) {
        return FolderTests
            // Asynchronously and concurrently count the # of entries
            // in the folder starting at rootDir.
            .performCount(rootDir, true);
    }
	
    /**
     * This method returns all the entries in the folder starting at
     * {@code rootDir}.
     *
     * WebFlux maps HTTP GET requests sent to the /{rootDir} endpoint
     * to this method.
     *
     * @param rootDir The root directory to start the search
     * @return Returns all the entries in the folder starting 
     * at {@code rootDir}
     */
    @GetMapping("/{rootDir}")
    public Mono<Dirent> createFolder(@PathVariable String rootDir) {
        return FolderTests
            // Asynchronously and concurrently create and return a
            // folder starting at rootDir.
            .createFolder(rootDir, true);
    }
}
