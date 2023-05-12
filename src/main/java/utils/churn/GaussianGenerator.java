package utils.churn;

import java.util.Random;

/**
 * GaussianGenerator is a class implementing a generator for churn values
 * based on the Gaussian (normal) distribution. The Gaussian distribution is a
 * continuous probability distribution characterized by its mean and standard
 * deviation. The distribution follows a bell curve shape, where values near
 * the mean are more likely to occur than values far from the mean.
 * <pre>
 * Gaussian Distribution Shape:
 *              |
 *              |                            ****
 *              |                         **      **
 *              |                       *            *
 *              |                     **              **
 * probability  |                  ***                  ***
 *              |              ****                        ****
 *              |         *****                                *****
 *              |    ****                                           ****
 *              +------------------------------------------------------------
 *                   |        |        |      |         |       |      |
 *                -3*std    -2*std   -std    mean      std    2*std  3*std
 *
 * </pre>
 * This class generates random numbers following the Gaussian distribution
 * defined by the mean and standard deviation provided. The values generated
 * can be optionally constrained to a specific range (min, max).
 */
public class GaussianGenerator implements ChurnGenerator {
  /**
   * The min value of the distribution, protocol parameter.
   */
  private final double min;
  /**
   * The max value of the distribution, protocol parameter.
   */
  private final double max;
  /**
   * Random generator.
   */
  private final Random rand;

  /**
   * The mean value of the Gaussian distribution, protocol parameter.
   */
  private final int mean;

  /**
   * The Standard Deviation of Gaussian distribution, protocol parameter.
   */
  private final int std;

  /**
   * Constructor of the GaussianGenerator.
   *
   * @param mean mean of the distribution.
   * @param std  standard deviation of the values
   */
  public GaussianGenerator(int mean, int std) {
    this(mean, std, 0, Double.MAX_VALUE);
  }

  /**
   * Constructor of the GaussianGenerator.
   *
   * @param mean mean of the distribution.
   * @param std standard deviation of the values
   * @param min minimum value of the distribution.
   * @param max maximum value of the distribution.
   */
  public GaussianGenerator(int mean, int std, double min, double max) {
    this.mean = mean;
    this.std = std;
    this.min = min;
    this.max = max;
    this.rand = new Random();
  }

  /**
   * Generates the next churn value.
   *
   * @return the next churn value in seconds.
   */
  @Override
  public int next() {
     int value = (int) Math.ceil(rand.nextGaussian() * this.std + this.mean);
      if (value < this.min) {
        return (int) this.min;
      } else if (value > this.max) {
        return (int) this.max;
      } else {
        return value;
      }
  }
}
