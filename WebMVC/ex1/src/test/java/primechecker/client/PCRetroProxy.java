package primechecker.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static primechecker.common.Constants.SERVER_BASE_URL;

@Component
public class PCRetroProxy {
    private final PCServerAPI pcServerAPI;

    PCRetroProxy() {
        pcServerAPI = buildApi();
    }

    private PCServerAPI buildApi() {
        return new Retrofit
            .Builder()
            .baseUrl(SERVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PCServerAPI.class);
    }

    public Integer checkIfPrime(Integer strategy,
                                Integer primeCandidate) {
        try {
            var result = pcServerAPI
                .checkIfPrime(strategy, primeCandidate).execute().body();
            System.out.println("checkIfPrime() result = " + result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> checkIfPrimeList
        (Integer strategy,
         List<Integer> primeCandidates, Boolean parallel) {
        try {
            var response = pcServerAPI
                    .checkIfPrimeList(strategy,
                            primeCandidates,
                            parallel).execute();

            if (response.isSuccessful())
                return response.body();
            else {
                System.out.println(response.errorBody().string());

                return null;
            }
        } catch (IOException e) {
            System.out.println("exception = " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
