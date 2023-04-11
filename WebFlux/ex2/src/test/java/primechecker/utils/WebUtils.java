package primechecker.utils;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        var result = responseEntity.getBody();

        var response = Objects.requireNonNull(result);

        // Return a List.
        return List
            // Convert the array in the response into a List.
            .of(response);
    }

    /**
     * Make an HTTP GET call to the server passing in the {@code url}
     * and returning a result of type {@code T[]}.
     *
     * @param url The URL to pass to the server via a GET request
     * @return A {@link List} of type {@code T} objects from the server
     */
    public static <K, V> Map<K, V> makeGetRequestMap(RestTemplate restTemplate,
                                                     String url) {
        var response = restTemplate
            // Send an HTTP GET request to the given URL and return
            // the response as ResponseEntity containing an Integer.
            .exchange(url,
                      // Send via an HTTP GET request.
                      HttpMethod.GET, 
                      null,
                      // The return type is an array of T objects.
                      new ParameterizedTypeReference<Map<K, V>>() {});
        return response.getBody();
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
     * Make an HTTP POST call to the server passing in the {@code url}
     * and returning a result of type {@link URI}.
     *
     * @param restTemplate The {@link RestTemplate} to invoke the
     *                     request on
     * @param url The URL to pass to the server via a POST request
     * @param body The body to send with the POST request
     * @return The {@link URI} containing the value for
     *         the {@code LOCATION} header
     */
    public static <B> URI makePostRequestLocation(RestTemplate restTemplate,
                                                  String url,
                                                  B body) {
        return restTemplate
            // Send an HTTP POST request to the given URL and return
            // the response as a URI.
            .postForLocation(url, body);
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
     * Decode the {@code queries {@link List} so it can be received
     * correctly from HTTP.
     *
     * @param queries The {@link List} to decode
     * @return The encoded {@link List}
     */
    public static List<String> decodeQueries(List<String> queries) {
        return queries
            // Convert List to a Stream.
            .stream()

            // Encode each String in the Stream.
            .map(query -> URLDecoder
                .decode(query,
                    StandardCharsets.UTF_8))

            // Convert the Stream back to a List.
            .toList();
    }

    /**
     * Decode the {@code query} {@link String} so it can be
     * received correctly from HTTP.
     *
     * @param query The {@link String} to decode
     * @return The encoded {@link String}
     */
    public static String decodeQuery(String query) {
        return URLDecoder
            // Decode the query so it can be received correctly from
            // HTTP.
            .decode(query,
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
    public static String list2String(List<String> list) {
        // Convert each String in the List into a
        // single comma-separated String.
        return String.join(",", list);
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

    /**
     * Return a Url {@link String} created from the {@code port},
     * {@code host}, and the {@code partialPath}.
     *
     * @param port The port to connect to
     * @param host The host to connect to
     * @param partialPath The partial path used to create the Uri
     *                     {@link String}
     * @return A Url {@link String} created from the params
     */
    public static String buildUrlString(int port,
                                        String host,
                                        String partialPath) {
        return UriComponentsBuilder
            // Make a new UriComponentsBuilder object.
            .newInstance()

            // Use the "http" protocol.
            .scheme("http")

            // Connect to this port.
            .port(port)

            // On this computer.
            .host(host)

            // At this path.
            .path(partialPath)

            // Build the String.
            .build()

            // Convert it to a Uri String.
            .toUriString();
    }
}
