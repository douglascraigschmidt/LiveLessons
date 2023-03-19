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
    private static int sITERATIONS = 10;

    /**
     * Number of tasks.
     */
    private static int sNUMBER_OF_TASKS = 10;

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
     * A test showcasing a one-shot Phaser that starts running a group
     * of tasks simultaneously.
     */
    private static void runTasks(List<MyTask> tasks) {
        // Create a new phaser with a value of 1 to register itself.
        Phaser phaser = new Phaser(1);

        // Iterate through all the tasks.
        tasks.forEach(task -> {
                // Register the party with the phaser.
                phaser.register();

                // Create/start a new thread to run the task when all
                // other threads are ready.
                new Thread(() -> {
                        // Await start of all the threads.
                        int phaseNumber = phaser.arriveAndAwaitAdvance();

                        // Set the phase number (used for diagnostics).
                        task.setPhaseNumber(phaseNumber);

                        // Run the task.
                        task.run();
                }).start();
            });

        // Allow calling thread to continue & deregister self so
        // threads can run.
        phaser.arriveAndDeregister();
    }

    /**
     * A test that showcases a cyclic Phaser that repeatedly performs
     * actions for a given number of iterations.
     */
    private static void startTasks(List<MyTask> tasks, int iterations) {
        // Create a new phaser that iterates a given number of times.
        Phaser phaser = new Phaser() {
                /**
                 * Hook method that decides whether to terminate the
                 * phaser or not.
                 */
                @Override
                protected boolean onAdvance(int phase, int regParties) {
                    // Terminate phaser when we've reached the number of
                    // iterators or there are no more parties registered.
                    return phase >= iterations || regParties == 0; 
                }
            };

        // Register to defer worker threads advancing to next phase
        // until the end of this method.
        phaser.register();

        // Iterate through all the tasks.
        tasks.forEach(task -> {
                // Register party with the phaser.
                phaser.register();

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
                        // Loop until phaser's terminated by onAdvance().
                }).start();
            });

        // Deregister self (allowing tasks to advance to next phase)
        // and don't wait.
        phaser.arriveAndDeregister();
    }


    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        System.out.println("Starting ex25 test");

        // Create a list of tasks.
        List<MyTask> tasks = IntStream
            // Create a stream from 1 to sNUMBER_OF_TASKS.
            .rangeClosed(1, sNUMBER_OF_TASKS)

            // Create a new MyTask object for each number in the
            // stream.
            .mapToObj(MyTask::new)

            // Convert the stream into a list.
            .collect(toList());

        // Run the test showcasing a one-shot Phaser that starts
        // running a group of tasks simultaneously.
        runTasks(tasks);

        // Run the test that showcases a cyclic Phaser that repeatedly
        // performs actions for a given number of iterations.
        startTasks(tasks, sITERATIONS);

        System.out.println("Finishing ex26 test");
    }

}
