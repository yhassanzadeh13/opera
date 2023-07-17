package utils.churn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    double result = generator.next();
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
   * Test if ExponentialGenerator throws an exception when min and max are outside the valid range.
   */
  @Test
  public void testMinAndMaxOutsideValidRange() {
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1.0, 100, 1));
  }

  /**
   * Test if the random number generation is within the expected range and if it behaves
   * consistently. For a given lambda and different min and max values, generate a large
   * number of samples and check if the generated values are within the expected range, i.e.,
   * min <= generated value <= max.
   */
  @Test
  public void testRandomNumberGenerationWithinRange() {
    double lambda = 0.5;
    int numberOfSamples = 1_000_000;
    int[] minValues = {1, 10, 100, 1000, 10_000, 100_000, 1000_000};
    int[] maxValues = {10, 100, 1000, 10_000, 100_000, 1000_000, 10_000_000};

    for (int i = 0; i < minValues.length; i++) {
      ExponentialGenerator generator = new ExponentialGenerator(
        lambda,
        minValues[i],
        maxValues[i]);
      for (int j = 0; j < numberOfSamples; j++) {
        double generatedValue = generator.next();
        assertTrue(generatedValue >= minValues[i],
          String.format("Generated value %f is smaller than the minimum value %d",
            generatedValue, minValues[i]));
        assertTrue(generatedValue <= maxValues[i],
          String.format("Generated value %f is larger than the maximum value %d",
            generatedValue, maxValues[i]));
      }
    }

  }

  /**
   * Test generated values for different lambda values and if the distribution follows the
   * expected behavior.
   * Test generated values for different lambda values.
   * Check if the distribution follows the expected behavior (higher lambda values result
   * in smaller mean and variance)
   */
  @Test
  public void testIncreasingLambdas() {
    int numberOfSamples = 1000;
    int minValue = 1; // Minimum value for the generated values.
    int maxValue = 100; // Maximum value for the generated values.

    // Case a: Test generated values for different lambda values
    double[] lambdas = {0.5, 1.0, 2.0, 5.0};
    double[] means = new double[lambdas.length]; // Mean of the generated values, one for each lambda.
    for (int i = 0; i < lambdas.length; i++) {
      ExponentialGenerator generator = new ExponentialGenerator(lambdas[i], minValue, maxValue);
      double sum = 0;
      for (int j = 0; j < numberOfSamples; j++) {
        double generatedValue = generator.next();
        assertTrue(generatedValue >= 0, "Generated value should be greater than or equal to 0");
        sum += generatedValue;
      }
      double mean = sum / numberOfSamples;
      assertTrue(mean > 0, "Mean should be greater than 0");
      means[i] = mean;
    }

    for (int i = 0; i < means.length - 1; i++) {
      assertTrue(means[i] > means[i + 1], "Mean should decrease with increasing lambda");
    }
  }

  /**
   * Test generated values for different min and max values and if the distribution follows the
   * expected behavior. Check if the distribution follows the expected behavior (higher min and
   * max values result in larger mean and variance).
   */
  @Test
  public void testIncreasingRanges() {
    int numberOfSamples = 1000;
    double lambda = 0.5;

    // Case a: Test generated values for different lambda values
    int[] minValues = {1, 10, 100, 1000, 10000}; // min value for the generated values.
    int[] maxValues = {2, 20, 200, 2000, 20000}; // max value for the generated values.
    double[] means = new double[minValues.length]; // Mean of the generated values, one for each lambda.
    double[] variances = new double[minValues.length]; // Variance of the generated values, one for each lambda.
    for (int i = 0; i < minValues.length; i++) {
      ExponentialGenerator generator = new ExponentialGenerator(
        lambda,
        minValues[i],
        maxValues[i]);
      double sum = 0;
      double sumOfSquares = 0;
      for (int j = 0; j < numberOfSamples; j++) {
        double generatedValue = generator.next();
        assertTrue(generatedValue >= 0, "Generated value should be greater than or equal to 0");
        sum += generatedValue;
        sumOfSquares += generatedValue * generatedValue;
      }
      double mean = sum / numberOfSamples;
      double variance = sumOfSquares / numberOfSamples - mean * mean; // E[X^2] - E[X]^2
      assertTrue(mean > 0, "Mean should be greater than 0");
      assertTrue(variance >= 0, "Variance should be greater than 0");
      means[i] = mean;
      variances[i] = variance;
    }

    for (int i = 0; i < means.length - 1; i++) {
      assertTrue(means[i] < means[i + 1], "Mean should increase with increasing range");
      assertTrue(variances[i] < variances[i + 1], "Variance should increase with increasing");
    }
  }

  /**
   * Test extreme cases for lambda value.
   * Case a: Lambda value = Double.MAX_VALUE
   * Case b: Lambda value = Double.MIN_VALUE
   * Case c: Lambda value = Double.POSITIVE_INFINITY
   * Case d: Lambda value = Double.NEGATIVE_INFINITY
   * Case e: Lambda value = Double.NaN
   * Case f: Lambda value = 0
   * Case g: Lambda value = -1
   * Case h: Lambda value = 1
   */
  @Test
  public void testLambdaExtremeCases() {
    // Case a: Lambda value = Double.MAX_VALUE
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(Double.MAX_VALUE,
      1, 100));

    // Case b: Lambda value = Double.MIN_VALUE
    assertDoesNotThrow(() -> new ExponentialGenerator(Double.MIN_VALUE, 1, 100));

    // Case c: Lambda value = Double.POSITIVE_INFINITY
    assertThrows(IllegalArgumentException.class,
      () -> new ExponentialGenerator(Double.POSITIVE_INFINITY, 1, 100));

    // Case d: Lambda value = Double.NEGATIVE_INFINITY
    assertThrows(IllegalArgumentException.class,
      () -> new ExponentialGenerator(Double.NEGATIVE_INFINITY, 1, 100));

    // Case e: Lambda value = Double.NaN
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(Double.NaN, 1, 100));

    // Case f: Lambda value = 0
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(0, 1, 100));

    // Case g: Lambda value = -1
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(-1, 1, 100));

    // Case h: Lambda value = 1
    assertDoesNotThrow(() -> new ExponentialGenerator(1, 1, 100));
  }


  /**
   * Test extreme cases for min and max values, i.e. min = max, min > max, min = 0, max = 0, min = 1, max = 1,
   * min = 0, max = -1, min = -1, max = 0. All cases should throw an IllegalArgumentException.
   */
  @Test
  public void testMinMaxExtremeCases() {
    // Case a: min = 0, max = 0
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, 0, 0));

    // Case b: min = 0, max = 1
    assertDoesNotThrow(() -> new ExponentialGenerator(1, 0, 1));

    // Case c: min = 1, max = 0
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, 1, 0));

    // Case d: min = 1, max = 1
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, 1, 1));

    // Case e: min = 0, max = -1
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, 0, -1));

    // Case f: min = -1, max = 0
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, -1, 0));

    // Case g: min = -1, max = -1
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, -1, -1));

    // Case i: min = 0, max = Integer.MAX_VALUE
    assertDoesNotThrow(() -> new ExponentialGenerator(1, 0, Integer.MAX_VALUE));

    // Case j: min = Integer.MAX_VALUE, max = Integer.MAX_VALUE
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, Integer.MAX_VALUE, Integer.MAX_VALUE));

    // Case k: min = Integer.Nan, max = 1
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, Integer.MIN_VALUE, 1));

    // Case l: min = Integer.MIN_VALUE, max = Integer.MIN_VALUE
    assertThrows(IllegalArgumentException.class, () -> new ExponentialGenerator(1, Integer.MIN_VALUE, Integer.MIN_VALUE));
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
      double generatedValue = generator.next();
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
