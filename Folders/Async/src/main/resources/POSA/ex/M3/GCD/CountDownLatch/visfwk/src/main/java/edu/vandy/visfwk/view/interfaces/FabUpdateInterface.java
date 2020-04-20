package edu.vandy.visfwk.view.interfaces;

import android.support.design.widget.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Interface (With default methods) to allow Presenter Layer to
 * interact with FABs.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface FabUpdateInterface {
    /**
     * Storage used to bypass limitations of variables in Interfaces
     * for storing references to the two FABs.
     */
    ArrayList<WeakReference<FloatingActionButton>> buttons =
            new ArrayList<>(2);

    /**
     * Get the set FAB.
     */
    default FloatingActionButton getFABSet() {
        return getFAB(0);
    }

    /**
     * Get the start/stop FAB.
     */
    default FloatingActionButton getFABStartOrStop() {
        return getFAB(1);
    }

    /**
     * Helper method for localizing access to the FABs so that no
     * other class has to know about underlying storage in this
     * Interface.
     */
    default FloatingActionButton getFAB(int location) {
        if (buttons.get(location) == null
                || buttons.get(location).get() == null)
            throw new RuntimeException("FAB was not initialized in code for " +
                    "default interface methods to work");

        return buttons.get(location).get();
    }

    /**
     * Initialize with the set FAB to allow this Interface and its
     * default methods to operate properly upon the FAB created by the
     * UserInterface Views.
     */
    default void initializeFABSet(FloatingActionButton floatingActionButton) {
        buttons.add(0,
                new WeakReference<>(floatingActionButton));
    }

    /**
     * Initialize with the start/stop FAB to allow this Interface and
     * its default methods to operate properly upon the FAB created by
     * the UserInterface Views.
     */
    default void initializeFABStartStop(FloatingActionButton floatingActionButton) {
        buttons.add(1,
                new WeakReference<>(floatingActionButton));
    }
}
