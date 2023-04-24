package utils.churn;

import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 * Base class for all generators.
 */
public interface ChurnGenerator {
  /**
   * Generates the next churn value in seconds.
   *
   * @return the next churn value in seconds.
   */
  int next();
}
