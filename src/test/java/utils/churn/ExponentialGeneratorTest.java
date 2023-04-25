package utils.churn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExponentialGeneratorTest {
  private ExponentialGenerator generator;
  private final int min = 1;
  private final int max = 100;
  private final double lambda = 5;

  @BeforeEach
  public void setUp() {
    generator = new ExponentialGenerator(min, max, lambda);
  }

  @Test
  public void constructorTest() {
    assertNotNull(generator, "ExponentialGenerator should be instantiated");
  }

  @Test
  public void nextGeneratesWithinRangeTest() {
    for (int i = 0; i < 1000; i++) {
      int result = generator.next();
      assertTrue(result >= min && result <= max, "Generated value should be within specified range");
    }
  }

  @Test
  public void nextGeneratesDifferentValuesTest() {
    int initialValue = generator.next();
    boolean differentValueFound = false;

    for (int i = 0; i < 1000; i++) {
      int newValue = generator.next();
      if (initialValue != newValue) {
        differentValueFound = true;
        break;
      }
    }

    assertTrue(differentValueFound, "Generated values should vary over time");
  }

  @Test
  public void constructorThrowsExceptionOnInvalidLambda() {
    assertThrows(IllegalArgumentException.class, () -> {
      new ExponentialGenerator(min, max, -1);
    }, "Constructor should throw an exception for negative lambda");
  }

}
