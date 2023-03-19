package vandy.mooc.pingpong.model;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import vandy.mooc.pingpong.presenter.PingPongPresenter;

/**
 * This class is a proxy that provides access to the PingPongThread.
 * It plays the "Model" role in the Model-View-Presenter (MVP) pattern
 * by acting upon requests from the Presenter.  It implements a
 * factory method that creates the appropriate subclasses of
 * PingPongThread.
 */
public class PingPongModel {
    /**
     * Used for Android debugging.
     */
    private final static String TAG = 
        PingPongModel.class.getName();

    /**
     * Maximum number of ping and pong threads.
     */
    private final static int sMAX_PING_PONG_THREADS = 2;

    /**
     * Constants used to distinguish between ping and pong threads.
     */
    private final static int sPING_THREAD = 0;
    private final static int sPONG_THREAD = 1;

    /**
     * Factory method that creates the designated instances of the
     * PingPongThread subclass based on the @code syncMechanism
     * parameter.
     */
    public List<Thread> makePingPongThreads(PingPongPresenter presenter,
                                            int maxIterations,
                                            String syncMechanism) {
        // Create a list of Threads.
        List<Thread> threadList =
            Arrays.asList(new Thread[sMAX_PING_PONG_THREADS]);

        // Create the appropriate subclass of PingPongThread based on
        // the syncMechanism string.
        switch (syncMechanism) {
            case "SEMA": {
                // Create the Java Semaphores that ensure threads print
                // "ping" and "pong" in the correct alternating order.
                Semaphore pingSema =
                        new Semaphore(1); // Starts out unlocked.

                Semaphore pongSema =
                        new Semaphore(0);

                threadList.set(sPING_THREAD,
                               new PingPongThreadSema(presenter,
                                                      "ping",
                                                      pingSema,
                                                      pongSema,
                                                      maxIterations));
                threadList.set(sPONG_THREAD,
                               new PingPongThreadSema(presenter,
                                                      "pong",
                                                      pongSema,
                                                      pingSema,
                                                      maxIterations));
                break;
            }
            case "MONOBJ": {
                // Create the BinarySemaphores (implemented as Java
                // build-in monitor objects) that ensure threads print
                // "ping" and "pong" in the correct alternating order.
                PingPongThreadMonObj.BinarySemaphore pingSema =
                        new PingPongThreadMonObj.BinarySemaphore(true); // Start out unlocked.

                PingPongThreadMonObj.BinarySemaphore pongSema =
                        new PingPongThreadMonObj.BinarySemaphore(false);

                threadList.set(sPING_THREAD,
                               new PingPongThreadMonObj(presenter,
                                                        "ping",
                                                        pingSema,
                                                        pongSema,
                                                        maxIterations));
                threadList.set(sPONG_THREAD,
                               new PingPongThreadMonObj(presenter,
                                                        "pong",
                                                        pongSema,
                                                        pingSema,
                                                        maxIterations));
                break;
            }
            case "COND":
                // Create the ReentrantLock and Conditions that ensure
                // threads print "ping" and "pong" in the correct
                // alternating order.
                ReentrantLock lock = new ReentrantLock();
                Condition pingCond = lock.newCondition();
                Condition pongCond = lock.newCondition();

                // Create the ping and pong threads.
                PingPongThreadCond pingThread = 
                    new PingPongThreadCond(presenter,
                                           "ping",
                                           lock,
                                           pingCond,
                                           pongCond,
                                           true,
                                           maxIterations);
        
                PingPongThreadCond pongThread = 
                    new PingPongThreadCond(presenter,
                                           "pong",
                                           lock,
                                           pongCond,
                                           pingCond,
                                           false,
                                           maxIterations);

                // Exchange Thread IDs.
                pingThread.setOtherThreadId(pongThread.getId());
                pongThread.setOtherThreadId(pingThread.getId());

                // Set the ping and pong threads in the list.
                threadList.set(sPING_THREAD,
                               pingThread);
                threadList.set(sPONG_THREAD,
                               pongThread);
                break;
        }

        return threadList;
    }
}
