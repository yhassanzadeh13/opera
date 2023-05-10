package utils.churn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void next_alwaysReturnsValueInRange() {
    for (int i = 0; i < 1000; i++) {
      int value = generator.next();
      assertTrue(value >= min && value < max,
              "Generated value should be within the specified range");
    }
  }

  @Test
  void next_generatesUniformlyDistributedValues() {
    int sampleSize = 1000;
    int tolerance = (int) (sampleSize * 0.1);
    int[] counts = new int[max - min];
    for (int i = 0; i < sampleSize; i++) {
      int value = generator.next();
      counts[value - min]++;
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
   *
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
      int value = generator.next();
      counts.put(value, counts.getOrDefault(value, 0) + 1);
      assertTrue(counts.get(value) <= tolerance,
              "Value count should be within the acceptable tolerance");
    }
  }
}

