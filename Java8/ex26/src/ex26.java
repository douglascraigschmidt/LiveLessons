import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows various ways to apply one-shot and cyclic Java
 * Phasers.
 */
public class ex26 {
    /**
     * Number of iterations.
     */
    private static final int sITERATIONS = 10;

    /**
     * Number of tasks.
     */
    private static final int sNUMBER_OF_TASKS = 10;

    /**
     * A simple class that tracks and print out the task number and
     * phase number.
     */
    static class MyTask 
        implements Runnable {
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

    /**
     * A test showcasing a one-shot Phaser that runs a group of {@code
     * tasks} simultaneously.
     */
    private static void runOneShotTasks(List<MyTask> tasks) {
        System.out.println("Entering runOneShotTasks()");

        // Create a phaser that plays the role of an entry barrier
        // and is initialized with a value of 1 to register itself.
        Phaser entryPhaser = new Phaser(1);

        // Create a phaser that plays the role of an exit barrier
        // and is initialized with the number of tasks to complete.
        // This usage pattern of Phaser is similar to a CountDownLatch.
        Phaser exitPhaser = new Phaser(tasks.size());

        // Iterate through all the tasks.
        tasks.forEach(task -> {
                // Register the party with the phaser.
                entryPhaser.register();

                // Create/start a new thread to run the task when all
                // other threads are ready.
                new Thread(() -> {
                        // Await start of all the threads.
                        int phaseNumber = entryPhaser.arriveAndAwaitAdvance();

                        // Set the phase number (used for diagnostics).
                        task.setPhaseNumber(phaseNumber);

                        // Run the task.
                        task.run();

                        // Indicate that the thread has arrived at the
                        // exit barrier and is terminating, which acts
                        // like CountDownLatch.countDown().
                        exitPhaser.arrive();
                }).start();
            });

        // Allow calling thread to continue and deregister self so
        // threads can run.
        entryPhaser.arriveAndDeregister();

        // Block on the exit barrier until all the threads exit.
        exitPhaser.awaitAdvance(0);
        System.out.println("Leaving runOneShotTasks()");
    }

    /**
     * A test that showcases a cyclic Phaser that repeatedly performs
     * actions on the List of {@code tasks} for a given number of
     * {@code iterations}.
     */
    private static void runCyclicTasks(List<MyTask> tasks, int iterations) {
        System.out.println("Entering runCyclicTasks()");

        // Create a phaser that iterates 'iterations' number of times.
        Phaser phaser = new Phaser() {
                /**
                 * Hook method that decides whether to terminate the
                 * phaser or not.
                 */
                @Override
                protected boolean onAdvance(int phase, int regParties) {
                    // Terminate phaser when we've reached the number of
                    // iterations or there are no more parties registered.
                    return (phase + 1) == iterations || regParties == 0;
                }
            };

        // Register the calling thread (to defer worker threads
        // advancing to next phase until the end of this method) and
        // all of the tasks (so we don't need to do this within the
        // forEach() loop below).
        phaser.bulkRegister(1 + tasks.size());

        // Iterate through all the tasks.
        tasks.forEach(task -> {
                // Create/start a new thread to run the task.
                new Thread(() -> { 
                        do {
                            // Run the task.
                            task.run();

                            // Await phase completion of all other
                            // tasks/threads.
                            int phaseNumber = phaser.arriveAndAwaitAdvance();

                            // Set phase number (used for
                            // diagnostics).
                            task.setPhaseNumber(phaseNumber);
                        } while (!phaser.isTerminated());
                        // Loop until the phaser's terminated by
                        // onAdvance().
                }).start();
            });

        // Loop until the phaser's terminated by onAdvance().
        while (!phaser.isTerminated())
            // Await phase completion of all other
            // tasks/threads.
            phaser.arriveAndAwaitAdvance();

        System.out.println("Leaving runCyclicTasks()");
    }


    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        System.out.println("Starting ex26 test");

        // Run the test showcasing a one-shot Phaser that starts
        // running a group of tasks simultaneously.
        runOneShotTasks(makeTasks());

        // Run the test that showcases a cyclic Phaser that repeatedly
        // performs actions for a given number of iterations.
        runCyclicTasks(makeTasks(), sITERATIONS);

        System.out.println("Finishing ex26 test");
    }
    /**
     * @return A List of MyTask objects
     */
    private static List<MyTask> makeTasks() {
        // Create and return a list of tasks.
        return IntStream
                // Create a stream from 1 to sNUMBER_OF_TASKS.
                .rangeClosed(1, sNUMBER_OF_TASKS)

                // Create a new MyTask object for each number in the
                // stream.
                .mapToObj(MyTask::new)

                // Convert the stream into a list.
                .collect(toList());
    }

}
