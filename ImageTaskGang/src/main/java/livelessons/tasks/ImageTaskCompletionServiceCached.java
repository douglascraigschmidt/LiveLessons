package livelessons.tasks;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.filters.OutputFilterDecorator;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Customizes the {@link ImageTaskGang} to use the {@link
 * ExecutorCompletionService} in conjunction with a cached thread pool
 * to concurrently download a list of images from web servers, apply
 * image processing filters to each image, and store the results in
 * files that can be displayed to users via various means defined by
 * the context in which this class is used.
 *
 * This class implements the roles of the "Proactive Initiator" and
 * "Completion Handler" in the Proactor pattern and also plays the
 * role of the "Concrete Class" in the Template Method pattern.
 */
public class ImageTaskCompletionServiceCached
       extends ImageTaskCompletionService {
    /**
     * Constructor initializes the superclass and fields.
     */
    public ImageTaskCompletionServiceCached(Filter[] filters,
                                            List<List<URL>> urlLists) {
        // Initialize the super class.
        super(filters, 
              urlLists,
              "ImageTaskCompletionServiceCached");
    }

    /**
     * Hook method that returns a cached thread pool implementation of
     * the {@link Executor}.
     */
    @Override
    public Executor executorHook() {
        // Create an Executor with a cached pool of threads, which
        // grow and shrink dynamically as new tasks are executed.
        return Executors.newCachedThreadPool();
    }
}    
