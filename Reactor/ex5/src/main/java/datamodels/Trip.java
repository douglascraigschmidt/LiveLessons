package datamodels;

import java.time.LocalDate;

/**
 * Data structure that defines a trip.
 */
public class Trip {
    /**
     * Date of the departure trip.
     */
    private LocalDate departureDate;

    /**
     * Date of the return trip.
     */
    private LocalDate returnDate;

    /**
     * Airport code for the departing airport.
     */
    private String departureAirport;

    /**
     * Airport code for the arriving airport.
     */
    private String arrivalAirport;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    public Trip() {
    }

    /**
     * Constructor initializes the fields.
     * @param departureDate
     * @param returnDate
     * @param departureAirport
     * @param arrivalAirport
     */
    public Trip(LocalDate departureDate,
                LocalDate returnDate,
                String departureAirport,
                String arrivalAirport) {
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
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

    /**
     * Factory method that creates a new {@code Trip} object.
     *
     * @param departureDate
     * @param returnDate
     * @param departureAirport
     * @param arrivalAirport
     * @return A new {@code Trip} object.
     */
    public static Trip valueOf(LocalDate departureDate,
                               LocalDate returnDate,
                               String departureAirport,
                               String arrivalAirport) {
        return new Trip(departureDate, returnDate, departureAirport, arrivalAirport);
    }
}
