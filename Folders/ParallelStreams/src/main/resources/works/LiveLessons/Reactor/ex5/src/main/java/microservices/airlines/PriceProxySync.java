package microservices.airlines;

import datamodels.TripRequest;
import datamodels.TripResponse;

import java.util.List;

/**
 * This interface provides the basis for synchronously querying
 * airline flight price databases.
 */
public interface PriceProxySync {
    /**
     * Returns a List that contains {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param scheduler Thread pool used to perform the computations
     * @param trip The trip to search for price information
     * @return A List that contains {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    List<TripResponse> findTrips(TripRequest tripRequest);
}
