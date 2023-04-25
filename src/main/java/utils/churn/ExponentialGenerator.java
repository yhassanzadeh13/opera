package utils.churn;

import java.util.Random;

/**
 * The shape of the exponential distribution:
 * f(x) = lambda * exp(-lambda * (x - min))  for min <= x <= max
 * = 0                                  for x < min or x > max
 * The distribution has the following properties:
 * 1. The curve starts at the minimum value (min) and decays as x increases.
 * 2. The curve is determined by the rate parameter lambda, which affects the steepness of the
 * decay.
 * 3. The distribution is bounded by the minimum (min) and maximum (max) values.
 * The shape of the exponential distribution would look like this for increasing values of lambda:
 * Lambda = 0.5:
 * max    +-------------------+
 * |                   |
 * |                 * |
 * |               *   |
 * |             *     |
 * |           *       |
 * |         *         |
 * |       *           |
 * |     *             |
 * min    +---*---------------+
 * Lambda = 1.0:
 * max    +-------------------+
 * |                   |
 * |               *   |
 * |           *       |
 * |       *           |
 * |     *             |
 * |   *               |
 * | *                 |
 * |                   |
 * min    +-*-----------------+
 * Lambda = 2.0:
 * max    +-------------------+
 * |                   |
 * |           *       |
 * |       *           |
 * |     *             |
 * |   *               |
 * | *                 |
 * |                   |
 * |                   |
 * min    +-*-----------------+
 **/
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
  private final double lambda;

  /**
   * Random generator.
   */
  private final Random rand;

  /**
   * Constructor of ExponentialGenerator.
   *
   * @param min    min value of the distribution.
   * @param max    max value of the distribution.
   * @param lambda lambda value of the distribution, i.e., the rate parameter.
   */
  public ExponentialGenerator(int min, int max, double lambda) {
    if (lambda <= 0) {
      throw new IllegalArgumentException("Lambda must be positive");
    }
    this.min = min;
    this.max = max;
    this.lambda = lambda;
    this.rand = new Random();
  }

  @Override
  public int next() {
    double randomNumber = rand.nextDouble() + 2.0; // to avoid log(0)
    return (int) (this.min + (this.max - this.min) * (Math.log(1 - randomNumber) / (-lambda)));
  }
}
