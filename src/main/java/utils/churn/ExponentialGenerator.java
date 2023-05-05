package utils.churn;

import java.util.Random;

/**
 * ExponentialGenerator is a class that generates random numbers according to an exponential
 * distribution.
 * The exponential distribution models the time between events in a Poisson process, where events
 * occur
 * continuously and independently at a constant average rate. The distribution is characterized
 * by its
 * rate parameter (lambda), which determines the shape of the distribution.
 * The probability density function (PDF) of an exponential distribution is:
 * f(x) = λ * exp(-λ * x) for x >= 0
 * Some key properties of the exponential distribution are:
 * 1. Memorylessness
 * 2. Exponential decay
 * 3. Skewness
 * 4. No upper bound
 * The following ASCII graphs show the exponential distribution with different lambda values:
 * (Note: These are simple visualizations, not accurate plots)
 * Lambda = 0.5:
 * f(x)
 * |
 * 0.5 +-------**----------------
 * |        *
 * 0.4 +         *
 * |          *
 * 0.3 +           *
 * |            *
 * 0.2 +             *
 * |              *
 * 0.1 +               *
 * |                *
 * 0.0 +-------------------------
 * 0  1  2  3  4  5  6  7  8
 * Lambda = 1:
 * f(x)
 * |
 * 1.0 +------**---------------
 * |       *
 * 0.8 +        *
 * |         *
 * 0.6 +          *
 * |           *
 * 0.4 +            *
 * |             *
 * 0.2 +              *
 * |               *
 * 0.0 +-----------------------
 * 0  1  2  3  4  5  6  7
 * Lambda = 2:
 * f(x)
 * |
 * 2.0 +-----**----------------
 * |      *
 * 1.6 +       *
 * |        *
 * 1.2 +         *
 * |          *
 * 0.8 +           *
 * |            *
 * 0.4 +             *
 * |              *
 * 0.0 +-----------------------
 * 0  1  2  3  4  5  6  7
 */
public class ExponentialGenerator implements ChurnGenerator {
    /**
     * The lambda value of the distribution, protocol parameter.
     */
    private final double lambda;

    /**
     * Random generator.
     */
    private final Random rand;

    /**
     * The min value of the distribution, protocol parameter.
     */
    private final int min;

    /**
     * The max value of the distribution, protocol parameter.
     */
    private final int max;

    /**
     * Constructor of ExponentialGenerator.
     *
     * @param lambda lambda value of the distribution, i.e., the rate parameter. It must be positive.
     *               the higher the lambda, the mean and variance of the distribution are smaller.
     * @param min    minimum value of the distribution.
     * @param max    maximum value of the distribution.
     */
    public ExponentialGenerator(double lambda, int min, int max) {
        this(lambda, min, max, new Random());
    }

    /**
     * Constructor of ExponentialGenerator.
     *
     * @param lambda lambda value of the distribution, i.e., the rate parameter. It must be positive.
     * @param rand   random generator.
     * @param min    minimum value of the distribution.
     * @param max    maximum value of the distribution.
     */
    public ExponentialGenerator(double lambda, int min, int max, Random rand) {
        if (lambda <= 0) {
            throw new IllegalArgumentException(String.format("Lambda (%f) must be positive",
                    lambda));
        }

        if (lambda >= Double.MAX_VALUE) {
            throw new IllegalArgumentException("Lambda must be smaller than Double.MAX_VALUE");
        }

        if (Double.isNaN(lambda)) {
            throw new IllegalArgumentException(String.format("Lambda (%f) must be a number",
                    lambda));
        }

        if (min >= max) {
            throw new IllegalArgumentException(String.format("Min (%d) must be smaller than max (%d)",
                    min, max));
        }

        this.lambda = lambda;
        this.rand = rand;
        this.min = min;
        this.max = max;
    }

    /**
     * Generates the next value of the distribution as an integer between min and max.
     *
     * @return the next value of the distribution as an integer.
     */
    @Override
    public int next() {
        // generates a uniform random number between 0 and 1 and adds a small value to avoid log(0)
        double u = rand.nextDouble() + 0.000_000_000_1; // to avoid log(0)
        // generates the next value according to the exponential distribution.
        double e = -Math.log(1 - u) / lambda;
        // amplifies the value to fit the desired range.
        e = e * (max - min) + min;

        // checks if the value is within the desired range (exponential distribution is not bounded).
        if (e < min) {
            e = min;
        }

        if (e > max) {
            e = max;
        }

        // returns the next value as an integer.
        return (int) Math.round(e);
    }
}
