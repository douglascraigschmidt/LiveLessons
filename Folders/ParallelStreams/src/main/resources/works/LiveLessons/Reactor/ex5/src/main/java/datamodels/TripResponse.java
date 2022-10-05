package datamodels;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Data structure that defines a response for a trip, which is
 * returned by various microservices to indicate which flight legs
 * match a {@code TripRequest}.
 */
@Data
public class TripResponse {
   /**
     * Date and time of the departure.
     */
    public Date departureDateTime;

    /**
     * Date and time of the arrival.
     */
    public Date arrivalDateTime;

    /**
     * Date and time of the return departure.
     */
    public Date returnDepartureDateTime;

    /**
     * Date and time of the return arrival.
     */
    public Date returnArrivalDateTime;

    /**
     * Airport code for the departing airport.
     */
    public String departureAirport;

    /**
     * Airport code for the arriving airport.
     */
    public String arrivalAirport;

    /**
     * Price
     */
    public Double price;

    /**
     * Airline code, e.g., "AA", "SWA", etc.
     */
    public String airlineCode;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    @SuppressWarnings("unused") // Used in serialization by both WebFlux and Spring
    public TripResponse() {
    }

    /**
     * Constructor initializes the fields.
     *
     * @param departureDateTime       Departure date and time
     * @param arrivalDateTime         Arrival date and time
     * @param returnDepartureDateTime Return departure date and time
     * @param returnArrivalDateTime   Return arrival date and time
     * @param departureAirport        Departure airport
     * @param arrivalAirport          Arrival airport
     * @param price                   Price of the flight leg
     * @param airlineCode             Airline code (e.g., "SWA", "AA", etc.)
     */
    public TripResponse(Date departureDateTime,
                        Date arrivalDateTime,
                        Date returnDepartureDateTime,
                        Date returnArrivalDateTime,
                        String departureAirport,
                        String arrivalAirport,
                        Double price,
                        String airlineCode) {
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.returnDepartureDateTime = returnDepartureDateTime;
        this.returnArrivalDateTime = returnArrivalDateTime;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.price = price;
        this.airlineCode = airlineCode;
    }

    /**
     * Factory method that creates a new {@code TripResponse} object.
     *
     * @param departureDateTime       Departure date and time
     * @param arrivalDateTime         Arrival date and time
     * @param returnDepartureDateTime Return departure date and time
     * @param returnArrivalDateTime   Return arrival date and time
     * @param departureAirport        Departure airport
     * @param arrivalAirport          Arrival airport
     * @param price                   Price of the flight leg
     * @param airlineCode             Airline code (e.g., "SWA", "AA", etc.)
     */
    public static TripResponse valueOf(Date departureDateTime,
                                       Date arrivalDateTime,
                                       Date returnDepartureDateTime,
                                       Date returnArrivalDateTime,
                                       String departureAirport,
                                       String arrivalAirport,
                                       Double price,
                                       String airlineCode) {
        return new TripResponse(departureDateTime,
                arrivalDateTime,
                returnDepartureDateTime,
                returnArrivalDateTime,
                departureAirport,
                arrivalAirport,
                price,
                airlineCode);
    }

    /**
     * Create a new TripResponse with its price converted to another
     * currency by multiplying by the specified exchange {@code rate}.
     */
    public TripResponse convert(double rate) {
        // Update the price to reflect the exchange rate!
        return valueOf(
                departureDateTime,
                arrivalDateTime,
                returnDepartureDateTime,
                returnArrivalDateTime,
                departureAirport,
                arrivalAirport,
                price * rate,
                airlineCode);
    }

    /**
     * Note that the id primary key member is not used
     * in either equality or hash comparisons.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripResponse)) return false;
        TripResponse that = (TripResponse) o;
        return Objects.equals(departureDateTime, that.departureDateTime)
                && Objects.equals(arrivalDateTime, that.arrivalDateTime)
                && Objects.equals(returnDepartureDateTime, that.returnDepartureDateTime)
                && Objects.equals(returnArrivalDateTime, that.returnArrivalDateTime)
                && Objects.equals(departureAirport, that.departureAirport)
                && Objects.equals(arrivalAirport, that.arrivalAirport)
                && Objects.equals(price, that.price)
                && Objects.equals(airlineCode, that.airlineCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                departureDateTime,
                arrivalDateTime,
                returnDepartureDateTime,
                returnArrivalDateTime,
                departureAirport,
                arrivalAirport,
                price,
                airlineCode
        );
    }

    @Override
    public String toString() {
        return "TripResponse{" + "departureDateTime=" + departureDateTime +
                ", arrivalDateTime=" + arrivalDateTime +
                ", returnDepartureDateTime=" + returnDepartureDateTime +
                ", returnArrivalDateTime=" + returnArrivalDateTime +
                ", departureAirport='" + departureAirport + '\'' +
                ", arrivalAirport='" + arrivalAirport + '\'' +
                ", price=" + price +
                ", airlineCode='" + airlineCode + '\'' +
                '}';
    }
}
