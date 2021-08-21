package utils.generator;


import java.util.Random;

/**
 * A generator which uses rand.nextGaussian to generate.
 */
public class GaussianGenerator extends BaseGenerator {

  private Random rand;
  private int mean;
  private int std;

  /**
   * Constructor of the GaussianGenerator.
   *
   * @param mean mean of the values
   * @param std standard deviation of the values
   */
  public GaussianGenerator(int mean, int std) {
    this.mean = mean;
    this.std = std;
    this.rand = new Random();
  }

  @Override
  public int next() {
    return (int) Math.ceil(rand.nextGaussian() * this.std + this.mean);
  }
}
