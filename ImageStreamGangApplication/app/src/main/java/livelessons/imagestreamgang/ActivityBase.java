package livelessons.imagestreamgang;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import livelessons.imagestreamgang.utils.PermissionUtils;

/**
 * Super class that handles permissions.
 */
public class ActivityBase
       extends Activity {
    /**
     * Handle the onPostCreate() hook to call permission helper to handle all
     * permission requests using the API 23 permission model framework.
     * <p>
     * The framework will callback to request this application to provide a
     * descriptive reason for the permission request that is then displayed to
     * the user. The user has the opportunity to grant or deny the permission
     * request. The callback is also handled automatically by the permission
     * helper class.
     *
     * @param savedInstanceState A saved state or null.
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        // Call permission helper to manage all API 23 permission requests.
        PermissionUtils.onPostCreate(this, savedInstanceState);

        // Always call super class method.
        super.onPostCreate(savedInstanceState);
    }

    /**
     * API 23 (M) callback received when a permissions request has been
     * completed. Redirect request to permission helper.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Redirect hook call to permission helper method.
        PermissionUtils.onRequestPermissionsResult(this,
                                                   requestCode,
                                                   permissions,
                                                   grantResults);
    }
}
