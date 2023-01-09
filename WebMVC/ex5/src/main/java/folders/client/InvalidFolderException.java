package folders.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Define a {@link RuntimeException} that conveys the error thrown by
 * the server when a {@link InvalidFolderException} occurs.
 */
public class InvalidFolderException
       extends RuntimeException {
    /**
     * When a Spring Boot server returns an error via the {@code
     * ResponseStatusException} class, this is the JSON format that is
     * received in the Retrofit errorBody.
     */
    public static class ErrorResponseStatus {
        /**
         * Time when the exception occurred.
         */
        public Date timestamp;

        /**
         * The status of the response, e.g., 404.
         */
        public int status;
        
        /**
         * The error (exception) that occurred on the remote service.
         */
        public String error;

        /**
         * The reason the exception occurred (set by the remote
         * service).
         */
        public String message;

        /**
         * The path of the original request.
         */
        public String path;

        /**
         * Display the received error response in a pretty JSON format
         * similar to how it was received from the server.
         *
         * @return Formatted JSON string
         */
        public String toString() {
            // Create a String form of the error response.
            return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(this);
        }
    }

    /**
     * Store the {@link ErrorResponseStatus}.
     */
    public ErrorResponseStatus mErrorResponseStatus;

    /**
     * Create an exception from the {@link InputStream} containing
     * the JSon returned from the server.
     *
     * @param stream An {@link InputStream} containing the JSon
     *               returned from the server
     * @throws IOException May occur under weird circumstances
     */
    InvalidFolderException(InputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSon into the corresponding object.
        mErrorResponseStatus = mapper
            .readValue(stream,
                       ErrorResponseStatus.class);
    }

    /**
     * Return the string form of the exception.
     */
    @Override
    public String getMessage() {
        return mErrorResponseStatus.toString();
    }
}
