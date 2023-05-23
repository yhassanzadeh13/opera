package utils.churn;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeibullGeneratorTest {
  @Test
  public void testConstructor() {
    double min = 1;
    double max = 10;
    double shapeParameter = 2;
    double scaleParameter = 3;
    WeibullGenerator weibullGenerator = new WeibullGenerator(min, max, shapeParameter, scaleParameter);

    assertNotNull(weibullGenerator, "WeibullGenerator should not be null");
  }

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

  @Test
  public void testNextFollowsDistribution() {
    double min = 0;
    double max = Double.MAX_VALUE;
    double shapeParameter = 2;
    double scaleParameter = 3;
    double TOLERANCE = 1E-1;
    WeibullGenerator weibullGenerator = new WeibullGenerator(min, max, shapeParameter, scaleParameter);

    int sampleSize = 1000000;
    DescriptiveStatistics stats = new DescriptiveStatistics();

    for (int i = 0; i < sampleSize; i++) {
      stats.addValue(weibullGenerator.next());
    }

    double sampleMean = stats.getMean();
    double sampleVariance = stats.getVariance();
    double sampleSkewness = stats.getSkewness();

    // Weibull distribution parameters
    double expectedMean = scaleParameter * Math.exp(1.0 / shapeParameter);
    double expectedVariance = Math.pow(scaleParameter, 2) * (0.0);
    // double expectedSkewness = (Math.exp(3.0 / shapeParameter) - 3 * Math.exp(2.0 / shapeParameter) + 2) / Math.sqrt(0.0);

    assertTrue(Math.abs(expectedMean - sampleMean) < TOLERANCE, String.format("Sample mean %f should be close to expected mean %f", sampleMean, expectedMean));
    assertTrue(Math.abs(expectedVariance - sampleVariance) < TOLERANCE, String.format("Sample variance %f should be close to expected variance %f", sampleVariance, expectedVariance));
    // assertTrue(Math.abs(expectedSkewness - sampleSkewness) < TOLERANCE, String.format("Sample skewness %f should be close to expected skewness %f", sampleSkewness, expectedSkewness));
  }
}
