package metrics;

import io.prometheus.client.Collector;
import java.util.HashMap;

/**
 *
 */
public class SimulatorMetric {

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
