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

  protected static final HashMap<String, Collector> collectors = new HashMap<>();
  protected static final HashMap<String, Type> collectorsTypes = new HashMap<>();
  protected static final String NAMESPACE = "simulator";
  protected static final String LABEL_NAME = "uuid";
  protected static final String HELP_MSG = "opera native metric";

}
