package datamodels;

import java.util.Date;

/**
 * Data structure that defines a request for a trip.
 */
public class TripRequest {
    /**
     * Date and time of the departure.
     */
    private Date departureDateTime;

    /**
     * Date and time of the return departure.
     */
    private Date returnDepartureDateTime;

    /**
     * Airport code for the departing airport.
     */
    private String departureAirport;

    /**
     * The three-letter airport code for the arriving airport.
     */
    private String arrivalAirport;

    /**
     * Number of passengers.
     */
    private Integer passengers;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    public TripRequest() {
    }

    /**
     * Constructor initializes the fields.
     * @param departureDateTime Departure date
     * @param returnDepartureDateTime Return date
     * @param departureAirport Departure airport
     * @param arrivalAirport Arrival airport
     * @param passengers Number of passengers
     */
    public TripRequest(Date departureDateTime,
                       Date returnDepartureDateTime,
                       String departureAirport,
                       String arrivalAirport,
                       int passengers) {
        this.departureDateTime = departureDateTime;
        this.returnDepartureDateTime = returnDepartureDateTime;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.passengers = passengers;
    }

    /**
     * @return Returns true if there's a match between this {@code
     * TripRequest} and the {@code tripRequest} param
     */
    @Override
    public boolean equals(Object tripRequest) {
        if (tripRequest.getClass() != this.getClass())
            return false;

        TripRequest t = (TripRequest) tripRequest;
        return this.departureDateTime
            .equals(t.departureDateTime)
            && this.returnDepartureDateTime
            .equals(t.returnDepartureDateTime)
            && this.departureAirport
            .equals(t.departureAirport)
            && this.arrivalAirport
            .equals(t.arrivalAirport);
    }

    /**
     * @return Returns true if there's a match between this
     * TripRequest and the {@code tripResponse} param
     */
    public boolean equals(TripResponse tripResponse) {
        return this.departureDateTime
            .equals(tripResponse.departureDateTime)
            && this.returnDepartureDateTime
            .equals(tripResponse.returnDepartureDateTime)
            && this.departureAirport
            .equals(tripResponse.departureAirport)
            && this.arrivalAirport
            .equals(tripResponse.arrivalAirport);
    }

    /**
     * @return A String representation of this {@code TripRequest}
     * object
     */
    @Override
    public String toString() {
        return departureDateTime
            + ", "
            + returnDepartureDateTime
            + ", "
            + departureAirport
            + ", "
            + arrivalAirport
            + ", "
            + passengers;
    }

    /**
     * Gets the departure date and time.
     */
    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    /**
     * Sets the departure date and time.
     */
    public void setDepartureDateTime(Date departureDate) {
        this.departureDateTime = departureDate;
    }

    /**
     * Gets the return departure date and time.
     */
    public Date getReturnDepartureDateTime() {
        return returnDepartureDateTime;
    }

    /**
     * Sets the return departure date and time.
     */
    public void setReturnDateTime(Date returnDepartureDateTime) {
        this.returnDepartureDateTime = returnDepartureDateTime;
    }

    /**
     * Gets the departure airport code.
     */
    public String getDepartureAirport() {
        return departureAirport;
    }

    /**
     * Sets the departure airport code.
     */
    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    /**
     * Gets the arrival airport code.
     */
    public String getArrivalAirport() {
        return arrivalAirport;
    }

    /**
     * Sets the arrival airport code.
     */
    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    /**
     * Gets the number of passengers.
     */
    public int getPassengers() {
        return passengers;
    }

    /**
     * Sets the number of passengers.
     */
    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    /**
     * Factory method that creates a new {@code TripReqest} object.
     *
     * @param departureDateTime Departure date
     * @param returnDepartureDateTime Return date
     * @param departureAirport Departure airport
     * @param arrivalAirport Arrival airport
     * @param passengers Number of passengers
     * @return An initialized {@code TripRequest}
     */
    public static TripRequest valueOf(Date departureDateTime,
                                      Date returnDepartureDateTime,
                                      String departureAirport,
                                      String arrivalAirport,
                                      int passengers) {
        return new TripRequest(departureDateTime,
                               returnDepartureDateTime,
                               departureAirport,
                               arrivalAirport,
                               passengers);
    }
}
