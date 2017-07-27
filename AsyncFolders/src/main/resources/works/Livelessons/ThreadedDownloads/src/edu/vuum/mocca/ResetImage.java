package edu.vuum.mocca;

import edu.vuum.mocca.R;

/**
 * @class ResetImage
 *
 * @brief This class resets the image displayed to the user with the
 *        default image.
 */
public class ResetImage implements ButtonStrategy {
    /**
     * Replace the current image with the default image.
     */
    public void downloadAndDisplayImage(DownloadContext downloadContext) {
        downloadContext.showToast("Resetting image to the default");
        downloadContext.resetImage(R.drawable.default_image);
    }

    /**
     * "Cancel" a download.
     */
    @Override
    public void cancelDownload(DownloadContext downloadContext) {
    }
}
