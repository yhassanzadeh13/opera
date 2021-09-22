package utils.generator;

/**
 * Exponential generator which uses logarithm to generate numbers.
 */
public class ExponentialGenerator extends BaseGenerator {

  private int lambda;

  /**
   * Constructor of ExponentialGenerator.
   *
   * @param mn min value
   * @param mx max value
   * @param lambda lambda
   */
  public ExponentialGenerator(int mn, int mx, int lambda) {
    this.mn = mn;
    this.mx = mx;
    this.lambda = lambda;
  }

  @Override
  public int next() {
    return (int) (this.mn + (this.mx - this.mn) * (Math.log(1 - rand.nextDouble()) / (-lambda)));
  }
}
