package metrics.opera;

import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

  @SuppressFBWarnings(value = "MS_MUTABLE_COLLECTION_PKGPROTECT", justification = "going to be revamped")
  protected static final HashMap<String, Collector> collectors = new HashMap<>();
  
  @SuppressFBWarnings(value = "MS_MUTABLE_COLLECTION_PKGPROTECT", justification = "going to be revamped")
  protected static final HashMap<String, Type> collectorsTypes = new HashMap<>();
}
