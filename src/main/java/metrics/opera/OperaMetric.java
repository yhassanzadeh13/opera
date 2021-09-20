package metrics.opera;

import io.prometheus.client.Collector;
import java.util.HashMap;

/**
 * Simulator metric which consist of types and informative constant variables.
 */
public class OperaMetric {

  protected static final HashMap<String, Collector> collectors = new HashMap<>();
  protected static final HashMap<String, TYPE> collectorsTypes = new HashMap<>();
  enum TYPE {
    COUNTER,
    GAUGE,
    HISTOGRAM,
    SUMMARY
  }
}
