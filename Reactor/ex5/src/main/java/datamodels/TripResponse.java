package datamodels;

import java.time.LocalDateTime;

/**
 * Data structure that defines a trip.
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
     * @param departureDateTime
     * @param arrivalDateTime
     * @param returnDepartureDateTime
     * @param returnArrivalDateTime
     * @param departureAirport
     * @param arrivalAirport
     * @param price
     * @param airlineCode
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

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass())
            return false;

        TripResponse t = (TripResponse) o;
        return this.departureDateTime.toLocalDate().equals(t.departureDateTime.toLocalDate())
            && this.returnDepartureDateTime.toLocalDate().equals(t.returnDepartureDateTime.toLocalDate())
            && this.departureAirport.equals(t.departureAirport)
            && this.arrivalAirport.equals(t.arrivalAirport);
    }

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

    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(LocalDateTime departureDate) {
        this.departureDateTime = departureDate;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public LocalDateTime getReturnDepartureDateTime() {
        return returnDepartureDateTime;
    }

    public void setReturnDateTime(LocalDateTime returnDepartureDateTime) {
        this.returnDepartureDateTime = returnDepartureDateTime;
    }

    public LocalDateTime getReturnArrivalDateTime() {
        return returnArrivalDateTime;
    }

    public void setReturnArrivalDateTime(LocalDateTime returnArrivalDateTime) {
        this.returnArrivalDateTime = returnArrivalDateTime;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    /**
     * Factory method that creates a new {@code Trip} object.
     *
     * @param departureDateTime
     * @param arrivalDateTime
     * @param returnDepartureDateTime
     * @param returnArrivalDateTime
     * @param departureAirport
     * @param arrivalAirport
     * @param price
     * @param airlineCode
     * @return
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
