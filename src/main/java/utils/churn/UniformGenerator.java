package utils.churn;

import java.util.Random;

/**
 * Implementing Uniform distribution for generating churn values.
 */
public class UniformGenerator implements ChurnGenerator {
  /**
   * The min value of the distribution, protocol parameter.
   */
  private final int min;
  /**
   * The max value of the distribution, protocol parameter.
   */
  private final int max;

  /**
   * Random generator.
   */
  private final Random rand;

  public UniformGenerator(int min, int max) {
    this.min = min;
    this.max = max;
    this.rand = new Random();
  }

  @Override
  public int next() {
    return min + rand.nextInt(max - min);
  }
}
