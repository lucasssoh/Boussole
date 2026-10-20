package io.github.lucasssoh.boussole.format.model.expectation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An exact number, so that {@code 3/4} and {@code 0.75} are equal. Always reduced, with a
 * positive denominator.
 */
public record Rational(BigInteger numerator, BigInteger denominator) implements Comparable<Rational> {

    private static final Pattern FRACTION = Pattern.compile("\\s*(-?\\d+(?:[.,]\\d+)?)\\s*/\\s*(\\d+(?:[.,]\\d+)?)\\s*");
    private static final Pattern DECIMAL = Pattern.compile("\\s*(-?\\d+(?:[.,]\\d+)?)\\s*");

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);

    public Rational {
        if (denominator.signum() == 0) {
            throw new IllegalArgumentException("denominator is zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        if (!gcd.equals(BigInteger.ONE) && gcd.signum() != 0) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
    }

    /** Reads {@code 48}, {@code -2.5}, {@code 1,5} or {@code 3/4}. */
    public static Optional<Rational> parse(String text) {
        var fraction = FRACTION.matcher(text);
        if (fraction.matches()) {
            Rational denominator = ofDecimal(fraction.group(2));
            return denominator.equals(ZERO)
                    ? Optional.empty()
                    : Optional.of(ofDecimal(fraction.group(1)).divide(denominator));
        }
        var decimal = DECIMAL.matcher(text);
        return decimal.matches() ? Optional.of(ofDecimal(decimal.group(1))) : Optional.empty();
    }

    private static Rational ofDecimal(String text) {
        BigDecimal value = new BigDecimal(text.replace(',', '.'));
        return value.scale() <= 0
                ? new Rational(value.toBigIntegerExact(), BigInteger.ONE)
                : new Rational(value.unscaledValue(), BigInteger.TEN.pow(value.scale()));
    }

    public Rational subtract(Rational other) {
        return new Rational(
                numerator.multiply(other.denominator).subtract(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator));
    }

    public Rational divide(Rational other) {
        return new Rational(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
    }

    public Rational abs() {
        return new Rational(numerator.abs(), denominator);
    }

    @Override
    public int compareTo(Rational other) {
        return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
    }

    @Override
    public String toString() {
        return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator;
    }
}
