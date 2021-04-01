/**
 * A simple class that tracks and print out the task number and phase
 * number.
 */
class MyTask implements Runnable {
    /**
     * Current phase number.
     */
    int mPhaseNumber;

    /**
     * The task number.
     */
    int mTaskNumber;

    /**
     * Constructor initalizes the field.
     */
    MyTask(int taskNumber) {
        mTaskNumber = taskNumber;
    }

    /**
     * Set the phase number.
     */
    void setPhaseNumber(int number) {
        mPhaseNumber = number;
    }

    /**
     * Hook method that runs the task.
     */
    @Override
    public void run() {
        // Print out some diagnostic information.
        System.out.println("Task #" 
                           + mTaskNumber
                           + " has phase #" 
                           + mPhaseNumber 
                           + " at " 
                           + System.currentTimeMillis());
    }
};

