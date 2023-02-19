package primechecker.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME_LIST;

public interface PCServerAPI {
    @GET(CHECK_IF_PRIME)
    Call<Integer> checkIfPrime(
        @Query("strategy") Integer strategy,
        @Query("primeCandidate") Integer primeCandidate);
    
    @GET(CHECK_IF_PRIME_LIST)
    Call<List<Integer>> checkIfPrimeList(
        @Query("strategy") Integer strategy,
        @Query("primeCandidates") List<Integer> primeCandidates,
        @Query("parallel") Boolean parallel);
}
