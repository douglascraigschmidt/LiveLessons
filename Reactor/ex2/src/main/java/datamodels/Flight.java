package datamodels;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Data structure that defines a response for a flight, which is
 * returned by various microservices to indicate which flights
 * match a {@code FlightRequest}.
 */
@Data
//@RequiredArgsConstructor
//@NoArgsConstructor(force = true)
@Builder
@With
public class Flight {
    Long id;
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

    public static String insertIntoTableHeader() {
        return "insert into FLIGHT (\n" +
               "\tid,\n" +
               "\tdeparture_airport,\n" +
               "\tdeparture_date,\n" +
               "\tdeparture_time,\n" +
               "\tarrival_airport,\n" +
               "\tarrival_date,\n" +
               "\tarrival_time,\n" +
               "\tkilometers,\n" +
               "\tprice,\n" +
               "\tcurrency,\n" +
               "\tairline_code\n" +
               "\tcapacity\n" +
               ") values\n";
    }

    /**
     * Used for create sample data.
     * @return SQL insert value entry string.
     */
    public String insertTableValueEntry() {
        return "(default" +
                ", '" + departureAirport + "'" +
                ", '" + departureDate + "'" +
                ", '" + departureTime + "'" +
                ", '" + arrivalAirport + "'" +
                ", '" + arrivalDate + "'" +
                ", '" + arrivalTime + "'" +
                ", " + kilometers +
                ", " + price +
                ", '" + currency + "'" +
                ", '" + airlineCode + "'" +
                ')';
    }

    /**
     * Creates an SQL insert table definition that can be used
     * to pre-load the FLIGHT database table.
     *
     * @param flights List {@link Flight} objects.
     * @return A complete table insert definition.
     */
    public static String toSqlInsertString(List<Flight> flights) {
        StringBuilder builder =
                new StringBuilder(Flight.insertIntoTableHeader());

        for (int i = 0; i < flights.size(); i++) {
            builder.append("\t");
            builder.append(flights.get(i).insertTableValueEntry());
            builder.append(i < flights.size() - 1 ? ",\n" : ";");
        }

        return builder.toString();
    }
}