package datamodels;

import lombok.*;

import java.time.LocalDate;

/**
 * This "Plain Old Java Object" (POJO) class defines a request for a
 * {@link Flight} object.
 * 
 * The {@code @Value} annotation assigns default values to variables.
 *
 * The {@code @NoArgsConstructor} will generate a constructor with no
 * parameter.
 *
 * The {@code @Builder} annotation automatically creates a static
 * builder factory method for this class that can be used as follows:
 *
 * FlightRequest flightRequest = FlightRequest
 *   .builder()
 *   .departureAirport("JFK")
 *   .arrivalAirport("BWI")
 *   ...
 *   .build();
 * 
 * The {@code @With} annotation generates a method that constructs a
 * clone of the object, but with a new value for this one field.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@With
public class FlightRequest {
    /**
     * The three letter code name of the departure airport.
     */
    String departureAirport;

    /**
     * The three letter code name of the arrival airport.
     */
    String arrivalAirport;

    /**
     * The date of the departure.
     */
    LocalDate departureDate;

    /**
     * The number of passengers who want to travel.
     */
    int passengers;

    /**
     * The currency of the price.
     */
    String currency;

    /**
     * @return A {@link String} representation of this {@code Flight}
     */
    @Override
    public String toString() {
        return "departureAirport = " + departureAirport +
                " arrivalAirport = " + arrivalAirport +
                " departureDate = " + departureDate;
    }
}
