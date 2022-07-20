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
    public long id;
    public String departureAirport;
    public LocalDate departureDate;
    public LocalTime departureTime;
    public String arrivalAirport;
    public LocalDate arrivalDate;
    public LocalTime arrivalTime;
    public int kilometers;
    public double price;
    public String currency;
    public String airlineCode;

    public Flight(
            long id,
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
        this.id = id;
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
        return "id = " + id +
                " airlineCode = " + airlineCode +
                " departureTime = " + departureTime +
                " price = " + price +
                " currency = " + currency;
    }
}