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
 *
 */
@RestController
@RequestMapping("/folders")
public class FolderController {
    /**
     *
     */
    @GetMapping("/{rootDir}/_search")
    public Mono<Long> searchWord(@PathVariable String rootDir,
                                 @RequestParam String key) {
        Mono<Dirent> rootFolderM = 
            FolderTests.createFolder(rootDir, true);

        return FolderTests
            .performFolderSearch(rootFolderM, key, true);
    }
	
    /**
     *
     */
    @GetMapping("/{rootDir}/_count")
    public Mono<Long> countEntries(@PathVariable String rootDir) {
        Mono<Dirent> rootFolderM =
            FolderTests.createFolder(rootDir, true);

        return FolderTests
            .performCount(rootFolderM, true);
    }
	
    /**
     *
     */
    @GetMapping("/{rootDir}")
    public Mono<Dirent> createFolder(@PathVariable String rootDir) {
        return FolderTests
            .createFolder(rootDir, true);
    }
}
