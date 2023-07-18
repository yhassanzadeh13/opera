package utils.churn;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

/**
 * This is a class used to test the WeibullGenerator class. It tests the constructor of the WeibullGenerator class,
 * tests the next() method for generated values within the range and tests the distribution of generated values.
 */
public class WeibullGeneratorTest {
  /**
   * Test the constructor of the WeibullGenerator class.
   * The constructor should return a non-null WeibullGenerator instance.
   */
  @Test
  public void testConstructor() {
    double min = 1;
    double max = 10;
    double shapeParameter = 2;
    double scaleParameter = 3;
    WeibullGenerator weibullGenerator = new WeibullGenerator(min, max, shapeParameter, scaleParameter);

    assertNotNull(weibullGenerator, "WeibullGenerator should not be null");
  }

  /**
   * Test the next() method of the WeibullGenerator class.
   * It creates an instance of the WeibullGenerator and checks if the next() method returns a value in the specified range.
   */
  @Test
  public void testNextInRange() {
    double sample;
    boolean inRange;
    double min = 1;
    double max = 10;
    double shapeParameter = 2;
    double scaleParameter = 3;
    WeibullGenerator weibullGenerator = new WeibullGenerator(min, max, shapeParameter, scaleParameter);

    for (int i = 0; i < 100; i++) {
      sample = weibullGenerator.next();
      inRange = sample >= 1 && sample <= 10;

      assertTrue(inRange, String.format("Sample %f should be in range [1, 10]", sample));
    }
  }

  /**
   * Test if the next() method of the WeibullGenerator class generates values following the Weibull distribution.
   * It creates an instance of the WeibullGenerator, generates a large sample and tests if the sample mean and variance
   * are close to the expected mean and variance of the Weibull distribution.
   */
  @Test
  public void testNextFollowsDistribution() {
    double min = 0;
    double max = Double.MAX_VALUE;
    double shapeParameter = 2;
    double scaleParameter = 3;
    double TOLERANCE = 1E-2;
    WeibullGenerator weibullGenerator = new WeibullGenerator(min, max, shapeParameter, scaleParameter);

    int sampleSize = 1000000;
    DescriptiveStatistics stats = new DescriptiveStatistics();

    for (int i = 0; i < sampleSize; i++) {
      stats.addValue(weibullGenerator.next());
    }

    double sampleMean = stats.getMean();
    double sampleVariance = stats.getVariance();

    WeibullDistribution weibullDistribution = new WeibullDistribution(shapeParameter, scaleParameter);
    double expectedMean = weibullDistribution.getNumericalMean();
    double expectedVariance = weibullDistribution.getNumericalVariance();

    assertTrue(Math.abs(expectedMean - sampleMean) < TOLERANCE,
               String.format("Sample mean %f should be close to expected mean %f", sampleMean, expectedMean));
    assertTrue(Math.abs(expectedVariance - sampleVariance) < TOLERANCE,
               String.format("Sample variance %f should be close to expected variance %f", sampleVariance, expectedVariance));
  }
}
