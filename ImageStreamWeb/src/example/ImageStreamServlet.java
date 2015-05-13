package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.google.gson.Gson;

import example.ImageStreamParallel;
import example.PlatformStrategy.InputSource;
import example.PlatformStrategy;
import filters.Filter;
import filters.GrayScaleFilter;
import filters.NullFilter;

/**
 * Servlet implementation class ImageStreamServlet
 */
@WebServlet("/ImageStreamServlet")
public class ImageStreamServlet extends HttpServlet {
    /**
     * A required field of the HttpServlet.
     * Unnecessary to us
     */
    private static final long serialVersionUID = 1L;
	
    /**
     * The list of filters to apply to the downloaded images
     */
    private static Filter[] FILTERS = {
        new NullFilter(),
        new GrayScaleFilter()
    };
	
    /**
     * The Gson JSON converter responsible for parsing the
     * request URL Lists
     */
    private final Gson gson = new Gson();
	
    /**
     * @see Servlet#init(ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
		
        // Coerce the JSON string into a usable list of string lists
        String json = request.getReader().readLine();
        String[][] inputStrings = gson.fromJson(json, String[][].class);
		
        // Perform a conversion of strings to URLs
        List<List<URL>> inputUrls = new ArrayList<List<URL>>();
        for(String[] sList : inputStrings) {
            List<URL> urlList = new ArrayList<URL>();
            for(String s : sList) {
                urlList.add(new URL(s));
            }
            inputUrls.add(urlList);
        }
			
        // Initializes the Platform singleton with the appropriate
        // PlatformStrategy, which in this case will be the
        // ConsolePlatform.
        PlatformStrategy.instance
            (new PlatformStrategyProxy((System.out), 
                                       getServletContext(),
                                       inputUrls));
        
        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Starting all the tests");
        
        // Create an exit barrier with a count of one to
        // synchronize with the completion of the image
        // downloading and processing in the TaskGang.
        final CountDownLatch exitBarrier = 
            new CountDownLatch(1);

        // Create a completion hook that decrements the exit barrier
        // by one so its count equals 0.
        final Runnable completionHook = () -> exitBarrier.countDown();

        // Call the makeImageStream() factory method to create the
        // designated ImageStream and then run it in a separate
        // Thread.
        new Thread(new ImageStreamParallel(FILTERS, 
                                           PlatformStrategy.instance()
                                           .getUrlIterator(InputSource.NETWORK),
                                           new RetrieveAndSendDataTask(response, 
                                                                       completionHook))).start();
        try {
            // Barrier synchronizer that wait for the ImageStream
            // to finish all its processing.
            exitBarrier.await();
        } catch (InterruptedException e) {
            PlatformStrategy.instance().errorLog("MainConsole", 
                                                 "await interrupted");
        }
        
        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Ending all the tests");
			
    }
	
    /**
     * A wrapper around the completionHook to the ImageStream that
     * builds and sends the response JSON string
     */
    private class RetrieveAndSendDataTask implements Runnable {
		
        private final HttpServletResponse mResponse;
        private final Runnable mEndTask;
		
        public RetrieveAndSendDataTask(HttpServletResponse response, Runnable endTask) {
            mResponse = response;
            mEndTask = endTask;
        }

        @Override
            public void run() {
            // Begin at the top level directory
            File externalFile = 
                new File(PlatformStrategy.instance().getDirectoryPath());
			
            ImageVisitor imageVisitor = new ImageVisitor();
			
            try {
                // Visit all subdirectories, building the JSON String
                Files.walkFileTree(externalFile.toPath(), imageVisitor);
				
                // Write the response string
                mResponse.setContentLength(imageVisitor.getJsonString().length());
                mResponse.getWriter().write(imageVisitor.getJsonString());
				
                // Clean the tmp directory
                FileUtils.cleanDirectory(externalFile);
				
                // Run the completionHook originally intended for the
                // ImageStream
                mEndTask.run();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	
        /**
         * A custom FileVisitor that constructs the JSON string based
         * on the files downloaded by the ImageStream
         */
        private class ImageVisitor implements FileVisitor<Path> {
			
            /**
             * Allows us to efficiently build a JSON string
             */
            private final StringBuilder mJsonBuilder;
			
            public ImageVisitor() {
                mJsonBuilder = new StringBuilder();
            }
	
            @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                // Begin the JSON object string if we are at the parent,
                // otherwise append a new object representing the next filter
                mJsonBuilder.append(
                                    isParentDir(dir) ? "{\"filterList\":[" :
                                    "{\"filterName\":\"" + dir.getFileName() + "\","
                                    + "\"imageData\":[");
                return FileVisitResult.CONTINUE;
            }
	
            @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                // Open the file and read its bytes
                File imgFile = file.toFile();
		        
                // Ignore hidden files
                if (imgFile.isHidden()) {
                    return FileVisitResult.CONTINUE;
                }
		        
                FileInputStream fstream = new FileInputStream(imgFile);
                byte fileContent[] = new byte[(int) imgFile.length()];
                fstream.read(fileContent);
                fstream.close();
	            
                // Encode the bytes of the image as a string
                String encodedImage = 
                    DatatypeConverter.printBase64Binary(fileContent);
	            
                // Append the file specifics as part of the 
                // imageData list for this filter
                mJsonBuilder.append("{\"imageName\":\"" 
                                    + file.getFileName() + "\","
                                    + "\"image\":\"" 
                                    + encodedImage + "\"},");
                return FileVisitResult.CONTINUE;
            }
	
            @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException {
                // Handle error - for now, we don't care if a file visit fails
                return FileVisitResult.CONTINUE;
            }
	
            @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
                // end the entire object string if we are leaving
                // the parent directory, otherwise end the current imageData
                // object
                clean(mJsonBuilder).append((isParentDir(dir) ? "]}" : "]},"));
                return FileVisitResult.CONTINUE;
            }
			
            // Allows us to reference the string outside of the class
            public String getJsonString() {
                return mJsonBuilder.toString();
            }
			
            // Clean the string. Currently, this method only removes an
            // unnecessary comma from the end of a list, as this will
            // invalidate a JSON string
            private StringBuilder clean(StringBuilder jsonBuilder) {
                if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',')
                    return jsonBuilder.deleteCharAt(
                                                    jsonBuilder.lastIndexOf(","));
                return jsonBuilder;
            }
			
            // Helper method for determining whether the visitor
            // is at the top directory or a subdirectory
            private boolean isParentDir(Path dir) {
                return dir.toString().equalsIgnoreCase(
                                                       PlatformStrategy.instance().getDirectoryPath());
            }
	
        }
    }

}
