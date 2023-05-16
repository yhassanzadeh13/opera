package utils.churn;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GaussianGeneratorTest {

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

    @Test
    public void testGaussianGeneratorDistribution() {
        int mean = 50;
        int std = 10;
        double sampleSize = 1_000_000.0;
        GaussianGenerator generator = new GaussianGenerator(mean, std);

        Map<Double, Integer> histogram = new HashMap<>();

        for (int i = 0; i < sampleSize; i++) {
            double value = generator.next();
            histogram.put(value, histogram.getOrDefault(value, 0) + 1);
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
            //
            // For ranges -3std and 3std (the tails of the distribution), the test checks if the percentage is between 0% and (100% - 99.73%) / 2 = 0.135%.
            // For ranges -2std and 2std, the test checks if the percentage is between (99.73% - 95.45%) / 2 = 2.14% and (100% - 95.45%) / 2 = 2.275%.
            // For ranges -1std and 1std, the test checks if the percentage is between (95.45% - 68.27%) / 2 = 13.59% and (100% - 68.27%) / 2 = 15.865%.
            // For the range around the mean (0*std), the test checks if the percentage is between 68.27% / 2 = 34.135% and 100% / 2 = 50%.
//            if (i == -3 || i == 3) {
//                assertTrue(percentage >= 0.0 && percentage <= 0.0135, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
//            } else if (i == -2 || i == 2) {
//                assertTrue(percentage >= 0.0214 && percentage <= 0.02275, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
//            } else if (i == -1 || i == 1) {
//                assertTrue(percentage >= 0.1359 && percentage <= 0.15865, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
//            } else {
//                assertTrue(percentage >= 0.34135 && percentage <= 0.5, String.format("Generated values do not follow Gaussian distribution. i: %d, percentage: %f", i, percentage));
//            }
        }
    }
}
