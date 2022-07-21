package datamodels;

import lombok.*;
import utils.ExchangeRate;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This "Plain Old Java Object" (POJO) class defines an immutable
 * response for a flight, which is returned by various microservices
 * to indicate which flights match a {@link FlightRequest}.
 *
 * The {@code @Value} annotation assigns default values to variables.
 *
 * The {@code @NoArgsConstructor} will generate a constructor with no
 * parameter.
 * 
 * The {@code @Builder} annotation automatically creates a static
 * builder factory method for this class that can be used as follows:
 *
 * Flight flight = Flight
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
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@With
public class Flight {
    /**
     * The three letter code name of the departure airport.
     */
    String departureAirport;

    /**
     * The date of the departure.
     */
    LocalDate departureDate;

    /**
     * The time of the departure.
     */
    LocalTime departureTime;

    /**
     * The three letter code name of the arrival airport.
     */
    String arrivalAirport;

    /**
     * The date of the arrival.
     */
    LocalDate arrivalDate;

    /**
     * The time of the arrival.
     */
    LocalTime arrivalTime;

    /**
     * The distance in kilometers from the departure to the arrival
     * airports.
     */
    int kilometers;

    /**
     * The price of the flight.
     */
    double price;

    /**
     * The currency of a flight's price.
     */
    String currency;

    /**
     * The airline code.
     */
    String airlineCode;

    /**
     * Constructor that initializes all the fields.
     */ 
    public Flight(String departureAirport,
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
     * Builds a new immutable {@link Flight} object that has a {@code
     * price} expressed in the {@code toCurrency} currency.
     *
     * @param toCurrency The currency to convert to
     * @return A new immutable {@link Flight} with the {@code
     *         toCurrency} {@code price}
     */
    public Flight inCurrency(String toCurrency) {
        return toBuilder()
            // Set the price to the conversion based on the given
            // toCurrency.
            .price(ExchangeRate.convert(price, currency, toCurrency))

            // Set the currency.
            .currency(toCurrency)

            // Build the Flight object.
            .build();
    }

    /**
     * Factory method that builds a {@link FlightRequest}
     */
    public FlightRequest buildRequest() {
        return FlightRequest.builder()
            // Set the departure airport.
            .departureAirport(departureAirport)

            // Set the departure date.
            .departureDate(departureDate)

            // Set the arrival airport.
            .arrivalAirport(arrivalAirport)

            // Set the currency.
            .currency(currency)

            // Build the FlightRequest object.
            .build();
    }

    /**
     * @return A {@link String} representation of this {@code Flight}
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
            + price
            + ", "
            + airlineCode
            + ", "
            + currency;
    }
}
