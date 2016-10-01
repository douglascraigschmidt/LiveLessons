import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * A generic class that performs a designated task on each element of
 * input data of type T.
 */
public class InputProcessingThread<T>
       extends Thread {
    /**
     * Current index into the input data.
     */ 
    private int mIndex;

    /**
     * The input list that's processed one element at a time by a
     * task.
     */
    private final List<T> mInputData;

    /**
     * Task to apply on an input data element.
     */
    private Function<T, Void> mTask;

    /**
     * Constructor initializes the fields to store the @a task to
     * perform on each element of the @a inputData.
     */
    public InputProcessingThread(Function<T, Void> task,
                                 List<T> inputData) {
        mIndex = 0;
        mInputData = inputData;
        mTask = task;
    }
    /**
     * Factory method that creates a Thread that will apply a task to
     * process one element of the input data in the background.
     */
    public Thread createThread() {
        return new Thread(()
                          // Create lambda to run in background
                          // Thread.
                          -> {
                              // Get input data associated with this
                              // index and increment the index.
                              T element = 
                                  mInputData.get(mIndex++);
                                  
                              // Apply the task to process the input
                              // data element.
                              mTask.apply(element);
        });
    }

    /**
     * @return the size of the input data list.
     */
    int size() {
        return mInputData.size();
    }
}

