package livelessons.imagestreamgang.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;

import livelessons.imagestreamgang.R;
import livelessons.imagestreamgang.utils.PermissionRequest;

/**
 * Super class that handles permissions.
 */
public class MainActivityBase
        extends Activity {
    /**
     * Debugging tag
     */
    protected String TAG = this.getClass().getName();

    /**
     * Available for sub-classes to set with PermissionRequest#with() call.
     */
    protected PermissionRequest mPermissionRequest;

    /**
     * Handle the onPostCreate() hook to call permission helper to
     * handle all permission requests using the API 23 permission
     * model framework.
     * <p>
     * The framework will callback to request this application to
     * provide a descriptive reason for the permission request that is
     * then displayed to the user. The user has the opportunity to
     * grant or deny the permission request. The callback is also
     * handled automatically by the permission helper class.
     *
     * @param savedInstanceState A saved state or null.
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        // Submit a permission request to ensure that this app has the
        // required permissions for writing and reading external storage.
        mPermissionRequest =
                PermissionRequest.with(this)
                        .permissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .rationale(R.string.permission_read_write_rationale)
                        .granted(R.string.permission_read_write_granted)
                        .denied(R.string.permission_read_write_denied)
                        .snackbar((ViewGroup)findViewById(android.R.id.content))
                        .submit();

        // Always call super class method.
        super.onPostCreate(savedInstanceState);
    }

    /**
     * API 23 (M) callback received when a permissions request has been
     * completed. Redirect callback to permission helper.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        // Redirect hook call to permission helper method.
        if (mPermissionRequest != null) {
            mPermissionRequest.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
            mPermissionRequest = null; // request no longer needed
        }
    }

    /**
     * Hook method invoked when the screen orientation changes.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Logs the orientation of the screen, but doesn't do anything else.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
            Log.d(TAG,
                  "Now running in landscape mode");
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Log.d(TAG,
                  "Now running in portrait mode");
    }
}
