package metrics.opera;

import java.util.HashMap;

import io.prometheus.client.Collector;

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

  protected static final HashMap<String, Collector> collectors = new HashMap<>();
  protected static final HashMap<String, Type> collectorsTypes = new HashMap<>();
}
