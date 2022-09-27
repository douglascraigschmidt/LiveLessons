package folders.server;

import folders.datamodel.Dirent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static folders.common.Constants.EndPoint.*;

/**
 * This Spring controller demonstrates how WebMVC can be used to
 * handle HTTP GET requests via Java streams programming.  These GET
 * requests are mapped to various methods that perform operations on
 * recursively-structured folders containing documents and/or
 * sub-folders.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebMVC uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@code FolderController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping(FOLDERS)
public class FolderController {
    /**
     * This auto-wired field connects the {@link FolderController} to
     * the {@link FolderService}.
     */
    @Autowired
    private FolderService mFolderService;

    /**
     * This method returns a {@link Long} that counts the number of
     * times a {@code word} appears in the folder starting at {@code
     * rootDir}.
     *
     * WebMVC maps HTTP GET requests sent to the
     * "/{rootDir}/SearchFolder" endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A {@link Long} that counts the number of times {@code
     *         word} appears in the folder starting at {@code rootDir}
     */
    @GetMapping(PATH + SEARCH)
    public Long searchWord(@PathVariable String rootDir,
                           @RequestParam String word,
                           @RequestParam Boolean concurrent) {
        return mFolderService
            // Forward to the service.
            .searchWord(rootDir, word, concurrent);
    }

    /**
     * This method returns a {@link List} that containing all the
     * documents where a {@code word} appears in the folder, starting
     * at {@code rootDir}.
     *
     * WebMVC maps HTTP GET requests sent to the
     * "/{rootDir}/getDocuments" endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done
     *                   concurrently or not
     * @return A {@link List} containing all the documents where
     *         {@code word} appears in the folder starting at {@code
     *         rootDir}
     */
    @GetMapping(PATH + GET_DOCUMENTS)
    public List<Dirent> getDocuments(@PathVariable String rootDir,
                                     @RequestParam String word,
                                     @RequestParam Boolean concurrent) {
        return mFolderService
            // Forward to the service.
            .getDocuments(rootDir, word, concurrent);
    }
	
    /**
     * This method returns {@link Long} that counts the number
     * of entries in the folder starting at {@code rootDir}.
     *
     * WebMVC maps HTTP GET requests sent to the
     * "/{rootDir}/countDocuments" endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Long} that counts the number of entries in the
     *         folder starting at {@code rootDir}
     */
    @GetMapping(PATH + COUNT_DOCUMENTS)
    public Long countEntries(@PathVariable String rootDir,
                             @RequestParam Boolean concurrent) {
        return mFolderService
            // Forward to the service.
            .countEntries(rootDir, concurrent);
    }

    /**
     * This method returns {@link Long} that counts the number of
     * lines in entries in the folder starting at {@code rootDir}.
     *
     * WebMVC maps HTTP GET requests sent to the
     * "/{rootDir}/countLines" endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Long} that counts the number of lines in
     *         entries in the folder starting at {@code rootDir}
     */
    @GetMapping(PATH + COUNT_LINES)
    public Long countLines(@PathVariable String rootDir,
                           @RequestParam Boolean concurrent) {
        return mFolderService
            // Forward to the service.
            .countLines(rootDir, concurrent);
    }

    /**
     * This method returns a {@link Dirent} that contains all the
     * entries in the folder, starting at {@code rootDir}.
     *
     * WebMVC maps HTTP GET requests sent to the
     * "/{rootDir}/createFolder" endpoint to this method.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the folder should be created
     *                   concurrently or not
     * @return A {@link Dirent} that contains all the entries in the
     *         folder starting at {@code rootDir}
     */
    @GetMapping(PATH + CREATE_FOLDER)
    public Dirent createFolder(@PathVariable String rootDir,
                               @RequestParam Boolean concurrent) {
        return mFolderService
            // Forward to the service.
            .createFolder(rootDir, concurrent);
    }
}
