package edu.vandy.lockmanager.common;

/**
 * The {@code Callback} interface defines methods for handling the
 * completion or failure of an asynchronous operation.
 *
 */
public interface Callback {
    /**
     * Called when the asynchronous operation completes successfully.
     *
     * @param result the lock returned by the operation
     */
    void onSuccess(Lock result);

    /**
     * Called when an error occurs during the asynchronous operation.
     *
     * @param throwable the throwable representing the error
     */
    void onError(Throwable throwable);
}
