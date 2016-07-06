package livelessons.imagestreamgang;

import android.app.Application;
import android.content.Context;

public class TheApp extends Application {
    /**
     * Static self-reference for static helpers.
     */
    private static TheApp sTheApp;

    /**
     * Save application instance in static for easy access.
     */
    public TheApp() {
        sTheApp = this;
    }

    /**
     * Returns application instance.
     *
     * @return The application instance.
     */
    public static TheApp getApp() {
        return sTheApp;
    }

    /**
     * Returns application instance as a context.
     *
     * @return The application instance as a context.
     */
    public static Context getContext() {
        return sTheApp;
    }
}
