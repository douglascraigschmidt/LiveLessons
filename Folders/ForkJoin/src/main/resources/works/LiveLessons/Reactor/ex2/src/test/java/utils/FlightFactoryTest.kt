package utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@OptIn(ExperimentalTime::class)
class FlightFactoryTest {
    private val roundingMode = RoundingMode.HALF_EVEN
    private val scale = 2
    private val debug = false

    private val Double.bigDecimal: BigDecimal
        get() = BigDecimal(this)

    private val BigDecimal.reverseRate: BigDecimal
        get() = BigDecimal("1").divide(this, roundingMode)

    private val usdRates = mapOf(
        "USD" to BigDecimal("1.0"),
        "GBP" to BigDecimal("0.84"),
        "EUR" to BigDecimal("0.99")
    )

    private fun Double.round(): Double = this.bigDecimal.toDouble()

    private fun convertToUSD(from: String, value: BigDecimal): BigDecimal {
        if (from == "USD") {
            return value
        }
        val rate = usdRates[from]
        val result = value.divide(rate, 20, roundingMode)
        log("convert${from}ToUSD($value) = $result")
        return result
    }

    /** Deliberately throws and exception for unknown currencies. */
    private fun convertUSD(to: String, value: BigDecimal): BigDecimal {
        if (to == "USD") {
            return value
        }
        val rate = requireNotNull(usdRates[to])
        val result = rate.times(value)
        log("convertUSDTo$to($value) = $result")
        return result
    }

    private fun convertCurrency(from: String, to: String, value: BigDecimal): BigDecimal {
        if (from == to) {
            return value
        }
        val usdValue = convertToUSD(from, value)
        val toValue = convertUSD(to, usdValue)
        return toValue
    }

    private fun generateValues(max: BigDecimal = BigDecimal(10000)): Sequence<BigDecimal> {
        var next = 1.0.bigDecimal
        val inc = 0.01.bigDecimal

        val expected = generateSequence {
            next = next.add(inc)
            next.takeIf { it.compareTo(max) == -1 }
        }
        return expected
    }

    @Test
    fun bug1dot5GPBToUSD() {
        val value = "1.5".toBigDecimal()
        val from = "GBP"
        val to = "USD"

        log("--- $from($value) -> $to ---")

        val r1 = convertCurrency(from, to, value)
//        val r2 = convertCurrency(to, from, r1.setScale(scale, roundingMode))
        val r2 = convertCurrency(to, from, r1)

        assertThat(r2.setScale(scale, roundingMode)).isEqualTo(value.setScale(scale, roundingMode))
    }

    @Test
    fun convertToUSDAndBack() {
        generateValues().forEach { value ->
            currencies.filterNot { it == "USD" }.forEach { from ->
                val r1 = convertToUSD(from, value)
                val r2 = convertUSD(from, r1.setScale(scale, roundingMode))
                assertThat(r2.setScale(scale, roundingMode))
                    .isEqualTo(value.setScale(scale, roundingMode))
            }
        }
    }

    @Test
    fun convertToAnotherCurrencyAndBackAgain() {
        generateValues().forEach { value ->
            currencies.forEach { from ->
                currencies.forEach { to ->
                    log("--- $from -> $to ---")
                    val r1 = convertCurrency(from, to, value)
                    log("$value $from -> $to = $r1")
                    val r2 = convertCurrency(to, from, r1)
                    log("$r1 $to -> $from = $r2")
                    assertThat(r2.setScale(scale, roundingMode))
                        .isEqualTo(value.setScale(scale, roundingMode))
                }
            }
        }
    }

    @Test
    fun convertCurrenciesTransitive() {
        measureTime {
            generateValues(max = 5000.0.toBigDecimal()).forEach { value ->
                val valueScaled = BigDecimal(value.toString()).setScale(2, roundingMode)
                currencies.forEach { a ->
                    currencies.forEach { b ->
                        currencies.forEach { c ->
                            val pass = "--- $a($value) -> $b -> $c ---"
                            log(pass)

                            try {
                                // First check that a -> c is reflexive.
                                val ac = convertCurrency(a, c, value)
                                log("$value $a -> $c = $ac")

                                val aca = convertCurrency(c, a, ac)
                                log("$aca $c -> $a = $aca")

                                val acaScaled = BigDecimal(aca.toString()).setScale(2, roundingMode)
                                assertThat(acaScaled).isEqualTo(valueScaled)

                                // check that a -> b -> c is transitive.

                                val ab = convertCurrency(a, b, value)
                                log("$value [$a -> $b] -> $c = $ab")

                                val abc = convertCurrency(b, c, ab)
                                log("$ab $a -> [$b -> $c] = $abc")

                                val abcScaled = BigDecimal(abc.toString()).setScale(2, roundingMode)
                                val acScaled = BigDecimal(ac.toString()).setScale(2, roundingMode)
                                assertThat(abcScaled).isEqualTo(acScaled)

                                val abca = convertCurrency(c, a, abc)
                                log("$abc [$a -> $b -> $c] -> $a = $abca")

                                val abcaScaled = BigDecimal(abca.toString()).setScale(2, roundingMode)
                                assertThat(abcaScaled).isEqualTo(valueScaled)

                                // check that c -> b -> a is transitive.

                                val cb = convertCurrency(c, b, abc)
                                log("$value [$c -> $b] -> $a = $cb")

                                val cba = convertCurrency(b, a, cb)
                                log("$ab $c -> [$b -> $a] = $cba")

                                val cbaScaled = BigDecimal(cba.toString()).setScale(2, roundingMode)
                                assertThat(cbaScaled).isEqualTo(valueScaled)
                            } catch (t: Throwable) {
                                println("FAILED: $pass")
                                throw t
                            }
                        }
                    }
                }
            }
        }
    }

    private val currencies: Set<String>
        get() = usdRates.keys

    private fun log(msg: String) {
        if (debug) {
            println(msg)
        }
    }

    sealed class Stream {
        object End : Stream()
        class Item(val data: Long) : Stream()
    }
}