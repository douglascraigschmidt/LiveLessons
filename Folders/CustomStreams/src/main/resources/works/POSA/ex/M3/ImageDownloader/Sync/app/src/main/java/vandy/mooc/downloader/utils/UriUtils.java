package vandy.mooc.downloader.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.URLUtil;
import java.io.File;
import java.util.List;

/**
 * A utility class containing methods for creating and manipulating
 * Uri objects.
 */
public class UriUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = "UriUtils";

    /**
     * File provider identifier.
     */
    private static final String FILE_PROVIDER_AUTHORITY =
            "vandy.mooc.downloader.fileprovider";

    private UriUtils() {
        throw new AssertionError();
    }

    /**
     * Builds an action intent and converts the passed local file uri
     * to a content uri with read permission for all applications that
     * can process the intent. This method is designed to work on all
     * versions of Android.
     *
     * @param context  A context.
     * @param pathName A local file path.
     * @param action   The intent action.
     * @param type     The intent type.
     * @return The built intent.
     */
    public static Intent buildFileProviderReadUriIntent(Context context,
                                                        String pathName,
                                                        String action,
                                                        String type) {
        // Build a content uri.
        Uri uri = FileProvider.getUriForFile
                       (context,
                        getFileProviderAuthority(),
                        new File(pathName));

        // Create and initialize the intent.
        Intent intent =
                new Intent()
                        .setAction(action)
                        .setDataAndType(uri, type);

        // Call helper method that uses the most secure permission granting
        // model for the each API.
        grantUriPermissions
                (context,
                 intent,
                 Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return intent;
    }

    /**
     * Builds an action intent and converts the passed local file uri
     * to a content uri with read permission for all applications that
     * can process the intent. This method is designed to work on all
     * versions of Android.
     *
     * @param context A context.
     * @param uri     A local file uri.
     * @param action  The intent action.
     * @param type    The intent type.
     * @return The built intent.
     */
    public static Intent buildFileProviderReadUriIntent(Context context,
                                                        Uri uri,
                                                        String action,
                                                        String type) {
        return buildFileProviderReadUriIntent(context,
                getPathNameFromFileUri(uri),
                action,
                type);
    }

    /**
     * @return Application file provider authority.
     */
    public static String getFileProviderAuthority() {
        return FILE_PROVIDER_AUTHORITY;
    }

    /**
     * Grants the specified uri permissions to all packages that can
     * process the intent. The most secure granting model is used for
     * the current API. This method is designed to work on all
     * versions of Android but has been tested only on API 23, and 24.
     *
     * @param context     A context.
     * @param intent      An intent containing a data uri that was obtained from
     *                    FileProvider.getUriForFile().
     * @param permissions The permissions to grant.
     */
    public static void grantUriPermissions(Context context,
                                           Intent intent,
                                           int permissions) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Find all packages that support this intent and grant
            // them the specified permissions.
            List<ResolveInfo> resInfoList =
                context.getPackageManager().queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(
                    packageName,
                    intent.getData(),
                    permissions);
            }
        } else {
            // Just grant permissions to all apps.
            intent.addFlags(permissions);
        }
    }

    /**
     * Converts a local file uri to a local path name. This will throw an
     * IllegalArgumentException if the passed uri is not a valid file uri.
     *
     * @param uri A uri that references a local file.
     * @return The path name suitable for passing to the File class.
     */
    public static String getPathNameFromFileUri(Uri uri) {
        if (!URLUtil.isFileUrl(uri.toString())) 
            throw new IllegalArgumentException("Invalid file uri");

        return uri.getPath();
    }
}
