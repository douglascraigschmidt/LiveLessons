package livelessons.imagestreamgang.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;

/**
 * Provides some general utility helper methods.
 */
public final class UiUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = "UiUtils";

    /**
     * Helper to show a short toast message.
     *
     * @param context activity context
     * @param text    string to display
     */
    @SuppressWarnings({"SameParameterValue", "unused"})
    @UiThread
    public static void showToast(Context context, String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(
                    "showToast requires a valid string");
        }

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

        // Also duplicate Toast message in log file for debugging.
        Log.d(TAG, text);
    }

    /**
     * Helper to show a short toast message.
     *
     * @param context activity context
     * @param id      resource id of string to display
     */
    @UiThread
    public static void showToast(Context context, @StringRes int id) {
        Toast.makeText(context, id, Toast.LENGTH_SHORT).show();

        // Also duplicate Toast message in log file fro debugging.
        Log.d(TAG, context.getResources().getString(id));
    }
}
