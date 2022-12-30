package edu.vandy.mathservices.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A Java utility class that provides helper methods for dealing with
 * Spring web programming.
 */
public final class WebUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private WebUtils() {}

    /**
     * Make an HTTP GET call to the server passing in the {@code url}
     * and returning a result of type {@code T}.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T} to return from GET
     * @return The result of type {@code T} from the server
     */
    public static <T> T makeGetRequest(RestTemplate restTemplate,
                                       String url,
                                       Class<T> clazz) {
        return restTemplate
            // Retrieve a representation by doing a GET on the URL.
            .getForEntity(url, clazz)

            // Returns the body of this entity.
            .getBody();
    }

    /**
     * Make an HTTP GET call to the server passing in the {@code url}
     * and returning a result of type {@code T[]}.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T[]} to return from GET
     * @return The result of type {@code T[]} from the server
     */
    public static <T> List<T> makeGetRequestList(RestTemplate restTemplate,
                                                 String url,
                                                 Class<T[]> clazz) {
        ResponseEntity<T[]> responseEntity = restTemplate
            // Send an HTTP GET request to the given URL and return
            // the response as ResponseEntity containing an Integer.
            .exchange(url,
                      // Send via an HTTP GET request.
                      HttpMethod.GET, null,
                      // The return type is an array of Integer objects.
                      clazz);

        // Return a List.
        return List
            // Convert the array in the response into a List.
            .of(Objects.requireNonNull(responseEntity.getBody()));
    }

    /**
     * Convert the contents of the {@link List} param into a
     * comma-separated {@link String}.

     * @param list The {@link List} to convert to a comma-separate
     *        string
     * @return A comma-separated string containing the contents of the
     *         {@link List}
     */
    public static <T> String list2String(List<T> list) {
        return list
            // Convert the List elements into a Stream.
            .stream()

            // Convert each Stream element into a String.
            .map(Object::toString)

            // Trigger intermediate operations and convert each String
            // in the Stream into a single comma-separated String.
            .collect(Collectors.joining(","));
    }
}
