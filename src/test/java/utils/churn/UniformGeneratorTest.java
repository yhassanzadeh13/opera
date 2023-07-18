package utils.churn;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for UniformGenerator class.
 */
class UniformGeneratorTest {
  private int min;
  private int max;
  private UniformGenerator generator;

  @BeforeEach
  void setUp() {
    min = 0;
    max = 1000;
    generator = new UniformGenerator(min, max);
  }

  /**
   * Test method that checks if the generated value is always within the specified range.
   * This test is repeated 1000 times to ensure a sufficient number of samples are tested.
   */
  @Test
  void next_alwaysReturnsValueInRange() {
    for (int i = 0; i < 1000; i++) {
      double value = generator.next();
      assertTrue(value >= min && value < max,
                 "Generated value should be within the specified range");
    }
  }


  /**
   * Test method that checks if the generated values are uniformly distributed within the specified range.
   * It generates 10K samples and counts the occurrences of each value, comparing them to the expected count.
   * An acceptable tolerance is defined to account for minor variations in counts.
   */
  @Test
  void next_generatesUniformlyDistributedValues() {
    int sampleSize = 1_000_000;
    int tolerance = (int) (sampleSize * 0.1);
    int[] counts = new int[max - min];
    for (int i = 0; i < sampleSize; i++) {
      double value = generator.next();
      counts[(int) value - min]++;
    }

    for (int count : counts) {
      assertTrue(count > 0,
                 "Each value in the range should be generated at least once");
      assertTrue(count >= sampleSize / max - tolerance && count <= sampleSize / max + tolerance,
                 "Value count should be within the acceptable tolerance");
    }
  }

  /**
   * This test is not guaranteed to pass, but it should pass most of the time. If it fails, it
   * should be run again. If it fails consistently, there is a problem with the generator.
   * <p>
   * Evaluates the uniqueness of the generated values. The test passes if the number of unique
   * values is within the acceptable tolerance, i.e., it keeps generating random values
   * and tracks the number of duplicates. If the number of duplicates is within the acceptable
   * tolerance, the test passes.
   */
  @Test
  void next_generatesUniqueValues() {
    int tolerance = (int) ((max - min) * 0.01); // 1% tolerance on duplicates
    assertTrue(tolerance > 0, "Tolerance should be greater than 0");
    Map<Integer, Integer> counts = new java.util.HashMap<>();
    for (int i = 0; i < (max - min); i++) {
      int value = (int) generator.next();
      counts.put(value, counts.getOrDefault(value, 0) + 1);
      assertTrue(counts.get(value) <= tolerance,
                 "Value count should be within the acceptable tolerance");
    }
  }

  /**
   * Test method that checks if the generated values have a mean and variance close to the expected
   * mean and variance of a uniform distribution.
   * It generates 10000 samples and computes the sample mean and variance, comparing them to the
   * theoretical mean and variance of a uniform distribution.
   * An acceptable tolerance is defined to account for minor variations.
   */
  @Test
  void next_generatesValuesWithExpectedMeanAndVariance() {
    int sampleSize = 10_000;
    double tolerance = 0.05;

    double expectedMean = (min + max) / 2.0;
    double expectedVariance = Math.pow(max - min, 2) / 12.0;

    double sum = 0;
    double sumSquared = 0;

    for (int i = 0; i < sampleSize; i++) {
      double value = generator.next();
      sum += value;
      sumSquared += Math.pow(value, 2);
    }

    double sampleMean = sum / sampleSize;
    double sampleVariance = (sumSquared - (Math.pow(sum, 2) / sampleSize)) / (sampleSize - 1);

    assertTrue(Math.abs(((float) (expectedMean - sampleMean)) / expectedMean) < tolerance,
               "Sample mean should be close to the expected mean of a uniform distribution");
    assertTrue(Math.abs(((float) (expectedVariance - sampleVariance)) / expectedVariance) < tolerance,
               "Sample variance should be close to the expected variance of a uniform distribution");
  }
}

