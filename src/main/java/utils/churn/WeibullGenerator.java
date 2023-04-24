package utils.churn;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 * Implementation of the Weibull distribution for generating churn values.
 */
public class WeibullGenerator implements ChurnGenerator {
  /**
   * The min value of the distribution, protocol parameter.
   */
  private final int mn;
  /**
   * The max value of the distribution, protocol parameter.
   */
  private final int mx;
  /**
   * Random generator.
   */
  private final JDKRandomGenerator rand;
  WeibullDistribution generator;

  /**
   * Constructor for WeibullDistribution.
   *
   * @param mn    min value
   * @param mx    max value
   * @param alpha first value for Weibull Distribution
   */
  public WeibullGenerator(int mn, int mx, int alpha, int beta) {
    this.mn = mn;
    this.mx = mx;
    this.rand = new JDKRandomGenerator();
    this.generator = new WeibullDistribution(rand, alpha, beta,
        WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }

  @Override
  public int next() {
    return (int) (mn + (mx - mn) * this.generator.sample());
  }
}
