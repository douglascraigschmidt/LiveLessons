package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;

/**
 * This interface provides the contract for the RESTful {@link
 * BaseController} API being consumed by defining the HTTP GET and
 * POST methods that can be used to interact with the API, along with
 * the expected request and response parameters for each method.
 *
 * This interface uses Retrofit annotations that provide metadata
 * about the API, such as the type of HTTP request (i.e., {@code GET}
 * or {@code PUT}), the parameter types (which are annotated with
 * {@code Path}, {@code Body}, or {@code Query} tags), and the
 * expected response format (which are all wrapped in {@link Call}
 * objects).  Retrofit uses these annotations and method signatures to
 * generate an implementation of the interface that the client uses to
 * make HTTP requests to the API.
 */
public interface QuoteAPI {
    /**
     * Get a {@link List} containing the requested {@link Quote}
     * objects.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation.
     * @return An {@link Call} object that yields a {@link List}
     *         containing all the {@link Quote} objects on success and
     *         an error message on failure.
     */
    @GET("{routename}" + "/" + GET_ALL_QUOTES)
    Call<List<Quote>> getAllQuotes(@Path("routename") String routename);

    /**
     * Get a {@link List} containing the requested {@link Quote}
     * objects.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation.
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}, which is passed in the body
     *                 of the {@code POST} request.
     * @param parallel Get the {@code quoteIds} in parallel if true,
     *                 else run sequentially, which is passed as part
     *                 of the URL.
     * @return A {@link Call} object that yields a {@link List}
     *         containing all the {@link Quote} objects on success and
     *         an error message on failure.
     */
    @POST("{routename}" + "/" + POST_QUOTES)
    Call<List<Quote>> getQuotes(@Path("routename") String routename,
                                @Body List<Integer> quoteIds,
                                @Query("parallel") Boolean parallel);

    /**
     * Search for quotes containing the given {@link List} of {@code
     * queries}.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation.
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request.
     * @param parallel Search for the {@code queries} in parallel if
     *                 true, else run sequentially, which is passed
     *                 as part of the URL.
     * @return A {@link Call} object that yields a {@link List}
     *         containing all the {@link Quote} objects on success and
     *         an error message on failure.
     */
    @POST("{routename}" + "/" + POST_SEARCHES)
    Call<List<Quote>> search(@Path("routename") String routename,
                             @Body List<String> queries,
                             @Query("parallel") Boolean parallel);
}
