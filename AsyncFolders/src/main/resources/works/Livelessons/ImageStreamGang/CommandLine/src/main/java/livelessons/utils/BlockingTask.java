package livelessons.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * A Java utility class that simplifies the integration of blocking
 * Runnables and Suppliers with the common Java fork/join thread pool.
 * This class is a variant of the one that is described at
 * http://stackoverflow.com/questions/37512662/is-there-anything-wrong
 * -with-using-i-o-managedblocker-in-java8-parallelstream.
 */
public class BlockingTask {
    /**
     * Logging tag.
     */
    private static final String TAG = BlockingTask.class.getName();

    /**
     * A utility class should always define a private constructor.
     */
    private BlockingTask() {
    }

    /**
     * This method enables blocking Suppliers to be used efficiently
     * with the common Java fork/join thread pool.
     */
    public static<T> T callInManagedBlock(final Supplier<T> supplier) {
        // Create a helper object to encapsulate the supplier.
        final SupplierManagedBlocker<T> managedBlock =
            new SupplierManagedBlocker<>(supplier);

        try {
            /*
            // Submit managedBlock to the common ForkJoin thread pool.
            System.out.println(TAG
                               + "calling managedBlock() in thread "
                               + Thread.currentThread());
                               */
            ForkJoinPool.managedBlock(managedBlock);
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        // Return the results.
        return managedBlock.getResult();
    }

    /**
     * This method enables blocking Runnables to be used efficiently
     * with the common Java fork/join thread pool.
     */
    public static void runInManagedBlock(final Runnable runnable) {
        // Create a helper object to encapsulate the runnable.
        final RunnableManagedBlock managedBlock =
            new RunnableManagedBlock(runnable);

        try {
            // Submit managedBlock to the common ForkJoin thread pool.
            ForkJoinPool.managedBlock(managedBlock);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * This class is an adapter that enables a blocking Supplier to be
     * used efficient with the common fork/join thread pool.
     */
    private static class SupplierManagedBlocker<T>
            implements ForkJoinPool.ManagedBlocker {
        /**
         * The blocking supplier.
         */
        private final Supplier<T> mSupplier;

        /**
         * Result returned when the supplier is done.
         */
        private T mResult;

        /**
         * Keeps track of whether the blocking supplier is done.
         */
        private boolean mDone = false;

        /**
         * Constructor initializes the field.
         */
        private SupplierManagedBlocker(final Supplier<T> supplier) {
            mSupplier = supplier;
        }

        /**
         * Calls the blocking Supplier's get() method.
         */
        @Override
        public boolean block() {
            mResult = mSupplier.get();
            mDone = true;
            return true;
        }

        /**
         * Returns true if blocking supplier has finished, else false.
         */
        @Override
        public boolean isReleasable() {
            return mDone;
        }

        /**
         * Returns the result obtained from the blocking supplier.
         */
        T getResult() {
            return mResult;
        }
    }

    /**
     * This class is an adapter that enables a blocking Runnable to be
     * used efficient with the common fork/join thread pool.
     */
    private static class RunnableManagedBlock
            implements ForkJoinPool.ManagedBlocker {
        /**
         * The blocking runnable.
         */
        private final Runnable mRunnable;

        /**
         * Keeps track of whether the blocking runnable is done.
         */
        private boolean mDone = false;

        /**
         * Constructor initializes the field.
         */
        private RunnableManagedBlock(final Runnable runnable) {
            mRunnable = runnable;
        }

        /**
         * Calls the blocking Runnable's run() method.
         */
        @Override
        public boolean block() {
            mRunnable.run();
            mDone = true;
            return true;
        }

        /**
         * Returns true if blocking supplier has finished, else false.
         */
        @Override
        public boolean isReleasable() {
            return mDone;
        }
    }
}
