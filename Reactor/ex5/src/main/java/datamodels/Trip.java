package datamodels;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data structure that defines a trip.
 */
public class Trip {
    /**
     * Date and time of the departure.
     */
    private LocalDateTime departureDateTime;

    /**
     * Date and time of the arrival.
     */
    private LocalDateTime arrivalDateTime;

    /**
     * Date and time of the return departure.
     */
    private LocalDateTime returnDepartureDateTime;

    /**
     * Date and time of the return arrival.
     */
    private LocalDateTime returnArrivalDateTime;

    /**
     * Airport code for the departing airport.
     */
    private String departureAirport;

    /**
     * Airport code for the arriving airport.
     */
    private String arrivalAirport;

    /**
     * Price
     */
    private Double price;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    public Trip() {
    }

    /**
     * Constructor initializes the fields.
     * @param departureDateTime
     * @param arrivalDateTime
     * @param returnDepartureDateTime
     * @param returnArrivalDateTime
     * @param departureAirport
     * @param arrivalAirport
     */
    public Trip(LocalDateTime departureDateTime,
                LocalDateTime arrivalDateTime,
                LocalDateTime returnDepartureDateTime,
                LocalDateTime returnArrivalDateTime,
                String departureAirport,
                String arrivalAirport) {
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.returnDepartureDateTime = returnDepartureDateTime;
        this.returnArrivalDateTime = returnArrivalDateTime;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
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
     */
    public Trip(LocalDateTime departureDateTime,
                LocalDateTime arrivalDateTime,
                LocalDateTime returnDepartureDateTime,
                LocalDateTime returnArrivalDateTime,
                String departureAirport,
                String arrivalAirport,
                Double price) {
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.returnDepartureDateTime = returnDepartureDateTime;
        this.returnArrivalDateTime = returnArrivalDateTime;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass())
            return false;

        Trip t = (Trip) o;
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
            + price; 
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

    /**
     * Factory method that creates a new {@code Trip} object.
     *
     * @param departureDateTime
     * @param arrivalDateTime
     * @param returnDepartureDateTime
     * @param returnArrivalDateTime
     * @param departureAirport
     * @param arrivalAirport
     * @return
     */
    public static Trip valueOf(LocalDateTime departureDateTime,
                               LocalDateTime arrivalDateTime,
                               LocalDateTime returnDepartureDateTime,
                               LocalDateTime returnArrivalDateTime,
                               String departureAirport,
                               String arrivalAirport) {
        return new Trip(departureDateTime,
                        arrivalDateTime,
                        returnDepartureDateTime,
                        returnArrivalDateTime,
                        departureAirport,
                        arrivalAirport,
                        0.0);
    }
}
