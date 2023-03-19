package folders.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Custom exception for handling an invalid name of a Folder.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Invalid Folder")
public class InvalidFolderException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidFolderException(String path) {
        super(path);
    }
}
