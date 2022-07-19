package datamodels;

import lombok.*;

import java.time.LocalDate;

/**
 * Data structure that defines a request for a flight.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@With
public class FlightRequest {
    String departureAirport;
    String arrivalAirport;
    LocalDate departureDate;
    int passengers;
    String currency;

    @Override
    public String toString() {
        return "departureAirport = " + departureAirport +
                " arrivalAirport = " + arrivalAirport +
                " departureDate = " + departureDate;
    }
}
