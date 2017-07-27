package edu.vuum.mocca;

/**
 * @class ButtonStrategy
 *
 * @brief Implement this interface to define the concurrency strategy
 *        used to download and display an image when the user clicks a
 *        button.  This interface plays the role of the "Strategy" in
 *        the Strategy pattern.
 */
public interface ButtonStrategy {
    /**
     * Define a strategy for downloading and displaying an image,
     * which is guided by the DownloadContext.
     */
    void downloadAndDisplayImage(DownloadContext downloadContext);

    /**
     * Define a strategy for canceling a download.
     */
    void cancelDownload(DownloadContext downloadContext);
}
