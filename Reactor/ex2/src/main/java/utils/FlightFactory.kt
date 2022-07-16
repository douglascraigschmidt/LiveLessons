package utils

import datamodels.Flight
import datamodels.FlightRequest
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * This class creates a list random round flight flights for any number
 * of airlines, airports, travel dates, and number of daily flights.
 */
object FlightFactory {
    private val random = Random()
    private const val MIN_USD_PRICE_PER_KM = 0.1
    private const val MAX_USD_PRICE_PER_KM = 0.3
    private const val BASE_PRICE = 50
    private const val MIN_DISTANCE_KM = 200
    private const val MAX_DISTANCE_KM = 5000
    private const val FLIGHT_SPEED_KPH = 400
    private const val RUNWAY_MINUTES = 15
    private const val AIRPORTS = 3
    private const val AIRLINES = 2
    private const val AIRLINE_DAILY_FLIGHTS = 1
    private const val DAYS = 3
    private const val CURRENCY = "USD"

    /**
     * Builds a list of flights matching a flight request built using the
     * first flight in a random list of flights. This method should only
     * be used for FLIGHT and BEST_PRICE requests.
     *
     * @return A random list of Flight objects.
     */
    fun buildExpectedFlights(currency: String, currencies: List<String>): List<Flight> {
        val flights = builder().build()
        val flightRequest = buildRequestFrom(flights[0])

        flightRequest.currency = currency

        return flights.filter { flight ->
            (flight.departureAirport == flightRequest.departureAirport) &&
                    (flight.departureDate == flightRequest.departureDate) &&
                    (flight.arrivalAirport == flightRequest.arrivalAirport)
        }.map {
            Flight(
                /* departureAirport = */ it.departureAirport,
                /* departureDate = */ it.departureDate,
                /* departureTime = */ it.departureTime,
                /* arrivalAirport = */ it.arrivalAirport,
                /* arrivalDate = */ it.arrivalDate,
                /* arrivalTime = */ it.arrivalTime,
                /* kilometers = */ it.kilometers,
                /* price = */ it.price,
                /* currency = */ currencies[random.nextInt(currencies.size)],
                /* airlineCode = */ it.airlineCode
            )
        }
    }

    fun randomFlight(): Flight =
        builder()
            .airlines(1)
            .airports(2)
            .dailyFlights(1)
            .build()[0]

    fun builder(): Builder = Builder()

    private fun randomFlights(builder: Builder): List<Flight> {
        val flightPaths: MutableList<FlightPath> = ArrayList()

        val airports = builder.airports

        val pairs = mutableListOf<Pair<String, String>>()

        val result = airports.flatMapIndexedTo(pairs) { i: Int, str: String ->
            listOf(Pair("", ""))
        }

        for (i in 0 until builder.airports.size - 1) {
            val fromAirport = builder.airports[i]
            for (j in i + 1 until builder.airports.size) {
                val toAirport = builder.airports[j]
                val distance = random(builder.minDistanceKm, builder.maxDistanceKm)

                // There ...
                flightPaths.add(
                    FlightPath(
                        fromAirport = fromAirport,
                        toAirport = toAirport,
                        distance = distance
                    )
                )

                // and back again ...
                flightPaths.add(
                    FlightPath(
                        fromAirport = toAirport,
                        toAirport = fromAirport,
                        distance = distance
                    )
                )
            }
        }
        val flights = builder.fromDate.datesUntil(builder.toDate.plusDays(1)).flatMap { date: LocalDate ->
            builder.airlines.stream().flatMap { airline: String ->
                flightPaths.stream().flatMap { flight: FlightPath ->
                    IntStream.range(0, builder.dailyFlights).mapToObj { __: Int ->
                        randomFlight(
                            flight.fromAirport,
                            flight.toAirport,
                            date,
                            flight.distance,
                            builder.basePrice,
                            builder.minPricePerKm,
                            builder.maxPricePerKm,
                            airline,
                            builder.currency
                        )
                    }
                }
            }
        }.collect(Collectors.toList())
        val days = builder.fromDate.datesUntil(builder.toDate.plusDays(1)).count()
        val expected = (builder.airlines.size
                * flightPaths.size
                * days
                * builder.dailyFlights)
        return flights
    }

    private fun randomFlight(
        departureAirport: String?,
        arrivalAirport: String?,
        date: LocalDate,
        distance: Int,
        basePrice: Int,
        minPricePerKm: Double,
        maxPricePerKm: Double,
        airlineCode: String,
        currency: String
    ): Flight {
        val flightTime = flightTime(distance)
        val departureTime = randomDepartureTime(flightTime)
        val arrivalTime = arrivalTime(departureTime, flightTime)
        return Flight.builder()
            .departureAirport(departureAirport)
            .departureDate(date)
            .departureTime(departureTime)
            .arrivalAirport(arrivalAirport)
            .arrivalTime(arrivalTime)
            .arrivalDate(date)
            .airlineCode(airlineCode)
            .kilometers(distance)
            .price(randomPrice(distance, basePrice, minPricePerKm, maxPricePerKm).toDouble())
            .currency(currency)
            .build()
    }

    fun buildRequestFrom(flight: Flight): FlightRequest {
        return FlightRequest.builder()
            .departureAirport(flight.departureAirport)
            .departureDate(flight.departureDate)
            .arrivalAirport(flight.arrivalAirport)
            .currency(flight.currency)
            .build()
    }

    private fun random(min: Double, max: Double): Double {
        return min + random.nextDouble() * (max - min)
    }

    private fun random(min: Int, max: Int): Int {
        return random(min.toDouble(), max.toDouble()).toInt()
    }

    private fun randomPrice(
        distance: Int, basePrice: Int, min: Double, max: Double
    ): Int {
        return basePrice + (distance * random(min, max)).toInt()
    }

    private fun flightTime(distance: Int): LocalTime {
        return LocalTime.ofSecondOfDay(RUNWAY_MINUTES + (distance.toDouble() / FLIGHT_SPEED_KPH * 60 * 60).toLong())
    }

    private fun randomDepartureTime(flightTime: LocalTime): LocalTime {
        // Latest departure hour 24 hours - flightTime.hours.
        return LocalTime.of(
            random.nextInt(23 - flightTime.hour),
            random.nextInt(60)
        )
    }

    private fun arrivalTime(departureTime: LocalTime, flightTime: LocalTime): LocalTime {
        return LocalTime.from(departureTime)
            .plusHours(flightTime.hour.toLong())
            .plusMinutes(flightTime.minute.toLong())
    }

    private fun randomStrings(count: Int): List<String> {
        val strings: MutableList<String> = ArrayList()
        val length = (Math.log10(count.toDouble()) + 1).toInt()
        for (i in 1..count) {
            strings.add(String.format("%0" + length + "d", i))
        }
        return strings
    }

    /**
     * For testing.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val flights = builder().airlines("AA").build()
        flights.sort(Comparator.comparing { obj: Flight -> obj.departureDate })
        println(Flight.toSqlInsertString(flights))
    }

    private data class FlightPath(
        val fromAirport: String,
        val toAirport: String,
        val distance: Int
    )

    class Builder {
        var dailyFlights = AIRLINE_DAILY_FLIGHTS
        var fromDate: LocalDate = LocalDate.now()
        var toDate: LocalDate = fromDate.plusDays(DAYS.toLong())
        var currency = CURRENCY
        var airports = randomStrings(AIRPORTS)
        var airlines = randomStrings(AIRLINES)
        var minDistanceKm = MIN_DISTANCE_KM
        var maxDistanceKm = MAX_DISTANCE_KM
        var minPricePerKm = MIN_USD_PRICE_PER_KM
        var maxPricePerKm = MAX_USD_PRICE_PER_KM
        var basePrice = BASE_PRICE
        fun basePrice(basePrice: Int): Builder {
            this.basePrice = basePrice
            return this
        }

        fun airlines(airlines: Int): Builder {
            this.airlines = randomStrings(airlines)
            return this
        }

        fun airlines(vararg airlines: String): Builder {
            airlines(*airlines)
            return this
        }

        fun airlines(airlines: List<String>): Builder {
            this.airlines = airlines.stream()
                .distinct()
                .collect(Collectors.toList())
            return this
        }

        fun airports(airports: Int): Builder {
            this.airports = randomStrings(airports)
            return this
        }

        fun airports(vararg airports: String): Builder {
            airlines(*airports)
            return this
        }

        fun airports(airports: List<String>): Builder {
            this.airports = airports.stream()
                .distinct()
                .collect(Collectors.toList())
            return this
        }

        fun dailyFlights(dailyFlights: Int): Builder {
            this.dailyFlights = dailyFlights
            return this
        }

        fun minDistanceKm(minDistance: Int): Builder {
            minDistanceKm = minDistance
            return this
        }

        fun maxDistanceKm(maxDistance: Int): Builder {
            maxDistanceKm = maxDistance
            return this
        }

        fun minPricePerKm(minPricePerKm: Double): Builder {
            this.minPricePerKm = minPricePerKm
            return this
        }

        fun maxPricePerKm(maxPricePerKm: Double): Builder {
            this.maxPricePerKm = maxPricePerKm
            return this
        }

        fun from(fromDate: LocalDate): Builder {
            this.fromDate = fromDate
            return this
        }

        fun from(fromDate: String?): Builder {
            this.fromDate = LocalDate.parse(fromDate)
            return this
        }

        fun to(toDate: LocalDate): Builder {
            this.toDate = toDate
            return this
        }

        fun to(toDate: String?): Builder {
            this.toDate = LocalDate.parse(toDate)
            return this
        }

        fun currency(currency: String): Builder {
            this.currency = currency
            return this
        }

        fun build(): List<Flight> {
            return randomFlights(this)
        }
    }
}
