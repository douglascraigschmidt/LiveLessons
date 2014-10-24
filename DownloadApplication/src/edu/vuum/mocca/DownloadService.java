package edu.vuum.mocca;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

/**
 * @class DownloadService
 *
 * @brief DownloadService receives an Intent containing a URL (which
 *        is a type of URI) and a Messenger. It downloads the file at
 *        the URL, stores it on the file system, then returns the path
 *        name to the caller using the supplied Messenger.
 * 
 *        The DownloadService class implements the CommandProcessor
 *        pattern and The Messenger is used as part of the Active
 *        Object pattern.
 */
public class DownloadService extends Service 
{
    /**
     * Looper associated with the HandlerThread.
     */
    private volatile Looper mServiceLooper;

    /**
     * Processes Messages sent to it from onStartCommnand() that
     * indicate which images to download from a remote server.
     */
    private volatile ServiceHandler mServiceHandler;

    /**
     * Hook method called when DownloadService is first launched by
     * the Android ActivityManager.
     */
    public void onCreate() {
        super.onCreate();
        
        // Create and start a background HandlerThread since by
        // default a Service runs in the UI Thread, which we don't
        // want to block.
        HandlerThread thread =
            new HandlerThread("DownloadService");
        thread.start();
        
        // Get the HandlerThread's Looper and use it for our Handler.
        mServiceLooper = thread.getLooper();
        mServiceHandler =
            new ServiceHandler(mServiceLooper);
    }

    /**
     * Hook method called each time a Started Service is sent an
     * Intent via startService().
     */
    public int onStartCommand(Intent intent, 
                              int flags,
                              int startId) {
        // Create a Message that will be sent to ServiceHandler to
        // retrieve animagebased on the URI in the Intent.
        Message message =
            mServiceHandler.makeDownloadMessage(intent,
                                                startId);
        
        // Send the Message to ServiceHandler to retrieve an image
        // based on contents of the Intent.
        mServiceHandler.sendMessage(message);
        
        // Don't restart the DownloadService automatically if its
        // process is killed while it's running.
        return Service.START_NOT_STICKY;
    }

    /**
     * Helper method that returns pathname if download succeeded.
     */
    public static String getPathname(Message message) {
        // Extract the data from Message, which is in the form
        // of a Bundle that can be passed across processes.
        Bundle data = message.getData();

        // Extract the pathname from the Bundle.
        String pathname = data.getString("PATHNAME");

        // Check to see if the download succeeded.
        if (message.arg1 != Activity.RESULT_OK 
            || pathname == null)
            return null;
        else
            return pathname;
    }

    /**
     * Factory method to make the desired Intent.
     */
    public static Intent makeIntent(Context context,
                                    Uri uri,
                                    Handler downloadHandler) {
        // Create the Intent that's associated to the DownloadService
        // class.
        Intent intent = new Intent(context,
                                   DownloadService.class);

        // Set the URI as data in the Intent.
        intent.setData(uri);

        // Create and pass a Messenger as an "extra" so the
        // DownloadService can send back the pathname.
        intent.putExtra("MESSENGER",
                        new Messenger(downloadHandler));
        return intent;
    }

    /**
     * @class ServiceHandler
     *
     * @brief An inner class that inherits from Handler and uses its
     *        handleMessage() hook method to process Messages sent to
     *        it from onStartCommnand() that indicate which images to
     *        download.
     */
    private final class ServiceHandler extends Handler {
        /**
         * Class constructor initializes the Looper.
         * 
         * @param Looper
         *            The Looper that we borrow from HandlerThread.
         */
    	public ServiceHandler(Looper looper) {
            super(looper);
    	}

        /**
         * A factory method that creates a Message to return to the
         * DownloadActivity with the pathname of the downloaded image.
         */
        private Message makeReplyMessage(String pathname){
            Message message = Message.obtain();
            // Return the result to indicate whether the download
            // succeeded or failed.
            message.arg1 = pathname == null 
                ? Activity.RESULT_CANCELED 
                : Activity.RESULT_OK;

            Bundle data = new Bundle();

            // Pathname for the downloaded image.
            data.putString("PATHNAME", 
            			   pathname);
            message.setData(data);
            return message;
        }

        /**
         * A factory method that creates a Message that contains
         * information on the image to download and how to stop the
         * Service.
         */
        private Message makeDownloadMessage(Intent intent,
                                            int startId){
            Message message = Message.obtain();
            // Include Intent & startId in Message to indicate which URI
            // to retrieve and which request is being stopped when
            // download completes.
            message.obj = intent;
            message.arg1 = startId;
            return message;
        }

        /**
         * Retrieves the designated image and reply to the
         * DownloadActivity via the Messenger sent with the Intent.
         */
        private void downloadImageAndReply(Intent intent) {
            // Download the requested image.
            String pathname = downloadImage(DownloadService.this,
                                            intent.getData().toString());

            // Extract the Messenger.
            Messenger messenger = (Messenger)
                    intent.getExtras().get("MESSENGER");

            // Send the pathname via the messenger.
            sendPath(messenger, pathname);
        }

        /**
         * Send the pathname back to the DownloadActivity via the
         * messenger.
         */
        private void sendPath(Messenger messenger, 
                              String pathname) {
            // Call factory method to create Message.
            Message message = makeReplyMessage(pathname);
        
            try {
                // Send pathname to back to the DownloadActivity.
                messenger.send(message);
            } catch (RemoteException e) {
                Log.e(getClass().getName(),
                      "Exception while sending.",
                      e);
            }
        }

	/**
	 * Create a file to store the result of a download.
	 * 
	 * @param context
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private File getTemporaryFile(final Context context,
                                      final String url) throws IOException {
            return context.getFileStreamPath(Base64.encodeToString(url.getBytes(),
                                                                   Base64.NO_WRAP)
                                             + System.currentTimeMillis());
	}

	/**
	 * Copy the contents of an InputStream into an OutputStream.
	 * 
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	private int copy(final InputStream in,
                         final OutputStream out) throws IOException {
            final int BUFFER_LENGTH = 1024;
            final byte[] buffer = new byte[BUFFER_LENGTH];
            int totalRead = 0;
            int read = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                totalRead += read;			
            }

            return totalRead;
	}

	/**
	 * Download the requested image and return the local file path.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public String downloadImage(final Context context,
                                    final String url) {
            try {
                final File file = getTemporaryFile(context, url);
                Log.d(getClass().getName(), "    downloading to " + file);

                final InputStream in = (InputStream)
                    new URL(url).getContent();
                final OutputStream out =
                    new FileOutputStream(file);

                copy(in, out);
                in.close();
                out.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
                Log.e(getClass().getName(),
                      "Exception while downloading. Returning null.");
                Log.e(getClass().getName(),
                      e.toString());
                e.printStackTrace();
                return null;
            }
	}

        /**
         * Hook method that retrieves an image from a remote server.
         */
        public void handleMessage(Message message) {
            // Download the designated image and reply to the
            // DownloadActivity via the Messenger sent with the
            // Intent.
            downloadImageAndReply((Intent) message.obj);
            
            // Stop the Service using the startId, so it doesn't stop
            // in the middle of handling another download request.
            stopSelf(message.arg1);
        }
    }
    
    /**
     * Hook method called back to shutdown the Looper.
     */
    public void onDestroy() {
        mServiceLooper.quit();
    }

    /**
     * This hook method is a no-op since we're a Start Service.
     */
    public IBinder onBind(Intent arg0) {
        return null;
    }
}

