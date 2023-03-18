package edu.vandy.quoteservices.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

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
     * @return A {@link List} of type {@code T} objects from the server
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
     * and returning a {@link List} of type {@code T} objects from the
     * server.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T[]} to return from GET
     * @return A {@link List} of type {@code T} objects from the server
     */
    public static <T, B> List<T> makePostRequestList(RestTemplate restTemplate,
                                                     String url,
                                                     B body,
                                                     Class<T[]> clazz) {
        ResponseEntity<T[]> responseEntity = restTemplate
            // Send an HTTP GET request to the given URL and return
            // the response as ResponseEntity containing a T[].
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
     * Make an HTTP GET call to the server passing in the {@code url}
     * and returning a {@link Flux} that emits type {@code T} objects
     * from the server.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T[]} to return from GET
     * @return A {@link Flux} that emits type {@code T} objects from
     *         the server
     */
    public static <T, B> Flux<T> makeGetRequestFlux(WebClient webClient,
                                                    String url,
                                                    Class<T> clazz) {
        return webClient
            // Create a GET request.
            .get()

            // Add the 'uri' to the GET request.
            .uri(url)

            // Send the HTTP GET request to the given URL.
            .retrieve()

            // Return the response as Flux that emits 'clazz'.
            .bodyToFlux(clazz);
    }

    /**
     * Make an HTTP POST call to the server passing in the {@code url}
     * and returning a {@link Flux} that emits type {@code T} objects
     * from the server.
     *
     * @param url The URL to pass to the server via a GET request
     * @param clazz The type {@code T[]} to return from POST
     * @return A {@link Flux} that emits type {@code T} objects from
     *         the server
     */
    public static <T, B> Flux<T> makePostRequestFlux(WebClient webClient,
                                                     String url,
                                                     B body,
                                                     Class<T> clazz) {
        return webClient
            // Create a POST request.
            .post()

            // Add the 'uri' to the POST request.
            .uri(url)

            // Add the 'body' to the POST request.
            .bodyValue(body)

            // Send the POST HTTP request to the given URL.
            .retrieve()

            // Return the response as Flux that emits 'clazz'.
            .bodyToFlux(clazz);
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

    /**
     * Return a Uri {@link String} created from the {@code
     * partialPath}.
     *
     * @param partialPath The partial path used to create the Uri
     *                    {@link String}
     * @return A Uri {@link String} created from the {@code
     *         partialPath}
     */
    public static String buildUriString(String partialPath) {
        return UriComponentsBuilder
            // Create a partial uri from this partialPath.
            .fromPath(partialPath)

            // Create the UriComponentsBuilder.
            .build()

            // Convert to a URI String. 
            .toUriString();
    }
}
