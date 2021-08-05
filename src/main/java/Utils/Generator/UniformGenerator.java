package utils.generator;

/**
 * Uniform number generator which generates number between max and min randomly.
 */
public class UniformGenerator extends BaseGenerator {
  public UniformGenerator(int mn, int mx) {
    this.mn = mn;
    this.mx = mx;
  }

  @Override
  public int next() {
    return mn + rand.nextInt(mx - mn);
  }
}
