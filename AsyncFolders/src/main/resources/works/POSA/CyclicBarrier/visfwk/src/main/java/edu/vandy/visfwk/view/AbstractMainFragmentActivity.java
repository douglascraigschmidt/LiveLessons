package edu.vandy.visfwk.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import edu.vandy.visfwk.R;

/**
 * Super class for the app's main (Fragment) Activity.
 */
public abstract class AbstractMainFragmentActivity
       extends AppCompatActivity {
    /**
     * TAG for storing/retrieving the TestTaskFragment from the
     * FragmentManager.
     */
    private final static String TestTaskFragmentName =
        "TestTaskFragment";

    /**
     * The onCreate lifecycle method
     *
     * @param savedInstanceState
     * 	If the activity is being re-initialized after previously being
     * 	shut down then this Bundle contains the data it most recently
     * 	supplied in onSaveInstanceState(Bundle). Note: Otherwise it is
     * 	null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view (layout to use).
        setContentView(R.layout.main_fragment_activity);

        // Get fragment manager from support library.
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TestTaskFragmentName) == null) {
            // Add tester fragment.
            FragmentTransaction trx = fm.beginTransaction();

            // Create fragment to display.
            Fragment fragment =
                makeTesterFragment();

            // Configure the FragmentManger to retain this fragment
            // even upon Activity restart.
            fragment.setRetainInstance(true);

            // Add fragment to Activity layout and commit the
            // transaction.
            trx.add(R.id.testing_fragment_container,
                    fragment,
                    TestTaskFragmentName);
            trx.commit();
        }
    }

    /**
     * This factory method sets what fragment to use in this app.
     *
     * @return Fragment to display in the Activity.
     */
    public abstract Fragment makeTesterFragment();

    /**
     * Handle Menu being pressed.
     *
     * @param menu
     * 	The options menu in which you place your items.
     *
     * @return boolean You must return true for the menu to be
     *                 displayed; if you return false it will  
     *                 not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.menu_main,
                                  menu);
        return true;
    }

    /**
     * Handle Menu Item being pressed.
     *
     * @param item
     * 	the item pressed.
     *
     * @return boolean Return false to allow normal menu processing to
     *                 proceed, true to consume it here. 
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) 
            return true;

        return super.onOptionsItemSelected(item);
    }
}
