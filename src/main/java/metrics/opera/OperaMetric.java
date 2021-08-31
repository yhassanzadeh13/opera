package metrics.opera;

import io.prometheus.client.Collector;
import java.util.HashMap;

/**
 * Simulator metric which consist of types and informative constant variables.
 */
public class OperaMetric {

  enum Type {
    COUNTER,
    GAUGE,
    HISTOGRAM,
    SUMMARY
  }

  public static final String LABEL_UUID = "uuid";

  protected static final HashMap<String, Collector> collectors = new HashMap<>();
  protected static final HashMap<String, Type> collectorsTypes = new HashMap<>();
  protected static final String NAMESPACE = "simulator";

}
