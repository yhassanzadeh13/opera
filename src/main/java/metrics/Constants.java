package metrics;

public class Constants {

  public static final String UUID = "uuid";

  public class Namespace {
    public static final String NETWORK = "network";
    public static final String DEMO = "demo";
  }




  /**
   * Contains default constant values for histogram collectors.
   */
  public class Histogram {
    public static final double[] DEFAULT_HISTOGRAM = new double[]{Double.MAX_VALUE};
  }
}
