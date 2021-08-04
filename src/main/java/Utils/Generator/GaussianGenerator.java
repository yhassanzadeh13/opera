package utils.generator;


import java.util.Random;

public class GaussianGenerator extends BaseGenerator {

  private Random rand;
  private int mean;
  private int std;

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
