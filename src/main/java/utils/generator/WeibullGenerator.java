package utils.generator;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 * Weibull Generator generates number using Weibull Distribution.
 */
public class WeibullGenerator extends BaseGenerator {
  WeibullDistribution generator;

  /**
   * Constructor for WeibullDistribution.
   *
   * @param mn min value
   * @param mx max value
   * @param alpha first value for Weibull Distribution
   */
  public WeibullGenerator(int mn, int mx, int alpha, int beta) {
    this.mn = mn;
    this.mx = mx;
    this.generator = new WeibullDistribution(rand, alpha, beta, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }

  @Override
  public int next() {
    return (int) (mn + (mx - mn) * this.generator.sample());
  }
}
