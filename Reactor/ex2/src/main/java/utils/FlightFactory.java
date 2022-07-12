package utils;

import datamodels.Flight;
import datamodels.FlightRequest;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class creates a list random round flight flights for any number
 * of airlines, airports, travel dates, and number of daily flights.
 */
public class FlightFactory {
    private static final Random random = new Random();
    private static final double MIN_USD_PRICE_PER_KM = 0.1;
    private static final double MAX_USD_PRICE_PER_KM = 0.3;
    private static final int BASE_PRICE = 50;
    private static final int MIN_DISTANCE_KM = 200;
    private static final int MAX_DISTANCE_KM = 5000;
    private static final int FLIGHT_SPEED_KPH = 400;
    private static final int RUNWAY_MINUTES = 15;

    private static final int AIRPORTS = 3;
    private static final int AIRLINES = 2;
    private static final int AIRLINE_DAILY_FLIGHTS = 1;
    private static final int DAYS = 3;
    private static final String CURRENCY = "USD";

    @Builder
    private static class FlightPath {
        String fromAirport;
        String toAirport;
        int distance;
    }

    /**
     * Builds a list of flights matching a flight request built using the
     * first flight in a random list of flights. This method should only
     * be used for FLIGHT and BEST_PRICE requests.
     *
     * @return A random list of Flight objects.
     */
    public static List<Flight> buildExpectedFlights(String currency, List<String> currencies) {
        List<Flight> flights = FlightFactory.builder().build();
        FlightRequest flightRequest = FlightFactory.buildRequestFrom(flights.get(0));
        flightRequest.setCurrency(currency);

        return flights.stream()
                .filter(flight ->
                        flight.getDepartureAirport()
                                .equals(flightRequest.getDepartureAirport()) &&
                                flight.getDepartureDate()
                                        .equals(flightRequest.getDepartureDate()) &&
                                flight.getArrivalAirport()
                                        .equals(flightRequest.getArrivalAirport()))
                .peek(flight -> {
                    String randomCurrency = currencies.get(random.nextInt(currencies.size()));
                    flight.setCurrency(randomCurrency);
                })
                .collect(Collectors.toList());
    }

    public static RandomFlightBuilder builder() {
        return new RandomFlightBuilder();
    }

    public static Flight randomFlight() {
        return builder()
                .airlines(1)
                .airports(2)
                .dailyFlights(1)
                .build().get(0);
    }

    public static class RandomFlightBuilder {
        int dailyFlights = AIRLINE_DAILY_FLIGHTS;
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(DAYS);
        String currency = CURRENCY;
        List<String> airports = randomStrings(AIRPORTS);
        List<String> airlines = randomStrings(AIRLINES);
        int minDistanceKm = MIN_DISTANCE_KM;
        int maxDistanceKm = MAX_DISTANCE_KM;
        double minPricePerKm = MIN_USD_PRICE_PER_KM;
        double maxPricePerKm = MAX_USD_PRICE_PER_KM;
        int basePrice = BASE_PRICE;

        public RandomFlightBuilder basePrice(int basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public RandomFlightBuilder airlines(int airlines) {
            this.airlines = randomStrings(airlines);
            return this;
        }

        public RandomFlightBuilder airlines(String... airlines) {
            airlines(Arrays.asList(airlines));
            return this;
        }

        public RandomFlightBuilder airlines(List<String> airlines) {
            this.airlines = airlines.stream()
                    .distinct()
                    .collect(Collectors.toList());
            return this;
        }

        public RandomFlightBuilder airports(int airports) {
            this.airports = randomStrings(airports);
            return this;
        }

        public RandomFlightBuilder airports(String... airports) {
            airlines(Arrays.asList(airports));
            return this;
        }

        public RandomFlightBuilder airports(List<String> airports) {
            this.airports = airports.stream()
                    .distinct()
                    .collect(Collectors.toList());
            return this;
        }

        public RandomFlightBuilder dailyFlights(int dailyFlights) {
            this.dailyFlights = dailyFlights;
            return this;
        }

        public RandomFlightBuilder minDistanceKm(int minDistance) {
            this.minDistanceKm = minDistance;
            return this;
        }

        public RandomFlightBuilder maxDistanceKm(int maxDistance) {
            this.maxDistanceKm = maxDistance;
            return this;
        }

        public RandomFlightBuilder minPricePerKm(double minPricePerKm) {
            this.minPricePerKm = minPricePerKm;
            return this;
        }

        public RandomFlightBuilder maxPricePerKm(double maxPricePerKm) {
            this.maxPricePerKm = maxPricePerKm;
            return this;
        }

        public RandomFlightBuilder from(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public RandomFlightBuilder from(String fromDate) {
            this.fromDate = LocalDate.parse(fromDate);
            return this;
        }

        public RandomFlightBuilder to(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public RandomFlightBuilder to(String toDate) {
            this.toDate = LocalDate.parse(toDate);
            return this;
        }

        public RandomFlightBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public List<Flight> build() {
            return randomFlights(this);
        }
    }

    private static List<Flight> randomFlights(RandomFlightBuilder builder) {
        List<FlightPath> flightPaths = new ArrayList<>();
        for (int i = 0; i < builder.airports.size() - 1; i++) {
            String fromAirport = builder.airports.get(i);
            for (int j = i + 1; j < builder.airports.size(); j++) {
                String toAirport = builder.airports.get(j);
                int distance = random(builder.minDistanceKm, builder.maxDistanceKm);
                flightPaths.add(FlightPath.builder()
                        .fromAirport(fromAirport)
                        .toAirport(toAirport)
                        .distance(distance)
                        .build());
                flightPaths.add(FlightPath.builder()
                        .fromAirport(toAirport)
                        .toAirport(fromAirport)
                        .distance(distance)
                        .build());
            }
        }

        List<Flight> flights =
                builder.fromDate.datesUntil(builder.toDate.plusDays(1)).flatMap(date ->
                        builder.airlines.stream().flatMap(airline ->
                                flightPaths.stream().flatMap(flight ->
                                        IntStream.range(0, builder.dailyFlights).mapToObj(__ ->
                                                randomFlight(
                                                        flight.fromAirport,
                                                        flight.toAirport,
                                                        date,
                                                        flight.distance,
                                                        builder.basePrice,
                                                        builder.minPricePerKm,
                                                        builder.maxPricePerKm,
                                                        airline,
                                                        builder.currency)
                                        )
                                )
                        )
                ).collect(Collectors.toList());

        long days = builder.fromDate.datesUntil(builder.toDate.plusDays(1)).count();
        long expected = builder.airlines.size()
                * flightPaths.size()
                * days
                * builder.dailyFlights;

        return flights;
    }

    private static Flight randomFlight(
            String departureAirport,
            String arrivalAirport,
            LocalDate date,
            int distance,
            int basePrice,
            double minPricePerKm,
            double maxPricePerKm,
            String airlineCode,
            String currency
    ) {

        LocalTime flightTime = flightTime(distance);
        LocalTime departureTime = randomDepartureTime(flightTime);
        LocalTime arrivalTime = arrivalTime(departureTime, flightTime);

        return Flight.builder()
                .departureAirport(departureAirport)
                .departureDate(date)
                .departureTime(departureTime)
                .arrivalAirport(arrivalAirport)
                .arrivalTime(arrivalTime)
                .arrivalDate(date)
                .airlineCode(airlineCode)
                .kilometers(distance)
                .price(randomPrice(distance, basePrice, minPricePerKm, maxPricePerKm))
                .currency(currency)
                .build();
    }

    public static FlightRequest buildRequestFrom(Flight flight) {
        return FlightRequest.builder()
                .departureAirport(flight.getDepartureAirport())
                .departureDate(flight.getDepartureDate())
                .arrivalAirport(flight.getArrivalAirport())
                .currency(flight.getCurrency())
                .build();
    }

    private static double random(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static int random(int min, int max) {
        return (int) random((double) min, max);
    }

    private static int randomPrice(
            int distance, int basePrice, double min, double max) {
        return basePrice + (int) (distance * random(min, max));
    }

    private static LocalTime flightTime(int distance) {
        return LocalTime.ofSecondOfDay(RUNWAY_MINUTES +
                (long) ((double) distance / FLIGHT_SPEED_KPH * 60 * 60));
    }

    private static LocalTime randomDepartureTime(LocalTime flightTime) {
        // Latest departure hour 24 hours - flightTime.hours.
        return LocalTime.of(
                random.nextInt(23 - flightTime.getHour()),
                random.nextInt(60));
    }

    private static LocalTime arrivalTime(LocalTime departureTime, LocalTime flightTime) {
        return LocalTime.from(departureTime)
                .plusHours(flightTime.getHour())
                .plusMinutes(flightTime.getMinute());
    }

    private static List<String> randomStrings(int count) {
        List<String> strings = new ArrayList<>();
        int length = (int) (Math.log10(count) + 1);
        for (int i = 1; i <= count; i++) {
            strings.add(String.format("%0" + length + "d", i));
        }

        return strings;
    }

    /**
     * For testing.
     */
    public static void main(String[] args) {
        List<Flight> flights = builder().airlines("AA").build();

        flights.sort(Comparator.comparing(Flight::getDepartureDate));
        System.out.println(Flight.toSqlInsertString(flights));
    }
}