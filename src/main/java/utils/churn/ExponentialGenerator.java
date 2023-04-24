package utils.churn;

import java.util.Random;

/**
 * Exponential generator which uses logarithm to generate numbers.
 */
public class ExponentialGenerator implements ChurnGenerator {
  /**
   * The min value of the distribution, protocol parameter.
   */
  private final int min;

  /**
   * The max value of the distribution, protocol parameter.
   */
  private final int max;

  /**
   * The lambda value of the distribution, protocol parameter.
   */
  private final int lambda;

  /**
   * Random generator.
   */
  private final Random rand;

  /**
   * Constructor of ExponentialGenerator.
   *
   * @param min min value
   * @param max max value
   * @param lambda lambda
   */
  public ExponentialGenerator(int min, int max, int lambda) {
    this.min = min;
    this.max = max;
    this.lambda = lambda;
    this.rand = new Random();
  }

  @Override
  public int next() {
    return (int) (this.min + (this.max - this.min) * (Math.log(1 - rand.nextDouble()) / (-lambda)));
  }
}
