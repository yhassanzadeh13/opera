package metrics;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Global constants used across codebase.
 */
public class Constants {

  public static final String IDENTIFIER = "id";

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
    private static final double[] DEFAULT_HISTOGRAM = new double[]{Double.MAX_VALUE};
    private static final double[] MESSAGE_SIZE_HISTOGRAM = new double[]{100, 1000, 100_000, 1_000_000, 10_000_000};

    public static double[] getDefaultHistogram() {
      return DEFAULT_HISTOGRAM.clone();
    }

    public static double[] getMessageSizeHistogram() {
      return MESSAGE_SIZE_HISTOGRAM.clone();
    }
  }

}
