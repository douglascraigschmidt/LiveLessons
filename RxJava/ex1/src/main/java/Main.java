import folder.Dirent;
import folder.Folder;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;
import utils.TestDataFactory;

/**
 * This example shows the use of RxJava to process entries in a
 * recursively-structured directory folder sequentially and in
 * parallel.
 */
public class Main {
    /**
     * The input "works" to process, which is a large recursive folder
     * containing thousands of subfolders and files.
     */
    private static final String sWORKS = "works";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) throws InterruptedException {
        display("Starting test");
        Single<Dirent> rootFolderS =
            RxUtils.makeHotSingle(createFolder(sWORKS);

        SingleSubject subject = SingleSubject.create();
        rootFolderS.subscribe(subject);

        countEntries(subject);
        countEntries(subject);

        display("Ending test");
    }

    /**
     * Asynchronously create an in-memory folder containing all the works.
     *
     * @param works Name of the directory in the file system containing the works.
     * @return A folder containing all works in {@code works}
     */
    private static Single<Dirent> createFolder(String works) {
        // Return a single to the initialized folder.
        return Folder
            // Asynchronously create a folder containing all the works
            // in the root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works));
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     *
     * @param rootFolderS In-memory folder containing the works.
     */
    private static void countEntries(Single<Dirent> rootFolderS) {
        display("Entering countEntries");

        rootFolderS
            .flatMap(rootFolder -> {
                    System.out.println("folder size = "
                                       + rootFolder.getSize());

                    return Observable
                        // Create a stream of dirents starting at the rootFolder.
                        .fromIterable(rootFolder)

                        .doOnNext(dirent -> System.out.println(dirent.getName()))

                        // Count the # of elements in the stream.
                        .count();
                })
            .blockingSubscribe(count -> // Print the results.
                               display("number of entries in the folder = "
                                       + count));

        display("Leaving countEntries");
    }

    /**
     * Display {@code string} if the program is run in verbose mode.
     */
    static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }
}
