package livelessons.imagestreamgang.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;

import livelessons.imagestreamgang.R;

/**
 * Support methods used to implement the API permission code model.
 */
@TargetApi(Build.VERSION_CODES.M)
public final class PermissionUtils {
    /**
     * Callback ID used in permission request calls.
     */
    private static final int REQUEST_EXTERNAL_STORAGE_READ_WRITE = 1;

    /*
     * Activity lifecycle helper methods.
     */

    /**
     * Handle the onPostCreate() hook to issue a request to the API 23
     * permission model framework for read and write external storage.
     * <p/>
     * The framework will callback to request this application to
     * provide a descriptive reason for the permission request that is
     * then displayed to the user. The user has the opportunity to
     * grant or deny the permission request.
     *
     * @param savedInstanceState A saved state or null.
     */
    @SuppressWarnings("UnusedParameters")
    public static void onPostCreate(Activity activity,
                                    @Nullable Bundle savedInstanceState) {
        // For downloading images to external storage, we need comply
        // with the API 23 permission model that explicitly requests
        // permissions from the user. In our case we need to read
        // and write to external storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Call helper to do all the work.
            requestExternalStorageReadWritePermission(activity);
        }
    }

    /*
     * API M Permission helper methods.
     */

    /**
     * Handles API 23 compliant request for read/write to external storage.
     */
    @SuppressWarnings("WeakerAccess")
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestExternalStorageReadWritePermission(Activity activity) {
        final boolean readPermission =
            ActivityCompat.checkSelfPermission
                (activity, 
                 Manifest.permission.READ_EXTERNAL_STORAGE) 
            == PackageManager.PERMISSION_GRANTED;
        final boolean writePermission =
            ActivityCompat.checkSelfPermission
                (activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED;

        // External storage read/write permission has not been
        // granted.
        if (!readPermission || !writePermission) 
            PermissionUtils.requestExternalStorageReadWritePermission
                (activity,
                 (ViewGroup) activity.findViewById(android.R.id.content));
    }

    /**
     * Helper method to be called from the activity's
     * onRequestPermissionResults() hook method which is called when a
     * permissions request has been completed.
     *
     * @return returns true if the permission is handled; false if not.
     */
    @SuppressWarnings({"UnusedReturnValue", "UnusedParameters"})
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean onRequestPermissionsResult(Activity activity,
                                                     int requestCode,
                                                     @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode
            == PermissionUtils.REQUEST_EXTERNAL_STORAGE_READ_WRITE) {
            // Received a read/write external storage permission request code.

            // Get the activity's main view.
            ViewGroup layout =
                (ViewGroup) activity.findViewById(android.R.id.content);
            assert layout != null;

            // Now display the grant or denial of this permission request
            // via an unobtrusive toast message.
            if (grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Show granted replay message.
                Snackbar.make(layout,
                              R.string.permission_available_external_storage,
                              Snackbar.LENGTH_SHORT).show();
            } else {
                // Show denied replay message.
                Snackbar.make(layout, R.string.permissions_not_granted,
                              Snackbar.LENGTH_SHORT).show();
            }

            // Signal that we have handled the permissions.
            return true;
        } else {
            // Signal that we did not handle the permissions.
            return false;
        }
    }

    /**
     * Check that all given permissions have been granted by verifying
     * that each entry in the given array is of the value {@link
     * PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    @SuppressWarnings("unused")
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) 
            return false;

        // Verify that each required permission has been granted,
        // otherwise return false.
        for (int result : grantResults) 
            if (result != PackageManager.PERMISSION_GRANTED) 
                return false;

        return true;
    }

    /**
     * Requests the external storage read/write permission.  If the
     * permission has been denied previously, a SnackBar will prompt
     * the user to grant the permission, otherwise it is requested
     * directly.
     */
    private static void requestExternalStorageReadWritePermission
        (final Activity activity, final ViewGroup layout) {

        boolean shouldRequestRead =
            ActivityCompat.shouldShowRequestPermissionRationale
                (activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean shouldRequestWrite =
            ActivityCompat.shouldShowRequestPermissionRationale
                (activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (shouldRequestRead || shouldRequestWrite) {
            // Provide an additional rationale to the user if the
            // permission was not granted and the user would benefit
            // from additional context for the use of the
            // permission. For example if the user has previously
            // denied the permission.
            Snackbar snackbar =
                Snackbar.make(layout,
                              R.string.read_write_permission,
                              Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction
                (R.string.ok_button,
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         ActivityCompat.requestPermissions
                             (activity, 
                              new String[] {
                                 Manifest.permission.READ_EXTERNAL_STORAGE,
                                 Manifest.permission.WRITE_EXTERNAL_STORAGE},
                              REQUEST_EXTERNAL_STORAGE_READ_WRITE);
                     }
                 });

            snackbar.show();
        } else {
            // Permission has not been granted yet. Request it
            // directly.
            ActivityCompat.requestPermissions
                (activity,
                 new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                 REQUEST_EXTERNAL_STORAGE_READ_WRITE);
        }
    }
}
