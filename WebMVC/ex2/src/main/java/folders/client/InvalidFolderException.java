package folders.client;

import java.util.Date;

/**
 * Define a {@link RuntimeException} that conveys the error thrown by
 * the server when a {@link InvalidFolderException} occurs.
 */
public class InvalidFolderException
       extends RuntimeException {
    InvalidFolderException(String message) {
        super(message);
    }
}
