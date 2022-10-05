package utils;

import java.math.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Arbitrary-precision fraction, utilizing BigIntegers for numerator
 * and denominator.  Fraction is immutable, and guaranteed not to have
 * a null numerator or denominator.  Denominator will always be
 * positive (so sign is carried by numerator, and a zero-denominator
 * is impossible).
 * 
 * Based on Kip Robinson's BigFraction class available at
 * https://github.com/kiprobinson/BigFraction.
 */
public final class BigFraction 
       extends Number 
       implements Comparable<Number> {
    private static final long serialVersionUID = 3L; //because Number is Serializable
    private final BigInteger mNumerator;
    private final BigInteger mDenominator;
  
    //some constants used
    private final static BigInteger BIGINT_TWO = BigInteger.valueOf(2);
    private final static BigInteger BIGINT_FIVE = BigInteger.valueOf(5);
  
    /** The value 0/1. */
    public final static BigFraction ZERO = new BigFraction(BigInteger.ZERO, BigInteger.ONE, Reduced.YES);
    /** The value 1/1. */
    public final static BigFraction ONE = new BigFraction(BigInteger.ONE, BigInteger.ONE, Reduced.YES);
    /** The value 1/2. */
    public final static BigFraction ONE_HALF = new BigFraction(BigInteger.ONE, BIGINT_TWO, Reduced.YES);
    /** The value 1/10. */
    public final static BigFraction ONE_TENTH = new BigFraction(BigInteger.ONE, BigInteger.TEN, Reduced.YES);
    /** The value 10/1. */
    public final static BigFraction TEN = new BigFraction(BigInteger.TEN, BigInteger.ONE, Reduced.YES);
  
    private static enum Reduced { YES, NO };
  
    /**
     * <strong>Note:</strong> {@link #valueOf(Number)} should be preferred for performance reasons.
     * This constructor is provided for convenience.
     * 
     * @param n Number to convert to BigFraction.
     * @see #valueOf(Number)
     */
    public BigFraction(Number n) {
        BigFraction bf = valueOf(n);
        mNumerator = bf.mNumerator;
        mDenominator = bf.mDenominator;
    }
  
    /**
     * <strong>Note:</strong> {@link #valueOf(Number, Number)} should be preferred for performance reasons.
     * This constructor is provided for convenience.
     * 
     * @param numerator numerator of new BigFraction
     * @param denominator denominator of new BigFraction
     * @see #valueOf(Number, Number)
     */
    public BigFraction(Number numerator, Number denominator) {
        BigFraction bf = valueOf(numerator, denominator, true);
        mNumerator = bf.mNumerator;
        mDenominator = bf.mDenominator;
    }
  
    /**
     * <strong>Note:</strong> {@link #valueOf(Number, Number)} should be preferred for performance reasons.
     * This constructor is provided for convenience.
     * 
     * @param numerator numerator of new BigFraction
     * @param denominator denominator of new BigFraction
     * @param reduce true if the fraction should be reduced to simplest form, else false.
     * @see #valueOf(Number, Number)
     */
    public BigFraction(Number numerator, Number denominator, boolean reduce) {
        BigFraction bf = valueOf(numerator, denominator, reduce);
        mNumerator = bf.mNumerator;
        mDenominator = bf.mDenominator;
    }
  
    /**
     * <strong>Note:</strong> {@link #valueOf(String)} should be preferred for performance reasons.
     * This constructor is provided for convenience.
     * 
     * @param s String to convert to parse as BigFraction
     * @see #valueOf(String)
     */
    public BigFraction(String s)
    {
        BigFraction bf = valueOf(s);
        mNumerator = bf.mNumerator;
        mDenominator = bf.mDenominator;
    }
  
    /**
     * <strong>Note:</strong> {@link #valueOf(String, int)} should be preferred for performance reasons.
     * This constructor is provided for convenience.
     * 
     * @param s String to convert to parse as BigFraction
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @see #valueOf(String, int)
     */
    public BigFraction(String s, int radix)
    {
        BigFraction bf = valueOf(s, radix);
        mNumerator = bf.mNumerator;
        mDenominator = bf.mDenominator;
    }
  
    /**
     * Constructs a BigFraction from given number. If the number is not one of the
     * known implementations of Number class, then {@link Number#doubleValue()}
     * will be used for construction.<br>
     * <br>
     * Warning: when using floating point numbers, round-off error can result
     * in answers that are unexpected. For example:<br> 
     * {@code     System.out.println(BigFraction.valueOf(1.1))}<br>
     * will print:<br>
     * {@code     2476979795053773/2251799813685248}<br>
     * <br>
     * This is because 1.1 cannot be expressed exactly in binary form. The
     * computed fraction is exactly equal to the internal representation of
     * the double-precision floating-point number. (Which, for {@code 1.1}, is:
     * {@code (-1)^0 * 2^0 * (1 + 0x199999999999aL / 0x10000000000000L)}.)<br>
     * <br>
     * In many cases, {@code BigFraction.valueOf(Double.toString(d))} may give the result
     * the user expects.
     * 
     * @param n Any Number to be converted to a BigFraction
     * @return a fully reduced fraction equivalent to {@code n}. Guaranteed to be non-null.
     * 
     * @throws IllegalArgumentException if n is null.
     */
    public static BigFraction valueOf(Number n)
    {
        if(n == null)
            throw new IllegalArgumentException("Null parameter.");
    
        if(n instanceof BigFraction)
            return (BigFraction)n;
        else if(isInt(n))
            return new BigFraction(toBigInteger(n), BigInteger.ONE, Reduced.YES);
        else if(n instanceof BigDecimal)
            return valueOfHelper((BigDecimal)n);
        else
            throw new UnsupportedOperationException();
    }
  
    /**
     * Constructs a BigFraction with given numerator and denominator. Fraction
     * will be reduced to lowest terms. If fraction is negative, negative sign will
     * be carried on numerator, regardless of how the values were passed in. The numerator
     * and denominator can both be non-integers.<br>
     * <br>
     * Example: {@code BigFraction.valueOf(8.5, -6.25); //-34/25}<br>
     * <br>
     * Warning: when using floating point numbers, round-off error can result
     * in answers that are unexpected. For example,<br>
     * {@code     System.out.println(BigFraction.valueOf(1.1))}<br>
     * <br>
     * This is because 1.1 cannot be expressed exactly in binary form. The
     * computed fraction is exactly equal to the internal representation of
     * the double-precision floating-point number. (Which, for {@code 1.1}, is:
     * {@code (-1)^0 * 2^0 * (1 + 0x199999999999aL / 0x10000000000000L)}.)<br>
     * <br>
     * In many cases, {@code BigFraction.valueOf(Double.toString(d))} may give the result
     * the user expects.
     * 
     * @param numerator any Number to be used as the numerator. This does not need to be an integer.
     * @param denominator any Number to be used as the denominator. This does not need to be an integer.
     * @return a fully reduced fraction equivalent to {@code numerator/denominator}. Guaranteed to be non-null.
     * 
     * @throws ArithmeticException if denominator == 0.
     * @throws IllegalArgumentException if numerator or denominator is null.
     */
    public static BigFraction valueOf(Number numerator, Number denominator) {
        return valueOf(numerator, denominator, true);
    }

    /**
     * Constructs a BigFraction with given numerator and denominator. Fraction
     * will be reduced to lowest terms. If fraction is negative, negative sign will
     * be carried on numerator, regardless of how the values were passed in. The numerator
     * and denominator can both be non-integers.<br>
     * <br>
     * Example: {@code BigFraction.valueOf(8.5, -6.25); //-34/25}<br>
     * <br>
     * Warning: when using floating point numbers, round-off error can result
     * in answers that are unexpected. For example,<br>
     * {@code     System.out.println(BigFraction.valueOf(1.1))}<br>
     * <br>
     * This is because 1.1 cannot be expressed exactly in binary form. The
     * computed fraction is exactly equal to the internal representation of
     * the double-precision floating-point number. (Which, for {@code 1.1}, is:
     * {@code (-1)^0 * 2^0 * (1 + 0x199999999999aL / 0x10000000000000L)}.)<br>
     * <br>
     * In many cases, {@code BigFraction.valueOf(Double.toString(d))} may give the result
     * the user expects.
     * 
     * @param numerator any Number to be used as the numerator. This does not need to be an integer.
     * @param denominator any Number to be used as the denominator. This does not need to be an integer.
     * @param reduce true if the fraction should be reduced to simplest form, else false.
     * @return a fully reduced fraction equivalent to {@code numerator/denominator}. Guaranteed to be non-null.
     * 
     * @throws ArithmeticException if denominator == 0.
     * @throws IllegalArgumentException if numerator or denominator is null.
     */
    public static BigFraction valueOf(Number numerator, Number denominator, boolean reduce)
    {
        if(numerator == null)
            throw new IllegalArgumentException("Numerator is null.");
    
        if(denominator == null)
            throw new IllegalArgumentException("Denominator is null.");
    
        if(isInt(numerator) && isInt(denominator))
            return new BigFraction(toBigInteger(numerator), toBigInteger(denominator), Reduced.NO, reduce);
        else if(numerator instanceof BigDecimal && denominator instanceof BigDecimal)
            return valueOfHelper((BigDecimal)numerator, (BigDecimal)denominator);
    
        //else: convert numerator and denominator to fractions, and divide
        //(n1/d1)/(n2/d2) = (n1*d2)/(d1*n2)
        BigFraction f1 = valueOf(numerator);
        BigFraction f2 = valueOf(denominator);
        return new BigFraction(f1.mNumerator.multiply(f2.mDenominator),
                               f1.mDenominator.multiply(f2.mNumerator),
                               Reduced.NO,
                               reduce);
    }
  
    /**
     * Constructs a BigFraction from a String. Expected format is {@code numerator/denominator},
     * but "{@code /denominator}" part is optional. Either numerator or denominator may be a floating-point
     * decimal number, which is in the same format as a parameter to the
     * {@link BigDecimal#BigDecimal(String)} constructor.<br>
     * <br>
     * Numerator or denominator can also be expressed as a repeating decimal, such as 0.(1) = 0.1111...
     * Scientific notation is not allowed when using repeating digits.<br>
     * <br>
     * Examples:<br>
     * {@code BigFraction.valueOf("11"); //11/1}<br>
     * {@code BigFraction.valueOf("22/34"); //11/17}<br>
     * {@code BigFraction.valueOf("2e4/-0.64"); //-174375/4}<br>
     * {@code BigFraction.valueOf("0.(1)"); //1/9}<br>
     * {@code BigFraction.valueOf("12.34(56)"); //122222/9900}<br>
     * 
     * @param s a string representation of a number or fraction
     * @return a fully reduced fraction equivalent to the specified string. Guaranteed to be non-null.
     * 
     * @throws NumberFormatException  if the string cannot be properly parsed.
     * @throws ArithmeticException if denominator == 0.
     * @throws IllegalArgumentException if s is null.
     * 
     * @see BigDecimal#BigDecimal(String)
     */
    public static BigFraction valueOf(String s)
    {
        return valueOf(s, 10);
    }

    /**
     * Constructs a BigFraction from a String. Expected format is {@code numerator/denominator},
     * but "{@code /denominator}" part is optional.<br>
     * <br>
     * If {@code radix == 10}: either numerator or denominator may be a floating-point
     * decimal number, which is in the same format as a parameter to the
     * {@link BigDecimal#BigDecimal(String)} constructor.<br>
     * <br>
     * If {@code radix != 10}: the numerator and denominator may be a radixed string
     * string in that base, but <b>cannot</b> contain a scientific notation exponent.
     * The numerator and denominator must be in a format that, with the radix point removed,
     * can be parsed by the {@link BigInteger#BigInteger(String, int)} constructor. This means
     * that scientific notation is <em>not</em> allowed in bases other than 10.<br>
     * <br>
     * Numerator or denominator can also be expressed as a radixed string with a repeating
     * digit, such as 0.(1) = 0.1111... This is allowed with any radix. Scientific notation
     * is not allowed when using repeating digits.<br>
     * <br>
     * Examples:<br>
     * {@code BigFraction.valueOf("11", 10); //11/1}<br>
     * {@code BigFraction.valueOf("22/34", 10); //11/17}<br>
     * {@code BigFraction.valueOf("2e4/-0.64", 10); //-174375/4}<br>
     * {@code BigFraction.valueOf("dead/beef", 16); //57005/48879}<br>
     * {@code BigFraction.valueOf("lazy.fox", 36); //15459161339/15552}<br>
     * {@code BigFraction.valueOf("0.(1)", 10); //1/9}<br>
     * {@code BigFraction.valueOf("12.34(56)", 10); //122222/9900}<br>
     * {@code BigFraction.valueOf("0.(1)", 16); //1/15}<br>
     * {@code BigFraction.valueOf("the.lazy(fox)", 36); //2994276908470787/78362484480}<br>
     *
     *
     * @param s a string representation of a number or fraction
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @return a fully reduced fraction equivalent to the specified string. Guaranteed to be non-null.
     *
     * @throws NumberFormatException  if the string cannot be properly parsed.
     * @throws ArithmeticException if denominator == 0.
     * @throws IllegalArgumentException if s is null.
     *
     * @see BigDecimal#BigDecimal(String)
     * @see BigInteger#BigInteger(String, int)
     */
    public static BigFraction valueOf(String s, int radix)
    {
        if(s == null)
            throw new IllegalArgumentException("Null argument.");

        if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        String num = null;
        String den = null;

        int slashPos = s.indexOf('/');
        if(slashPos < 0)
            {
                num = s;
            }
        else
            {
                num = s.substring(0, slashPos);
                den = s.substring(slashPos+1, s.length());
            }

        int parenPos = s.indexOf('(');
        if(radix == 10 && parenPos < 0)
            {
                //if radix is 10, and we don't have repeating digits, we piggy-back on BigDecimal
                if(den == null)
                    return valueOfHelper(new BigDecimal(num));
                else
                    return valueOfHelper(new BigDecimal(num), new BigDecimal(den));
            }
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Returns the numerator of this fraction.
     * @return numerator of this fraction.
     */
    public final BigInteger getNumerator()
    {
        return mNumerator;
    }
  
    /**
     * Returns the denominator of this fraction.
     * @return denominator of this fraction.
     */
    public final BigInteger getDenominator() {
        return mDenominator;
    }
  
    /**
     * Returns this + n.
     * @param n number to be added to this
     * @return this + n
     * @throws IllegalArgumentException if n is null.
     */
    public BigFraction add(Number n)
    {
        if(isZero(n))
            return this;
    
        if(n == null)
            throw new IllegalArgumentException("Null argument");
    
        if(isInt(n))
            {
                //n1/d1 + n2 = (n1 + d1*n2)/d1
                return new BigFraction(mNumerator.add(mDenominator.multiply(toBigInteger(n))),
                                       mDenominator, Reduced.YES);
            }
        else
            {
                BigFraction f = valueOf(n);
      
                //n1/d1 + n2/d2 = (n1*d2 + d1*n2)/(d1*d2)
                return new BigFraction(mNumerator.multiply(f.mDenominator).add(mDenominator.multiply(f.mNumerator)),
                                       mDenominator.multiply(f.mDenominator), Reduced.NO);
            }
    }
  
    /**
     * Returns a + b, represented as a BigFraction. Equivalent to {@code BigFraction.valueOf(a).add(b)}.
     * Provided as static method to make code easier to write in some instances.
     * 
     * @param a number to be added
     * @param b number to be added
     * @return a + b
     * @throws IllegalArgumentException if a or b is null.
     */
    public static BigFraction sum(Number a, Number b)
    {
        return valueOf(a).add(b);
    }
  
    /**
     * Returns this - n.
     * @param n number to be subtracted from this
     * @return this - n
     * @throws IllegalArgumentException if n is null.
     */
    public BigFraction subtract(Number n)
    {
        if(isZero(n))
            return this;
    
        if(n == null)
            throw new IllegalArgumentException("Null argument");
    
        if(isInt(n))
            {
                //n1/d1 - n2 = (n1 - d1*n2)/d1
                return new BigFraction(mNumerator.subtract(mDenominator.multiply(toBigInteger(n))),
                                       mDenominator, Reduced.YES);
            }
        else
            {
                BigFraction f = valueOf(n);
      
                //n1/d1 - n2/d2 = (n1*d2 - d1*n2)/(d1*d2)
                return new BigFraction(mNumerator.multiply(f.mDenominator)
                                                 .subtract(mDenominator.multiply(f.mNumerator)),
                                       mDenominator.multiply(f.mDenominator), Reduced.NO);
            }
    }
  
    /**
     * Returns n - this. Sometimes this results in cleaner code than
     * rearranging the code to use subtract().
     * 
     * @param n number to subtract this from
     * @return n - this
     * @throws IllegalArgumentException if n is null.
     */
    public BigFraction subtractFrom(Number n)
    {
        if(isZero(n))
            return this.negate();
    
        if(n == null)
            throw new IllegalArgumentException("Null argument");
    
        if(isInt(n))
            {
                //n1 - n2/d2 = (d2*n1 - n2)/d2
                return new BigFraction(mDenominator.multiply(toBigInteger(n)).subtract(mNumerator),
                                       mDenominator, Reduced.YES);
            }
        else
            {
                BigFraction f = valueOf(n);
      
                //n1/d1 - n2/d2 = (n1*d2 - d1*n2)/(d1*d2)
                return new BigFraction(f.mNumerator.multiply(mDenominator).subtract(f.mDenominator.multiply(mNumerator)),
                                       f.mDenominator.multiply(mDenominator), Reduced.NO);
            }
    }
  
    /**
     * Returns a - b, represented as a BigFraction. Equivalent to {@code BigFraction.valueOf(a).subtract(b)}.
     * Provided as static method to make code easier to write in some instances.
     * 
     * @param a number to subtract from (minuend)
     * @param b number to be subtracted from a (subtrahend)
     * @return a - b
     * @throws IllegalArgumentException if a or b is null.
     */
    public static BigFraction difference(Number a, Number b)
    {
        return valueOf(a).subtract(b);
    }
  
    /**
     * Returns this * n.
     * @param n number to be multiplied by this
     * @return this * n
     * @throws IllegalArgumentException if n is null.
     */
    public BigFraction multiply(Number n)
    {
        if(isZero(n))
            return BigFraction.ZERO;
        if(isOne(n))
            return this;
    
        BigFraction f = valueOf(n);
    
        //(n1/d1)*(n2/d2) = (n1*n2)/(d1*d2)
        return new BigFraction(mNumerator.multiply(f.mNumerator), mDenominator.multiply(f.mDenominator), Reduced.NO);
    }
  
    /**
     * Returns a * b, represented as a BigFraction. Equivalent to {@code BigFraction.valueOf(a).multiply(b)}.
     * Provided as static method to make code easier to write in some instances.
     * 
     * @param a number to be multiplied
     * @param b number to be multiplied
     * @return a * b
     * @throws IllegalArgumentException if a or b is null.
     */
    public static BigFraction product(Number a, Number b)
    {
        return valueOf(a).multiply(b);
    }
  
    /**
     * Returns this / n.
     * 
     * @param n number to divide this by (divisor)
     * @return this / n
     * @throws IllegalArgumentException if n is null.
     * @throws ArithmeticException if n == 0.
     */
    public BigFraction divide(Number n)
    {
        if(isOne(n))
            return this;
    
        //division is the same thing as constructing new fraction
        return valueOf(this, n);
    }
  
    /**
     * Returns n / this. Sometimes this results in cleaner code than
     * rearranging the code to use divide().
     * 
     * @param n number to be divided by this (dividend)
     * @return n / this
     * @throws IllegalArgumentException if n is null.
     * @throws ArithmeticException if this == 0.
     */
    public BigFraction divideInto(Number n)
    {
        if(isOne(n))
            return this.reciprocal();
        if(isZero(n) && !isZero(this))
            return BigFraction.ZERO;
    
        //division is the same thing as constructing new fraction
        return valueOf(n, this);
    }
  
    /**
     * Returns a / b, represented as a BigFraction. Equivalent to {@code BigFraction.valueOf(a).divide(b)}.
     * Also equivalent to {@code BigFraction.valueOf(a, b)}.
     * Provided as static method to make code easier to write in some instances.
     * 
     * @param a number to be divided (dividend)
     * @param b number by which to divide (divisor)
     * @return a / b
     * @throws IllegalArgumentException if a or b is null.
     * @throws ArithmeticException if b == 0.
     */
    public static BigFraction quotient(Number a, Number b)
    {
        //Note: a/b is the same thing as constructing a new fraction from a and b.
        return valueOf(a, b);
    }
  
    /**
     * Returns the greatest common divisor (also called greatest common factor) of
     * {@code this} and {@code n}.<br>
     * <br>
     * If {@code this} and {@code n} are both zero, returns {@code 0/1}.<br>
     * <br>
     * Note: The result will always be nonnegative, regardless of the signs of the inputs.<br>
     * <br>
     * When dealing with fractions, the divisors of a/b are: (a/b)/1, (a/b)/2, (a/b)/3, ... (a/b)/n.
     * Thus gcd(n1/d1, n2/d2) gives the largest fraction n3/d3, such that (n1/d1)/(n3/d3) is an integer,
     * and (n2/d2)/(n3/d3) is an integer.
     * 
     * @param n other value to compute gcd from.
     * @return greatest common divisor of {@code this} and {@code n}.
     */
    public BigFraction gcd(Number n) {
        BigFraction f = valueOf(n);
    
        if(isZero(this))
            return f.abs();
        if(isZero(f))
            return this.abs();
    
        //gcd((a/b),(c/d)) = gcd(a,c) / lcm(b,d)
        //                 = gcd(a,c) / (|b*d|/gcd(b,d))
        //Note: this result is guaranteed to be a reduced fraction.
        //If you try to further simplify this to: (gcd(a,c) * gcd(b,d)) / (|b*d|), then the
        //result will not be reduced, and the operation actually takes about 60% longer.
        BigInteger num = mNumerator.gcd(f.mNumerator);
        BigInteger den = mDenominator.multiply(f.mDenominator).abs().divide(mDenominator.gcd(f.mDenominator));
    
        return new BigFraction(num, den, Reduced.YES);
    }
  
    /**
     * Returns least common multiple of {@code this} and {@code n}.<br>
     * <br>
     * If {@code this} or {@code n} is zero, returns {@code 0/1}.<br>
     * <br>
     * Note: The result will always be nonnegative, regardless of the signs of the inputs.<br>
     * <br>
     * When dealing with fractions, the multiples of a/b are: (a/b)*1, (a/b)*2, (a/b)*3, ... (a/b)*n.
     * Thus lcm(n1/d1, n2/d2) gives the smallest fraction n3/d3, such that (n3/d3)/(n1/d1) is an integer,
     * and (n3/d3)/(n2/d2) is an integer.
     * 
     * @param n other value to compute lcm from.
     * @return least common multiple of {@code this} and {@code n}
     */
    public BigFraction lcm(Number n) {
        BigFraction f = valueOf(n);
    
        if(isZero(this) || isZero(f))
            return BigFraction.ZERO;
    
        //lcm((a/b),(c/d)) = lcm(a,c) / gcd(b,d)
        //                 = (|a*c| / gcd(a,c)) / gcd(b,d)
        //Note: this result is guaranteed to be a reduced fraction.
        //If you try to further simplify this to: |a*c| / (gcd(a,c) * gcd(b,d)), then the
        //result will not be reduced, and the operation actually takes about 60% longer.
        BigInteger num = mNumerator.multiply(f.mNumerator).abs().divide(mNumerator.gcd(f.mNumerator));
        BigInteger den = mDenominator.gcd(f.mDenominator);
    
        return new BigFraction(num, den, Reduced.YES);
    }
  
    /**
     * Returns this^exponent.<br>
     * <br>
     * Note: 0^0 will return 1/1. This is consistent with {@link Math#pow(double, double)},
     * {@link BigInteger#pow(int)}, and {@link BigDecimal#pow(int)}.
     * 
     * @param exponent power to raise this fraction to.
     * @return this^exponent
     * 
     * @throws ArithmeticException if {@code this == 0 && exponent < 0}.
     */
    public BigFraction pow(int exponent)
    {
        if(exponent < 0 && isZero(this))
            throw new ArithmeticException("Divide by zero: raising zero to negative exponent.");
    
        if(exponent == 0)
            return BigFraction.ONE;
        else if (exponent == 1)
            return this;
        else if (exponent > 0)
            return new BigFraction(mNumerator.pow(exponent), mDenominator.pow(exponent), Reduced.YES);
        else
            return new BigFraction(mDenominator.pow(-exponent), mNumerator.pow(-exponent), Reduced.YES);
    }
  
    /**
     * Returns 1/this.
     * 
     * @return 1/this
     * 
     * @throws ArithmeticException if this == 0.
     */
    public BigFraction reciprocal()
    {
        if(isZero(this))
            throw new ArithmeticException("Divide by zero: reciprocal of zero.");
    
        return new BigFraction(mDenominator, mNumerator, Reduced.YES);
    }
  
    /**
     * Returns the complement of this fraction, which is equal to 1 - this.
     * Useful for probabilities/statistics.
     * 
     * @return 1-this
     */
    public BigFraction complement()
    {
        //1 - n/d == d/d - n/d == (d-n)/d
        return new BigFraction(mDenominator.subtract(mNumerator), mDenominator, Reduced.YES);
    }
  
    /**
     * Returns -this. If this is zero, returns zero.
     * @return equivalent of {@code this.multiply(-1)}
     */
    public BigFraction negate()
    {
        return withSign(-mNumerator.signum());
    }
  
    /**
     * Returns the absolute value of this.
     * @return absolute value of this.
     */
    public BigFraction abs()
    {
        return withSign(1);
    }
  
    /**
     * Returns this, with sign set to the sign of {@code sgn} parameter.
     * Another way of saying it: returns the equivalent of {@code this.abs().multiply(Math.signum(sgn))}.<br>
     * <br>
     * Important Note: If this is zero, always returns zero. No exception thrown, even if we are trying
     * to set the sign of 0 to positive or negative.
     * 
     * @param sgn an integer less than, equal to, or greater than 0, whose sign will be assigned to the returned fraction.
     * @return equivalent of {@code this.abs().multiply(Math.signum(sgn))}.
     */
    public BigFraction withSign(int sgn)
    {
        if(sgn == 0 || isZero(this))
            return BigFraction.ZERO;
    
        int thisSignum = mNumerator.signum();
        if((thisSignum < 0 && sgn > 0) || (thisSignum > 0 && sgn < 0))
            return new BigFraction(mNumerator.negate(), mDenominator, Reduced.YES);
    
        return this;
    }
  
    /**
     * Returns -1, 0, or 1, representing the sign of this fraction.
     * @return -1, 0, or 1, representing the sign of this fraction.
     */
    public int signum()
    {
        return mNumerator.signum();
    }

    /**
     * Returns this rounded to the nearest whole number, using
     * RoundingMode.HALF_UP as the default rounding mode.
     * 
     * @return this fraction rounded to nearest whole number, using RoundingMode.HALF_UP.
     */
    public BigInteger round()
    {
        return round(RoundingMode.HALF_UP);
    }
  
    /**
     * Returns this fraction rounded to a whole number, using
     * the given rounding mode.
     * 
     * @param roundingMode rounding mode to use
     * @return this fraction rounded to a whole number, using the given rounding mode.
     * 
     * @throws ArithmeticException if RoundingMode.UNNECESSARY is used but
     *         this fraction does not exactly represent an integer.
     */
    public BigInteger round(RoundingMode roundingMode)
    {
        if(roundingMode == null)
            throw new IllegalArgumentException("Null argument");
    
        //Since fraction is always in lowest terms, this is an exact integer
        //iff the denominator is 1.
        if(mDenominator.equals(BigInteger.ONE))
            return mNumerator;
    
        //If the denominator was not 1, rounding will be required.
        if(roundingMode == RoundingMode.UNNECESSARY)
            throw new ArithmeticException("Rounding necessary");
    
        final Set<RoundingMode> ROUND_HALF_MODES = EnumSet.of(RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN);
    
        BigInteger intVal = null;
        BigInteger remainder = null;
    
        //Note:  The remainder is only needed if we are using HALF_X rounding mode, and the
        //       remainder is not one-half. Since computing the remainder can be a bit
        //       expensive, only compute it if necessary.
        if(ROUND_HALF_MODES.contains(roundingMode) && !mDenominator.equals(BIGINT_TWO))
            {
                BigInteger[] divMod = mNumerator.divideAndRemainder(mDenominator);
                intVal = divMod[0];
                remainder = divMod[1];
            }
        else
            {
                intVal = mNumerator.divide(mDenominator);
            }
    
        //For HALF_X rounding modes, convert to either UP or DOWN.
        if(ROUND_HALF_MODES.contains(roundingMode))
            {
                //Since fraction is always in lowest terms, the remainder is exactly
                //one-half iff the denominator is 2.
                if(mDenominator.equals(BIGINT_TWO))
                    {
                        if(roundingMode == RoundingMode.HALF_UP || (roundingMode == RoundingMode.HALF_EVEN && intVal.testBit(0)))
                            {
                                roundingMode = RoundingMode.UP;
                            }
                        else
                            {
                                roundingMode = RoundingMode.DOWN;
                            }
                    }
                else if (remainder.abs().compareTo(mDenominator.shiftRight(1)) <= 0)
                    {
                        //note:  x.shiftRight(1) === x.divide(2)
                        roundingMode = RoundingMode.DOWN;
                    }
                else
                    {
                        roundingMode = RoundingMode.UP;
                    }
            }
    
        //For ceiling and floor, convert to up or down (based on sign).
        if(roundingMode == RoundingMode.CEILING || roundingMode == RoundingMode.FLOOR)
            {
                //Use mNumerator.signum() instead of intVal.signum() to get correct answers
                //for values between -1 and 0.
                if(mNumerator.signum() > 0)
                    {
                        if(roundingMode == RoundingMode.CEILING)
                            roundingMode = RoundingMode.UP;
                        else
                            roundingMode = RoundingMode.DOWN;
                    }
                else
                    {
                        if(roundingMode == RoundingMode.CEILING)
                            roundingMode = RoundingMode.DOWN;
                        else
                            roundingMode = RoundingMode.UP;
                    }
            }
    
        //Sanity check... at this point all possible values should be turned to up or down.
        if(roundingMode != RoundingMode.UP && roundingMode != RoundingMode.DOWN)
            throw new IllegalArgumentException("Unsupported rounding mode: " + roundingMode.toString());
    
        if(roundingMode == RoundingMode.UP)
            {
                if (mNumerator.signum() > 0)
                    intVal = intVal.add(BigInteger.ONE);
                else
                    intVal = intVal.subtract(BigInteger.ONE);
            }
    
        return intVal;
    }
  
    /**
     * Rounds this fraction to the nearest multiple of the given number, using HALF_UP
     * rounding method.
     * 
     * @param n number to which we will round to the nearest multiple
     * 
     * @return this value, rounded to the nearest multiple of n
     * 
     * @throws IllegalArgumentException If n is null.
     * @throws ArithmeticException If n is zero or negative.
     */
    public BigFraction roundToNumber(Number n) {
        return roundToNumber(n, RoundingMode.HALF_UP);
    }
  
    /**
     * Rounds this fraction to the nearest multiple of the given number, using the
     * specified rounding method.<br>
     * <br>
     * Note for HALF_EVEN rounding method: this rounds to the nearest even multiple of
     * n, which may or may not be even. For example, if rounding to the nearest 2, every
     * result will be even. So 9 rounded to nearest 2 with HALF_EVEN will round to 8, since
     * 8=2*4 (4 being an even number), whereas 10=2*5 (5 being odd).
     * 
     * @param n number to which we will round to the nearest multiple
     * @param roundingMode rounding mode to use if the answer must be rounded
     * 
     * @return this value, rounded to the nearest multiple of n
     * 
     * @throws IllegalArgumentException If n is null.
     * @throws ArithmeticException If n is zero or negative.
     * @throws ArithmeticException if RoundingMode.UNNECESSARY is used but
     *         this fraction is not an exact multiple of the given value.
     */
    public BigFraction roundToNumber(Number n, RoundingMode roundingMode) {
        if(n == null || roundingMode == null)
            throw new IllegalArgumentException("Null argument");
    
        BigFraction f = valueOf(n);
    
        if(f.signum() <= 0)
            throw new ArithmeticException("newDenominator must be positive");
    
        return product(this.divide(f).round(roundingMode), f);
    }
  
    /**
     * Rounds the given fraction to the nearest fraction having the given denominator,
     * using HALF_UP rounding method, and returns the numerator of that fraction.
     * 
     * @param newDenominator denominator of fraction to round to.
     * 
     * @return numerator of rounded fraction (unreduced)
     * 
     * @throws IllegalArgumentException If newDenominator is null.
     * @throws ArithmeticException If newDenominator is zero or negative.
     * 
     * @see #roundToDenominator(BigInteger, RoundingMode)
     */
    public BigInteger roundToDenominator(BigInteger newDenominator)
    {
        return this.roundToDenominator(newDenominator, RoundingMode.HALF_UP);
    }
  
    /**
     * Rounds the given fraction to the nearest fraction having the given denominator,
     * using the given rounding method, and returns the numerator of that fraction.<br>
     * <br>
     * For example, given the fraction 7/15, if you wanted to know the nearest fraction
     * with denominator 6, it would be 2.8/6, which rounds to 3/6. This function would
     * return 3.<br>
     * <br>
     * Note: this is not reduced--3/6 is equivalent to 1/2, but this
     * function would still return 3. If newDenominator is 1, this method is equivalent
     * to round(). If this object is negative, the returned numerator will also be
     * negative.
     * 
     * @param newDenominator denominator of fraction to round to.
     * @param roundingMode rounding mode to use if the answer must be rounded.
     * 
     * @return numerator of rounded fraction (unreduced)
     * 
     * @throws IllegalArgumentException If newDenominator is null.
     * @throws ArithmeticException If newDenominator is zero or negative.
     * @throws ArithmeticException if RoundingMode.UNNECESSARY is used but
     *         this fraction cannot be represented exactly as a fraction with the
     *         given denominator.
     */
    public BigInteger roundToDenominator(BigInteger newDenominator, RoundingMode roundingMode)
    {
        if(newDenominator == null || roundingMode == null)
            throw new IllegalArgumentException("Null argument");
    
        if(newDenominator.compareTo(BigInteger.ZERO) <= 0)
            throw new ArithmeticException("newDenominator must be positive");
    
        //n1/d1 = x/d2  =>   x = (n1/d1)*d2
        return this.multiply(newDenominator).round(roundingMode);
    }
  
    /**
     * Returns a string representation of this, in the form
     * numerator/denominator. The denominator will
     * always be included, even if it is 1.
     * 
     * @return This fraction, represented as a string in the format {@code numerator/denominator}.
     */
    @Override
    public String toString()
    {
        return toString(10, false);
    }
  
    /**
     * Returns string representation of this, in the form of numerator/denominator, with numerator
     * and denominator represented in the given radix. The digit-to-character mapping provided by
     * {@link Character#forDigit} is used.
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @return This fraction, represented as a string in the format {@code numerator/denominator}.
     */
    public String toString(int radix)
    {
        return toString(radix, false);
    }
  
    /**
     * Returns a string representation of this, in the form of
     * numerator/denominator. Optionally, "/denominator" part
     * can be ommitted for whole numbers.
     * 
     * @param denominatorOptional If true, the denominator will be ommitted
     *        when it is unnecessary. For example, "7" instead of "7/1".
     * @return This fraction, represented as a string in the format {@code numerator/denominator}.
     */
    public String toString(boolean denominatorOptional)
    {
        return toString(10, denominatorOptional);
    }
  
    /**
     * Returns string representation of this, in the form of numerator/denominator, with numerator
     * and denominator represented in the given radix. The digit-to-character mapping provided by
     * {@link Character#forDigit} is used.<br>
     * <br>
     * Optionally, "/denominator" part can be ommitted for whole numbers.
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @param denominatorOptional If true, the denominator will be ommitted
     *        when it is unnecessary. For example, "7" instead of "7/1".
     * @return This fraction, represented as a string in the format {@code numerator/denominator}.
     */
    public String toString(int radix, boolean denominatorOptional)
    {
        if(denominatorOptional && mDenominator.equals(BigInteger.ONE))
            return mNumerator.toString(radix);
        return mNumerator.toString(radix) + "/" + mDenominator.toString(radix);
    }
  
    /**
     * Returns string representation of this object as a mixed fraction.
     * For example, 4/3 would be "1 1/3". For negative fractions, the
     * sign is carried only by the whole number and assumed to be distributed
     * across the whole value. For example, -4/3 would be "-1 1/3". For
     * fractions that are equal to whole numbers, only the whole number will
     * be displayed. For fractions which have absolute value less than 1,
     * this will be equivalent to {@link #toString}.
     * 
     * @return String representation of this fraction as a mixed fraction.
     */
    public String toMixedString()
    {
        return toMixedString(10);
    }
  
    /**
     * Returns string representation of this object as a mixed fraction.
     * For example, 4/3 would be "1 1/3". For negative fractions, the
     * sign is carried only by the whole number and assumed to be distributed
     * across the whole value. For example, -4/3 would be "-1 1/3". For
     * fractions that are equal to whole numbers, only the whole number will
     * be displayed. For fractions which have absolute value less than 1,
     * this will be equivalent to {@link #toString(int radix)}.<br>
     * <br>
     * The numbers are represented in the given radix. The digit-to-character mapping provided by
     * {@link Character#forDigit} is used.
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @return String representation of this fraction as a mixed fraction.
     */
    public String toMixedString(int radix)
    {
        if(mDenominator.equals(BigInteger.ONE))
            return mNumerator.toString();
    
        if(mNumerator.abs().compareTo(mDenominator) < 0)
            return toString(radix);
    
        BigInteger[] divmod = mNumerator.divideAndRemainder(mDenominator);
    
        return divmod[0].toString(radix) + " " + divmod[1].abs().toString(radix) + "/" + mDenominator.toString(radix);
    }
  
  
    /**
     * Returns decimal string representation of the fraction with the given number
     * of decimal digits using roundingMode ROUND_HALF_UP.
     * 
     * @param numDecimalDigits number of digits to be displayed after the decimal
     * @return decimal string representation of this fraction.
     * 
     * @throws ArithmeticException if roundingMode is UNNECESSARY but rounding is required.
     */
    public String toDecimalString(int numDecimalDigits)
    {
        return toRadixedString(10, numDecimalDigits, RoundingMode.HALF_UP);
    }
  
    /**
     * Converts the fraction to a string with the given number of decimal digits.
     * For example, if f is 1/3, f.toDecimalString(1): 0.3; f.toDecimalString(4): 0.3333.
     * If numDecimalDigits is 0, this method is equivalent to round().toString().
     * Will append trailing 0s as needed: (1/2).toDecimalString(3) is 0.500.
     * 
     * @param numDecimalDigits number of digits to be displayed after the decimal
     * @param roundingMode how to round the number if necessary
     * @return decimal string representation of this fraction.
     * 
     * @throws ArithmeticException if roundingMode is UNNECESSARY but rounding is required.
     */
    public String toDecimalString(int numDecimalDigits, RoundingMode roundingMode)
    {
        return toRadixedString(10, numDecimalDigits, roundingMode);
    }
  
    /**
     * Converts the fraction to a radixed string with the given number of fraction digits
     * after the radix point. Rounds using HALF_UP rounding mode. The digit-to-character mapping provided by
     * {@link Character#forDigit} is used.
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @param numFractionalDigits number of digits to be displayed after the radix point.
     * @return radixed string representation of this fraction.
     */
    public String toRadixedString(int radix, int numFractionalDigits)
    {
        return toRadixedString(radix, numFractionalDigits, RoundingMode.HALF_UP);
    }
  
  
    /**
     * Converts the fraction to a radixed string with the given number of fraction digits
     * after the radix point.<br>
     * <br>
     * For example, 1/8 in base 10 is 0.125. In base 2 it is 0.001.<br>
     * <br>
     * Will append trailing 0s as needed: (1/2).toRadixedString(3,2) is 0.100.<br>
     * <br>
     * If passed negative numFractionalDigits, rounds to nearest radix^(-numFractionalDigits). For example,
     * -1 means round to nearest 10, -2 means round to nearest 100, etc. No extra zeros are prepended in this
     * case, since the only time it would be necessary is if a value were rounded to zero.<br>
     * <br>
     * The digit-to-character mapping provided by {@link Character#forDigit} is used.
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @param numFractionalDigits number of digits to be displayed after the decimal
     * @param roundingMode how to round the number if necessary
     * @return radixed string representation of this fraction.
     * 
     * @throws ArithmeticException if roundingMode is UNNECESSARY but rounding is required.
     */
    public String toRadixedString(int radix, int numFractionalDigits, RoundingMode roundingMode)
    {
        if(roundingMode == null)
            throw new IllegalArgumentException("Null argument");
    
        if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
    
        //shortcut - if we don't want any fractional digits, this is equivalent to round()
        if(numFractionalDigits == 0)
            return this.round(roundingMode).toString(radix);
    
        if(numFractionalDigits > 0)
            {
                //multiply by (radix)^(digits), then round to integer
                BigInteger rounded = this.multiply(BigInteger.valueOf(radix).pow(numFractionalDigits)).round(roundingMode);
      
                //get the actual digits (ignoring the sign bit)
                String digits = rounded.abs().toString(radix);
      
                String beforeRadixPoint = "0";
                String afterRadixPoint = digits;
                int padLen = 0; //number of zeros we need to pad afterDecimal with with
      
                if(digits.length() > numFractionalDigits)
                    {
                        //we got too many digits... need to split into before/after decimal parts
                        beforeRadixPoint = digits.substring(0, digits.length() - numFractionalDigits);
                        afterRadixPoint = digits.substring(digits.length() - numFractionalDigits);
                    }
                else if (digits.length() < numFractionalDigits)
                    {
                        //we don't have enough digits. We will have to pad with zeros
                        padLen = numFractionalDigits - digits.length();
                    }
                //else: we got exactly the right number of digits. nothing to do!
      
                //create string builder to hold result. init buffer to max possible size: length of parts plus length of padding plus space for . and -
                StringBuilder sb = new StringBuilder(beforeRadixPoint.length() + afterRadixPoint.length() + padLen + 2);
      
                //Note: need to use sign of rounded, not sign of this, because if we round a small negative number to
                //zero the sign will be lost.
                if(rounded.signum() < 0)
                    sb.append('-');
      
                sb.append(beforeRadixPoint).append('.');
      
                for(int i = 0; i < padLen; i++)
                    sb.append('0');
      
                sb.append(afterRadixPoint);
      
                return sb.toString();
            }
        else
            {
                //numFractionalDigits is negative. divide out the number of digits then round to integer
                int absFractionalDigits = -numFractionalDigits;
      
                String rounded = this.divide(BigInteger.valueOf(radix).pow(absFractionalDigits)).round(roundingMode).toString(radix);
      
                //at this point, if we got 0, just return 0. No need to return something like "00000". if we have anything
                //other than 0, then we need to append as many 0s as abs(numFractionalDigits)
                if(rounded.equals("0"))
                    return "0";
      
                StringBuilder sb = new StringBuilder(rounded.length() + absFractionalDigits);
                sb.append(rounded);
                for(int i = 0; i < absFractionalDigits; i++)
                    sb.append('0');
      
                return sb.toString();
            }
    }
  
    /**
     * Converts the fraction to a radixed string with repeating digits. The
     * repeating digits are indicated by parenthesis: 1/9 becomes 0.(1)<br>
     * <br>
     * Equivalent to {@code toRepeatingString(10, false)}
     * 
     * @return radixed string representation of this fraction with repeating digits denoted in parenthesis.
     * 
     * @see #toRepeatingDigitString(int, boolean)
     */
    public String toRepeatingDigitString() {
        return toRepeatingDigitString(10, false);
    }
  
    /**
     * Converts the fraction to a radixed string with repeating digits. The
     * repeating digits are indicated by parenthesis: 1/9 becomes 0.(1)<br>
     * <br>
     * Equivalent to {@code toRepeatingString(10, forceRepeating)}
     * 
     * @param forceRepeating whether or not to force this function to always use a repeating fraction,
     *                       even if the radixed string terminates
     * @return radixed string representation of this fraction with repeating digits denoted in parenthesis.
     * 
     * @see #toRepeatingDigitString(int, boolean)
     */
    public String toRepeatingDigitString(boolean forceRepeating) {
        return toRepeatingDigitString(10, forceRepeating);
    }
  
    /**
     * Converts the fraction to a radixed string with repeating digits, in the given radix. The
     * repeating digits are indicated by parenthesis: 1/9 becomes 0.(1)<br>
     * <br>
     * Equivalent to {@code toRepeatingString(radix, false)}
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @return radixed string representation of this fraction with repeating digits denoted in parenthesis.
     * 
     * @see #toRepeatingDigitString(int, boolean)
     */
    public String toRepeatingDigitString(int radix) {
        return toRepeatingDigitString(radix, false);
    }
  
    /**
     * Converts the fraction to a radixed string with repeating digits, in the given radix. The
     * repeating digits are indicated by parenthesis: 1/9 becomes 0.(1)<br>
     * <br>
     * All rational fractions can be represented as a radixed string with repeating digits, but
     * some fractions can also be represented as a radixed string that terminates. In these cases,
     * the {@code forceRepeating} parameter can be used to force this function to return the
     * repeating fraction. For example, the fraction {@code 1/10} could be represented as terminating
     * string {@code "0.1"} or as repeating string {@code "0.0(9)"}.<br>
     * <br>
     * There is one special case for the value of 0. If {@code forceRepeating==true}, the return
     * value will be {@code "0.(0)"}. For all other fractions, the repeating digits will never
     * be all zeros.<br>
     * <br>
     * The repeating digits will always follow the radix point. For example, {@code 500/11} is
     * represented as {@code "45.(45)"}.<br>
     * <br>
     * <strong>Warning</strong>: This method is quite slow, as it essentially implements long division.<br>
     * <br>
     * The digit-to-character mapping provided by {@link Character#forDigit} is used.<br>
     * <br>
     * Examples:<br>
     * {@code BigFraction.valueOf(1,9).toRepeatingDigitString(10, false): 0.(1)}<br>
     * {@code BigFraction.valueOf(1).toRepeatingDigitString(10, false): 1.0}<br>
     * {@code BigFraction.valueOf(1).toRepeatingDigitString(10, true): 0.(9)}<br>
     * {@code BigFraction.valueOf(1,100).toRepeatingDigitString(10, false): 0.01}<br>
     * {@code BigFraction.valueOf(1,100).toRepeatingDigitString(10, true): 0.00(9)}<br>
     * {@code BigFraction.valueOf(45,22).toRepeatingDigitString(10, false): 2.0(45)}<br>
     * {@code BigFraction.valueOf(500,11).toRepeatingDigitString(10, false): 45.(45)}<br>
     * 
     * @param radix radix of the String representation. If the radix is outside the range from
     *              {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive, it will default to 10
     *              (as is the case for Integer.toString)
     * @param forceRepeating whether or not to force this function to always use a repeating fraction,
     *                       even if the radixed string terminates
     * @return radixed string representation of this fraction with repeating digits denoted in parenthesis.
     */
    public String toRepeatingDigitString(int radix, boolean forceRepeating) {
        if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
    
        //special case for 0
        if(isZero(this))
            return (forceRepeating ? "0.(0)" : "0.0");
    
        BigInteger absNum = mNumerator.abs();
        String sign = (mNumerator.signum() < 0 ? "-" : "");
        char maxDigit = Character.forDigit(radix-1, radix);
    
        //whole numbers are also easy
        if(mDenominator.equals(BigInteger.ONE))
            {
                if(forceRepeating)
                    return sign + absNum.subtract(BigInteger.ONE).toString(radix) + ".(" + maxDigit + ")";
                else
                    return mNumerator.toString(radix) + ".0";
            }
    
        //not a whole number or zero... we're going to have to do long division
        //first start by dividing to a remainder
        String iPart = "0";
        BigInteger dividend = absNum;
        if(dividend.compareTo(mDenominator) > 0)
            {
                BigInteger[] divmod = absNum.divideAndRemainder(mDenominator);
                iPart = divmod[0].toString(radix);
                dividend = divmod[1];
            }
    
        BigInteger bigRadix = BigInteger.valueOf(radix);
        StringBuilder quotient = new StringBuilder(); //stores the digits we get in long division algorithm
    
        //Next loop does the actual long division. Take dividend, add a zero on the end, divide by denominator,
        //append the quotient digit, then update dividend to the remainder. Keep track of dividends that we
        //have seen before--when we get a dividend that we have seen before, then the digits are repeating.
        //The value in the hash map is the index where we saw that dividend--we'll need it to split the
        //quotient string between static and repeating digits.
        Map<BigInteger, Integer> prevDividends = new HashMap<BigInteger, Integer>();
        while(!dividend.equals(BigInteger.ZERO) && !prevDividends.containsKey(dividend))
            {
                prevDividends.put(dividend, quotient.length());
                dividend = dividend.multiply(bigRadix); //same as appending a "0" in this base
      
                BigInteger[] divmod = dividend.divideAndRemainder(mDenominator);
                quotient.append(Character.forDigit(divmod[0].intValueExact(), radix));
                dividend = divmod[1];
            }
    
        StringBuilder result = new StringBuilder().append(sign).append(iPart).append('.');
    
        //if dividend is 0, the digits terminated
        if(dividend.equals(BigInteger.ZERO))
            {
                //if we are not forcing a repeating string, this is simple
                if(!forceRepeating)
                    return result.append(quotient).toString();
      
                //to force terminating, convert the qoutient into a big integer, and subtract one from it, then use
                //the largest value in this radix as the repeating digit. i.e. 0.11 becomes 0.10(9)
                String adjustedQuotient = new BigInteger(quotient.toString(), radix).subtract(BigInteger.ONE).toString(radix);
      
                //we may need to pad with leading zeros - the adjusted quotient won't have them
                int padLen = quotient.length() - adjustedQuotient.length();
                for(int i = 0; i < padLen; i++)
                    result.append('0');
      
                return result.append(adjustedQuotient).append('(').append(maxDigit).append(')').toString();
            }
    
        //insert parens around the repeating part
        int numStaticDigits = prevDividends.get(dividend);
        quotient.insert(numStaticDigits, '(').append(')');
    
        return result.append(quotient).toString();
    }
  
    /**
     * Returns if this object is equal to another object. In order to maintain symmetry,
     * this will *only* return true if the other object is a BigFraction. For looser
     * comparison to other Number objects, use the equalsNumber(Number) method.
     * 
     * @param o Object to compare to this
     * @return true if {@code o instanceof BigFraction} and is equal to this.
     * 
     * @see #equalsNumber(Number)
     */
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
    
        if(!(o instanceof BigFraction))
            return false;
    
        BigFraction f = (BigFraction)o;
        return mNumerator.equals(f.mNumerator) && mDenominator.equals(f.mDenominator);
    }
  
    /**
     * Returns if this object is equal to another Number object. Equivalent
     * to: {@code this.equals(BigFraction.valueOf(n))}
     * 
     * @param n number to compare this to
     * @return true if this is equivalent to {@code valueof(n)}
     */
    public boolean equalsNumber(Number n)
    {
        if(n == null)
            return false;
        return equals(valueOf(n));
    }
  
    /**
     * Returns a hash code for this object.
     * @return hash code for this object.
     */
    @Override
    public int hashCode()
    {
        //using the method generated by Eclipse, but streamlined a bit..
        return (31 + mNumerator.hashCode())*31 + mDenominator.hashCode();
    }
  
    /**
     * Returns a negative, zero, or positive number, indicating if this object
     * is less than, equal to, or greater than n, respectively.
     * 
     * @param n number to compare this to
     * @return integer indicating how this compares to given number
     * @throws IllegalArgumentException if n is null
     */
    @Override
    public int compareTo(Number n)
    {
        BigFraction f = valueOf(n);
    
        //easy case: this and f have different signs
        if(signum() != f.signum())
            return signum() - f.signum();
    
        //next easy case: this and f have the same denominator
        if(mDenominator.equals(f.mDenominator))
            return mNumerator.compareTo(f.mNumerator);
    
        //not an easy case, so first make the denominators equal then compare the mNumerators
        return mNumerator.multiply(f.mDenominator).compareTo(mDenominator.multiply(f.mNumerator));
    }
  
    /**
     * Returns the smaller of this and n. If they have equal value, this is returned.
     * Worth noting: if n is smaller, the returned Number is n, <i>not</i> a BigFraction
     * representing n.
     * 
     * @param n number to compare to this
     * @return smaller of this and n
     * @throws IllegalArgumentException if n is null
     */
    public Number min(Number n)
    {
        return (this.compareTo(n) <= 0 ? this : n);
    }
  
    /**
     * Returns the smaller of a and b. If they have equal value, a is returned.
     * Worth noting: the returned Number is always one of the two arguments, not
     * necessarily a BigFraction.
     * 
     * @param a one number to compare
     * @param b another number to compare
     * @return smaller of a and b
     * @throws IllegalArgumentException if a or b is null
     */
    public static Number min(Number a, Number b)
    {
        return (valueOf(a).compareTo(b) <= 0 ? a : b);
    }
  
  
    /**
     * Returns the larger of this and n. If they have equal value, this is returned.
     * Worth noting: if n is larger, the returned Number is n, <i>not</i> a BigFraction
     * representing n.
     * 
     * @param n number to compare to this
     * @return larger of this and n
     * @throws IllegalArgumentException if n is null
     */
    public Number max(Number n)
    {
        return (this.compareTo(n) >= 0 ? this : n);
    }
  
    /**
     * Returns the larger of a and b. If they have equal value, a is returned.
     * Worth noting: the returned Number is always one of the two arguments, not
     * necessarily a BigFraction.
     * 
     * @param a one number to compare
     * @param b another number to compare
     * @return larger of a and b
     * @throws IllegalArgumentException if a or b is null
     */
    public static Number max(Number a, Number b)
    {
        return (valueOf(a).compareTo(b) >= 0 ? a : b);
    }
  
  
    /**
     * Returns the mediant of this and n. The mediant of a/b and c/d is
     * (a+c)/(b+d). It is guaranteed to be between a/b and c/d. Not to
     * be confused with the median!
     * 
     * @param n other number to use to compute mediant
     * @return mediant of this and n
     * @throws IllegalArgumentException if n is null
     */
    public BigFraction mediant(Number n)
    {
        BigFraction f = valueOf(n);
    
        //if the two fractions are equal, we can avoid the math
        if(this.equals(f))
            return this;
    
        return new BigFraction(mNumerator.add(f.mNumerator), mDenominator.add(f.mDenominator), Reduced.NO);
    }
  
    /**
     * Returns the mediant of a and b. Provided as static method for convenience.
     * 
     * @param a one number to use to compute mediant
     * @param b other number to use to compute mediant
     * @return mediant of a and b
     * @throws IllegalArgumentException if a or b is null
     * @see #mediant(Number)
     */
    public static BigFraction mediant(Number a, Number b)
    {
        return valueOf(a).mediant(b);
    }
  
    /**
     * Returns a BigDecimal representation of this fraction.<br>
     * <br>
     * If possible, the returned value will be exactly equal to the fraction. If not,
     * this is equivalent to {@code toBigDecimal(18)}, approximately the same precision
     * as a double-precision number.
     * 
     * @return This fraction represented as a BigDecimal (exactly, if possible).
     */
    public BigDecimal toBigDecimal()
    {
        //Implementation note:  A fraction can be represented exactly in base-10 iff its
        //denominator is of the form 2^a * 5^b, where a and b are nonnegative integers.
        //(In other words, if there are no prime factors of the denominator except for
        //2 and 5, or if the denominator is 1). So to determine if this denominator is
        //of this form, continually divide by 2 to get the number of 2's, and then
        //continually divide by 5 to get the number of 5's. Afterward, if the denominator
        //is 1 then there are no other prime factors.
    
        //Note: number of 2's is given by the number of trailing 0 bits in the number
        int twos = mDenominator.getLowestSetBit();
        BigInteger tmpDen = mDenominator.shiftRight(twos); // x / 2^n === x >> n
    
        int fives = 0;
        BigInteger[] divMod = null;
    
        //while(tmpDen % 5 == 0) { tmpDen /= 5; fives++; }
        while(BigInteger.ZERO.equals((divMod = tmpDen.divideAndRemainder(BIGINT_FIVE))[1]))
            {
                tmpDen = divMod[0];
                fives++;
            }
    
        if(tmpDen.equals(BigInteger.ONE))
            {
                //This fraction will terminate in base 10, so it can be represented exactly as
                //a BigDecimal. We would now like to make the fraction of the form
                //unscaled / 10^scale. We know that 2^x * 5^x = 10^x, and our denominator is
                //in the form 2^twos * 5^fives. So use max(twos, fives) as the scale, and
                //multiply the numerator and deminator by the appropriate number of 2's or 5's
                //such that the denominator is of the form 2^scale * 5^scale. (Of course, we
                //only have to actually multiply the numerator, since all we need for the
                //BigDecimal constructor is the scale.)
                BigInteger unscaled = mNumerator;
                int scale = Math.max(twos, fives);
      
                if(twos < fives)
                    unscaled = unscaled.shiftLeft(fives - twos); //x * 2^n === x << n
                else if (fives < twos)
                    unscaled = unscaled.multiply(BIGINT_FIVE.pow(twos - fives));
      
                return new BigDecimal(unscaled, scale);
            }
    
        //else: this number will repeat infinitely in base-10. I used to try to figure out an
        //appropriate precision based the bit length of the numerator and denominator, but that
        //was just an approximation and not very useful in most circumstances. Instead, it now
        //uses 18 digits of precision (comparable to a double-precision number).
        return toBigDecimal(18);
    }
  
    /**
     * Returns a BigDecimal representation of this fraction, with a given precision.
     * @param precision  the number of significant figures to be used in the result.
     * @return BigDecimal representation of this fraction, to the given precision.
     */
    public BigDecimal toBigDecimal(int precision)
    {
        return new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator), new MathContext(precision, RoundingMode.HALF_EVEN));
    }
  
    //--------------------------------------------------------------------------
    //  IMPLEMENTATION OF NUMBER INTERFACE
    //--------------------------------------------------------------------------
    /**
     * Returns a long representation of this fraction. This value is
     * obtained by integer division of numerator by denominator. If
     * the value is greater than {@link Long#MAX_VALUE}, {@link Long#MAX_VALUE} will be
     * returned. Similarly, if the value is below {@link Long#MIN_VALUE},
     * {@link Long#MIN_VALUE} will be returned.
     * 
     * @return long representation of this fraction
     */
    @Override
    public long longValue()
    {
        BigInteger rounded = this.round(RoundingMode.DOWN);
        if(rounded.bitLength() > 63)
            return rounded.signum() < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
    
        return rounded.longValue();
    }
  
    /**
     * Returns an exact long representation of this fraction.
     * 
     * 
     * @return exact long representation of this fraction
     * @throws ArithmeticException if this has a nonzero fractional
     *                             part, or will not fit in a long.
     */
    public long longValueExact()
    {
        if(!mDenominator.equals(BigInteger.ONE) || mNumerator.bitLength() > 63)
            throw new ArithmeticException("Value does not have an exact long representation");
    
        return mNumerator.longValue();
    }
  
    /**
     * Returns an int representation of this fraction. This value is
     * obtained by integer division of numerator by denominator. If
     * the value is greater than {@link Integer#MAX_VALUE}, {@link Integer#MAX_VALUE} will be
     * returned. Similarly, if the value is below {@link Integer#MIN_VALUE},
     * {@link Integer#MIN_VALUE} will be returned.
     * 
     * @return int representation of this fraction
     */
    @Override
    public int intValue()
    {
        return (int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, longValue()));
    }
  
    /**
     * Returns an exact int representation of this fraction.
     * 
     * @return exact int representation of this fraction
     * @throws ArithmeticException if this has a nonzero fractional
     *                             part, or will not fit in an int.
     */
    public int intValueExact()
    {
        if(!mDenominator.equals(BigInteger.ONE) || mNumerator.bitLength() > 31)
            throw new ArithmeticException("Value does not have an exact int representation");
    
        return mNumerator.intValue();
    }
  
    /**
     * Returns a short representation of this fraction. This value is
     * obtained by integer division of numerator by mDenominator. If
     * the value is greater than {@link Short#MAX_VALUE}, {@link Short#MAX_VALUE} will be
     * returned. Similarly, if the value is below {@link Short#MIN_VALUE},
     * {@link Short#MIN_VALUE} will be returned.
     * 
     * @return short representation of this fraction
     */
    @Override
    public short shortValue()
    {
        return (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, longValue()));
    }
  
    /**
     * Returns an exact short representation of this fraction.
     * 
     * @return exact short representation of this fraction
     * @throws ArithmeticException if this has a nonzero fractional
     *                             part, or will not fit in a short.
     */
    public short shortValueExact()
    {
        if(!mDenominator.equals(BigInteger.ONE) || mNumerator.bitLength() > 15)
            throw new ArithmeticException("Value does not have an exact short representation");
    
        return mNumerator.shortValue();
    }
  
    /**
     * Returns a byte representation of this fraction. This value is
     * obtained by integer division of numerator by denominator. If
     * the value is greater than {@link Byte#MAX_VALUE}, {@link Byte#MAX_VALUE} will be
     * returned. Similarly, if the value is below {@link Byte#MIN_VALUE},
     * {@link Byte#MIN_VALUE} will be returned.
     * 
     * @return byte representation of this fraction
     */
    @Override
    public byte byteValue()
    {
        return (byte)Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, longValue()));
    }
  
    /**
     * Returns an exact byte representation of this fraction.
     * 
     * @return exact byte representation of this fraction
     * @throws ArithmeticException if this has a nonzero fractional
     *                             part, or will not fit in a byte.
     */
    public byte byteValueExact()
    {
        if(!mDenominator.equals(BigInteger.ONE) || mNumerator.bitLength() > 7)
            throw new ArithmeticException("Value does not have an exact byte representation");
    
        return mNumerator.byteValue();
    }
  
    /**
     * Returns the value of this fraction. If this value is beyond the
     * range of a double, {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY} will
     * be returned.
     * 
     * @return double representation of this fraction
     */
    @Override
    public double doubleValue()
    {
        //TODO: UNOPTIMIZED! Currently converting to BigDecimal then converting that to double.
    
        //note: must use precision+2 so that  new BigFraction(d).doubleValue() == d,
        //      for all possible double values.
        return toBigDecimal(MathContext.DECIMAL64.getPrecision() + 2).doubleValue();
    }
    
    /**
     * Returns the value of this fraction. If this value is beyond the
     * range of a float, {@link Float#POSITIVE_INFINITY} or {@link Float#NEGATIVE_INFINITY} will
     * be returned.
     * 
     * @return float representation of this fraction
     */
    @Override
    public float floatValue()
    {
        //TODO: UNOPTIMIZED! Currently converting to BigDecimal then converting that to float.
    
        //note: must use precision+2 so that  new BigFraction(f).floatValue() == f,
        //      for all possible float values.
        return toBigDecimal(MathContext.DECIMAL32.getPrecision() + 2).floatValue(); 
    }
  
    //--------------------------------------------------------------------------
    //  PRIVATE FUNCTIONS
    //--------------------------------------------------------------------------

    /**
     * Constructs a new BigFraction from the given BigDecimal object.
     */
    private static BigFraction valueOfHelper(BigDecimal d)
    {
        //BigDecimal format: unscaled / 10^scale.
        BigInteger tmpNumerator = d.unscaledValue();
        BigInteger tmpDenominator = BigInteger.ONE;
    
        //Special case for d == 0 (math below won't work right)
        //Note:  Cannot use d.equals(BigDecimal.ZERO), because BigDecimal.equals()
        //       does not consider numbers equal if they have different scales. So,
        //       0.00 is not equal to BigDecimal.ZERO.
        if(tmpNumerator.equals(BigInteger.ZERO))
            return BigFraction.ZERO;
    
        if(d.scale() < 0)
            {
                tmpNumerator = tmpNumerator.multiply(BigInteger.TEN.pow(-d.scale()));
            }
        else if (d.scale() > 0)
            {
                //Now we have the form:  unscaled / 10^scale = unscaled / (2^scale * 5^scale)
                //We know then that gcd(unscaled, 2^scale * 5^scale) = 2^commonTwos * 5^commonFives
      
                //Easy to determine commonTwos
                int commonTwos = Math.min(d.scale(), tmpNumerator.getLowestSetBit());
                tmpNumerator = tmpNumerator.shiftRight(commonTwos);
                tmpDenominator = tmpDenominator.shiftLeft(d.scale() - commonTwos);
      
                //Determining commonFives is a little trickier..
                int commonFives = 0;
      
                BigInteger[] divMod = null;
                //while(commonFives < d.scale() && tmpNumerator % 5 == 0) { tmpNumerator /= 5; commonFives++; }
                while(commonFives < d.scale() && BigInteger.ZERO.equals((divMod = tmpNumerator.divideAndRemainder(BIGINT_FIVE))[1]))
                    {
                        tmpNumerator = divMod[0];
                        commonFives++;
                    }
      
                if(commonFives < d.scale())
                    tmpDenominator = tmpDenominator.multiply(BIGINT_FIVE.pow(d.scale() - commonFives));
            }
        //else: d.scale() == 0: do nothing
    
        //Guaranteed there is no gcd, so fraction is in lowest terms
        return new BigFraction(tmpNumerator, tmpDenominator, Reduced.YES);
    }
  
    /**
     * Constructs a new BigFraction from two BigDecimals.
     * 
     * @throws ArithmeticException if denominator == 0.
     */
    private static BigFraction valueOfHelper(BigDecimal numerator, BigDecimal denominator)
    {
        //Note:  Cannot use .equals(BigDecimal.ZERO), because "0.00" != "0.0".
        if(denominator.unscaledValue().equals(BigInteger.ZERO))
            throw new ArithmeticException("Divide by zero: fraction denominator is zero.");
    
        //Format of BigDecimal: unscaled / 10^scale
        BigInteger tmpNumerator = numerator.unscaledValue();
        BigInteger tmpDenominator = denominator.unscaledValue();
    
        if(tmpNumerator.equals(BigInteger.ZERO))
            return BigFraction.ZERO;
    
        // (u1/10^s1) / (u2/10^s2) = u1 / (u2 * 10^(s1-s2)) = (u1 * 10^(s2-s1)) / u2
        if(numerator.scale() > denominator.scale())
            tmpDenominator = tmpDenominator.multiply(BigInteger.TEN.pow(numerator.scale() - denominator.scale()));
        else if(numerator.scale() < denominator.scale())
            tmpNumerator = tmpNumerator.multiply(BigInteger.TEN.pow(denominator.scale() - numerator.scale()));
        //else: scales are equal, do nothing.
    
        BigInteger gcd = tmpNumerator.gcd(tmpDenominator);
        tmpNumerator = tmpNumerator.divide(gcd);
        tmpDenominator = tmpDenominator.divide(gcd);
    
        if(tmpDenominator.signum() < 0)
            {
                tmpNumerator = tmpNumerator.negate();
                tmpDenominator = tmpDenominator.negate();
            }
    
        return new BigFraction(tmpNumerator, tmpDenominator, Reduced.YES);
    }

    /**
     * Private constructor that creates a BigFraction from a @a
     * numerator and @a denominator. A check is done to maintain a
     * positive denominator.
     * 
     * @param reduced  Indicates whether or not the fraction is already known to be
     *                   reduced to lowest terms.
     */
    private BigFraction(BigInteger numerator,
                        BigInteger denominator,
                        Reduced reduced) {
        this(numerator, denominator, reduced, true);
    }

    /**
     * Private constructor that creates a BigFraction from a @a
     * numerator and @a denominator. A check is done to maintain a
     * positive denominator.
     * 
     * @param reduced  Indicates whether or not the fraction is already known to be
     *                   reduced to lowest terms.
     * @param reduce Indicates whether or not the fraction should be reduced to lowest terms.
     */
    private BigFraction(BigInteger numerator,
                        BigInteger denominator,
                        Reduced reduced,
                        boolean reduce) {
        if(isZero(denominator))
            throw new ArithmeticException("Divide by zero: fraction denominator is zero.");
    
        //if numerator is zero, we don't care about the denominator. force it to 1.
        if(reduced == Reduced.NO && isZero(numerator)) {
            denominator = BigInteger.ONE;
            reduced = Reduced.YES;
        }
    
        //only numerator should be negative.
        if(denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
    
        //common special case - denominator is one. No need to do GCD check.
        if(reduced == Reduced.NO && isOne(denominator))
            reduced = Reduced.YES;
    
        if(reduced == Reduced.NO && reduce) {
            BigFraction bf = reduce(numerator, denominator);
            mNumerator = bf.mNumerator;
            mDenominator = bf.mDenominator;
        } else {
            mNumerator = numerator;
            mDenominator = denominator;
        }
    }
  
    /**
     * Performs a reduction on the @a numerator and @a denominator.
     */
    public static BigFraction reduce(BigInteger numerator, BigInteger denominator) {
        //create a reduced fraction
        BigInteger gcd = numerator.gcd(denominator);
        if(gcd.equals(BigInteger.ONE))
            return BigFraction.valueOf(numerator,
                                       denominator,
                                       false);
        else
            return BigFraction.valueOf(numerator.divide(gcd),
                                       denominator.divide(gcd),
                                       false);
    }

    /**
     * Performs a reduction on the @a bigFraction
     */
    public static BigFraction reduce(BigFraction bigFraction) {
        //create a reduced fraction
        return reduce(bigFraction.getNumerator(),
                      bigFraction.getDenominator());
    }

    /**
     * Converts a Number to a BigInteger. Assumes that a check on the
     * type of n has already been performed.
     */
    private static BigInteger toBigInteger(Number n) {
        if(n instanceof BigInteger)
            return (BigInteger)n;
    
        if(n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte || n instanceof AtomicInteger || n instanceof AtomicLong || n instanceof LongAdder || n instanceof LongAccumulator)
            return BigInteger.valueOf(n.longValue());
    
        if(n instanceof BigFraction)
            return ((BigFraction)n).mNumerator;

        if(n instanceof BigDecimal)
            {
                final BigDecimal bd = (BigDecimal)n;
                return bd.unscaledValue().multiply(BigInteger.TEN.pow(-bd.scale()));
            }

        throw new UnsupportedOperationException();
    }
  
    /**
     * Returns true if the given type can be converted to a BigInteger without loss
     * of precision. Returns true for the primitive integer types (Long, Integer, Short,
     * Byte, AtomicInteger, AtomicLong, LongAdder, LongAccumulator, or BigInteger).<br>
     * <br>
     * For BigFraction, returns true if denominator is 1.<br>
     * <br>
     * For double, float, DoubleAdder, DoubleAccumulator, and BigDecimal, analyzes the data. Otherwise returns false.<br>
     * <br>
     * Used to determine if a Number is appropriate to be passed into toBigInteger() method.
     */
    private static boolean isInt(Number n)
    {
        if(n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte || n instanceof BigInteger || n instanceof AtomicInteger || n instanceof AtomicLong || n instanceof LongAdder || n instanceof LongAccumulator)
            return true;
    
        if(n instanceof BigFraction)
            return ((BigFraction)n).getDenominator().equals(BigInteger.ONE);

        //BigDecimal format: unscaled / 10^scale
        if(n instanceof BigDecimal)
            return (((BigDecimal)n).scale() <= 0);
    
        //unknown type - use the doubleValue()
        final double d = n.doubleValue();
        if(d == 0.0)
            return true;
    
        if(Double.isInfinite(d) || Double.isNaN(d))
            return false;
    
        throw new UnsupportedOperationException();
    }
  
    /**
     * Returns true if n is a type that can be converted to a double without loss of precision (Float, Double, DoubleAdder, and DoubleAccumulator)
     */
    private static boolean isFloat(Number n)
    {
        return n instanceof Double || n instanceof Float || n instanceof DoubleAdder || n instanceof DoubleAccumulator;
    }
  
    /**
     * Returns true if the given Number represents zero. For unknown numbers, utilizes Number.doubleValue().
     */
    private final static boolean isZero(Number n)
    {
        //micro-optimization- most common type first...
        if(n instanceof BigFraction)
            return ((BigFraction)n).getNumerator().equals(BigInteger.ZERO);
    
        if(n instanceof BigInteger)
            return ((BigInteger)n).equals(BigInteger.ZERO);
    
        if(n == null)
            return false;
    
        if(n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte || n instanceof AtomicInteger || n instanceof AtomicLong || n instanceof LongAdder || n instanceof LongAccumulator)
            return n.longValue() == 0L;
    
        if(n instanceof BigDecimal)
            return ((BigDecimal)n).unscaledValue().equals(BigInteger.ZERO);
    
        //double or unknown type - use doubleValue()
        return n.doubleValue() == 0.0;
    }
  
    /**
     * Returns true if the given BigFraction represents zero. Overloaded as this is a common case.
     */
    private final static boolean isZero(BigFraction f)
    {
        return f.mNumerator.equals(BigInteger.ZERO);
    }
  
    /**
     * Returns true if the given Number represents one. For unknown numbers, utilizes Number.doubleValue().
     */
    private final static boolean isOne(Number n)
    {
        //micro-optimization- most common type first...
        if(n instanceof BigFraction)
            return ((BigFraction)n).equals(BigFraction.ONE);
    
        if(n == null)
            return false;
    
        if(n instanceof BigInteger)
            return ((BigInteger)n).equals(BigInteger.ONE);
    
        if(n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte || n instanceof AtomicInteger || n instanceof AtomicLong || n instanceof LongAdder || n instanceof LongAccumulator)
            return n.longValue() == 1L;
    
        if(n instanceof BigDecimal)
            return ((BigDecimal)n).compareTo(BigDecimal.ONE) == 0;
    
        //double or unknown type - use doubleValue()
        return n.doubleValue() == 1.0;
    }
  
}
