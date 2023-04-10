package edu.vandy.quoteservices.utils;

import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static edu.vandy.quoteservices.utils.ExceptionUtils.rethrowSupplier;

/**
 * This Java utility class defines method(s) that are useful
 * in conjunction with Retrofit.
 */
public class CallUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private CallUtils() {}

    /**
     * Execute the {@link Call} and return the {@link T} received
     * from the server on success and throws an {@link IOException} on
     * failure.
     *
     * @param call The {@link Call} returned from the Retrofit client
     *             API
     * @return The {@link T} received from the server on success
     */
    @SuppressWarnings("resource")
    public static <T> T executeCall(Call<T> call){
        return rethrowSupplier(() -> {
            // Execute the call.
            Response<T> response = call.execute();

            // If the request is successful return the body.
            if (response.isSuccessful()) {
                return response.body();
            } else {
                // If there's a failure then find out what failed
                // throw IOException.
                assert response.errorBody() != null;
                String errorMessage = response.errorBody().string();
                throw new IOException(errorMessage);
            }
        }).get();
    }
}
