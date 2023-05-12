package utils.churn;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GaussianGeneratorTest {

  @Test
  public void testGaussianGeneratorValuesInRange() {
    int mean = 50;
    int std = 10;
    int min = 30;
    int max = 70;
    GaussianGenerator generator = new GaussianGenerator(mean, std, min, max);

    for (int i = 0; i < 10000; i++) {
      int value = generator.next();
      assertTrue(value >= min && value <= max, "Generated value out of range");
    }
  }

  @Test
  public void testGaussianGeneratorDistribution() {
    int mean = 50;
    int std = 10;
    double sampleSize = 100_000.0;
    GaussianGenerator generator = new GaussianGenerator(mean, std);

    Map<Integer, Integer> histogram = new HashMap<>();

    for (int i = 0; i < sampleSize; i++) {
      int value = generator.next();
      histogram.put(value, histogram.getOrDefault(value, 0) + 1);
    }

    // Check if the generated values follow a Gaussian distribution
    for (int i = -3; i <= 3; i++) {
      int rangeMin = mean + i * std;
      int rangeMax = mean + (i + 1) * std;
      System.out.println("i:" + i + "rangeMin: " + rangeMin + " rangeMax: " + rangeMax);
      int count = 0;

      for (int value : histogram.keySet()) {
        if (value >= rangeMin && value < rangeMax) {
          count += histogram.get(value);
        }
      }

      double percentage = count / sampleSize;
      // The correct percentages are based on the properties of the Gaussian distribution and the 68-95-99.7 rule (also known as the empirical rule). According to this rule, for a Gaussian distribution:
      //
      // About 68.27% of the values fall within 1 standard deviation (±1σ) of the mean.
      // About 95.45% of the values fall within 2 standard deviations (±2σ) of the mean.
      // About 99.73% of the values fall within 3 standard deviations (±3σ) of the mean.
      //
      // Based on the empirical rule, the correct percentage ranges for the test should be:
      //
      // For ranges -3std and 3std (the tails of the distribution), the test checks if the percentage is between 0% and (100% - 99.73%) / 2 = 0.135%.
      // For ranges -2std and 2std, the test checks if the percentage is between (99.73% - 95.45%) / 2 = 2.14% and (100% - 95.45%) / 2 = 2.275%.
      // For ranges -1std and 1std, the test checks if the percentage is between (95.45% - 68.27%) / 2 = 13.59% and (100% - 68.27%) / 2 = 15.865%.
      // For the range around the mean (0*std), the test checks if the percentage is between 68.27% / 2 = 34.135% and 100% / 2 = 50%.
      if (i == -3 || i == 3) {
        assertTrue(percentage >= 0.0 && percentage <= 0.0135 * 2, String.format("Generated values do not follow Gaussian distribution: %f", percentage));
      } else if (i == -2 || i == 2) {
        assertTrue(percentage >= 0.0214 * 2 && percentage <= 0.02275 * 2, String.format("Generated values do not follow Gaussian distribution: %f", percentage));
      } else if (i == -1 || i == 1) {
        assertTrue(percentage >= 0.1359 * 2 && percentage <= 0.15865 * 2, String.format("Generated values do not follow Gaussian distribution: %f", percentage));
      } else {
        assertTrue(percentage >= 0.34135 * 2 && percentage <= 0.5 * 2, String.format("Generated values do not follow Gaussian distribution: %f", percentage));
      }
    }
  }
}
