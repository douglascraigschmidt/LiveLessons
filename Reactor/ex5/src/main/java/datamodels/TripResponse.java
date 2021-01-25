package datamodels;

import java.time.LocalDateTime;

/**
 * Data structure that defines a response for a trip, which is
 * returned by various microservices to indicate which flight legs
 * match a {@code TripRequest}.
 */
public class TripResponse {
    /**
     * Date and time of the departure.
     */
    LocalDateTime departureDateTime;

    /**
     * Date and time of the arrival.
     */
    LocalDateTime arrivalDateTime;

    /**
     * Date and time of the return departure.
     */
    LocalDateTime returnDepartureDateTime;

    /**
     * Date and time of the return arrival.
     */
    LocalDateTime returnArrivalDateTime;

    /**
     * Airport code for the departing airport.
     */
    String departureAirport;

    /**
     * Airport code for the arriving airport.
     */
    String arrivalAirport;

    /**
     * Price
     */
    Double price;

    /**
     * Airline code, e.g., "AA", "SWA", etc.
     */
    String airlineCode;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    public TripResponse() {
    }

    /**
     * Constructor initializes the fields.
     * @param departureDateTime Departure date and time
     * @param arrivalDateTime Arrival date and time
     * @param returnDepartureDateTime Return departure date and time
     * @param returnArrivalDateTime Return arrival date and time
     * @param departureAirport Departure airport
     * @param arrivalAirport Arrival airport
     * @param price Price of the flight leg
     * @param airlineCode Airline code (e.g., "SWA", "AA", etc.)
     */
    public TripResponse(LocalDateTime departureDateTime,
                        LocalDateTime arrivalDateTime,
                        LocalDateTime returnDepartureDateTime,
                        LocalDateTime returnArrivalDateTime,
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
     * @return Returns true if there's a match between this {@code
     * TripResponse} and the {@code tripResponse} param
     */
    @Override
    public boolean equals(Object tripResponse) {
        if (tripResponse.getClass() != this.getClass())
            return false;

        TripResponse t = (TripResponse) tripResponse;
        return this.departureDateTime.toLocalDate()
            .equals(t.departureDateTime.toLocalDate())
            && this.returnDepartureDateTime.toLocalDate()
            .equals(t.returnDepartureDateTime.toLocalDate())
            && this.departureAirport
            .equals(t.departureAirport)
            && this.arrivalAirport
            .equals(t.arrivalAirport);
    }

    /**
     * @return A String representation of this {@code TripResponse}
     */
    @Override
    public String toString() {
        return departureDateTime
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
            + ", "
            + price
            + ", "
            + airlineCode;
    }

    /**
     * Get the departure date and time.
     */
    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    /**
     * Set the departure date and time.
     */
    public void setDepartureDateTime(LocalDateTime departureDate) {
        this.departureDateTime = departureDate;
    }

    /**
     * Get the arrival date and time.
     */
    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    /**
     * Set the arrival date and time.
     */
    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    /**
     * Get the return departure date and time.
     */
    public LocalDateTime getReturnDepartureDateTime() {
        return returnDepartureDateTime;
    }

    /**
     * Set the return departure date and time.
     */
    public void setReturnDateTime(LocalDateTime returnDepartureDateTime) {
        this.returnDepartureDateTime = returnDepartureDateTime;
    }

    /**
     * Get the return arrival date and time.
     */
    public LocalDateTime getReturnArrivalDateTime() {
        return returnArrivalDateTime;
    }

    /**
     * Set the return arrival date and time.
     */
    public void setReturnArrivalDateTime(LocalDateTime returnArrivalDateTime) {
        this.returnArrivalDateTime = returnArrivalDateTime;
    }

    /**
     * Get the departure airport code.
     */
    public String getDepartureAirport() {
        return departureAirport;
    }

    /**
     * Set the departure airport code.
     */
    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    /**
     * Get the arrival airport code.
     */
    public String getArrivalAirport() {
        return arrivalAirport;
    }

    /**
     * Set the arrival airport code.
     */
    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    /**
     * Get the price for the flight leg.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Set the price for the flight leg.
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Get the airpline code.
     */
    public String getAirlineCode() {
        return airlineCode;
    }

    /**
     * Set the airpline code.
     */
    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    /**
     * Factory method that creates a new {@code Trip} object.
     *
     * @param departureDateTime Departure date and time
     * @param arrivalDateTime Arrival date and time
     * @param returnDepartureDateTime Return departure date and time
     * @param returnArrivalDateTime Return arrival date and time
     * @param departureAirport Departure airport
     * @param arrivalAirport Arrival airport
     * @param price Price of the flight leg
     * @param airlineCode Airline code (e.g., "SWA", "AA", etc.)
     */
    public static TripResponse valueOf(LocalDateTime departureDateTime,
                                       LocalDateTime arrivalDateTime,
                                       LocalDateTime returnDepartureDateTime,
                                       LocalDateTime returnArrivalDateTime,
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
}
