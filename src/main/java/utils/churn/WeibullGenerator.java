package utils.churn;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 * This class implements the Weibull distribution to generate churn values.
 * The Weibull distribution is a continuous probability distribution named after Wallodi Weibull,
 * who described it in detail in 1951.
 * It is a versatile distribution that can assume the characteristics of many types of statistical distributions based
 * on the values of the shape and scale parameters.
 * In the context of this class, it's represented by two parameters: shape and scale.
 * Shape parameter < 1: distribution represents high values at the beginning that decrease over time.
 * Shape parameter = 1: the distribution simulates the exponential distribution.
 * Shape parameter > 1: the distribution depicts low values that increases over time in a bell-shape.
 * The scale parameter is a positive real number that represents the scale of the distribution.
 * Here is the formula for the Weibull distribution probability density function:
 * f(x; shape, scale) = (shape / scale) * (x / scale)^(shape - 1) * exp(-(x / scale)^shape)
 * Weibull distribution is a generalization of the exponential distribution that adds an additional shape parameter.
 * It can model different types of distribution shapes including exponential, Rayleigh, and Frechet distributions,
 * all of which are specific cases of Weibull depending on the values of alpha and beta.
 * For the generation of random numbers following a Weibull distribution,
 * an instance of WeibullDistribution from the Apache Commons Math library is used.
 * https://en.wikipedia.org/wiki/Weibull_distribution
 */
public class WeibullGenerator implements ChurnGenerator {
    /**
     * The min value of the distribution, protocol parameter.
     */
    private final double min;
    /**
     * The max value of the distribution, protocol parameter.
     */
    private final double max;

    private final WeibullDistribution generator;

    /**
     * Constructor for WeibullDistribution.
     *
     * @param min            min value
     * @param max            max value
     * @param shapeParameter the shape parameter of the distribution.
     * @param scaleParameter the scale parameter of the distribution.
     */
    public WeibullGenerator(double min, double max, double shapeParameter, double scaleParameter) {
        this.min = min;
        this.max = max;
        JDKRandomGenerator rand = new JDKRandomGenerator();
        this.generator = new WeibullDistribution(
                rand,
                shapeParameter,
                scaleParameter,
                WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Generates a random number following the Weibull distribution within the range [min, max].
     *
     * @return a random number following the Weibull distribution within the range [min, max].
     */
    @Override
    public double next() {
        double sample = this.generator.sample();
        if (sample < min) {
            sample = min;
        } else if (sample > max) {
            sample = max;
        }
        return sample;
    }
}
