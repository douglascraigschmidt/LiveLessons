package berraquotes.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                      // The return type is an array of T objects.
                      clazz);

        // Return a List.
        return List
            // Convert the array in the response into a List.
            .of(Objects.requireNonNull(responseEntity.getBody()));
    }

    /**
     * Make an HTTP POST call to the server passing in the {@code url}
     * and returning a result of type {@code T[]}.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T[]} to return from GET
     * @return The result of type {@code T[]} from the server
     */
    public static <T, B> List<T> makePostRequestList(RestTemplate restTemplate,
                                                     String url,
                                                     B body,
                                                     Class<T[]> clazz) {
        ResponseEntity<T[]> responseEntity = restTemplate
            // Send an HTTP GET request to the given URL and return
            // the response as ResponseEntity containing an Integer.
            .postForEntity(url,
                           body,
                           // The return type is an array of T objects.
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

            // Convert T to String.
            .map(Object::toString)

            // Trigger intermediate operations and convert each String
            // in the Stream into a single comma-separated String.
            .collect(Collectors.joining(","));
    }

    /**
     * Encode the {@code queries {@link List} so it can be
     * passed correctly via HTTP.
     *
     * @param queries The {@link List} to encode
     * @return The encoded {@link List}
     */
    public static List<String> encodeQueries(List<String> queries) {
        return queries
            // Convert List to a Stream.
            .stream()

            // Encode each String in the Stream.
            .map(query -> URLEncoder
                 .encode(query,
                         StandardCharsets.UTF_8))

            // Convert the Stream back to a List.
            .toList();
    }

    /**
     * Encode the {@code query} {@link String} so it can be
     * passed correctly via HTTP.
     *
     * @param query The {@link String} to encode
     * @return The encoded {@link String}
     */
    public static String encodeQuery(String query) {
        return URLEncoder
            // Encode the query so it can be passed
            // correctly via HTTP.
            .encode(query,
                    StandardCharsets.UTF_8);
    }
}
