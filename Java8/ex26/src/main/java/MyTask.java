/**
 * A simple class that tracks and print out the task number and phase
 * number.
 */
class MyTask implements Runnable {
    /**
     * Current entry phase number.
     */
    int mEntryPhase;

    /**
     * Current entry phase number.
     */
    int mExitPhase = -1;

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
     * Set the phase numbers.
     */
    void setPhaseNumbers(int entryPhase,
                         int exitPhase) {
        mEntryPhase = entryPhase;
        mExitPhase = exitPhase;
    }

    /**
     * Hook method that runs the task.
     */
    @Override
    public void run() {
        String exitPhase = mExitPhase == -1
          ? ""
          : " and exit phase #" + mExitPhase;

        // Print out some diagnostic information.
        System.out.println("Task #" 
                           + mTaskNumber
                           + " has entry phase #"
                           + mEntryPhase
                           + exitPhase
                           + " at " 
                           + System.currentTimeMillis());
    }
};

