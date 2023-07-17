package utils.churn;

/**
 * Interface for the churn generators. Used to generate churn values, i.e., the time between two
 * consecutive arrivals in the system (inter-arrival time), and the time a node stays in the system
 * till it leaves (session length). All units are in milliseconds (ms).
 */
public interface ChurnGenerator {
  /**
   * Generates the next churn value in milliseconds (ms).
   *
   * @return the next churn value in milliseconds (ms).
   */
  double next();
}
