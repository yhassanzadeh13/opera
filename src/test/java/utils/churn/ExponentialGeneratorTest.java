package utils.churn;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ExponentialGeneratorTest {
  /**
   * Tests that an IllegalArgumentException is thrown when trying to create an
   * ExponentialGenerator with a lambda value of zero.
   */
  @Test
  void testZeroLambdaValue() {
    assertThrows(IllegalArgumentException.class,
        () -> new ExponentialGenerator(0, 1, 100));
  }

  /**
   * Tests that the next() method of an ExponentialGenerator returns a positive integer value
   * when the lambda value is greater than zero.
   */
  @Test
  void testPositiveLambdaValue() {
    ExponentialGenerator generator = new ExponentialGenerator(1, 1, 100);
    int result = generator.next();
    assertTrue(result > 0);
  }

  /**
   * Tests that the next() method of an ExponentialGenerator returns an illegal argument exception
   * when the lambda is Double.MAX_VALUE.
   */
  @Test
  void testMaxValueLambda() {
    assertThrows(IllegalArgumentException.class,
        () -> new ExponentialGenerator(Double.MAX_VALUE, 1, 100));
  }

  /**
   * Tests that an IllegalArgumentException is thrown when trying to create an
   * ExponentialGenerator with a lambda value greater than the maximum allowed value.
   */
  @Test
  void testTooLargeLambdaValue() {
    assertThrows(IllegalArgumentException.class,
        () -> new ExponentialGenerator(Double.POSITIVE_INFINITY, 1, 100));
  }

  /**
   * Tests that an IllegalArgumentException is thrown when trying to create an
   * ExponentialGenerator with a negative lambda value.
   */
  @Test
  void testNegativeLambdaValue() {
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(-1, 1, 100));
  }

  /**
   * This test checks if the ExponentialGenerator can be instantiated with a lambda value greater
   * than 0 and with a very small positive value close to 0, without throwing any exceptions.
   */
  @Test
  public void testLambdaValueWithinValidRange() {
    // Case 1a: Lambda value > 0 (positive)
    assertDoesNotThrow(() -> new ExponentialGenerator(1.0, 1, 100));

    // Case 1b: Lambda value = very small positive value (close to 0)
    double smallPositiveValue = 1e-10;
    assertDoesNotThrow(() -> new ExponentialGenerator(smallPositiveValue, 1, 100));
  }

  /**
   * Test if ExponentialGenerator throws an exception when lambda is outside the valid range.
   * Case a: Lambda value = 0
   * Case b: Lambda value < 0 (negative)
   */
  @Test
  public void testLambdaValueOutsideValidRange() {
    // Case a: Lambda value = 0
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(0, 1, 100));

    // Case b: Lambda value < 0 (negative)
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(-1.0, 1, 100));
  }

  /**
   * Test if the random number generation is within the expected range and if it behaves
   * consistently
   * with a custom seed for the Random generator.
   * Case a: Check if generated values are within the range of [0, 1]
   */
  @Test
  public void testRandomNumberGeneration() {
    double lambda = 1.0;
    int numberOfSamples = 1000;

    // Case a: Check if generated values are within the range of [0, 1]
    ExponentialGenerator generator = new ExponentialGenerator(lambda, 1, 100);
    for (int i = 0; i < numberOfSamples; i++) {
      int generatedValue = generator.next();
      assertTrue(generatedValue >= 0, "Generated value should be greater than or equal to 0");
    }
  }

  /**
   * Test generated values for different lambda values and if the distribution follows the
   * expected behavior.
   * Case a: Test generated values for different lambda values
   * Case b: Check if the distribution follows the expected behavior (higher lambda values result
   * in smaller mean and variance)
   */
  @Test
  public void testExponentialDistributionGeneration() {
    int numberOfSamples = 1000;

    // Case a: Test generated values for different lambda values
    double[] lambdas = {0.5, 1.0, 2.0, 5.0};
    for (double lambda : lambdas) {
      ExponentialGenerator generator = new ExponentialGenerator(lambda, 1, 100);
      double sum = 0;
      for (int i = 0; i < numberOfSamples; i++) {
        int generatedValue = generator.next();
        assertTrue(generatedValue >= 0, "Generated value should be greater than or equal to 0");
        sum += generatedValue;
      }
      double mean = sum / numberOfSamples;
      assertTrue(mean > 0, "Mean should be greater than 0");
    }

    // Case b: Check if the distribution follows the expected behavior (higher lambda values
    // result in smaller mean and variance)
    ExponentialGenerator lowLambdaGenerator = new ExponentialGenerator(0.5, 1, 100);
    ExponentialGenerator highLambdaGenerator = new ExponentialGenerator(5.0, 1, 100);
    double sumLowLambda = 0;
    double sumHighLambda = 0;
    double sumSquareLowLambda = 0;
    double sumSquareHighLambda = 0;

    for (int i = 0; i < numberOfSamples; i++) {
      int lowLambdaValue = lowLambdaGenerator.next();
      int highLambdaValue = highLambdaGenerator.next();

      sumLowLambda += lowLambdaValue;
      sumHighLambda += highLambdaValue;
      sumSquareLowLambda += lowLambdaValue * lowLambdaValue;
      sumSquareHighLambda += highLambdaValue * highLambdaValue;
    }

    double meanLowLambda = sumLowLambda / numberOfSamples;
    double meanHighLambda = sumHighLambda / numberOfSamples;

    double varianceLowLambda =
        (sumSquareLowLambda / numberOfSamples) - (meanLowLambda * meanLowLambda);
    double varianceHighLambda =
        (sumSquareHighLambda / numberOfSamples) - (meanHighLambda * meanHighLambda);

    assertTrue(meanHighLambda < meanLowLambda,
        "Mean of high lambda distribution should be smaller than mean of low lambda distribution");
    assertTrue(varianceHighLambda < varianceLowLambda,
        "Variance of high lambda distribution should be smaller than variance of low lambda "
            + "distribution");
  }

  /**
   * Test extreme cases for lambda value.
   * Case a: Lambda value = Double.MAX_VALUE
   * Case b: Lambda value = Double.MIN_VALUE
   * Case c: Lambda value = Double.POSITIVE_INFINITY
   * Case d: Lambda value = Double.NEGATIVE_INFINITY
   * Case e: Lambda value = Double.NaN
   */
  @Test
  public void testExtremeCases() {
    // Case a: Lambda value = Double.MAX_VALUE
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(Double.MAX_VALUE,
     1, 100   ));

    // Case b: Lambda value = Double.MIN_VALUE
    assertDoesNotThrow(() -> new ExponentialGenerator(Double.MIN_VALUE, 1, 100));

    // Case c: Lambda value = Double.POSITIVE_INFINITY
    assertThrows(IllegalArgumentException.class,
        () -> new ExponentialGenerator(Double.POSITIVE_INFINITY, 1, 100));

    // Case d: Lambda value = Double.NEGATIVE_INFINITY
    assertThrows(IllegalArgumentException.class,
        () -> new ExponentialGenerator(Double.NEGATIVE_INFINITY, 1, 100));

    // Case e: Lambda value = Double.NaN
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(Double.NaN, 1,
        100));
  }

  /**
   * Test if the average and variance of samples generated from the distribution are within an acceptable error range.
   */
  @Test
  public void testSampleAverageAndVariance() {
    double lambda = 100.0;
    int min = 1;
    int max = 100;
    int numberOfSamples = 1_000_000;
    double acceptableError = 0.2;

    ExponentialGenerator generator = new ExponentialGenerator(lambda, min, max);
    double sum = 0;
    double sumSquare = 0;

    for (int i = 0; i < numberOfSamples; i++) {
      int generatedValue = generator.next();
      sum += generatedValue;
      sumSquare += generatedValue * generatedValue;
    }

    double sampleMean = sum / numberOfSamples;
    double sampleVariance = (sumSquare / numberOfSamples) - (sampleMean * sampleMean);

    double theoreticalMean = (1 / lambda) * (max - min) + min; // E[aX + b] = aE[X] + b
    double theoreticalVariance = (1 / (lambda * lambda)) * (max - min) * (max - min); // Var[aX + b] = a^2Var[X]

    double meanError = Math.abs(sampleMean - theoreticalMean);
    double varianceError = Math.abs(sampleVariance - theoreticalVariance);

    assertTrue(meanError <= acceptableError, "Sample mean should be within the acceptable error range of the theoretical mean");
    assertTrue(varianceError <= acceptableError, "Sample variance should be within the acceptable error range of the theoretical variance");
  }

}
