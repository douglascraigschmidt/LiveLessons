package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.utils.FutureUtils;
import livelessons.imagestreamgang.utils.Image;

import static java.util.stream.Collectors.toList;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to
 * download, process, and store images concurrently.
 */
public class ImageStreamCompletableFuture2
       extends ImageStream {

    private static class FilterDecoratorWithImage {
        public FilterDecoratorWithImage(Filter filter, Image image) {
            mFilter = filter;
            mImage = image;
        }
        public Image run() {
            return mFilter.filter(mImage);
        }
        public Filter mFilter;
        public Image mImage;
    }
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
                                        Iterator<List<URL>> urlListIterator,
                                        Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Perform the ImageStream processing, which uses Java 8
     * CompletableFutures to download, process, and store images
     * concurrently.
     */
    @Override
    protected void processStream() {
        final List<CompletableFuture<List<CompletableFuture<Image>>>> listOfFutures = getInput()
                // Concurrently process each URL in the input List.
                .parallelStream()

                // Filter out URLs that are already cached.
                .filter(this::urlNotCached)

                // Submit non-cached URLs for asynchronous downloading,
                // which returns a stream of unfiltered Image futures.
                .map(this::makeImageAsync)

                // After each Image future completes then apply the
                // createFilterDecoratorWithImage() method, which returns
                // a List of FilterDecoratorWithImage objects stored in a
                // CompletableFuture.
                .map(imageFuture ->
                        imageFuture.thenApply(this::makeFilterDecoratorsWithImage))

                // ...
                .map(listFilterDecoratorsFuture ->
                        listFilterDecoratorsFuture.thenCompose(this::applyFiltersAsync))

                // Terminate the stream, which returns a List of filtered images.
                .collect(toList());

        final CompletableFuture<List<List<CompletableFuture<Image>>>> allImagesDone =
                FutureUtils.joinAll(listOfFutures);
        try {
            allImagesDone.get().stream()
                .forEach(list -> list.forEach(imageFuture -> {
                    try {
                        Log.d(TAG,
                                "image file name = "
                                        + imageFuture.get().getFileName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously create an Image from the @a url parameter.
     */
    private CompletableFuture<Image> makeImageAsync(URL url) {
        Log.d(TAG, "In makeImageAsync()" );
        return CompletableFuture.supplyAsync(() -> makeImage(url), getExecutor());
    }

    /**
     * Create a List of FilterDecoratorWithImage objects corresponding
     * to the @a image parameter.
     */
    private List<FilterDecoratorWithImage> makeFilterDecoratorsWithImage(Image image) {
        Log.d(TAG,
                "In makeFilterDecoratorsWithImage()");
        return mFilters
            .parallelStream()
            // Create an OutputDecoratedFilter for each image.
            .map(filter ->
                 new FilterDecoratorWithImage(makeFilterDecorator(filter), 
                                              image))
            .collect(toList());
    }

    /**
     * Asynchronously create an filtered Image from the @a
     * decoratedFilterWithImage parameter.
     */
    private CompletableFuture<List<CompletableFuture<Image>>> applyFiltersAsync
                (List<FilterDecoratorWithImage> decoratedFiltersWithImage) {
        Log.d( TAG,
                "In applyFilterAsync()" );
        List<CompletableFuture<Image>> listOfFutures =
            decoratedFiltersWithImage
            .parallelStream()
            .map(decoratedFilterWithImage -> CompletableFuture.supplyAsync(decoratedFilterWithImage::run,
                                                                           getExecutor()))
            .collect(toList());

        CompletableFuture<List<CompletableFuture<Image>>> future = new CompletableFuture<>();
        future.complete(listOfFutures);
        return future;
    }
}
