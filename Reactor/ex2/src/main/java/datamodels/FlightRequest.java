package datamodels;

import lombok.*;

import java.time.LocalDate;

/**
 * Data structure that defines a request for a flight.
 */
@Data
@With
@Builder
public class FlightRequest {
    String departureAirport;
    String arrivalAirport;
    LocalDate departureDate;
    int passengers;
    String currency;
}
