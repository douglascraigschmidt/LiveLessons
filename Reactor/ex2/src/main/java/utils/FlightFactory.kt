package utils

import datamodels.Flight
import datamodels.FlightRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.stream.Collectors
import kotlin.math.log10
import kotlin.math.min

/**
 * This class creates a list random round flight flights for any
 * number of airlines, airports, travel dates, and number of daily
 * flights.
 */
object FlightFactory {
    private const val AIRLINES = 2
    private const val DAYS = 1
    private const val BASE_PRICE_USD = 100
    private const val MIN_PRICE_PER_KM_USD = 0.4
    private const val MAX_PRICE_PER_KM_USD = 0.7
    private const val MIN_DISTANCE_KM = 200
    private const val MAX_DISTANCE_KM = 5000
    private const val FLIGHT_SPEED_KPH = 400
    private const val RUNWAY_MINUTES = 15
    private const val GENERATE_RETURN_FLIGHTS = false
    private const val DAILY_FLIGHTS = 100
    private const val MIN_LOWEST_PRICE_MATCHES = 1
    private const val MAX_LOWEST_PRICE_MATCHES = 3

    private val random = Random()

    data class Currency(
        val name: String,
        val rates: Map<String, Double>
    )

    @JvmStatic
    fun buildRandomRequestFrom(flights: List<Flight>): FlightRequest {
        return flights[random(0, flights.size)].buildRequest()
    }

    class Builder {
        var airports = randomStrings(2, prefix = "Airport-")
        var fromAirport = airports.first()
        var toAirport = airports.last()
        var minFlights = DAILY_FLIGHTS
        var maxFlights = DAILY_FLIGHTS
        var fromDate: LocalDate = LocalDate.now()
        var toDate: LocalDate = fromDate.plusDays(DAYS.toLong())
        var currencies = ExchangeRate.Currency.values().map { it.toString() }
        var airlines = randomStrings(AIRLINES)
        var minDistanceKm = MIN_DISTANCE_KM
        var maxDistanceKm = MAX_DISTANCE_KM
        var minPricePerKm = MIN_PRICE_PER_KM_USD
        var maxPricePerKm = MAX_PRICE_PER_KM_USD
        var basePrice = BASE_PRICE_USD
        var minLowestPriceMatches = MIN_LOWEST_PRICE_MATCHES
        var maxLowestPriceMatches = MAX_LOWEST_PRICE_MATCHES

        fun validate() {
            check(minLowestPriceMatches in 1..maxLowestPriceMatches)
            check(minFlights in 1..maxFlights)
            check(minDistanceKm in 1..maxDistanceKm)
            check(0.0 < minPricePerKm && minPricePerKm <= maxPricePerKm)
            check(airports.isNotEmpty())
            check(airlines.isNotEmpty())
            check(currencies.isNotEmpty())
            check(fromDate <= toDate)
            check(minFlights - maxLowestPriceMatches > 0)
        }

        fun minDailyFlights(min: Int): Builder {
            this.minFlights = min
            return this
        }

        fun maxDailyFlights(max: Int): Builder {
            this.maxFlights = max
            return this
        }

        fun minLowestPriceMatches(matches: Int): Builder {
            this.minLowestPriceMatches = matches
            return this
        }

        fun maxLowestPriceMatches(matches: Int): Builder {
            this.minLowestPriceMatches = matches
            return this
        }

        fun basePrice(basePrice: Int): Builder {
            this.basePrice = basePrice
            return this
        }

        fun airlines(airlines: Int): Builder {
            airlines(randomStrings(airlines))
            return this
        }

        fun airlines(vararg airlines: String): Builder {
            airlines(listOf(*airlines))
            return this
        }

        fun airlines(airlines: List<String>): Builder {
            this.airlines = airlines.stream()
                .distinct()
                .collect(Collectors.toList())
            return this
        }

        fun fromAirport(airport: String): Builder {
            fromAirport = airport
            return this
        }

        fun toAirport(airport: String): Builder {
            toAirport = airport
            return this
        }

        fun minFlights(min: Int): Builder {
            this.minFlights = min
            return this
        }

        fun maxFlights(max: Int): Builder {
            this.maxFlights = max
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

        fun currencies(currencies: List<String>): Builder =
            this.also {
                it.currencies = currencies
            }

        fun generateFlights(): List<Flight> {
            validate()

            var minPrice = Double.MAX_VALUE
            val flights = mutableListOf<Flight>()

            val distance = random(minDistanceKm, maxDistanceKm)

            var flightCount = random(minFlights, maxFlights + 1)
            val matches = random(minLowestPriceMatches, maxLowestPriceMatches)

            (1..flightCount).map { count ->
                val flightTime = flightTime(distance)
                val departureDateTime =
                    LocalDateTime.of(fromDate, randomDepartureTime(flightTime))
                val arrivalDateTime =
                    departureDateTime
                        .plusHours(flightTime.hour.toLong())
                        .plusMinutes(flightTime.minute.toLong())


                val price =
                    if (count <= flightCount - matches) {
                        randomPrice(
                            distance,
                            basePrice,
                            minPricePerKm,
                            maxPricePerKm
                        ).toDouble()
                            .also {
                                minPrice = min(minPrice, it)
                            }
                    } else {
                        minPrice - 10.0
                    }

                val currency = currencies.random()

                val flight = Flight(
                    fromAirport,
                    departureDateTime.toLocalDate(),
                    departureDateTime.toLocalTime(),
                    toAirport,
                    arrivalDateTime.toLocalDate(),
                    arrivalDateTime.toLocalTime(),
                    distance,
                    ExchangeRate.convert(price, "USD", currency),
                    currency,
                    airlines.random()
                )

                flights.add(flight)
            }

            check(flights.count() in minFlights..maxFlights)

            return flights.shuffled(random)
        }
    }

    private fun randomStrings(count: Int, prefix: String = ""): List<String> {
        val strings: MutableList<String> = ArrayList()
        val length = (log10(count.toDouble()) + 1).toInt()
        for (i in 1..count) {
            strings.add(String.format("$prefix%0" + length + "d", i))
        }
        return strings
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
        return LocalTime.ofSecondOfDay(
            RUNWAY_MINUTES + (distance.toDouble() / FLIGHT_SPEED_KPH * 60 * 60).toLong()
        )
    }

    /**
     * Produces a random departure time that ensures that the
     * arrival time will be before midnight on the same day.
     */
    private fun randomDepartureTime(flightTime: LocalTime): LocalTime {
        // Latest departure hour 24 hours - flightTime.hours.
        check(flightTime.hour <= 23)
        random.nextInt(flightTime.minute)

        val hour: Int
        val min: Int

        return try {
            hour = random.nextInt(23 - flightTime.hour)
            min = random.nextInt(flightTime.minute)

            LocalTime.of(hour, min)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun arrivalTime(departureTime: LocalTime, flightTime: LocalTime): LocalTime =
        LocalTime.from(departureTime)
            .plusHours(flightTime.hour.toLong())
            .plusMinutes(flightTime.minute.toLong())
}
