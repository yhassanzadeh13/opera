package utils.churn;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for GaussianGenerator class.
 */
public class GaussianGeneratorTest {

    /**
     * Tests if the generated values are within the specified range. This test is repeated 10K times.
     */
    @Test
    public void testGaussianGeneratorValuesInRange() {
        int mean = 50;
        int std = 10;
        int min = 30;
        int max = 70;
        GaussianGenerator generator = new GaussianGenerator(mean, std, min, max);

        for (int i = 0; i < 10000; i++) {
            double value = generator.next();
            assertTrue(value >= min && value <= max, "Generated value out of range");
        }
    }

    /**
     * Performs the 99.7, 95 and 68.2 test for the generated values. The test is performed for 1M samples.
     */
    @Test
    public void testGaussianGeneratorDistribution() {
        int mean = 50;
        int std = 10;
        double sampleSize = 1_000_000.0;
        GaussianGenerator generator = new GaussianGenerator(mean, std);

        Map<Double, Double> histogram = new HashMap<>();

        for (int i = 0; i < sampleSize; i++) {
            double value = generator.next();
            histogram.put(value, histogram.getOrDefault(value, (double) 0) + 1);
        }

        double total = 0;
        for (double key : histogram.keySet()) {
            total += histogram.get(key);
        }
        assertEquals(total, sampleSize, "Total count should be equal to sample size");

        // Check if the generated values follow a Gaussian distribution
        for (int i = -3; i <= 3; i++) {
            int rangeMin = mean + (i * std);
            int rangeMax = mean + ((i + 1) * std);
            int count = 0;

            for (double key : histogram.keySet()) {
                if (key >= rangeMin && key < rangeMax) {
                    count += histogram.get(key);
                }
            }

            double percentage = (double) count / sampleSize;
            System.out.println("i:" + i + " rangeMin: " + rangeMin + " rangeMax: " + rangeMax + " count: " + count + " percentage: " + percentage);
            // The correct percentages are based on the properties of the Gaussian distribution and the 68-95-99.7 rule (also known as the empirical rule). According to this rule, for a Gaussian distribution:
            //
            // About 68.27% of the values fall within 1 standard deviation (±1σ) of the mean.
            // About 95.45% of the values fall within 2 standard deviations (±2σ) of the mean.
            // About 99.73% of the values fall within 3 standard deviations (±3σ) of the mean.
            //
            // Based on the empirical rule, the correct percentage ranges for the test should be:
            // Within 1 standard deviation (±1σ) of the mean, check if around 68% of the values are generated in this range, i.e., 68.27% / 2 = 34.135% and 100% / 2 = 50%.
            if (i == -1 || i == 0) {
                assertTrue(percentage > 0.34 && percentage < 0.5, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
            }
            // Within 2 standard deviations (±2σ) of the mean, check if around 95% of the values are generated in this range, i.e., (95.45 - 68.27) / 2 = 13.5%.
            else if (i == -2 || i == 1) {
                assertTrue(percentage > 0.13 && percentage < 0.34, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
            }
            // Within 3 standard deviations (±3σ) of the mean, check if around 99% of the values are generated in this range, i.e., (99.73 - 95.45) / 2 = 2.14%.
            else if (i == -3 || i == 2) {
                assertTrue(percentage > 0.02 && percentage < 0.13, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
            }
        }
    }
}

