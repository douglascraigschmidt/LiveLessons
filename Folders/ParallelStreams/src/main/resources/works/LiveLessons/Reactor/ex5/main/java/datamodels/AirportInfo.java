package datamodels;

/**
 * Data structure that defines information about an airport.
 */
public class AirportInfo {
    /**
     * The three-letter airport code.
     */
    private String airportCode;

    /**
     * The airport name (e.g., city, state, and country).
     */
    private String airportName;

    /**
     * Default constructor needed for WebFlux param passing.
     */
    public AirportInfo() {
    }

    /**
     * Constructor initializes the fields.
     * @param airportCode The three-letter airport code
     * @param airportName The airport name (e.g., city, state, and country).
     */
    public AirportInfo(String airportCode,
                       String airportName) {
        this.airportCode = airportCode;
        this.airportName = airportName;
    }

    /**
     * @return Returns true if there's a match between this {@code
     * AirportInfo} and the {@code airportInfo} param
     */
    @Override
    public boolean equals(Object airportInfo) {
        if (airportInfo.getClass() != this.getClass())
            return false;

        AirportInfo t = (AirportInfo) airportInfo;
        return this.airportCode
            .equals(t.airportCode)
            && this.airportName
            .equals(t.airportName);
    }

    /**
     * @return A String representation of this {@code AirportInfo}
     * object
     */
    @Override
    public String toString() {
        return airportName
            + " - "
            + airportCode;
    }

    /**
     * Gets the airport code.
     */
    public String getAirportCode() {
        return airportCode;
    }

    /**
     * Sets the airport code.
     */
    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    /**
     * Gets the airport name (e.g., city, state, and country).
     */
    public String getAirportName() {
        return airportName;
    }

    /**
     * Sets the airport name (e.g., city, state, and country).
     */
    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    /**
     * Factory method that creates a new {@code AirportInfo} object.
     *
     * @param airportCode The three-letter airport code
     * @param airportName The airport name (e.g., city, state, and country).
     */
    public static AirportInfo valueOf(String airportCode,
                                      String airportName) {
        return new AirportInfo(airportCode, airportName);
    }
}
