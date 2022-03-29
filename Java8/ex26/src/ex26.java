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

    /**
     * This test showcases a one-shot {@link Phaser} that runs a
     * {@link List} of {@code tasks} simultaneously.
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
        tasks
            .forEach(task -> {
                    // Register the party with the phaser.
                    entryPhaser.register();

                    // Create/start a new thread to run the task when
                    // all other threads are ready.
                    new Thread(makeOneShotRunnable(entryPhaser,
                                                   exitPhaser,
                                                   task)).start();
                });

        // Allow calling thread to continue and deregister itself so
        // the other threads can start to run.
        entryPhaser.arriveAndDeregister();

        // Block on the exit barrier until all the threads exit.
        exitPhaser.awaitAdvance(0);

        System.out.println("Leaving runOneShotTasks()");
    }

    /**
     * This factory method creates a {@link Runnable} that
     * demonstrates the use of Java entry and exit
     * {@link Phaser} objects.
     *
     * @param entryPhaser An entry {@link Phaser} instance
     * @param exitPhaser  An exit {@link Phaser} instance
     * @param task A {@link MyTask} instance
     * @return An initialized {@link Runnable}
     */
    private static Runnable makeOneShotRunnable(Phaser entryPhaser,
                                                Phaser exitPhaser,
                                                MyTask task) {
        // Return a runnable lambda.
        return () -> {
            // Await start of all the threads.
            int phaseNumber = entryPhaser.arriveAndAwaitAdvance();

            // Set the phase numbers (used for diagnostics).
            task.setPhaseNumbers(phaseNumber,
                                 exitPhaser.getPhase());

            // Run the task.
            task.run();

            // Indicate that the thread has arrived at the exit
            // barrier and is terminating, which acts like
            // CountDownLatch.countDown().
            exitPhaser.arrive();
        };
    }

    /**
     * This test showcases a cyclic {@link Phaser} that repeatedly
     * performs actions on the {@link List} of {@code tasks} for a
     * given number of {@code iterations}.
     */
    private static void runCyclicTasks(List<MyTask> tasks,
                                       int iterations) {
        System.out.println("Entering runCyclicTasks()");

        // Create a phaser that iterates 'iterations' number of times.
        Phaser phaser = new Phaser() {
                /**
                 * Hook method that decides whether to terminate the
                 * phaser or not at the end of each phase.
                 */
                @Override
                protected boolean onAdvance(int phase, int regParties) {
                    // Terminate phaser when the number of iterations
                    // are reached or no more parties are registered.
                    return (phase + 1) == iterations || regParties == 0;
                }
            };

        // Register the calling thread (to defer worker threads
        // advancing to next phase via a loop) and all the tasks (so
        // we don't need to do this within forEach() below).
        phaser.bulkRegister(1 + tasks.size());

        // Iterate through all the tasks.
        tasks
            .forEach(task ->
                     // Create/start a new thread that demonstrates a
                     // cyclic Phaser.
                     new Thread(makeCyclicRunnable(phaser, task))
                                .start());

        // Loop until the phaser's terminated by onAdvance().
        while (!phaser.isTerminated())
            // Await phase completion of all tasks running
            // in other threads.
            phaser.arriveAndAwaitAdvance();

        System.out.println("Leaving runCyclicTasks()");
    }

    /**
     * This factory method creates a {@link Runnable} that
     * demonstrates a Java cyclic {@link Phaser}.
     *
     * @param phaser An {@link Phaser} instance
     * @param task A {@link MyTask} instance
     * @return An initialized {@link Runnable}
     */
    private static Runnable makeCyclicRunnable(Phaser phaser,
                                               MyTask task) {
        // Return a runnable lambda.
        return () -> {
            do {
                // Run the task.
                task.run();

                // Await phase completion of all other tasks/threads.
                int phaseNumber = phaser.arriveAndAwaitAdvance();

                // Set phase number (used for diagnostics).
                task.setPhaseNumbers(phaseNumber, -1);
            } while (!phaser.isTerminated());
            // Loop until phaser's terminated by onAdvance().
        };
    }
}
