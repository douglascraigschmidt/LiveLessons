package microservices.apigateway;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;

/**
 *
 */
public class FlightRequest {
    /**
     *
     */
    public TripRequest tripRequest;

    /**
     *
     */
    public CurrencyConversion currencyConversion;

    /**
     *
     * @param tripRequest
     * @param currencyConversion
     */
    public FlightRequest(TripRequest tripRequest,
                         CurrencyConversion currencyConversion) {
        this.tripRequest = tripRequest;
        this.currencyConversion = currencyConversion;
    }
}
