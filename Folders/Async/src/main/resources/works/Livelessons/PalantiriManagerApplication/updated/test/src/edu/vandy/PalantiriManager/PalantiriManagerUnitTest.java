package edu.vandy.PalantiriManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import edu.vandy.model.Palantir;
import edu.vandy.model.PalantiriManager;
import edu.vandy.presenter.BeingThread;
import edu.vandy.utils.Options;

/**
 * Unit test for the PalantiriManager.
 */
public class PalantiriManagerUnitTest {
    /** 
     * Keep track of if a runtime exception occurs
     */
    volatile boolean mFailed = false;
    
    /**
     * Keep track of if a thread is interrupted.
     */
    volatile boolean mInterrupted = false;

    protected volatile boolean exc = false;
    
    @Test
        public void testPalantiriManager() {
        PalantiriManager palantiriManager = makePalantiri(2);
        assertNotNull(palantiriManager);
    }

    public PalantiriManager makePalantiri(int palantiriCount) {
    	// Create a list to hold the generated Palantiri.
        final List<Palantir> palantiri =
            new ArrayList<Palantir>();		

        // Create a new Random number generator.
        final Random random = new Random();

        // Create and add each new Palantir into the list.  The id of
        // each Palantir is its position in the list.
        for (int i = 0; i < palantiriCount; ++i) 
            palantiri.add(new Palantir(i,
                                       random));

        // Create a PalantiriManager that is used to mediate
        // concurrent access to the List of Palantiri.
        return new PalantiriManager(palantiri);
    }

    @Test
    public void testAcquire() throws InterruptedException {
        Thread t = new BeingThread(new Runnable(){
                @Override
                    public void run() {
                        try {
                        PalantiriManager palantiriManager =
                            makePalantiri(2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        palantiriManager.acquire();
		        assertEquals(palantiriManager.availablePermits(), 1);
		        palantiriManager.acquire();
		        assertEquals(palantiriManager.availablePermits(), 0);
                    }
                    catch(AssertionError e){
                        exc = true;
                        System.out.println(e);
                    }
                }
        	
            }, 0, null);
        t.start();
        t.join();
        assertEquals(exc,false);
        exc = false;
    }

    @Test
        public void testRelease() throws InterruptedException {
    	Thread t = new BeingThread(new Runnable(){

                @Override
                    public void run() {
                    try {
                        PalantiriManager palantiriManager =
                            makePalantiri(2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        Palantir palantir1 = palantiriManager.acquire();
                        assertEquals(palantiriManager.availablePermits(), 1);
                        Palantir palantir2 = palantiriManager.acquire();
                        assertEquals(palantiriManager.availablePermits(), 0);
                        palantiriManager.release(palantir1);
                        assertEquals(palantiriManager.availablePermits(), 1);
                        palantiriManager.release(palantir2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        palantiriManager.release(null);
                        assertEquals(palantiriManager.availablePermits(), 2);
                    }
                    catch(AssertionError e){
                        exc = true;
                    }
                }
        	
            }, 0, null);
    	t.start();
    	t.join();
    	assertEquals(exc,false);
    	exc = true;
    }
	
    @Test
        public void testAvailablePermits() throws InterruptedException{
        Thread t = new BeingThread(new Runnable(){
                @Override
                    public void run() {
                    try{
                        PalantiriManager palantiriManager =
   		            makePalantiri(2);
   		        assertEquals(palantiriManager.availablePermits(), 2);
   		        palantiriManager.acquire();
   		        assertEquals(palantiriManager.availablePermits(), 1);
                    }
                    catch(AssertionError e) {
                        exc = true;
                    }
                }
           	
            }, 0, null);
        t.start();
        t.join();
        assertEquals(exc,false);
        exc = true;
    }
    
    @Test
    public void testConcurrentAccess() {
    	// The number of threads that will be trying to run at once.
    	final int THREAD_COUNT = 5;

    	// The number of threads that we want to let run at once.
    	final int PERMIT_COUNT = 2;

    	// The number of times each thread will try to access the
    	// semaphore.
    	final int ACCESS_COUNT = 5;

    	final PalantiriManager palantiriManager =
            makePalantiri(PERMIT_COUNT);

    	assertTrue(THREAD_COUNT > PERMIT_COUNT);
    	
    	// The number of threads that currently have a permit.
    	final AtomicLong runningThreads = new AtomicLong(0);

    	// Keep track of the threads we have so we can wait for them
    	// to finish later.
    	Thread threads[] =
            new Thread[THREAD_COUNT];
    	
    	for (int i = 0;
             i < THREAD_COUNT;
             ++i) {
            final Thread t = new BeingThread(new Runnable() {
                    @Override
                    public void run() {
                        Random random = new Random();
                        for (int i = 0;
                             i < ACCESS_COUNT;
                             ++i) {
                            Palantir palantir;
                            try { 
                                // Acquire a permit from the Manager.
                                palantir = palantiriManager.acquire();
                            }
                            catch (Exception e) {
                                mInterrupted = true;
                                return;
                            }
		                    
                            // Increment the number of threads that have a permit.
                            long running = runningThreads.incrementAndGet();
	                        
                            // If there are more threads running than
                            // are supposed to be, throw an error.
                            if (running > PERMIT_COUNT)
                                throw new RuntimeException();
	                        
                            // Wait for an indeterminate amount of time.
                            try {
                                Thread.sleep(random.nextInt(140) + 10);
                            } catch (InterruptedException e) {}
	                        
                            // Decrement the number of threads that have a permit.
                            runningThreads.decrementAndGet();
	                        
                            // Release the permit
                            palantiriManager.release(palantir);
                        }
                    }
                }, 
                i,
                null);
    		
            // If any of the threads throw an exception, then we failed.
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        mFailed = true;
                    }
                });
    		
            threads[i] = t;
    	}

        for (final Thread t : threads)
            t.start();
    	
        for (final Thread t : threads)
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("The main thread was interrupted for some reason.");
            }
    	
    	assertFalse(mFailed);
    	assertFalse("One of the threads was interrupted while calling acquire(). This shouldn't "
                    + "happen (even if your Semaphore is wrong).", mInterrupted);
    }
}
