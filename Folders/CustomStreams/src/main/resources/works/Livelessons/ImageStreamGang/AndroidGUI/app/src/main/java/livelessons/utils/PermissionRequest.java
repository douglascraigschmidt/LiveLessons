package livelessons.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import livelessons.R;

/**
 * Support methods used to implement the API permission code model.
 */
@TargetApi(Build.VERSION_CODES.M)
public class PermissionRequest {
    /**
     * Logging tag.
     */
    private static final String TAG = "PermissionRequest";

    /**
     * Generic permission code used by this class.
     */
    private static AtomicInteger mRequestId = new AtomicInteger(0);

    private final Activity mActivity;
    private final ViewGroup mLayout;
    private final int mRationaleId;
    private final int mGrantedId;
    private final int mDeniedId;
    private final String[] mPermissions;
    private final Callback mCallback;
    private int mRequestCode;

    private PermissionRequest(Builder builder) {
        mActivity = builder.mActivity;
        mLayout = builder.mLayout;
        mRationaleId = builder.mRationaleId;
        mGrantedId = builder.mGrantedId;
        mDeniedId = builder.mDeniedId;
        mPermissions = builder.mPermissions;
        mCallback = builder.mCallback;
    }

    /**
     * Check that all given permissions have been granted by verifying that each
     * entry in the given array is of the value
     * {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int... grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted,
        // otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the {@code activity} target for this permission request and returns
     * a Fluent Builder object for setting additional request attributes. The
     * activity is required to either extend PermissionActivity or override
     * ContextCompat#onRequestPermissionResults() and redirect the call to
     * PermissionRequest.onRequestPermissionResults().
     *
     * @param activity The {@code mActivity} for the request.
     * @return A new Builder object.
     */
    @NonNull
    public static Builder with(@NonNull Activity activity) {
        return new Builder(activity);
    }

    /**
     * Submits a requests for a set of permissions for an activity.
     */
    private PermissionRequest submit() {
        int showRationale = 0;

        ArrayList<String> requests = new ArrayList<>();

        for (final String permission : mPermissions) {
            if (mActivity.checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                requests.add(permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity, permission)) {
                    showRationale++;
                }
            }
        }

        mRequestCode = mRequestId.addAndGet(1);

        if (requests.isEmpty()) {
            // All the requests are already granted so immediately call the
            // callback handler.
            if (mCallback != null) {
                mCallback.onPermissionsGranted();
            }
        } else {
            // Permission has not been granted yet, so submit a request.
            if (showRationale == 0) {
                // No rationale required; submit the request.
                ActivityCompat.requestPermissions(
                        mActivity, mPermissions, mRequestCode);
            } else {
                // Provide an additional rationale to the user if the permission
                // was not granted and the user would benefit from additional
                // context for the use of the permission. For example if the
                // user has previously denied the permission.
                showRationale();
            }
        }

        return this;
    }

    /**
     * Called from PermissionActivity to process the results of a permission
     * request. Activities using this class that do not extend the
     * PermissionActivity class are required to override
     * ContextCompat#onRequestPermissionResults() and redirect the call to this
     * method.
     *
     * @param requestCode  The request code passed in
     * {@link Builder#permissions(String...)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either
     *                     {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never
     *                     null.
     */
    public boolean onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        // display the grant or denial of this permission request
        // via an unobtrusive toast message.
        if (verifyPermissions(grantResults)) {
            // Show granted message.
            showMessage(mGrantedId);
            if (mCallback != null) {
                mCallback.onPermissionsGranted();
            }
        } else {
            // Show denied message.
            showMessage(mDeniedId);
            if (mCallback != null) {
                mCallback.onPermissionsDenied();
            }
        }

        return true;
    }

    /**
     * Uses either Snackbar or Toast depending on snackbar snackbar setting.
     *
     * @param id A string resource to display.
     */
    @MainThread
    private void showMessage(@StringRes int id) {
        if (mLayout != null) {
            Snackbar.make(
                    mLayout, id, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, id, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convenience method that shows an about dialog for this application. It
     * has been placed in this ViewUtils class so that it can be easily accessed
     * from any activity.
     */
    private void showRationale() {
        String msg;

        // If the default rational is set, then add the list of requested
        // permissions as a format argument. Note that this is just the default
        // behaviour and apps should design and set their own meaningful
        // rationales.
        if (mRationaleId == R.string.permission_rationale) {
            String permissionStrings = "";
            for (final String permission : mPermissions) {
                permissionStrings += "\n" + permission;
            }

            msg = mActivity.getString(R.string.permission_rationale)
                    + permissionStrings;
        } else {
            // App supplied rationale.
            msg = mActivity.getString(mRationaleId);
        }

        if (mLayout != null) {
            Snackbar snackbar =
                    Snackbar.make(
                            mLayout,
                            msg,
                            Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(
                    R.string.permissions_ok_button,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /// Submit the request.
                            ActivityCompat.requestPermissions(
                                    mActivity, mPermissions, mRequestCode);
                        }
                    });

            snackbar.show();
        } else {
            final AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(mActivity);

            // Set dialog title.
            alertDialogBuilder.setTitle("Permission Request");

            // Set dialog message.
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setPositiveButton(
                    R.string.permissions_ok_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            /// Submit the request.
                            ActivityCompat.requestPermissions(
                                    mActivity, mPermissions, mRequestCode);
                        }
                    });

            // Create alert dialog.
            final AlertDialog alertDialog = alertDialogBuilder.create();

            // Show it.
            alertDialog.show();
        }
    }

    /**
     * Callback invoked when the permission request completes.
     */
    public interface Callback {
        void onPermissionsGranted();

        void onPermissionsDenied();
    }

    /**
     * {@code PermissionRequest} builder static inner class.
     */
    public static final class Builder {
        private final Activity mActivity;
        private ViewGroup mLayout;
        private int mRationaleId;
        private int mGrantedId;
        private int mDeniedId;
        private String[] mPermissions;
        private Callback mCallback;

        private Builder(Activity activity) {
            mActivity = activity;
        }

        /**
         * Sets a layout used for Snackbar display. If this value is not set,
         * permission rationale requests will use a dialog and resuls will be
         * posted in a Toast message.
         *
         * @param layout the {@code snackbar} to set
         * @return A reference to this Builder
         */
        @NonNull
        public Builder snackbar(@NonNull ViewGroup layout) {
            if (mLayout != null) {
                throw new IllegalArgumentException(
                        "A snackbar layout has already been set.");
            }
            mLayout = layout;
            return this;
        }

        /**
         * Sets the a string resource that will be displayed in a SnackBar when
         * requesting the permissions.
         *
         * @param id A string resource explaining why the permissions are
         *           needed.
         * @return A reference to this Builder
         */
        @NonNull
        public Builder rationale(@StringRes int id) {
            if (mRationaleId != 0) {
                throw new IllegalArgumentException(
                        "A rationale string resource has already been set.");
            }
            mRationaleId = id;
            return this;
        }

        /**
         * Sets the a string resource that will be displayed in a SnackBar when
         * the requested permissions are all granted.
         *
         * @param id A string resource displaying a granted message.
         * @return A reference to this Builder
         */
        @NonNull
        public Builder granted(@StringRes int id) {
            if (mGrantedId != 0) {
                throw new IllegalArgumentException(
                        "A granted string resource has already been set.");
            }
            mGrantedId = id;
            return this;
        }

        /**
         * Sets the a string resource that will be displayed in a SnackBar when
         * any of the requested permissions is denied.
         *
         * @param id A string resource displaying a permission denied message.
         * @return A reference to this Builder
         */
        @NonNull
        public Builder denied(@StringRes int id) {
            if (mDeniedId != 0) {
                throw new IllegalArgumentException(
                        "A granted string resource has already been set.");
            }
            mDeniedId = id;
            return this;
        }

        /**
         * Sets the permissions that will be requested.
         *
         * @param permissions The permissions to request.
         * @return a reference to this Builder
         */
        @NonNull
        public Builder permissions(@NonNull String... permissions) {
            if (mPermissions != null) {
                throw new IllegalArgumentException(
                        "Permissions have already been set.");
            }
            mPermissions = permissions;
            return this;
        }

        /**
         * Sets the a Callback that will be used to notify the results of the
         * permission request.
         *
         * @param callback the callback to set
         * @return a reference to this Builder
         */
        @NonNull
        public Builder callback(@NonNull Callback callback) {
            if (mCallback != null) {
                throw new IllegalArgumentException(
                        "A callback has already been set.");
            }
            mCallback = callback;
            return this;
        }

        /**
         * Returns a {@code PermissionRequest} built from the parameters
         * previously set.
         *
         * @return a {@code PermissionRequest} built with parameters of this
         * {@code PermissionRequest.Builder}
         */
        @NonNull
        public PermissionRequest submit() {
            // Validate required fields.
            if (mActivity == null) {
                throw new NullPointerException("An activity must be set.");
            }
            if (mPermissions == null) {
                throw new NullPointerException("Permissions must be set.");
            }

            if (mRationaleId == 0) {
                Log.w(TAG, "Default rationale should only be used during "
                        + "development.");

                mRationaleId = R.string.permission_rationale;
            }

            if (mGrantedId == 0) {
                mGrantedId = R.string.permissions_granted;
            }

            if (mDeniedId == 0) {
                mDeniedId = R.string.permissions_denied;
            }

            return new PermissionRequest(this).submit();
        }
    }
}
