package edu.vandy.gcdtesttask.view;

import android.support.v4.app.Fragment;

import edu.vandy.visfwk.view.AbstractMainFragmentActivity;

/**
 * The main (Fragment) Activity of the GCD app that showcases the
 * CountDownLatch barrier synchronizer.  This Activity is listed in
 * the AndroidManifest.xml file and launched by Android.
 */
public class GCDMainActivity
       extends AbstractMainFragmentActivity {
    /**
     * This factory method sets what Fragment to use in this app.
     *
     * @return Fragment to display in the Activity.
     */
    @Override
    public Fragment makeTesterFragment() {
        return new TestTaskFragment();
    }
}
