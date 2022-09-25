package vandy.mooc.threadconfig.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Uses retained fragments to manage state information across runtime
 * configuration changes to an activity.  Plays the role of the
 * "Originator" in the Memento pattern.
 */
public class RetainedFragmentManager {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();

    /**
     * Name used to identify the RetainedFragment.
     */
    private final String mRetainedFragmentTag;
        
    /**
     * WeakReference to the FragmentManager.
     */
    private final WeakReference<FragmentManager> mFragmentManager;

    /**
     * Reference to the RetainedFragment.
     */
    private RetainedFragment mRetainedFragment;

    /**
     * Constructor initializes fields.
     */ 
    public RetainedFragmentManager(FragmentManager fragmentManager,
                                   String retainedFragmentTag) {
        // Store a WeakReference to the Activity.
        mFragmentManager =
            new WeakReference<FragmentManager>(fragmentManager);

        // Store the tag used to identify the RetainedFragment.
        mRetainedFragmentTag = retainedFragmentTag;
    }

    /**
     * Initializes the RetainedFragment the first time it's called.
     *
     * @returns true if it's first time the method's been called, else
     *          false.
     */
    public boolean firstTimeIn() {
        try {
            // Find the RetainedFragment on Activity restarts.  The
            // RetainedFragment has no UI so it must be referenced via
            // a tag.
            mRetainedFragment = (RetainedFragment) 
                mFragmentManager.get().findFragmentByTag(mRetainedFragmentTag);

            // A value of null means it's the first time in, so there's
            // extra work to do.
            if (mRetainedFragment == null) {
                Log.d(TAG,
                      "Creating new RetainedFragment "
                      + mRetainedFragmentTag);

                // Create a new RetainedFragment.
                mRetainedFragment = new RetainedFragment();

                // Commit this RetainedFragment to the FragmentManager.
                mFragmentManager.get().beginTransaction().
                    add(mRetainedFragment,
                        mRetainedFragmentTag).commit();
                return true;
            } 
            // A value of non-null means it's not first time in.
            else {
                Log.d(TAG,
                      "Returning existing RetainedFragment "
                      + mRetainedFragmentTag);
                return false;
            }
        } catch (NullPointerException e) {
            Log.d(TAG,
                  "NPE in firstTimeIn()");
            return false;
        }
    }

    /**
     * Add the @a object with the @a key.
     */
    public void put(String key, Object object) {
        mRetainedFragment.put(key, object);
    }

    /**
     * Add the @a object with its class name.
     */
    public void put(Object object) {
        put(object.getClass().getName(), object);
    }

    /**
     * Get the object with @a key.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) mRetainedFragment.get(key);
    }

    /**
     * Return the Activity the RetainedFragment is attached to or null
     * if it's not currently attached.
     */
    public Activity getActivity() {
        return mRetainedFragment.getActivity();
    }

    /**
     * "Headless" Fragment that retains state information between
     * configuration changes.  Plays the role of the "Memento" in the
     * Memento pattern.
     */
    public static class RetainedFragment extends Fragment {
        /**
         * Maps keys to objects.
         */
        private HashMap<String, Object> mData =
            new HashMap<String, Object>();

        /**
         * Hook method called when a new instance of Fragment is
         * created.
         *
         * @param savedInstanceState
         *            object that contains saved state information.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Ensure the data survives runtime configuration changes.
            setRetainInstance(true);
        }

        /**
         * Add the @a object with the @a key.
         */
        public void put(String key, Object object) {
            mData.put(key, object);
        }

        /**
         * Add the @a object with its class name.
         */
        public void put(Object object) {
            put(object.getClass().getName(), object);
        }

        /**
         * Get the object with @a key.
         */
        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) mData.get(key);
        }
    }
}
