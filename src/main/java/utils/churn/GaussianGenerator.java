package utils.churn;

import java.util.Random;

/**
 * Implementation of the Gaussian distribution for generating churn values.
 */
public class GaussianGenerator implements ChurnGenerator {
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
    this.mean = mean;
    this.std = std;
    this.rand = new Random();
  }

  /**
   * Generates the next churn value in seconds.
   *
   * @return the next churn value in seconds.
   */
  @Override
  public int next() {
    return (int) Math.ceil(rand.nextGaussian() * this.std + this.mean);
  }
}
