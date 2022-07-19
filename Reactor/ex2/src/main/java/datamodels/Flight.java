package datamodels;

import lombok.*;
import utils.ExchangeRate;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Immutable data structure that defines a response for a FlightRequest.
 */
@Value
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@With
public class Flight {
    String departureAirport;
    LocalDate departureDate;
    LocalTime departureTime;
    String arrivalAirport;
    LocalDate arrivalDate;
    LocalTime arrivalTime;
    int kilometers;
    double price;
    String currency;
    String airlineCode;

    public Flight(
            String departureAirport,
            LocalDate departureDate,
            LocalTime departureTime,
            String arrivalAirport,
            LocalDate arrivalDate,
            LocalTime arrivalTime,
            int kilometers,
            double price,
            String currency,
            String airlineCode
    ) {
        this.departureAirport = departureAirport;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalAirport = arrivalAirport;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.kilometers = kilometers;
        this.price = price;
        this.currency = currency;
        this.airlineCode = airlineCode;
    }

    /**
     * Builds a new immutable Flight Object that has a {@code price} expressed in the
     * {@code toCurrency} currency.
     *
     * @param toCurrency The currency to convert to.
     * @return A new immutable Flight with the {@code toCurrency} {@code price}.
     */
    public Flight inCurrency(String toCurrency) {
        return toBuilder()
                .price(ExchangeRate.convert(price, currency, toCurrency))
                .currency(toCurrency)
                .build();
    }

    public FlightRequest buildRequest() {
        return FlightRequest.builder()
                .departureAirport(departureAirport)
                .departureDate(departureDate)
                .arrivalAirport(arrivalAirport)
                .currency(currency)
                .build();
    }

    /**
     * @return A String representation of this {@code TripResponse}
     */
    @Override
    public String toString() {
        return /* departureDateTime
            + ", "
            + arrivalDateTime
            + ", "
            + returnDepartureDateTime
            + ", "
            + returnArrivalDateTime
            + ", "
            + departureAirport
            + ", "
            + arrivalAirport
            + ", " */
                +price
                        + ", "
                        + airlineCode
                        + ", "
                        + currency;
    }
}