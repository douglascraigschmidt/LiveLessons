package livelessons.platspec;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import livelessons.R;
import livelessons.utils.ExceptionUtils;
import livelessons.utils.Options;

import static java.util.stream.Collectors.toList;
import static livelessons.TheApp.getApp;
import static livelessons.utils.NetUtils.isResourceUrl;

/**
 * A utility class containing static methods whose implementations are
 * specific to a particular platform (e.g., the Java platform vs. the
 * Android platform).  This implementation is specific for the Android
 * platform.
 */
public final class PlatSpec {
    // To refer to bar.png under your package's res/drawable/ directory, use
    // "file:///android_res/drawable/bar.png". Use "drawable" to refer to
    // "drawable-hdpi" directory as well.
    private static final String RESOURCE_BASE = "file:///android_res/";

    /**
     * The path to the image directory.
     */
    private static final String IMAGE_DIRECTORY_PATH =
        Environment.getExternalStorageDirectory().toString();

    /**
     * Logging tag.
     */
    private static final String TAG = PlatSpec.class.getName();

    /**
     * A utility class should always define a private constructor.
     */
    private PlatSpec() {
    }
    
    /**
     * Return the path to the external storage directory.
     */
    public static String getDirectoryPath() {
        return IMAGE_DIRECTORY_PATH;
    }

    /**
     * Creates an input stream for the passed URL. This method will
     * support both normal URLs and any URL located in the application
     * resources.
     *
     * @param url     Any URL including a resource URL.
     * @return An input stream.
     * @throws IOException
     */
    public static InputStream getInputStream(URL url)
            throws IOException {
        if (isResourceUrl(url.toString())) {
            Log.d(TAG, "Loading image from app resources");

            // Both URL and Uri classes will not accept a proper
            // android resource scheme but will accept the prefix
            // "file:///android_res/". To get an apk resource input
            // stream, simply replace this prefix so that the
            // resulting url can be passed to the application's
            // content resolver.
            String resUrl = 
                url.toString().replace(RESOURCE_BASE,
                                       ContentResolver.SCHEME_ANDROID_RESOURCE
                                       + "://");
            return getApp().getContentResolver().openInputStream(Uri.parse(resUrl));
        } else {
            // Normal URL.
            return url.openStream();
        }
    }

    /**
     * Gets the list of lists of URLs from which the user wants to
     * download images.
     */
    public static List<List<URL>> getUrlLists(Object c, Object l) {
        Context context = (Context) c;
        LinearLayout listUrlGroups = (LinearLayout) l;

        // Iterate over the children of the LinearLayout that holds
        // the list of URL lists.
        int numChildViews =
            listUrlGroups.getChildCount();

        List<List<URL>> inputUrls =
            new ArrayList<>();

        for (int i = 0; i < numChildViews; ++i) {
            AutoCompleteTextView child = (AutoCompleteTextView)
                listUrlGroups.getChildAt(i);

            // Convert the input string into a list of URLs
            // and add it to the main list.
            inputUrls.add
                (PlatSpec.convertUrlStringToUrls(child.getText().toString()));
        }
        
        return inputUrls;
    }

    /**
     * Create a new URL list from a @a urlStringOfUrls that contains a
     * list of URLs separated by commas and add them to the URL list
     * that's returned.
     */
    private static List<URL> convertUrlStringToUrls(String urlStringOfNames) {
        // Create a Function that returns a new URL object when
        // applied and which converts checked URL exceptions into
        // runtime exceptions.
        Function<String, URL> urlFactory = 
            ExceptionUtils.rethrowFunction(URL::new);

        return Pattern
            // Create a regular expression for the "," separator.
            .compile(",")

            // Use regular expression to split urlStringOfNames into a
            // Stream<String>.
            .splitAsStream(urlStringOfNames)

            // Convert each string in the stream to a URL.
            .map(urlFactory::apply)

            // Create a list of URLs.
            .collect(toList());
    }

    /**
     * Returns a List of default resource URL lists.
     */
    public static List<List<URL>> getDefaultResourceUrlList(Object obj,
                                                            String[] defaultImageNames)
            throws MalformedURLException {
        Context context = (Context) obj;

        return Stream
            // Convert the array of strings into a stream of strings.
            .of(defaultImageNames)

            // Map each string in the list into a list of URLs.
            .map(stringOfNames
                 -> {
                     // Create a function that returns a new URL object when
                     // applied and which converts any checked URL exceptions into
                     // runtime exceptions.
                     Function<String, URL> urlFactory =
                         ExceptionUtils.rethrowFunction(URL::new);

                    return Pattern
                        // Create a regular expression for the "," separator.
                        .compile(",")

                        // Use regular expression to split stringOfNames into a
                        // Stream<String>.
                        .splitAsStream(stringOfNames)

                        // Concatenate the url prefix with each name.
                        .map(name 
                             -> urlFactory.apply(getResourcesUrl
                                                 (context,
                                                  // Remove the suffix from the name.
                                                  name.substring(0, 
                                                                 name.lastIndexOf('.')))))

                        // Create a list of URLs.
                        .collect(toList());
                 })

            // Create and return a list of a list of URLs.
            .collect(toList());
    }

    /**
     * Returns a URL String that will map to any application resource.
     *
     * @param context Any context.
     * @param resName The name of a resource.
     * @return A String URL that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    private static String getResourcesUrl(Context context,
                                          String resName)
            throws Resources.NotFoundException {
        return getResourcesUri(context, 
                               getId(resName,
                                     R.raw.class)).toString();
    }

    /**
     * Return the resource id of a resource name.
     */
    public static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: "
                                       + resourceName + " / " + c, e);
        }
    }

    /**
     * Returns a Uri that will map to any application resource.
     *
     * @param context Any context
     * @param resId Any resource id
     * @return A Uri that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    private static Uri getResourcesUri(Context context, int resId) {
        return Uri.parse(RESOURCE_BASE
                         + context.getResources().getResourcePackageName(resId)
                         + '/'
                         + context.getResources().getResourceTypeName(resId)
                         + '/'
                         + context.getResources().getResourceEntryName(resId));
    }
}
