package metrics;

/**
 * Global constants used across codebase.
 */
public class Constants {

  public static final String UUID = "uuid";

  /**
   * General namespaces used for metric collection. By the general we mean those
   * shared among at least two classes.
   */
  public static class Namespace {
    public static final String NETWORK = "network";
    public static final String DEMO = "demo";
    public static final String TEST = "test";
  }


  /**
   * Contains default constant values for histogram collectors.
   */
  public static class Histogram {
    public static final double[] DEFAULT_HISTOGRAM = new double[]{Double.MAX_VALUE};
  }
}
